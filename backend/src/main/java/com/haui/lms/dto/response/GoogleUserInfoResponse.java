package com.haui.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoResponse(
        // @formatter:off
        // Google ID
        String sub, String name, @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName, String picture, String email,
        @JsonProperty("email_verified") Boolean emailVerified) {
}
