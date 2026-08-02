package com.haui.lms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OutlookUserResponse(
        // @formatter:off
        String id,

        @JsonProperty("displayName") String displayName,

        @JsonProperty("givenName") String givenName,

        @JsonProperty("surname") String surname,

        String mail,

        String userPrincipalName) {
    public String getEmail() {
        return (mail != null && !mail.isBlank()) ? mail : userPrincipalName;
    }
}
