package com.haui.lms.service.impl;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.dto.response.JournalTrendResponse;
import com.haui.lms.dto.response.openalex.OpenAlexWorksResponse;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Gop du lieu bai bao thanh so lieu xu huong theo quoc gia, to chuc va tac gia.
 * <p>
 * Doi tuong nay <b>co trang thai va khong an toan da luong</b>: moi lan phan tich phai tao mot instance rieng. Cach
 * dung la goi add() cho tung bai roi goi toResponse() mot lan duy nhat o cuoi.
 * <p>
 * Cach dem lam dung theo journaltrends.com, hai kieu khac nhau chay song song:
 * <ul>
 * <li><b>Fractional</b> cho quoc gia va to chuc, theo cong thuc <code>1 / so quoc gia rieng biet cua bai</code>. Luu y
 * la chia theo so quoc gia <i>rieng biet trong ca bai</i>, khong phai theo so dong tac gia: mot bai co 4 tac gia An Do
 * va 1 tac gia Duc thi moi nuoc duoc 0.5, chu khong phai 0.8 va 0.2.</li>
 * <li><b>Presence</b> cho tac gia. Moi tac gia duoc tinh tron 1 cho moi bai ho tham gia.</li>
 * </ul>
 * <p>
 * Bai khong co thong tin quoc gia hoac to chuc duoc gom vao nhom <b>Unknown</b> chu khong bi bo di. Nho vay tong cua
 * cac quoc gia trong mot nam luon bang dung so bai cua nam do, va frontend moi tinh duoc phan tram cho chinh xac. Neu
 * bo di thi mau so bi hut va moi ty le deu bi thoi phong.
 * <p>
 * Frontend co the tu suy ra nhom "Other" cho phan ngoai top N: lay so bai cua nam tru di tong cac muc duoc tra ve.
 */
class TrendAggregator {

    /**
     * Nhom danh cho bai ma OpenAlex khong co thong tin quoc gia hoac to chuc.
     * <p>
     * journaltrends con co them buoc suy luan de doan quoc gia cho nhung bai nay (nut "Infer Unknown"), o day chua lam.
     */
    private static final String UNKNOWN_KEY = "UNKNOWN";
    private static final String UNKNOWN_NAME = "Unknown";

    private final Map<Integer, Integer> worksByYear = new HashMap<>();
    private final Map<String, EntityStat> countries = new HashMap<>();
    private final Map<String, EntityStat> institutions = new HashMap<>();
    private final Map<String, EntityStat> authors = new HashMap<>();

    private int totalWorks;

    int getTotalWorks() {
        return totalWorks;
    }

    /**
     * Cong mot bai bao vao ket qua. Bai khong co nam xuat ban bi bo qua vi khong xep duoc vao truc thoi gian.
     */
    void add(OpenAlexWorksResponse.Work work) {
        if (work == null || work.publicationYear() == null) {
            return;
        }

        int year = work.publicationYear();
        totalWorks++;
        worksByYear.merge(year, 1, Integer::sum);

        List<OpenAlexWorksResponse.Authorship> authorships = work.authorships() == null ? List.of()
                : work.authorships();

        distribute(countries, collectCountries(authorships), year);
        distribute(institutions, collectInstitutions(authorships), year);
        countAuthors(authorships, year);
    }

    /**
     * Gom tat ca quoc gia rieng biet xuat hien trong ca bai. Dung LinkedHashMap de thu tu on dinh, tien khi doc log.
     */
    private Map<String, String> collectCountries(List<OpenAlexWorksResponse.Authorship> authorships) {
        Map<String, String> nameByKey = new LinkedHashMap<>();
        for (OpenAlexWorksResponse.Authorship authorship : authorships) {
            if (authorship == null || authorship.countries() == null) {
                continue;
            }
            for (String code : authorship.countries()) {
                if (StringUtils.hasText(code)) {
                    String normalized = code.trim().toUpperCase(Locale.ROOT);
                    nameByKey.putIfAbsent(normalized, countryName(normalized));
                }
            }
        }
        return nameByKey;
    }

    private Map<String, String> collectInstitutions(List<OpenAlexWorksResponse.Authorship> authorships) {
        Map<String, String> nameByKey = new LinkedHashMap<>();
        for (OpenAlexWorksResponse.Authorship authorship : authorships) {
            if (authorship == null || authorship.institutions() == null) {
                continue;
            }
            for (OpenAlexWorksResponse.Institution institution : authorship.institutions()) {
                if (institution != null && StringUtils.hasText(institution.id())) {
                    nameByKey.putIfAbsent(institution.id(), institution.displayName());
                }
            }
        }
        return nameByKey;
    }

