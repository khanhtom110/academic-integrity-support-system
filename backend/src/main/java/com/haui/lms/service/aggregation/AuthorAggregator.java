package com.haui.lms.service.aggregation;

import com.haui.lms.dto.aggregation.AuthorAggregationDTO;
import com.haui.lms.dto.aggregation.AuthorStatDTO;
import com.haui.lms.dto.openalex.AuthorshipDTO;
import com.haui.lms.dto.openalex.OpenAlexWorkDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AuthorAggregator {
    private static final int TOP_AUTHORS = 30;

    public Accumulator createAccumulator() {

        return new Accumulator();
    }

    public void aggregateBatch(Accumulator accumulator, List<OpenAlexWorkDTO> works) {

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

            Set<String> authorsInWork = new HashSet<>();

            for (AuthorshipDTO authorship : authorships) {

                if (authorship == null || authorship.getAuthor() == null) {

                    continue;
                }

                String authorId = authorship.getAuthor().getId();

                String authorName = authorship.getAuthor().getDisplayName();

                if (authorId == null || authorId.isBlank()) {

                    continue;
                }

                if (authorName == null || authorName.isBlank()) {

                    authorName = authorId;
                }

                if (!authorsInWork.add(authorId)) {

                    continue;
                }

                accumulator.authorNames.putIfAbsent(authorId, authorName);

                accumulator.authorTotals.merge(authorId, 1, Integer::sum);

                accumulator.authorYear.computeIfAbsent(authorId, a -> new HashMap<>()).merge(year, 1, Integer::sum);
            }
        }
    }

    public AuthorAggregationDTO finish(Accumulator accumulator) {

        // 1. SORT AUTHOR
        List<String> sortedAuthors = accumulator.authorTotals.keySet().stream().sorted((a, b) -> {

            int totalCompare = Integer.compare(accumulator.authorTotals.get(b), accumulator.authorTotals.get(a));

            if (totalCompare != 0) {

                return totalCompare;
            }

            return accumulator.authorNames.getOrDefault(a, a)
                    .compareToIgnoreCase(accumulator.authorNames.getOrDefault(b, b));
        }).toList();

        List<String> topAuthorIds = sortedAuthors.stream().limit(TOP_AUTHORS).toList();

        List<AuthorStatDTO> authorStats = new ArrayList<>();

        for (String authorId : topAuthorIds) {

            AuthorStatDTO stat = new AuthorStatDTO();

            stat.setName(accumulator.authorNames.getOrDefault(authorId, authorId));

            stat.setTotal(accumulator.authorTotals.getOrDefault(authorId, 0));

            authorStats.add(stat);
        }

        Set<Integer> yearsSet = new HashSet<>();

        for (Map<Integer, Integer> yearly : accumulator.authorYear.values()) {

            yearsSet.addAll(yearly.keySet());
        }

        List<Integer> years = new ArrayList<>(yearsSet);

        years.sort(Integer::compareTo);

        Map<String, Map<Integer, Integer>> authorData = new HashMap<>();

        for (String authorId : topAuthorIds) {
            Map<Integer, Integer> yearlyData = new HashMap<>();

            Map<Integer, Integer> existingData = accumulator.authorYear.getOrDefault(authorId, Collections.emptyMap());

            for (Integer year : years) {

                yearlyData.put(year, existingData.getOrDefault(year, 0));
            }

            String authorName = accumulator.authorNames.getOrDefault(authorId, authorId);

            authorData.put(authorName, yearlyData);
        }

        AuthorAggregationDTO result = new AuthorAggregationDTO();

        /*
         * Danh sách author vẫn chứa:
         *
         * id name total
         */

        result.setAuthors(authorStats);

        result.setData(authorData);

        return result;
    }

    public static class Accumulator {

        private final Map<String, Integer> authorTotals = new HashMap<>();

        private final Map<String, Map<Integer, Integer>> authorYear = new HashMap<>();

        private final Map<String, String> authorNames = new HashMap<>();
    }

}
