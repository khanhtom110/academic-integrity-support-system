package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TopicShareDTO {
    private String id;
    @JsonProperty("display_name")
    private String displayName;
    private Double value;
    private TopicHierarchy subfield;
    private TopicHierarchy field;
    private TopicHierarchy domain;

}
