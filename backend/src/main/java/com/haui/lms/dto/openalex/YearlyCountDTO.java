package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class YearlyCountDTO {
    private Integer year;

    @JsonProperty("works_count")
    private Integer worksCount;

    @JsonProperty("oa_works_count")
    private Integer oaWorksCount;

    @JsonProperty("cited_by_count")
    private Long citedByCount;

}
