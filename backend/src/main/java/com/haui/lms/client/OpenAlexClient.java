package com.haui.lms.client;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.openalex.OpenAlexAutocompleteResponse;
import com.haui.lms.dto.response.openalex.OpenAlexSourceResponse;
import com.haui.lms.exception.extended.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

    /**
     * Header OpenAlex tra ve so tien con lai trong ngay. Ngan sach reset luc 00:00 UTC, tuc 7h sang gio Viet Nam.
     */
    private static final String HEADER_REMAINING_USD = "X-RateLimit-Remaining-USD";

    /**
     * Con duoi muc nay thi log canh bao de biet duong ma xu ly truoc khi het han muc.
     */
    private static final double LOW_BUDGET_WARN_THRESHOLD = 0.1d;

    private final RestTemplate restTemplate;

    @Value("${openalex.api.base-url}")
    private String baseUrl;

    @Value("${openalex.api.mailto}")
    private String mailto;

    @Value("${openalex.api.key:}")
    private String apiKey;

    /**
     * Goi y tap chi theo tu khoa. OpenAlex tra ve toi da 10 ket qua.
     */
    public OpenAlexAutocompleteResponse autocompleteSources(String query) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/autocomplete/sources").queryParam("q", query)
                .queryParam("mailto", mailto).toUriString();

        try {
            return exchange(url, OpenAlexAutocompleteResponse.class);

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("OpenAlex daily quota exhausted. Query: {}", query, e);
            throw new AppException(429, ErrorMessage.Journal.RATE_LIMIT_EXCEEDED);

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
            return exchange(url, OpenAlexSourceResponse.class);

        } catch (HttpClientErrorException.NotFound e) {
            // Tach rieng truong hop khong tim thay: day khong phai loi he thong
            log.info("Journal not found on OpenAlex. ISSN: {}", issn);
            throw new AppException(404, ErrorMessage.Journal.JOURNAL_NOT_FOUND);

        } catch (HttpClientErrorException.TooManyRequests e) {
            // Het han muc trong ngay. Tach rieng khoi 503 vi neu gop chung thi nguoi doc log se di tim loi mang,
            // trong khi thuc te chi can doi den luc reset hoac dung API key co han muc cao hon.
            log.error("OpenAlex daily quota exhausted. ISSN: {}", issn, e);
            throw new AppException(429, ErrorMessage.Journal.RATE_LIMIT_EXCEEDED);

        } catch (RestClientException e) {
            log.error("OpenAlex source lookup failed. ISSN: {}", issn, e);
            throw new AppException(503, ErrorMessage.Journal.OPENALEX_UNAVAILABLE);
        }
    }

    /**
     * Goi OpenAlex kem API key. Key duoc gui qua header Authorization thay vi tham so tren URL, de no khong bi in ra
     * log hay stack trace khi co loi.
     * <p>
     * Khong co key thi van goi duoc, chi la han muc chi con 1/10 va bi tinh chung theo IP.
     */
    private <T> T exchange(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                responseType);

        logRemainingBudget(response.getHeaders());
        return response.getBody();
    }

    /**
     * Ghi lai ngan sach con lai sau moi lan goi. Nho vay khi bi 429 thi log truoc do da cho thay han muc tut dan, thay
     * vi dot ngot bao loi ma khong ro nguyen nhan.
     */
    private void logRemainingBudget(HttpHeaders headers) {
        String remaining = headers.getFirst(HEADER_REMAINING_USD);
        if (!StringUtils.hasText(remaining)) {
            return;
        }

        try {
            double remainingUsd = Double.parseDouble(remaining);
            if (remainingUsd <= LOW_BUDGET_WARN_THRESHOLD) {
                log.warn("OpenAlex budget is running low: ${} left until midnight UTC", remaining);
            } else {
                log.debug("OpenAlex budget left today: ${}", remaining);
            }
        } catch (NumberFormatException e) {
            // Header doi dinh dang thi bo qua, khong lam hong request cua nguoi dung
            log.debug("Cannot parse OpenAlex budget header: {}", remaining);
        }
    }
}
