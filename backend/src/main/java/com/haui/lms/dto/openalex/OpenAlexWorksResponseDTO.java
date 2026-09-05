package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenAlexWorksResponseDTO {
    private MetaDTO meta;

    private List<OpenAlexWorkDTO> results;

    @Data
    public static class MetaDTO {
        private Integer count;

        @JsonProperty("next_cursor")
        private String nextCursor;
    }
}
