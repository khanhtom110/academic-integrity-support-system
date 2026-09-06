package com.haui.lms.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Map response cua GET /authors tren OpenAlex.
 * <p>
 * Dung de suy ra quoc gia cho nhung bai ma truong countries bi bo trong. Goi theo lo bang filter=openalex_id:A1|A2|A3
 * nen mot lan goi tra ve duoc hang chuc tac gia va chi ton 1 credit.
 * <p>
 * Luu y ve do chinh xac: lastKnownInstitutions la noi cong tac <b>gan nhat</b> cua tac gia, khong phai noi ho cong tac
 * luc viet bai. Tac gia chuyen truong thi suy ra se sai, vi vay chuoi affiliation in tren chinh bai bao luon duoc uu
 * tien hon nguon nay.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAlexAuthorsResponse(List<Author> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String id, @JsonProperty("last_known_institutions") List<Institution> lastKnownInstitutions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Institution(@JsonProperty("country_code") String countryCode) {
    }
}
