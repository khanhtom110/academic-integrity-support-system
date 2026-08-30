package com.haui.lms.service.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haui.lms.dto.openalex.OpenAlexSourceDTO;
import com.haui.lms.dto.openalex.OpenAlexWorkDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import java.util.Map;

@Service
public class OpenAlexClient {

    private static final String BASE_URL = "https://api.openalex.org";

    private static final int PER_PAGE = 200;

    private static final String WORK_SELECT_FIELDS = "id,publication_year,type,authorships";

    private final RestClient restClient;

    public OpenAlexClient() {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    // LOOKUP JOURNAL BY ISSN
    public OpenAlexSourceDTO lookupByIssn(String issn) {

        if (issn == null || issn.isBlank()) {
            return null;
        }

        try {

            OpenAlexSourceDTO result = restClient
                    .get().uri(uriBuilder -> uriBuilder.path("/sources")
                            .queryParam("filter", "issn:" + normalizeIssn(issn)).queryParam("per-page", 1).build())
                    .retrieve().body(SourceSearchResponse.class).toSource();

            return result;

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            throw new OpenAlexRateLimitException("OpenAlex rate limit/quota exceeded: " + e.getResponseBodyAsString());
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("OpenAlex lookup failed: " + e.getMessage(), e);
        }
    }

    // FETCH WORKS - NEW ARCHITECTURE

    public WorksBatch fetchWorksBatch(String sourceId, String fromDate, String toDate, String cursor) {

        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId không được null hoặc rỗng");
        }

        if (fromDate == null || fromDate.isBlank()) {
            throw new IllegalArgumentException("fromDate không được null hoặc rỗng");
        }

        if (toDate == null || toDate.isBlank()) {
            throw new IllegalArgumentException("toDate không được null hoặc rỗng");
        }

        // QUAN TRỌNG:
        // Không gán lại biến cursor vì cursor sẽ được dùng trong lambda.
        final String requestCursor = (cursor == null || cursor.isBlank()) ? "*" : cursor;

        String filter = "primary_location.source.id:" + sourceId + ",from_publication_date:" + fromDate
                + ",to_publication_date:" + toDate;

        try {

            WorksResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/works").queryParam("filter", filter)
                            .queryParam("per-page", PER_PAGE).queryParam("cursor", requestCursor)
                            .queryParam("select", WORK_SELECT_FIELDS).build())
                    .retrieve().body(WorksResponse.class);


            if (response == null) {
                return new WorksBatch(List.of(), null);
            }

            String nextCursor = null;

            if (response.meta != null) {
                nextCursor = response.meta.nextCursor;
            }

            return new WorksBatch(response.results != null ? response.results : List.of(), nextCursor);

        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 504) {

                throw new OpenAlexQueryTimeoutException("OpenAlex query timeout. " + "Period=" + fromDate + " -> "
                        + toDate + ", cursor=" + requestCursor, e);
            }

            throw e;
        }
    }

    private String normalizeIssn(String issn) {

        if (issn == null) {
            return null;
        }

        String value = issn.trim().toUpperCase();

        // Nếu nhập 00928674 -> chuyển thành 0092-8674
        if (value.matches("\\d{8}")) {
            return value.substring(0, 4)
                    + "-"
                    + value.substring(4);
        }

        return value;
    }

    public static class WorksBatch {

        private final List<OpenAlexWorkDTO> works;

        private final String nextCursor;

        public WorksBatch(List<OpenAlexWorkDTO> works, String nextCursor) {
            this.works = works;
            this.nextCursor = nextCursor;
        }

        public List<OpenAlexWorkDTO> getWorks() {
            return works;
        }

        public String getNextCursor() {
            return nextCursor;
        }
    }


    private static class WorksResponse {

        @JsonProperty("meta")
        private Meta meta;

        @JsonProperty("results")
        private List<OpenAlexWorkDTO> results;
    }

    private static class Meta {

        @JsonProperty("next_cursor")
        private String nextCursor;
    }

    private static class SourceSearchResponse {

        @JsonProperty("results")
        private List<OpenAlexSourceDTO> results;

        public OpenAlexSourceDTO toSource() {

            if (results == null || results.isEmpty()) {

                return null;
            }

            return results.get(0);
        }
    }

    public static class OpenAlexQueryTimeoutException extends RuntimeException {

        public OpenAlexQueryTimeoutException(String message, Throwable cause) {

            super(message, cause);
        }
    }

    public static class OpenAlexRateLimitException extends RuntimeException {

        public OpenAlexRateLimitException(String message) {
            super(message);
        }
    }
}