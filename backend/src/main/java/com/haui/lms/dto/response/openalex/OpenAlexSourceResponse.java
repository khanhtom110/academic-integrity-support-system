package com.haui.lms.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Map response cua GET /sources/issn:{issn} tren OpenAlex.
 * <p>
 * Chi khai bao nhung truong thuc su dung den. OpenAlex tra ve nhieu truong hon the nhung @JsonIgnoreProperties se bo
 * qua, va khi ho them truong moi thi code nay khong vo.
 * <p>
 * Rat nhieu truong co the null: cac tap chi nho thuong khong cong bo APC, khong co quoc gia, chua duoc gan chu de.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAlexSourceResponse(
        // Dang https://openalex.org/S110447773
        String id,

        @JsonProperty("issn_l") String issnL,

        // Mot tap chi thuong co 2 ISSN: ban in va ban dien tu
        List<String> issn,

        @JsonProperty("display_name") String displayName,

        @JsonProperty("host_organization_name") String hostOrganizationName,

        @JsonProperty("homepage_url") String homepageUrl,

        @JsonProperty("country_code") String countryCode,

        String type,

        @JsonProperty("alternate_titles") List<String> alternateTitles,

        @JsonProperty("works_count") Integer worksCount,

        @JsonProperty("oa_works_count") Integer oaWorksCount,

        @JsonProperty("cited_by_count") Long citedByCount,

        @JsonProperty("summary_stats") SummaryStats summaryStats,

        @JsonProperty("is_oa") Boolean isOa,

        @JsonProperty("is_in_doaj") Boolean isInDoaj,

        @JsonProperty("is_core") Boolean isCore,

        @JsonProperty("is_ojs") Boolean isOjs,

        @JsonProperty("first_publication_year") Integer firstPublicationYear,

        @JsonProperty("last_publication_year") Integer lastPublicationYear,

        @JsonProperty("apc_usd") Integer apcUsd,

        @JsonProperty("apc_usd_by_year") List<ApcByYear> apcUsdByYear,

        // Sap xep giam dan theo so bai, moi phan tu co count
        List<Topic> topics,

        // Sap xep giam dan theo ty le, moi phan tu co value
        @JsonProperty("topic_share") List<Topic> topicShare,

        @JsonProperty("counts_by_year") List<CountByYear> countsByYear) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SummaryStats(
            // Ten truong bat dau bang chu so nen khong dat ten bien giong het duoc.
            // Day chinh la cong thuc cua Impact Factor
            @JsonProperty("2yr_mean_citedness") Double twoYearMeanCitedness,

            @JsonProperty("h_index") Integer hIndex,

            @JsonProperty("i10_index") Integer i10Index) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Topic(String id,

            @JsonProperty("display_name") String displayName,

            // Chi co trong mang topics
            Integer count,

            // Chi co trong mang topic_share
            Double value,

            Named subfield,

            Named field,

            Named domain) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Named(String id, @JsonProperty("display_name") String displayName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApcByYear(Integer year, Integer price) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountByYear(Integer year,

            @JsonProperty("works_count") Integer worksCount,

            @JsonProperty("oa_works_count") Integer oaWorksCount,

            @JsonProperty("cited_by_count") Long citedByCount) {
    }
}
