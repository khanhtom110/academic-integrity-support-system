package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class InstitutionDTO {
    private String id;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("country_code")
    private String countryCode;

    private String type;
}
