package com.haui.lms.dto.aggregation;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstitutionCountryDTO {
    private List<String> topInstitutions;
    private Map<String, Map<Integer, Double>> data;
    private Map<Integer, Double> papersPerYear;
    private Map<String, Map<Integer, Double>> percentages;
}
