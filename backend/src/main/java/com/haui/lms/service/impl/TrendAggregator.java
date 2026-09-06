package com.haui.lms.service.impl;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.dto.response.JournalTrendResponse;
import com.haui.lms.dto.response.openalex.OpenAlexWorksResponse;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
 * Hai cach dem duoc dung song song, day la diem de nham nhat:
 * <ul>
 * <li><b>Fractional</b> cho quoc gia va to chuc. Moi bai luon dong gop dung 1.0, chia deu cho cac dong tac gia roi chia
 * tiep cho cac quoc gia hoac to chuc trong dong do. Nho vay mot bai hop tac giua 3 nuoc khong bi dem thanh 3 bai.</li>
 * <li><b>Presence</b> cho tac gia. Moi tac gia duoc tinh tron 1 cho moi bai ho tham gia.</li>
 * </ul>
 */
class TrendAggregator {

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

        List<OpenAlexWorksResponse.Authorship> authorships = work.authorships();
        if (authorships == null || authorships.isEmpty()) {
            return;
        }

        // Moi dong tac gia duoc chia deu phan dong gop cua bai, tong cac dong bang dung 1.0
        double perAuthorship = 1d / authorships.size();

        // Mot tac gia co the dung ten o nhieu dong trong cung mot bai, chi tinh mot lan
        Set<String> countedAuthors = new HashSet<>();

        for (OpenAlexWorksResponse.Authorship authorship : authorships) {
            if (authorship == null) {
                continue;
            }

            addCountries(authorship, year, perAuthorship);
            addInstitutions(authorship, year, perAuthorship);
            addAuthor(authorship, year, countedAuthors);
        }
    }

    private void addCountries(OpenAlexWorksResponse.Authorship authorship, int year, double perAuthorship) {
        Set<String> codes = new LinkedHashSet<>();
        if (authorship.countries() != null) {
            for (String code : authorship.countries()) {
                if (StringUtils.hasText(code)) {
                    codes.add(code.trim().toUpperCase(Locale.ROOT));
                }
            }
        }

        if (codes.isEmpty()) {
            return;
        }

        double weight = perAuthorship / codes.size();
        for (String code : codes) {
            accumulate(countries, code, countryName(code), year, weight);
        }
    }

    private void addInstitutions(OpenAlexWorksResponse.Authorship authorship, int year, double perAuthorship) {
        Map<String, String> nameById = new HashMap<>();
        if (authorship.institutions() != null) {
            for (OpenAlexWorksResponse.Institution institution : authorship.institutions()) {
                if (institution != null && StringUtils.hasText(institution.id())) {
                    nameById.putIfAbsent(institution.id(), institution.displayName());
                }
            }
        }

        if (nameById.isEmpty()) {
            return;
        }

        double weight = perAuthorship / nameById.size();
        for (Map.Entry<String, String> entry : nameById.entrySet()) {
            accumulate(institutions, entry.getKey(), entry.getValue(), year, weight);
        }
    }

    private void addAuthor(OpenAlexWorksResponse.Authorship authorship, int year, Set<String> countedAuthors) {
        OpenAlexWorksResponse.Author author = authorship.author();
        if (author == null || !StringUtils.hasText(author.id())) {
            return;
        }

        // add() tra ve false neu tac gia nay da duoc tinh trong bai hien tai
        if (countedAuthors.add(author.id())) {
            accumulate(authors, author.id(), author.displayName(), year, 1d);
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

        return new JournalTrendResponse(openAlexId, issn, displayName, fromYear, toYear, totalWorks, countries.size(),
                institutions.size(), authors.size(), years, worksSeries, topEntries(countries),
                topEntries(institutions), topEntries(authors), Instant.now());
    }

    /**
     * Chi giu top N theo tong giam dan. Tra ve het thi response co the len toi hang chuc MB voi tap chi lon.
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
     * Doi ma ISO sang ten quoc gia bang thu vien chuan, khong can goi them API nhu ban truoc.
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
