package com.haui.lms.service.aggregation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haui.lms.dto.aggregation.CountryAggregationDTO;
import com.haui.lms.dto.openalex.AuthorshipDTO;
import com.haui.lms.dto.openalex.InstitutionDTO;
import com.haui.lms.dto.openalex.OpenAlexWorkDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CountryAggregator {

    private static final int TOP_COUNTRIES = 20;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    private volatile Map<String, String> countryNames;

    public CountryAggregator(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper) {

        this.restClient =
                restClientBuilder
                        .baseUrl("https://api.openalex.org")
                        .build();

        this.objectMapper = objectMapper;
    }

    public Accumulator createAccumulator() {

        return new Accumulator();
    }

    private Map<String, String> getCountryNames() {

        Map<String, String> local = countryNames;

        if (local != null) {
            return local;
        }

        synchronized (this) {

            if (countryNames != null) {
                return countryNames;
            }

            Map<String, String> result =
                    new HashMap<>();

            try {

                String json =
                        restClient.get()
                                .uri(uriBuilder ->
                                        uriBuilder
                                                .path("/countries")
                                                .queryParam("per-page", 200)
                                                .build())
                                .retrieve()
                                .body(String.class);

                if (json != null && !json.isBlank()) {

                    JsonNode root =
                            objectMapper.readTree(json);

                    JsonNode results =
                            root.get("results");

                    if (results != null &&
                            results.isArray()) {

                        for (JsonNode country : results) {

                            JsonNode codeNode =
                                    country.get("country_code");

                            JsonNode nameNode =
                                    country.get("display_name");

                            if (codeNode == null ||
                                    nameNode == null) {
                                continue;
                            }

                            String code =
                                    codeNode.asText()
                                            .trim()
                                            .toUpperCase();

                            String name =
                                    nameNode.asText()
                                            .trim();

                            if (!code.isBlank() &&
                                    !name.isBlank()) {

                                result.put(
                                        code,
                                        name
                                );
                            }
                        }
                    }
                }

            } catch (Exception e) {

                System.err.println(
                        "Cannot load OpenAlex countries: "
                                + e.getMessage()
                );
            }

            /*
             * Nếu API lỗi thì vẫn không làm crash job.
             *
             * CountryAggregator sẽ dùng code làm fallback.
             */
            countryNames = result;

            return result;
        }
    }

    private String resolveCountryName(
            String countryCode) {

        if (countryCode == null ||
                countryCode.isBlank()) {

            return "Unknown";
        }

        String code =
                countryCode.trim()
                        .toUpperCase();

        String name =
                getCountryNames().get(code);

        if (name != null &&
                !name.isBlank()) {

            return name;
        }

        return code;
    }

    public void aggregateBatch(
            Accumulator accumulator,
            List<OpenAlexWorkDTO> works) {

        if (accumulator == null ||
                works == null ||
                works.isEmpty()) {

            return;
        }

        for (OpenAlexWorkDTO work : works) {

            if (work == null) {
                continue;
            }

            Integer year =
                    work.getPublicationYear();

            if (year == null) {
                continue;
            }

            List<AuthorshipDTO> authorships =
                    work.getAuthorships();

            Set<String> pairs =
                    new HashSet<>();

            if (authorships != null) {

                for (AuthorshipDTO authorship :
                        authorships) {

                    if (authorship == null) {
                        continue;
                    }

                    List<InstitutionDTO> institutions =
                            authorship.getInstitutions();

                    if (institutions == null) {
                        continue;
                    }

                    for (InstitutionDTO institution :
                            institutions) {

                        if (institution == null) {
                            continue;
                        }

                        String countryCode =
                                institution.getCountryCode();

                        String institutionName =
                                institution.getDisplayName();

                        if (countryCode == null ||
                                countryCode.isBlank()) {
                            continue;
                        }

                        if (institutionName == null ||
                                institutionName.isBlank()) {
                            continue;
                        }

                        pairs.add(
                                countryCode.toUpperCase()
                                        + "|"
                                        + institutionName
                        );
                    }
                }
            }

            if (pairs.isEmpty()) {

                accumulator.countryYear
                        .computeIfAbsent(
                                "Unknown",
                                c -> new HashMap<>()
                        )
                        .merge(
                                year,
                                1.0,
                                Double::sum
                        );

                accumulator.countryTotals.merge(
                        "Unknown",
                        1.0,
                        Double::sum
                );

                continue;
            }

            Map<String, Set<String>> byCountry =
                    new HashMap<>();

            for (String pair : pairs) {

                int separator =
                        pair.indexOf('|');

                if (separator <= 0 ||
                        separator >= pair.length() - 1) {

                    continue;
                }

                String countryCode =
                        pair.substring(
                                0,
                                separator
                        );

                String institution =
                        pair.substring(
                                separator + 1
                        );

                String countryName =
                        resolveCountryName(
                                countryCode
                        );

                byCountry
                        .computeIfAbsent(
                                countryName,
                                c -> new HashSet<>()
                        )
                        .add(institution);
            }

            if (byCountry.isEmpty()) {
                continue;
            }

            int numberOfCountries =
                    byCountry.size();

            double fraction =
                    1.0 / numberOfCountries;

            for (String country :
                    byCountry.keySet()) {

                accumulator.countryYear
                        .computeIfAbsent(
                                country,
                                c -> new HashMap<>()
                        )
                        .merge(
                                year,
                                fraction,
                                Double::sum
                        );

                accumulator.countryTotals.merge(
                        country,
                        fraction,
                        Double::sum
                );
            }
        }
    }


    public CountryAggregationDTO finish(
            Accumulator accumulator) {

        Set<Integer> yearsSet =
                new HashSet<>();

        for (Map<Integer, Double> yearly :
                accumulator.countryYear.values()) {

            yearsSet.addAll(
                    yearly.keySet()
            );
        }

        List<Integer> years =
                new ArrayList<>(yearsSet);

        years.sort(Integer::compareTo);

        List<String> sortedCountries =
                accumulator.countryTotals
                        .keySet()
                        .stream()
                        .filter(
                                c -> !"Unknown".equals(c)
                        )
                        .sorted((a, b) ->
                                Double.compare(
                                        accumulator.countryTotals.get(b),
                                        accumulator.countryTotals.get(a)
                                )
                        )
                        .toList();

        List<String> topCountries =
                sortedCountries
                        .stream()
                        .limit(TOP_COUNTRIES)
                        .toList();

        Set<String> topCountrySet =
                new HashSet<>(topCountries);


        Map<String, Map<Integer, Double>> data =
                new HashMap<>();

        Map<String, Map<Integer, Double>> percentages =
                new HashMap<>();

        for (String country :
                topCountries) {

            Map<Integer, Double> yearlyData =
                    new HashMap<>();

            Map<Integer, Double> yearlyPercentage =
                    new HashMap<>();

            Map<Integer, Double> existing =
                    accumulator.countryYear
                            .getOrDefault(
                                    country,
                                    Collections.emptyMap()
                            );

            for (Integer year : years) {

                double value =
                        existing.getOrDefault(
                                year,
                                0.0
                        );

                value =
                        round(value);

                yearlyData.put(
                        year,
                        value
                );

                yearlyPercentage.put(
                        year,
                        0.0
                );
            }

            data.put(
                    country,
                    yearlyData
            );

            percentages.put(
                    country,
                    yearlyPercentage
            );
        }

        /*
         * ==========================================
         * OTHER
         * ==========================================
         */

        Map<Integer, Double> otherData =
                new HashMap<>();

        for (Map.Entry<String, Map<Integer, Double>> entry :
                accumulator.countryYear.entrySet()) {

            String country =
                    entry.getKey();

            if ("Unknown".equals(country) ||
                    topCountrySet.contains(country)) {
                continue;
            }

            for (Map.Entry<Integer, Double> yearEntry :
                    entry.getValue().entrySet()) {

                otherData.merge(
                        yearEntry.getKey(),
                        yearEntry.getValue(),
                        Double::sum
                );
            }
        }

        boolean hasOther =
                !otherData.isEmpty();

        if (hasOther) {

            for (Integer year : years) {

                otherData.put(
                        year,
                        round(
                                otherData.getOrDefault(
                                        year,
                                        0.0
                                )
                        )
                );
            }

            data.put(
                    "Other",
                    otherData
            );

            Map<Integer, Double> otherPercent =
                    new HashMap<>();

            for (Integer year : years) {
                otherPercent.put(
                        year,
                        0.0
                );
            }

            percentages.put(
                    "Other",
                    otherPercent
            );
        }

        boolean hasUnknown =
                accumulator.countryTotals
                        .getOrDefault(
                                "Unknown",
                                0.0
                        ) > 0;

        if (hasUnknown) {

            Map<Integer, Double> unknownData =
                    new HashMap<>();

            Map<Integer, Double> unknownPercent =
                    new HashMap<>();

            Map<Integer, Double> existing =
                    accumulator.countryYear
                            .getOrDefault(
                                    "Unknown",
                                    Collections.emptyMap()
                            );

            for (Integer year : years) {

                unknownData.put(
                        year,
                        round(
                                existing.getOrDefault(
                                        year,
                                        0.0
                                )
                        )
                );

                unknownPercent.put(
                        year,
                        0.0
                );
            }

            data.put(
                    "Unknown",
                    unknownData
            );

            percentages.put(
                    "Unknown",
                    unknownPercent
            );
        }

        List<String> finalTopCountries =
                new ArrayList<>(
                        topCountries
                );

        if (hasOther) {
            finalTopCountries.add(
                    "Other"
            );
        }

        if (hasUnknown) {
            finalTopCountries.add(
                    "Unknown"
            );
        }

        CountryAggregationDTO result =
                new CountryAggregationDTO();

        result.setTopCountries(
                finalTopCountries
        );

        result.setData(data);

        result.setPercentages(
                percentages
        );

        result.setTotals(
                accumulator.countryTotals
        );

        return result;
    }

    private double round(double value) {

        return Math.round(
                value * 1000.0
        ) / 1000.0;
    }

    public static class Accumulator {
        private final Map<String, Map<Integer, Double>>
                countryYear =
                new HashMap<>();

        private final Map<String, Double>
                countryTotals =
                new HashMap<>();
    }
}