package com.haui.lms.dto.aggregation;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InstitutionAggregationDTO {
    private List<String> countries;
    private Map<String, InstitutionCountryDTO> data;
}
