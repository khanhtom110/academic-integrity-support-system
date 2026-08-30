package com.haui.lms.service.aggregation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haui.lms.dto.aggregation.InstitutionAggregationDTO;
import com.haui.lms.dto.aggregation.InstitutionCountryDTO;
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
public class InstitutionAggregator {

    private static final int TOP_INSTITUTIONS = 20;

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    private volatile Map<String, String> countryNames;

    public InstitutionAggregator(
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

            Map<String, String> result = new HashMap<>();
            try {
                String json =
                        restClient
                                .get()
                                .uri(uriBuilder ->
                                        uriBuilder
                                                .path("/countries")
                                                .queryParam(
                                                        "per-page",
                                                        200
                                                )
                                                .build()
                                )
                                .retrieve()
                                .body(String.class);

                if (json != null && !json.isBlank()) {
                    JsonNode root = objectMapper.readTree(json);
                    JsonNode results = root.get("results");

                    if (results != null && results.isArray()) {
                        for (JsonNode country : results) {
                            JsonNode codeNode = country.get("country_code");
                            JsonNode nameNode = country.get("display_name");
                            if (codeNode == null || nameNode == null) {
                                continue;
                            }

                            String code =
                                    codeNode
                                            .asText()
                                            .trim()
                                            .toUpperCase();

                            String name =
                                    nameNode
                                            .asText()
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

            countryNames = result;
            return result;
        }
    }

    private String resolveCountryName(String countryCode) {
        if (countryCode == null ||
                countryCode.isBlank()) {
            return "Unknown";
        }
        String code = countryCode.trim().toUpperCase();
        String name = getCountryNames().get(code);
        if (name != null && !name.isBlank()) {
            return name;
        }
        return code;
    }

    public void aggregateBatch(
            Accumulator accumulator,
            List<OpenAlexWorkDTO> works) {

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
            List<AuthorshipDTO> authorships = work.getAuthorships();
            if (authorships == null || authorships.isEmpty()) {
                continue;
            }

            Set<String> pairs = new HashSet<>();
            for (AuthorshipDTO authorship : authorships) {
                if (authorship == null) {
                    continue;
                }

                List<InstitutionDTO> institutions = authorship.getInstitutions();

                if (institutions == null || institutions.isEmpty()) {
                    continue;
                }

                for (InstitutionDTO institution : institutions) {
                    if (institution == null) {
                        continue;
                    }

                    String countryCode = institution.getCountryCode();

                    String institutionName = institution.getDisplayName();

                    if (countryCode == null || countryCode.isBlank()) {
                        continue;
                    }

                    if (institutionName == null || institutionName.isBlank()) {
                        continue;
                    }

                    String countryName = resolveCountryName(countryCode);
                    pairs.add(
                            countryName
                                    + "|"
                                    + institutionName
                    );
                }
            }

            if (pairs.isEmpty()) {
                continue;
            }

            Map<String, Set<String>> byCountry = new HashMap<>();

            for (String pair : pairs) {
                int separator = pair.indexOf('|');
                if (separator <= 0 || separator >= pair.length() - 1) {
                    continue;
                }
                String country = pair.substring(0, separator);

                String institution = pair.substring(separator + 1);
                byCountry
                        .computeIfAbsent(
                                country,
                                c -> new HashSet<>()
                        )
                        .add(institution);
            }

            if (byCountry.isEmpty()) {
                continue;
            }

            int numberOfCountries = byCountry.size();

            double countryFraction = 1.0 / numberOfCountries;

            for (Map.Entry<String, Set<String>> entry : byCountry.entrySet()) {
                String country = entry.getKey();

                Set<String> institutions = entry.getValue();

                if (institutions == null || institutions.isEmpty()) {
                    continue;
                }

                accumulator
                        .cPapersPerYear
                        .computeIfAbsent(
                                country,
                                c -> new HashMap<>()
                        )
                        .merge(
                                year,
                                countryFraction,
                                Double::sum
                        );

                double institutionFraction = countryFraction / institutions.size();

                for (String institution : institutions) {
                    accumulator
                            .ciYearInst
                            .computeIfAbsent(
                                    country,
                                    c -> new HashMap<>()
                            )
                            .computeIfAbsent(
                                    year,
                                    y -> new HashMap<>()
                            )
                            .merge(
                                    institution,
                                    institutionFraction,
                                    Double::sum
                            );

                    accumulator
                            .ciTotalsByCountry
                            .computeIfAbsent(
                                    country,
                                    c -> new HashMap<>()
                            )
                            .merge(
                                    institution,
                                    institutionFraction,
                                    Double::sum
                            );
                }
            }
        }
    }

    public InstitutionAggregationDTO finish(Accumulator accumulator, List<String> topCountries) {
        InstitutionAggregationDTO result = new InstitutionAggregationDTO();

        if (accumulator == null) {
            result.setCountries(new ArrayList<>());
            result.setData(new HashMap<>());
            return result;
        }

        Set<Integer> yearsSet = new HashSet<>();

        for (Map<Integer, Double> yearly :
                accumulator
                        .cPapersPerYear
                        .values()) {

            yearsSet.addAll(
                    yearly.keySet()
            );
        }

        List<Integer> years = new ArrayList<>(yearsSet);
        years.sort(Integer::compareTo);

        List<String> safeTopCountries =
                topCountries == null
                        ? Collections.emptyList()
                        : topCountries;

        List<String> institutionCountries =
                safeTopCountries
                        .stream()
                        .filter(
                                accumulator
                                        .ciTotalsByCountry
                                        ::containsKey
                        )
                        .toList();

        Map<String, InstitutionCountryDTO>
                institutionData =
                new HashMap<>();

        for (String country :
                institutionCountries) {

            Map<String, Double>
                    institutionTotals =
                    accumulator
                            .ciTotalsByCountry
                            .get(country);

            if (institutionTotals == null ||
                    institutionTotals.isEmpty()) {

                continue;
            }

            List<String> sortedInstitutions =
                    institutionTotals
                            .keySet()
                            .stream()
                            .sorted(
                                    (a, b) ->
                                            Double.compare(
                                                    institutionTotals
                                                            .get(b),
                                                    institutionTotals
                                                            .get(a)
                                            )
                            )
                            .toList();


            List<String> topInstitutions =
                    sortedInstitutions
                            .stream()
                            .limit(
                                    TOP_INSTITUTIONS
                            )
                            .toList();

            Set<String> topInstitutionSet =
                    new HashSet<>(
                            topInstitutions
                    );

            Map<String, Map<Integer, Double>>
                    data =
                    new HashMap<>();

            Map<Integer, Map<String, Double>>
                    countryYearData =
                    accumulator
                            .ciYearInst
                            .getOrDefault(
                                    country,
                                    Collections.emptyMap()
                            );

            for (String institutionName :
                    topInstitutions) {

                Map<Integer, Double>
                        yearlyData =
                        new HashMap<>();

                for (Integer year :
                        years) {

                    double value =
                            countryYearData
                                    .getOrDefault(
                                            year,
                                            Collections.emptyMap()
                                    )
                                    .getOrDefault(
                                            institutionName,
                                            0.0
                                    );

                    value =
                            Math.round(
                                    value * 100.0
                            ) / 100.0;

                    yearlyData.put(
                            year,
                            value
                    );
                }

                data.put(
                        institutionName,
                        yearlyData
                );
            }

            Map<Integer, Double>
                    otherData =
                    new HashMap<>();

            boolean hasOther =
                    false;

            for (Map.Entry<Integer, Map<String, Double>>
                    yearEntry :
                    countryYearData.entrySet()) {

                Integer year =
                        yearEntry.getKey();

                Map<String, Double>
                        institutions =
                        yearEntry.getValue();

                if (institutions == null) {

                    continue;
                }

                for (Map.Entry<String, Double>
                        institutionEntry :
                        institutions.entrySet()) {

                    String institutionName =
                            institutionEntry.getKey();

                    if (topInstitutionSet
                            .contains(
                                    institutionName
                            )) {

                        continue;
                    }

                    hasOther = true;

                    otherData.merge(
                            year,
                            institutionEntry.getValue(),
                            Double::sum
                    );
                }
            }

            if (hasOther) {

                for (Integer year :
                        years) {

                    double value =
                            otherData
                                    .getOrDefault(
                                            year,
                                            0.0
                                    );

                    value =
                            Math.round(
                                    value * 100.0
                            ) / 100.0;

                    otherData.put(
                            year,
                            value
                    );
                }

                data.put(
                        "Other",
                        otherData
                );
            }

            /*
             * =================================================
             * 6. PAPERS PER YEAR
             * =================================================
             *
             * Đây là tổng fractional papers
             * của country trong từng năm.
             */

            Map<Integer, Double>
                    countryPapersPerYear =
                    new HashMap<>();

            Map<Integer, Double>
                    papers =
                    accumulator
                            .cPapersPerYear
                            .getOrDefault(
                                    country,
                                    Collections.emptyMap()
                            );

            for (Integer year :
                    years) {

                double value =
                        papers.getOrDefault(
                                year,
                                0.0
                        );

                value =
                        Math.round(
                                value * 100.0
                        ) / 100.0;

                countryPapersPerYear.put(
                        year,
                        value
                );
            }

            List<String> finalTopInstitutions =
                    new ArrayList<>(
                            topInstitutions
                    );

            if (hasOther) {

                finalTopInstitutions.add(
                        "Other"
                );
            }

            InstitutionCountryDTO countryDTO =
                    new InstitutionCountryDTO();

            countryDTO.setTopInstitutions(
                    finalTopInstitutions
            );

            countryDTO.setData(
                    data
            );

            countryDTO.setPapersPerYear(
                    countryPapersPerYear
            );

            institutionData.put(
                    country,
                    countryDTO
            );
        }

        result.setCountries(
                institutionCountries
        );

        result.setData(
                institutionData
        );

        return result;
    }

    public static class Accumulator {
        private final Map<
                String,
                Map<
                        Integer,
                        Map<String, Double>
                        >
                > ciYearInst =
                new HashMap<>();

        private final Map<
                String,
                Map<String, Double>
                > ciTotalsByCountry =
                new HashMap<>();

        private final Map<
                String,
                Map<Integer, Double>
                > cPapersPerYear =
                new HashMap<>();
    }
}
