package com.haui.lms.dto.aggregation;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CountryAggregationDTO {
    private List<String> topCountries;
    private Map<String, Map<Integer, Double>> data;
    private Map<String, Map<Integer, Double>> percentages;
    private Map<String, Double> totals;
}
