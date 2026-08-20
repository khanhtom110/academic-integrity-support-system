package com.haui.lms.service.impl;

import com.haui.lms.client.OpenAlexClient;
import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.JournalDetailResponse;
import com.haui.lms.dto.response.JournalSearchResponse;
import com.haui.lms.dto.response.openalex.OpenAlexAutocompleteResponse;
import com.haui.lms.dto.response.openalex.OpenAlexSourceResponse;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.service.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final OpenAlexClient openAlexClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openalex.cache.detail-ttl-days}")
    private long detailTtlDays;

    @Value("${openalex.cache.search-ttl-hours}")
    private long searchTtlHours;

    @Override
    public List<JournalSearchResponse> search(String query) {
        String keyword = query == null ? "" : query.trim();

        if (keyword.length() < CommonConstant.Journal.SEARCH_MIN_LENGTH) {
            throw new AppException(400, ErrorMessage.Journal.SEARCH_QUERY_TOO_SHORT);
        }

        String cacheKey = CommonConstant.Journal.CACHE_SEARCH_PREFIX + keyword.toLowerCase(Locale.ROOT);

        List<JournalSearchResponse> cached = readListFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        OpenAlexAutocompleteResponse response = openAlexClient.autocompleteSources(keyword);
        List<JournalSearchResponse> results = toSearchResults(response);

        writeToCache(cacheKey, results, searchTtlHours, TimeUnit.HOURS);
        return results;
    }

    @Override
    public JournalDetailResponse getByIssn(String issn) {
        String normalizedIssn = issn == null ? "" : issn.trim().toUpperCase(Locale.ROOT);

        if (!normalizedIssn.matches(CommonConstant.Journal.ISSN_REGEX)) {
            throw new AppException(400, ErrorMessage.Journal.INVALID_ISSN_FORMAT);
        }

        String cacheKey = CommonConstant.Journal.CACHE_DETAIL_PREFIX + normalizedIssn;

        JournalDetailResponse cached = readFromCache(cacheKey, JournalDetailResponse.class);
        if (cached != null) {
            return cached;
        }

        OpenAlexSourceResponse source = openAlexClient.getSourceByIssn(normalizedIssn);
        if (source == null) {
            throw new AppException(404, ErrorMessage.Journal.JOURNAL_NOT_FOUND);
        }

        JournalDetailResponse detail = toDetailResponse(source);

        writeToCache(cacheKey, detail, detailTtlDays, TimeUnit.DAYS);
        return detail;
    }

    // ==========================================
    // Mapping
    // ==========================================

    private List<JournalSearchResponse> toSearchResults(OpenAlexAutocompleteResponse response) {
        if (response == null || response.results() == null) {
            return Collections.emptyList();
        }

        List<JournalSearchResponse> results = new ArrayList<>();
        for (OpenAlexAutocompleteResponse.Result item : response.results()) {
            // Bo qua ket qua khong co ISSN vi khong the xem chi tiet duoc
            if (!StringUtils.hasText(item.externalId())) {
                continue;
            }
            results.add(new JournalSearchResponse(extractOpenAlexId(item.id()), item.externalId(), item.displayName(),
                    item.hint(), item.worksCount(), item.citedByCount()));
        }
        return results;
    }

    private JournalDetailResponse toDetailResponse(OpenAlexSourceResponse source) {
        OpenAlexSourceResponse.SummaryStats stats = source.summaryStats();

        return new JournalDetailResponse(extractOpenAlexId(source.id()), source.issnL(),
                source.issn() == null ? List.of() : source.issn(), source.displayName(),
                source.alternateTitles() == null ? List.of() : source.alternateTitles(), source.hostOrganizationName(),
                source.homepageUrl(), source.countryCode(), source.type(), source.worksCount(), source.oaWorksCount(),
                source.citedByCount(), stats == null ? null : stats.hIndex(), stats == null ? null : stats.i10Index(),
                stats == null ? null : stats.twoYearMeanCitedness(), source.isOa(), source.isInDoaj(), source.isCore(),
                source.isOjs(), source.firstPublicationYear(), source.lastPublicationYear(), source.apcUsd(),
                toApcHistory(source), toYearlyStats(source), toTopics(source), Instant.now());
    }

    private List<JournalDetailResponse.ApcByYear> toApcHistory(OpenAlexSourceResponse source) {
        if (source.apcUsdByYear() == null) {
            return List.of();
        }
        List<JournalDetailResponse.ApcByYear> history = new ArrayList<>();
        for (OpenAlexSourceResponse.ApcByYear item : source.apcUsdByYear()) {
            if (item.year() != null && item.price() != null) {
                history.add(new JournalDetailResponse.ApcByYear(item.year(), item.price()));
            }
        }
        // OpenAlex tra ve giam dan, dao lai tang dan de ve bieu do duong
        history.sort(Comparator.comparing(JournalDetailResponse.ApcByYear::year));
        return history;
    }

    private List<JournalDetailResponse.YearlyStat> toYearlyStats(OpenAlexSourceResponse source) {
        if (source.countsByYear() == null) {
            return List.of();
        }
        List<JournalDetailResponse.YearlyStat> stats = new ArrayList<>();
        for (OpenAlexSourceResponse.CountByYear item : source.countsByYear()) {
            if (item.year() != null) {
                stats.add(new JournalDetailResponse.YearlyStat(item.year(), item.worksCount(), item.oaWorksCount(),
                        item.citedByCount()));
            }
        }
        stats.sort(Comparator.comparing(JournalDetailResponse.YearlyStat::year));
        return stats;
    }

    /**
     * OpenAlex tra ve hai mang chu de rieng biet: topics co so bai, topic_share co ty le anh huong. Ham nay gop chung
     * lai theo id chu de.
     * <p>
     * influencePercent duoc chuan hoa trong pham vi cac chu de tra ve cho tong bang 100%, giong cach journaltrends.com
     * hien thi.
     */
    private List<JournalDetailResponse.TopicShare> toTopics(OpenAlexSourceResponse source) {
        List<OpenAlexSourceResponse.Topic> shares = source.topicShare();
        if (shares == null || shares.isEmpty()) {
            return List.of();
        }

        // Tra cuu so bai theo id chu de tu mang topics
        Map<String, Integer> countById = new HashMap<>();
        if (source.topics() != null) {
            for (OpenAlexSourceResponse.Topic topic : source.topics()) {
                if (topic.id() != null && topic.count() != null) {
                    countById.put(topic.id(), topic.count());
                }
            }
        }

        double totalShare = 0d;
        for (OpenAlexSourceResponse.Topic topic : shares) {
            if (topic.value() != null) {
                totalShare += topic.value();
            }
        }

        Integer journalWorks = source.worksCount();

        List<JournalDetailResponse.TopicShare> topics = new ArrayList<>();
        for (OpenAlexSourceResponse.Topic topic : shares) {
            Integer worksCount = countById.get(topic.id());

            Double worksPercent = (worksCount != null && journalWorks != null && journalWorks > 0)
                    ? round2(worksCount * 100d / journalWorks) : null;

            Double influencePercent = (topic.value() != null && totalShare > 0)
                    ? round2(topic.value() * 100d / totalShare) : null;

            topics.add(new JournalDetailResponse.TopicShare(topic.displayName(), nameOf(topic.subfield()),
                    nameOf(topic.field()), nameOf(topic.domain()), worksCount, worksPercent, influencePercent));
        }
        return topics;
    }

    private String nameOf(OpenAlexSourceResponse.Named named) {
        return named == null ? null : named.displayName();
    }

    /**
     * OpenAlex tra ve id dang URL đầy đủ, chi giu lai phan ma cho gon.
     */
    private String extractOpenAlexId(String fullId) {
        if (!StringUtils.hasText(fullId)) {
            return null;
        }
        int lastSlash = fullId.lastIndexOf('/');
        return lastSlash >= 0 ? fullId.substring(lastSlash + 1) : fullId;
    }

    private double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }

    // ==========================================
    // Cache
    // ==========================================

    private <T> T readFromCache(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null : objectMapper.readValue(json, type);
        } catch (Exception e) {
            // Cache hong thi coi nhu chua co, van goi API binh thuong
            log.warn("Failed to read cache. Key: {}", key, e);
            return null;
        }
    }

    private List<JournalSearchResponse> readListFromCache(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            JournalSearchResponse[] array = objectMapper.readValue(json, JournalSearchResponse[].class);
            return List.of(array);
        } catch (Exception e) {
            log.warn("Failed to read cache. Key: {}", key, e);
            return null;
        }
    }

    private void writeToCache(String key, Object value, long ttl, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl, unit);
        } catch (Exception e) {
            // Khong ghi duoc cache thi bo qua, khong lam hong request cua nguoi dung
            log.warn("Failed to write cache. Key: {}", key, e);
        }
    }
}
