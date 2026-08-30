package com.haui.lms.dto.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenAlexSourceDTO {
    private String id;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("issn_l")
    private String issnL;

    private List<String> issn;

    @JsonProperty("host_organization_name")
    private String hostOrganizationName;

    @JsonProperty("host_organization_lineage")
    private List<String> hostOrganizationLineage;

    @JsonProperty("country_code")
    private String countryCode;

    private String type;

    @JsonProperty("works_count")
    private Integer worksCount;

    @JsonProperty("cited_by_count")
    private Long citedByCount;

    @JsonProperty("summary_stats")
    private SummaryStatsDTO summaryStats;

    @JsonProperty("first_publication_year")
    private Integer firstPublicationYear;

    @JsonProperty("last_publication_year")
    private Integer lastPublicationYear;

    @JsonProperty("is_oa")
    private Boolean isOa;

    @JsonProperty("is_in_doaj")
    private Boolean isInDoaj;

    @JsonProperty("is_core")
    private Boolean isCore;

    @JsonProperty("apc_usd")
    private Integer apcUsd;

    @JsonProperty("homepage_url")
    private String homepageUrl;

    @JsonProperty("counts_by_year")
    private List<YearlyCountDTO> countsByYear;

    private List<TopicDTO> topics;

    @JsonProperty("topic_share")
    private List<TopicShareDTO> topicShare;
}
