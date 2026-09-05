package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SummaryStatsDTO {
    @JsonProperty("2yr_mean_citedness")
    private Double twoYrMeanCitedness;

    @JsonProperty("h_index")
    private Integer hIndex;

    @JsonProperty("i10_index")
    private Integer i10Index;
}