    /**
     * Chia deu 1.0 cua bai cho cac muc tim duoc. Khong tim duoc muc nao thi don ca 1.0 vao Unknown, nho vay tong luon
     * bang so bai.
     */
    private void distribute(Map<String, EntityStat> target, Map<String, String> nameByKey, int year) {
        if (nameByKey.isEmpty()) {
            accumulate(target, UNKNOWN_KEY, UNKNOWN_NAME, year, 1d);
            return;
        }

        double weight = 1d / nameByKey.size();
        for (Map.Entry<String, String> entry : nameByKey.entrySet()) {
            accumulate(target, entry.getKey(), entry.getValue(), year, weight);
        }
    }

    /**
     * Presence counting: moi tac gia duoc tinh tron 1 cho bai nay, du ho dung ten o nhieu dong tac gia.
     */
    private void countAuthors(List<OpenAlexWorksResponse.Authorship> authorships, int year) {
        Set<String> counted = new LinkedHashSet<>();
        for (OpenAlexWorksResponse.Authorship authorship : authorships) {
            OpenAlexWorksResponse.Author author = authorship == null ? null : authorship.author();
            if (author == null || !StringUtils.hasText(author.id())) {
                continue;
            }
            if (counted.add(author.id())) {
                accumulate(authors, author.id(), author.displayName(), year, 1d);
            }
        }
    }

    private void accumulate(Map<String, EntityStat> target, String key, String name, int year, double weight) {
        EntityStat stat = target.computeIfAbsent(key, unused -> new EntityStat());

        // OpenAlex thinh thoang thieu ten o mot vai ban ghi, giu lai ten dau tien tim duoc
        if (stat.name == null && StringUtils.hasText(name)) {
            stat.name = name;
        }

        stat.total += weight;
        stat.byYear.merge(year, weight, Double::sum);
    }

    JournalTrendResponse toResponse(String openAlexId, String issn, String displayName, int fromYear, int toYear) {
        List<Integer> years = worksByYear.keySet().stream().sorted().toList();

        List<JournalTrendResponse.YearCount> worksSeries = years.stream()
                .map(year -> new JournalTrendResponse.YearCount(year, worksByYear.get(year))).toList();

        return new JournalTrendResponse(openAlexId, issn, displayName, fromYear, toYear, totalWorks,
                countRealEntities(countries), countRealEntities(institutions), authors.size(), years, worksSeries,
                topEntries(countries), topEntries(institutions), topEntries(authors), Instant.now());
    }

    /**
     * Unknown khong phai mot quoc gia hay to chuc that nen khong tinh vao so luong rieng biet.
     */
    private int countRealEntities(Map<String, EntityStat> source) {
        return source.containsKey(UNKNOWN_KEY) ? source.size() - 1 : source.size();
    }

    /**
     * Chi giu top N theo tong giam dan. Tra ve het thi response co the len toi hang chuc MB voi tap chi lon.
     * <p>
     * Unknown van nam trong bang xep hang nhu mot muc binh thuong, giong cach journaltrends hien thi no trong tooltip.
     */
    private List<JournalTrendResponse.TrendEntry> topEntries(Map<String, EntityStat> source) {
        List<Map.Entry<String, EntityStat>> sorted = new ArrayList<>(source.entrySet());
        sorted.sort(
                Comparator.comparingDouble((Map.Entry<String, EntityStat> entry) -> entry.getValue().total).reversed());

        List<JournalTrendResponse.TrendEntry> result = new ArrayList<>();
        for (Map.Entry<String, EntityStat> entry : sorted) {
            if (result.size() >= CommonConstant.Journal.TOP_ENTITIES) {
                break;
            }

            EntityStat stat = entry.getValue();
            List<JournalTrendResponse.YearValue> series = stat.byYear.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(year -> new JournalTrendResponse.YearValue(year.getKey(), round2(year.getValue()))).toList();

            String key = shortId(entry.getKey());
            String name = stat.name != null ? stat.name : key;
            result.add(new JournalTrendResponse.TrendEntry(key, name, round2(stat.total), series));
        }
        return result;
    }

    /**
     * Doi ma ISO sang ten quoc gia bang thu vien chuan, khong can goi them API.
     */
    private String countryName(String code) {
        String name = Locale.of("", code).getDisplayCountry(Locale.ENGLISH);
        // getDisplayCountry tra lai chinh ma do neu khong nhan ra
        return StringUtils.hasText(name) && !name.equals(code) ? name : code;
    }

    /**
     * OpenAlex tra id dang URL day du, chi giu lai phan ma cho gon.
     */
    private String shortId(String fullId) {
        if (!StringUtils.hasText(fullId)) {
            return fullId;
        }
        int lastSlash = fullId.lastIndexOf('/');
        return lastSlash >= 0 ? fullId.substring(lastSlash + 1) : fullId;
    }

    private double round2(double value) {
        return Math.round(value * 100d) / 100d;
    }

    private static final class EntityStat {
        private String name;
        private double total;
        private final Map<Integer, Double> byYear = new HashMap<>();
    }
}
