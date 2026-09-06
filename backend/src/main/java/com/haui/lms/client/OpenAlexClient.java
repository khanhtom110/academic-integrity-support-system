package com.haui.lms.client;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.openalex.OpenAlexAuthorsResponse;
import com.haui.lms.dto.response.openalex.OpenAlexAutocompleteResponse;
import com.haui.lms.dto.response.openalex.OpenAlexSourceResponse;
import com.haui.lms.dto.response.openalex.OpenAlexWorksResponse;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.exception.extended.OpenAlexTimeoutException;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

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

    /**
     * Chi lay dung nhung truong can de tinh xu huong. Neu bo select thi moi ban ghi work nang gap hang chuc lan, keo ca
     * mot tap chi lon ve se mat vai tram MB.
     */
    private static final String WORKS_SELECT_FIELDS = "id,publication_year,authorships";

    /**
     * Chi can noi cong tac gan nhat de biet quoc gia cua tac gia.
     */
    private static final String AUTHORS_SELECT_FIELDS = "id,last_known_institutions";

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
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/autocomplete/sources").queryParam("q", query)
                .queryParam("mailto", mailto).build().encode().toUri();

        try {
            return exchange(uri, OpenAlexAutocompleteResponse.class);

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
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/sources/issn:" + issn).queryParam("mailto", mailto)
                .build().encode().toUri();

        try {
            return exchange(uri, OpenAlexSourceResponse.class);

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
     * Lay mot trang bai bao cua tap chi trong khoang ngay cho truoc.
     * <p>
     * Dung cursor thay vi page vi OpenAlex chan phan trang thuong o ban ghi thu 10.000, ma nhieu tap chi co so bai lon
     * hon the. Truyen cursor rong cho lan goi dau tien.
     *
     * @return trang ket qua, doc meta va nextCursor de biet con du lieu hay khong
     */
    public OpenAlexWorksResponse fetchWorks(String sourceId, LocalDate fromDate, LocalDate toDate, String cursor) {
        String filter = "primary_location.source.id:" + sourceId + ",from_publication_date:" + fromDate
                + ",to_publication_date:" + toDate;

        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/works").queryParam("filter", filter)
                .queryParam("select", WORKS_SELECT_FIELDS)
                .queryParam("per-page", CommonConstant.Journal.WORKS_PAGE_SIZE)
                .queryParam("cursor", StringUtils.hasText(cursor) ? cursor : "*").queryParam("mailto", mailto).build()
                .encode().toUri();

        try {
            return exchange(uri, OpenAlexWorksResponse.class);

        } catch (HttpServerErrorException.GatewayTimeout e) {
            // Khoang ngay qua rong nen OpenAlex xu ly khong kip. Nem rieng de ben goi chia nho khoang va thu lai.
            throw new OpenAlexTimeoutException("OpenAlex timed out for range " + fromDate + " to " + toDate, e);

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("OpenAlex daily quota exhausted while fetching works. Source: {}", sourceId, e);
            throw new AppException(429, ErrorMessage.Journal.RATE_LIMIT_EXCEEDED);

        } catch (RestClientException e) {
            log.error("OpenAlex works fetch failed. Source: {}, range: {} to {}", sourceId, fromDate, toDate, e);
            throw new AppException(503, ErrorMessage.Journal.OPENALEX_UNAVAILABLE);
        }
    }

    /**
     * Tra noi cong tac gan nhat cua mot lo tac gia, dung de suy ra quoc gia cho nhung bai ma OpenAlex bo trong truong
     * countries.
     * <p>
     * OpenAlex cho gop nhieu ma vao mot lan goi bang filter=openalex_id:A1|A2|A3, nen ca lo chi ton 1 credit thay vi
     * moi tac gia mot lan goi. Ben goi phai tu chia lo cho URL khong qua dai.
     */
    public OpenAlexAuthorsResponse fetchAuthors(List<String> authorIds) {
        String filter = "openalex_id:" + String.join("|", authorIds);

        URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/authors").queryParam("filter", filter)
                .queryParam("select", AUTHORS_SELECT_FIELDS)
                .queryParam("per-page", CommonConstant.Journal.WORKS_PAGE_SIZE).queryParam("mailto", mailto).build()
                .encode().toUri();

        try {
            return exchange(uri, OpenAlexAuthorsResponse.class);

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("OpenAlex daily quota exhausted while resolving authors", e);
            throw new AppException(429, ErrorMessage.Journal.RATE_LIMIT_EXCEEDED);

        } catch (RestClientException e) {
            log.error("OpenAlex author lookup failed for {} authors", authorIds.size(), e);
            throw new AppException(503, ErrorMessage.Journal.OPENALEX_UNAVAILABLE);
        }
    }

    /**
     * Goi OpenAlex kem API key. Key duoc gui qua header Authorization thay vi tham so tren URL, de no khong bi in ra
     * log hay stack trace khi co loi.
     * <p>
     * Khong co key thi van goi duoc, chi la han muc chi con 1/10 va bi tinh chung theo IP.
     */
    private <T> T exchange(URI uri, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
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
                log.warn("OpenAlex budget is running low: {} USD left until midnight UTC", remaining);
            } else {
                log.debug("OpenAlex budget left today: {} USD", remaining);
            }
        } catch (NumberFormatException e) {
            // Header doi dinh dang thi bo qua, khong lam hong request cua nguoi dung
            log.debug("Cannot parse OpenAlex budget header: {}", remaining);
        }
    }
}
