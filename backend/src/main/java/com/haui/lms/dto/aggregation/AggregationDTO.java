package com.haui.lms.dto.aggregation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AggregationDTO {
    // paperMeta khong nen nam trong cache
    @JsonProperty("schema_version")
    private Integer schemaVersion;

    private List<Integer> years;

    private Map<Integer, Integer> papersPerYear;

    private CountryAggregationDTO country;

    private SummaryDTO summary;

    private InstitutionAggregationDTO institution;

    private AuthorAggregationDTO author;


}
