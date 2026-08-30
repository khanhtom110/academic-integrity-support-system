package com.haui.lms.service.aggregation;

import com.haui.lms.dto.aggregation.AggregationDTO;
import com.haui.lms.dto.aggregation.AuthorAggregationDTO;
import com.haui.lms.dto.aggregation.CountryAggregationDTO;
import com.haui.lms.dto.aggregation.InstitutionAggregationDTO;
import com.haui.lms.dto.aggregation.InstitutionCountryDTO;
import com.haui.lms.dto.aggregation.SummaryDTO;
import com.haui.lms.dto.openalex.OpenAlexWorkDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AggregationService {
    private static final int SCHEMA_VERSION = 2;
    private final CountryAggregator countryAggregator;
    private final InstitutionAggregator institutionAggregator;
    private final AuthorAggregator authorAggregator;

    public AggregationService(CountryAggregator countryAggregator, InstitutionAggregator institutionAggregator,
            AuthorAggregator authorAggregator) {
        this.countryAggregator = countryAggregator;
        this.institutionAggregator = institutionAggregator;
        this.authorAggregator = authorAggregator;
    }

    public AggregationAccumulator createAccumulator() {
        return new AggregationAccumulator(countryAggregator.createAccumulator(),
                institutionAggregator.createAccumulator(), authorAggregator.createAccumulator());
    }

    public void aggregateBatch(AggregationAccumulator accumulator, List<OpenAlexWorkDTO> works) {
        if (accumulator == null || works == null || works.isEmpty()) {
            return;
        }

        for (OpenAlexWorkDTO work : works) {
            if (work == null) {
                continue;
            }
            Integer year = work.getPublicationYear();
            if (year == null) {
                continue;
            }
            // WORKS PER YEAR
            accumulator.years.add(year);
            accumulator.papersPerYear.merge(year, 1, Integer::sum);

            // CONTENT MIX
            String type = work.getType();
            String typeKey;
            if (type == null || type.isBlank()) {
                typeKey = "other";
            } else {
                typeKey = type.replaceFirst("^https?://openalex\\.org/types/", "").toLowerCase();
            }

            accumulator.contentMix.merge(typeKey, 1, Integer::sum);

            accumulator.contentMixByYear.computeIfAbsent(year, y -> new HashMap<>()).merge(typeKey, 1, Integer::sum);
        }

        // AUTHOR
        authorAggregator.aggregateBatch(accumulator.author, works);

        // COUNTRY
        countryAggregator.aggregateBatch(accumulator.country, works);

        // INSTITUTION
        institutionAggregator.aggregateBatch(accumulator.institution, works);
    }

    // FINISH
    public AggregationDTO finish(AggregationAccumulator accumulator) {
        AggregationDTO result = new AggregationDTO();
        result.setSchemaVersion(SCHEMA_VERSION);

        // YEARS
        List<Integer> years = new ArrayList<>(accumulator.years);
        years.sort(Integer::compareTo);
        result.setYears(years);

        result.setPapersPerYear(new HashMap<>(accumulator.papersPerYear));

        // COUNTRY
        CountryAggregationDTO countryResult = countryAggregator.finish(accumulator.country);
        calculateCountryPercentages(countryResult, accumulator.papersPerYear);
        result.setCountry(countryResult);

        // CONTENT MIX

        SummaryDTO summary = new SummaryDTO();

        summary.setContentMix(new HashMap<>(accumulator.contentMix));

        summary.setContentMixByYear(new HashMap<>(accumulator.contentMixByYear));

        // INSTITUTION
        InstitutionAggregationDTO institutionResult = institutionAggregator.finish(accumulator.institution,
                countryResult.getTopCountries());

        calculateInstitutionPercentages(institutionResult, accumulator.papersPerYear);

        result.setInstitution(institutionResult);

        // AUTHOR
        AuthorAggregationDTO authorResult = authorAggregator.finish(accumulator.author);

        calculateAuthorPercentages(authorResult, accumulator.papersPerYear);

        result.setAuthor(authorResult);

        result.setSummary(summary);

        return result;
    }

    private void calculateCountryPercentages(CountryAggregationDTO countryResult, Map<Integer, Integer> papersPerYear) {

        if (countryResult == null || countryResult.getData() == null) {

            return;
        }

        Map<String, Map<Integer, Double>> percentages = new HashMap<>();

        for (Map.Entry<String, Map<Integer, Double>> entry : countryResult.getData().entrySet()) {

            String country = entry.getKey();

            Map<Integer, Double> yearly = new HashMap<>();

            for (Map.Entry<Integer, Double> yearEntry : entry.getValue().entrySet()) {

                Integer year = yearEntry.getKey();

                double count = yearEntry.getValue();

                int total = papersPerYear.getOrDefault(year, 0);

                double percentage = total == 0 ? 0.0 : (count / total) * 100.0;

                yearly.put(year, round(percentage));
            }

            percentages.put(country, yearly);
        }

        countryResult.setPercentages(percentages);
    }

    private void calculateInstitutionPercentages(InstitutionAggregationDTO institutionResult,
            Map<Integer, Integer> papersPerYear) {

        if (institutionResult == null || institutionResult.getData() == null) {

            return;
        }

        for (InstitutionCountryDTO countryData : institutionResult.getData().values()) {

            if (countryData == null || countryData.getData() == null) {

                continue;
            }

            Map<String, Map<Integer, Double>> percentages = new HashMap<>();

            Map<Integer, Double> countryPapers = countryData.getPapersPerYear();

            for (Map.Entry<String, Map<Integer, Double>> entry : countryData.getData().entrySet()) {

                String institution = entry.getKey();

                Map<Integer, Double> yearlyPercentage = new HashMap<>();

                for (Map.Entry<Integer, Double> yearEntry : entry.getValue().entrySet()) {

                    Integer year = yearEntry.getKey();

                    double count = yearEntry.getValue();

                    /*
                     * Ưu tiên tổng của country trong năm.
                     */
                    double total = countryPapers == null ? 0.0 : countryPapers.getOrDefault(year, 0.0);

                    double percentage = total == 0.0 ? 0.0 : (count / total) * 100.0;

                    yearlyPercentage.put(year, round(percentage));
                }

                percentages.put(institution, yearlyPercentage);
            }

            countryData.setPercentages(percentages);
        }
    }

    private void calculateAuthorPercentages(AuthorAggregationDTO authorResult, Map<Integer, Integer> papersPerYear) {

        if (authorResult == null || authorResult.getData() == null) {

            return;
        }

        Map<String, Map<Integer, Double>> percentages = new HashMap<>();

        for (Map.Entry<String, Map<Integer, Integer>> entry : authorResult.getData().entrySet()) {

            String authorId = entry.getKey();

            Map<Integer, Double> yearly = new HashMap<>();

            for (Map.Entry<Integer, Integer> yearEntry : entry.getValue().entrySet()) {

                Integer year = yearEntry.getKey();

                int count = yearEntry.getValue();

                int total = papersPerYear.getOrDefault(year, 0);

                double percentage = total == 0 ? 0.0 : ((double) count / total) * 100.0;

                yearly.put(year, round(percentage));
            }

            percentages.put(authorId, yearly);
        }

        authorResult.setPercentages(percentages);
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    public static class AggregationAccumulator {

        private final Set<Integer> years = new HashSet<>();

        private final Map<Integer, Integer> papersPerYear = new HashMap<>();

        private final Map<String, Integer> contentMix = new HashMap<>();

        private final Map<Integer, Map<String, Integer>> contentMixByYear = new HashMap<>();

        private final AuthorAggregator.Accumulator author;

        private final CountryAggregator.Accumulator country;

        private final InstitutionAggregator.Accumulator institution;

        public AggregationAccumulator(CountryAggregator.Accumulator country,
                InstitutionAggregator.Accumulator institution, AuthorAggregator.Accumulator author) {

            this.country = country;

            this.institution = institution;

            this.author = author;
        }
    }
}