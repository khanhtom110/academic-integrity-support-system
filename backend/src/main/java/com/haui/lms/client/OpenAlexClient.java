package com.haui.lms.client;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.openalex.OpenAlexAutocompleteResponse;
import com.haui.lms.dto.response.openalex.OpenAlexSourceResponse;
import com.haui.lms.exception.extended.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Lop duy nhat trong he thong noi chuyen truc tiep voi OpenAlex. Tach rieng de neu sau nay doi nguon du lieu thi chi
 * phai sua o day.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAlexClient {

    private final RestTemplate restTemplate;

    @Value("${openalex.api.base-url}")
    private String baseUrl;

    @Value("${openalex.api.mailto}")
    private String mailto;

    /**
     * Goi y tap chi theo tu khoa. OpenAlex tra ve toi da 10 ket qua.
     */
    public OpenAlexAutocompleteResponse autocompleteSources(String query) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/autocomplete/sources").queryParam("q", query)
                .queryParam("mailto", mailto).toUriString();

        try {
            return restTemplate.getForObject(url, OpenAlexAutocompleteResponse.class);
        } catch (RestClientException e) {
            log.error("OpenAlex autocomplete failed. Query: {}", query, e);
            throw new AppException(503, ErrorMessage.Journal.OPENALEX_UNAVAILABLE);
        }
    }

    /**
     * Lay chi tiet tap chi theo ISSN. Chap nhan ca ISSN ban in lan ban dien tu, OpenAlex tu dan ve cung mot tap chi.
     */
    public OpenAlexSourceResponse getSourceByIssn(String issn) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/sources/issn:" + issn).queryParam("mailto", mailto)
                .toUriString();

        try {
            return restTemplate.getForObject(url, OpenAlexSourceResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            // Tach rieng truong hop khong tim thay: day khong phai loi he thong
            log.info("Journal not found on OpenAlex. ISSN: {}", issn);
            throw new AppException(404, ErrorMessage.Journal.JOURNAL_NOT_FOUND);

        } catch (RestClientException e) {
            log.error("OpenAlex source lookup failed. ISSN: {}", issn, e);
            throw new AppException(503, ErrorMessage.Journal.OPENALEX_UNAVAILABLE);
        }
    }
}
