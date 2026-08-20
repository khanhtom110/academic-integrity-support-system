package com.haui.lms.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Map response cua GET /autocomplete/sources tren OpenAlex. Dung cho o goi y khi nguoi dung go ten tap chi.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAlexAutocompleteResponse(List<Result> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            // Dang https://openalex.org/S110447773
            String id,

            @JsonProperty("display_name") String displayName,

            // Ten nha xuat ban, tra ve "host organization unknown" neu khong ro
            String hint,

            // Voi source thi day chinh la ISSN
            @JsonProperty("external_id") String externalId,

            @JsonProperty("works_count") Integer worksCount,

            @JsonProperty("cited_by_count") Long citedByCount) {
    }
}
