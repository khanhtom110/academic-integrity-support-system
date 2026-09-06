package com.haui.lms.service.impl;

import com.haui.lms.client.OpenAlexClient;
import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.JournalTrendResponse;
import com.haui.lms.dto.response.TrendJobResponse;
import com.haui.lms.dto.response.openalex.OpenAlexAuthorsResponse;
import com.haui.lms.dto.response.openalex.OpenAlexSourceResponse;
import com.haui.lms.dto.response.openalex.OpenAlexWorksResponse;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.exception.extended.OpenAlexTimeoutException;
import com.haui.lms.service.JournalTrendService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JournalTrendServiceImpl implements JournalTrendService {

    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    /**
     * Chay toi da hai job cung luc. Dat cao hon se tieu quota OpenAlex qua nhanh va de dinh 429.
     */
    private static final int MAX_CONCURRENT_JOBS = 2;

    private final OpenAlexClient openAlexClient;
    private final RedisJsonStore cache;

    /**
     * Luong nen chay job. Dat daemon de khong giu ung dung song khi tat.
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_JOBS, runnable -> {
        Thread thread = new Thread(runnable, "journal-trend");
        thread.setDaemon(true);
        return thread;
    });

    @Value("${openalex.trends.default-year-span}")
    private int defaultYearSpan;

    @Value("${openalex.trends.job-ttl-hours}")
    private long jobTtlHours;

    @Value("${openalex.trends.result-ttl-days}")
    private long resultTtlDays;

    @Value("${openalex.trends.infer-unknown-country}")
    private boolean inferUnknownByDefault;

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }

    // ==========================================
    // API
    // ==========================================

    @Override
    public TrendJobResponse createJob(String issn, Integer fromYear, Integer toYear, Boolean inferUnknown) {
        String normalizedIssn = normalizeIssn(issn);

        // Goi dong bo o day la co chu y: lookup theo ISSN khong ton credit, va nho no ma ISSN sai duoc bao 404 ngay
        // thay vi phai tao job roi doi job that bai.
        OpenAlexSourceResponse source = openAlexClient.getSourceByIssn(normalizedIssn);
        if (source == null) {
            throw new AppException(404, ErrorMessage.Journal.JOURNAL_NOT_FOUND);
        }

        int resolvedTo = resolveToYear(toYear, source);
        int resolvedFrom = resolveFromYear(fromYear, source, resolvedTo);
        if (resolvedFrom > resolvedTo) {
            throw new AppException(400, ErrorMessage.Journal.INVALID_YEAR_RANGE);
        }

        boolean infer = inferUnknown != null ? inferUnknown : inferUnknownByDefault;

        String jobId = UUID.randomUUID().toString();
        String resultKey = resultKey(normalizedIssn, resolvedFrom, resolvedTo, infer);

        // Da phan tich khoang nam nay roi thi tra ket qua luon, khong dot them credit
        if (cache.get(resultKey, JournalTrendResponse.class) != null) {
            TrendJobResponse cached = new TrendJobResponse(jobId, normalizedIssn, STATUS_COMPLETED, resolvedFrom,
                    resolvedTo, infer, null, null, 100, true, null, Instant.now(), Instant.now());
            saveJob(cached);
            return cached;
        }

        TrendJobResponse job = new TrendJobResponse(jobId, normalizedIssn, STATUS_QUEUED, resolvedFrom, resolvedTo,
                infer, 0, null, 0, false, null, Instant.now(), null);
        saveJob(job);

        String sourceId = source.id();
        String displayName = source.displayName();
        executor.submit(() -> runJob(job, sourceId, displayName, resultKey));

        return job;
    }

    @Override
    public TrendJobResponse getJob(String jobId) {
        TrendJobResponse job = cache.get(jobKey(jobId), TrendJobResponse.class);
        if (job == null) {
            throw new AppException(404, ErrorMessage.Journal.TREND_JOB_NOT_FOUND);
        }
        return job;
    }

    @Override
    public JournalTrendResponse getResult(String jobId) {
        TrendJobResponse job = getJob(jobId);

        if (STATUS_FAILED.equals(job.status())) {
            throw new AppException(500, ErrorMessage.Journal.TREND_JOB_FAILED + job.error());
        }

        if (!STATUS_COMPLETED.equals(job.status())) {
            // 409 chu khong phai 202: giu dung dinh dang ApiResponse cua he thong, frontend hoi status truoc roi moi
            // goi endpoint nay khi da COMPLETED.
            throw new AppException(409, ErrorMessage.Journal.TREND_JOB_NOT_READY);
        }

        JournalTrendResponse result = cache.get(
                resultKey(job.issn(), job.fromYear(), job.toYear(), Boolean.TRUE.equals(job.inferUnknown())),
                JournalTrendResponse.class);
        if (result == null) {
            // Ket qua het han truoc khi client kip lay. Bao nhu job khong con de client tao lai.
            throw new AppException(404, ErrorMessage.Journal.TREND_JOB_NOT_FOUND);
        }
        return result;
    }

    // ==========================================
    // Chay job
    // ==========================================

    private void runJob(TrendJobResponse job, String sourceId, String displayName, String resultKey) {
        log.info("Trend job started. Job: {}, ISSN: {}, years: {}-{}, inferUnknown: {}", job.jobId(), job.issn(),
                job.fromYear(), job.toYear(), job.inferUnknown());

        TrendJobResponse running = withStatus(job, STATUS_RUNNING);
        saveJob(running);

        try {
            TrendAggregator aggregator = new TrendAggregator();
            int totalYears = job.toYear() - job.fromYear() + 1;
            int doneYears = 0;

            for (int year = job.fromYear(); year <= job.toYear(); year++) {
                fetchRange(aggregator, sourceId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), 0);

                doneYears++;
                running = withProgress(running, aggregator.getTotalWorks(), doneYears * 100 / totalYears);
                saveJob(running);
            }

            inferMissingCountries(aggregator, Boolean.TRUE.equals(job.inferUnknown()));

            JournalTrendResponse result = aggregator.toResponse(shortId(sourceId), job.issn(), displayName,
                    job.fromYear(), job.toYear(), Boolean.TRUE.equals(job.inferUnknown()));
            cache.put(resultKey, result, Duration.ofDays(resultTtlDays));

            saveJob(new TrendJobResponse(job.jobId(), job.issn(), STATUS_COMPLETED, job.fromYear(), job.toYear(),
                    job.inferUnknown(), aggregator.getTotalWorks(), aggregator.getTotalWorks(), 100, false, null,
                    job.createdAt(), Instant.now()));

            log.info("Trend job completed. Job: {}, works: {}", job.jobId(), aggregator.getTotalWorks());

        } catch (Exception e) {
            log.error("Trend job failed. Job: {}, ISSN: {}", job.jobId(), job.issn(), e);
            saveJob(new TrendJobResponse(job.jobId(), job.issn(), STATUS_FAILED, job.fromYear(), job.toYear(),
                    job.inferUnknown(), running.processedWorks(), null, running.percent(), false, e.getMessage(),
                    job.createdAt(), Instant.now()));
        }
    }

    /**
     * Suy ra quoc gia cho nhung bai OpenAlex bo trong, bang noi cong tac gan nhat cua tac gia.
     * <p>
     * Tat suy luan thi van phai goi resolvePending de nhung bai do duoc don vao Unknown, neu khong tong se hut.
     */
    private void inferMissingCountries(TrendAggregator aggregator, boolean inferUnknown) {
        Set<String> authorIds = aggregator.pendingAuthorIds();

        if (!inferUnknown || authorIds.isEmpty()) {
            aggregator.resolvePending(Map.of());
            return;
        }

        log.info("Inferring country for {} authors with missing data", authorIds.size());
        aggregator.resolvePending(fetchAuthorCountries(authorIds));
    }

    /**
     * Tra quoc gia cua tac gia theo lo. Gop nhieu ma vao mot lan goi nen ca nghin tac gia cung chi ton vai chuc credit.
     */
    private Map<String, List<String>> fetchAuthorCountries(Set<String> authorIds) {
        List<String> ids = new ArrayList<>(authorIds);
        Map<String, List<String>> countryByAuthor = new HashMap<>();
        int batchSize = CommonConstant.Journal.AUTHOR_LOOKUP_BATCH_SIZE;

        for (int start = 0; start < ids.size(); start += batchSize) {
            List<String> batch = ids.subList(start, Math.min(start + batchSize, ids.size()));

            OpenAlexAuthorsResponse response = openAlexClient.fetchAuthors(batch);
            if (response == null || response.results() == null) {
                continue;
            }

            for (OpenAlexAuthorsResponse.Author author : response.results()) {
                if (author == null || !StringUtils.hasText(author.id())) {
                    continue;
                }

                List<String> codes = (author.lastKnownInstitutions() == null
                        ? List.<OpenAlexAuthorsResponse.Institution> of() : author.lastKnownInstitutions()).stream()
                                .map(OpenAlexAuthorsResponse.Institution::countryCode).filter(StringUtils::hasText)
                                .distinct().toList();

                if (!codes.isEmpty()) {
                    countryByAuthor.put(shortId(author.id()), codes);
                }
            }
        }
        return countryByAuthor;
    }

    /**
     * Keo het bai bao trong mot khoang ngay, di theo cursor cho den khi OpenAlex bao het.
     * <p>
     * Khi khoang ngay qua rong, OpenAlex tra 504 thay vi du lieu. Gap truong hop do thi chia doi khoang va thu lai tung
     * nua, toi da MAX_SPLIT_DEPTH lan. Mot nam bi chia toi da con khoang ba tuan, du nho de qua duoc.
     */
    private void fetchRange(TrendAggregator aggregator, String sourceId, LocalDate from, LocalDate to, int splitDepth) {
        try {
            String cursor = null;

            while (true) {
                OpenAlexWorksResponse page = openAlexClient.fetchWorks(sourceId, from, to, cursor);
                if (page == null || page.results() == null || page.results().isEmpty()) {
                    return;
                }

                for (OpenAlexWorksResponse.Work work : page.results()) {
                    aggregator.add(work);
                }

                String nextCursor = page.meta() == null ? null : page.meta().nextCursor();
                if (!StringUtils.hasText(nextCursor) || nextCursor.equals(cursor)) {
                    return;
                }
                cursor = nextCursor;
            }

        } catch (OpenAlexTimeoutException e) {
            if (splitDepth >= CommonConstant.Journal.MAX_SPLIT_DEPTH || !from.isBefore(to)) {
                throw e;
            }

            LocalDate middle = from.plusDays(Duration.between(from.atStartOfDay(), to.atStartOfDay()).toDays() / 2);
            log.warn("OpenAlex timed out, splitting range {} to {} at {}. Depth: {}", from, to, middle, splitDepth);

            fetchRange(aggregator, sourceId, from, middle, splitDepth + 1);
            fetchRange(aggregator, sourceId, middle.plusDays(1), to, splitDepth + 1);
        }
    }

    // ==========================================
    // Ho tro
    // ==========================================

    private String normalizeIssn(String issn) {
        String normalized = issn == null ? "" : issn.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches(CommonConstant.Journal.ISSN_REGEX)) {
            throw new AppException(400, ErrorMessage.Journal.INVALID_ISSN_FORMAT);
        }
        return normalized;
    }

    /**
     * Nam ket thuc khong duoc vuot qua nam hien tai, va mac dinh lay nam xuat ban gan nhat cua tap chi.
     */
    private int resolveToYear(Integer requested, OpenAlexSourceResponse source) {
        int currentYear = Year.now().getValue();
        int candidate = requested != null ? requested
                : (source.lastPublicationYear() != null ? source.lastPublicationYear() : currentYear);
        return Math.min(candidate, currentYear);
    }

    /**
     * Mac dinh chi lay defaultYearSpan nam gan nhat. Moi nam la it nhat mot lan goi OpenAlex nen quet ca lich su mot
     * tap chi lau doi co the ton hang tram credit cho mot lan xem.
     */
    private int resolveFromYear(Integer requested, OpenAlexSourceResponse source, int toYear) {
        if (requested != null) {
            return requested;
        }

        int span = toYear - defaultYearSpan + 1;
        Integer firstYear = source.firstPublicationYear();
        return firstYear != null ? Math.max(firstYear, span) : span;
    }

    private String shortId(String fullId) {
        if (!StringUtils.hasText(fullId)) {
            return fullId;
        }
        int lastSlash = fullId.lastIndexOf('/');
        return lastSlash >= 0 ? fullId.substring(lastSlash + 1) : fullId;
    }

    private TrendJobResponse withStatus(TrendJobResponse job, String status) {
        return new TrendJobResponse(job.jobId(), job.issn(), status, job.fromYear(), job.toYear(), job.inferUnknown(),
                job.processedWorks(), job.totalWorks(), job.percent(), job.fromCache(), job.error(), job.createdAt(),
                job.finishedAt());
    }

    private TrendJobResponse withProgress(TrendJobResponse job, int processedWorks, int percent) {
        return new TrendJobResponse(job.jobId(), job.issn(), job.status(), job.fromYear(), job.toYear(),
                job.inferUnknown(), processedWorks, job.totalWorks(), percent, job.fromCache(), job.error(),
                job.createdAt(), job.finishedAt());
    }

    private void saveJob(TrendJobResponse job) {
        cache.put(jobKey(job.jobId()), job, Duration.ofHours(jobTtlHours));
    }

    private String jobKey(String jobId) {
        return CommonConstant.Journal.CACHE_TREND_JOB_PREFIX + jobId;
    }

    /**
     * Tang so nay moi khi doi cau truc JournalTrendResponse (them/bo/doi kieu mot truong). Neu khong thi ket qua cu
     * tren Redis (TTL 7 ngay) van con nguyen dang cu, deserialize len se thieu truong moi va Jackson tu dien null thay
     * vi bao loi - rat de bi tuong nham la bug logic trong khi thuc chat la du lieu cache qua han cau truc.
     */
    private static final String RESPONSE_SCHEMA_VERSION = "v3";

    /**
     * Cache key co ca co suy luan, vi bat va tat cho ra hai bo so lieu khac han nhau.
     */
    private String resultKey(String issn, int fromYear, int toYear, boolean inferUnknown) {
        return CommonConstant.Journal.CACHE_TREND_PREFIX + RESPONSE_SCHEMA_VERSION + ":" + issn + ":" + fromYear + "-"
                + toYear + (inferUnknown ? ":inferred" : ":raw");
    }
}
