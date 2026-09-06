package com.haui.lms.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Map response cua GET /works tren OpenAlex.
 * <p>
 * Chi khai bao dung nhung truong can de tinh xu huong. Khi goi phai kem select=id,publication_year,authorships, neu
 * khong OpenAlex tra ve toan bo ban ghi va payload phinh len gap hang chuc lan.
 * <p>
 * Diem quan trong: authorships da chua san ten tac gia, ten to chuc va ma quoc gia. Nho vay khong phai goi them API nao
 * de tra cuu ten.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAlexWorksResponse(Meta meta, List<Work> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(Integer count,

            // Con trang tiep theo. Null hoac rong nghia la da het du lieu.
            @JsonProperty("next_cursor") String nextCursor) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Work(String id,

            @JsonProperty("publication_year") Integer publicationYear,

            List<Authorship> authorships) {
    }

    /**
     * Mot dong tac gia trong bai bao. Mot tac gia co the thuoc nhieu to chuc va nhieu quoc gia cung luc.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Authorship(Author author, List<Institution> institutions, List<String> countries) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String id, @JsonProperty("display_name") String displayName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Institution(String id, @JsonProperty("display_name") String displayName,
            @JsonProperty("country_code") String countryCode) {
    }
}
