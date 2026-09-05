package com.haui.lms.dto.aggregation;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AuthorAggregationDTO {
    private List<AuthorStatDTO> authors;
    private Map<String, Map<Integer, Integer>> data;
    private Map<String, Map<Integer, Double>> percentages;
}
