package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TopicHierarchy {
    private String id;

    @JsonProperty("display_name")
    private String displayName;
}
