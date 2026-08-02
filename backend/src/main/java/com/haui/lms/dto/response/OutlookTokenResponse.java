package com.haui.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OutlookTokenResponse(
        // @formatter:off
        @JsonProperty("access_token") String accessToken) {
}
