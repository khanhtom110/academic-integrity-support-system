package com.haui.lms.dto.aggregation;

import lombok.Data;

import java.util.Map;

@Data
public class SummaryDTO {
    private Integer totalWorks;
    private Integer uniqueCountries;
    private Integer uniqueAuthors;
    private Map<String, Integer> contentMix;
    private Map<Integer, Map<String, Integer>> contentMixByYear;
}
