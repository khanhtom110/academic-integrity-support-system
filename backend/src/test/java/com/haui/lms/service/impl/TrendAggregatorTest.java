package com.haui.lms.service.impl;

import com.haui.lms.dto.response.JournalTrendResponse;
import com.haui.lms.dto.response.openalex.OpenAlexWorksResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Khoa lai cach dem cua journaltrends. Cac con so trong day duoc doi chieu voi tooltip that tren journaltrends.com nen
 * neu ai sua cong thuc thi test se do ngay.
 */
class TrendAggregatorTest {

    private static final double DELTA = 0.001;

    @Test
    @DisplayName("Quoc gia chia theo so nuoc rieng biet cua bai, khong chia theo so dong tac gia")
    void countryUsesDistinctCountriesPerPaper() {
        TrendAggregator aggregator = new TrendAggregator();

        // Bai co 4 tac gia An Do va 1 tac gia Duc.
        // Dung cong thuc journaltrends thi moi nuoc duoc 0.5, khong phai 0.8 va 0.2.
        aggregator.add(work(2016, authorship("A1", "IN"), authorship("A2", "IN"), authorship("A3", "IN"),
                authorship("A4", "IN"), authorship("A5", "DE")));

        assertEquals(0.5, totalOf(aggregator, "IN"), DELTA);
        assertEquals(0.5, totalOf(aggregator, "DE"), DELTA);
    }

    @Test
    @DisplayName("Bai khong co quoc gia duoc gom vao Unknown thay vi bi bo di")
    void missingCountryGoesToUnknown() {
        TrendAggregator aggregator = new TrendAggregator();

        aggregator.add(work(2016, authorship("A1", "IN")));
        aggregator.add(work(2016, authorship("A2")));
        aggregator.add(work(2016, authorship("A3")));

        assertEquals(1.0, totalOf(aggregator, "IN"), DELTA);
        assertEquals(2.0, totalOf(aggregator, "UNKNOWN"), DELTA);
    }

    @Test
    @DisplayName("Tong cac quoc gia trong mot nam luon bang dung so bai cua nam do")
    void countryTotalEqualsWorkCount() {
        TrendAggregator aggregator = new TrendAggregator();

        aggregator.add(work(2016, authorship("A1", "IN"), authorship("A2", "DE")));
        aggregator.add(work(2016, authorship("A3", "IN")));
        aggregator.add(work(2016, authorship("A4")));
        aggregator.add(work(2017, authorship("A5", "US", "JP")));

        JournalTrendResponse response = aggregator.toResponse("S1", "1234-5678", "Test Journal", 2016, 2017);

        double year2016 = sumForYear(response, 2016);
        double year2017 = sumForYear(response, 2017);

        // Neu mau so nay sai thi che do "Stacked %" tren frontend se thoi phong moi ty le
        assertEquals(3.0, year2016, DELTA);
        assertEquals(1.0, year2017, DELTA);
        assertEquals(4, response.totalWorks());
    }

    @Test
    @DisplayName("Unknown khong duoc tinh vao so quoc gia rieng biet")
    void unknownIsNotCountedAsCountry() {
        TrendAggregator aggregator = new TrendAggregator();

        aggregator.add(work(2016, authorship("A1", "IN")));
        aggregator.add(work(2016, authorship("A2")));

        JournalTrendResponse response = aggregator.toResponse("S1", "1234-5678", "Test Journal", 2016, 2016);
        assertEquals(1, response.uniqueCountries());
    }

    @Test
    @DisplayName("Tac gia dem theo presence: dung ten hai lan trong cung bai van chi tinh mot")
    void authorCountedOncePerPaper() {
        TrendAggregator aggregator = new TrendAggregator();

        aggregator.add(work(2016, authorship("A1", "IN"), authorship("A1", "DE"), authorship("A2", "IN")));

        JournalTrendResponse response = aggregator.toResponse("S1", "1234-5678", "Test Journal", 2016, 2016);

        assertEquals(2, response.uniqueAuthors());
        assertTrue(response.authors().stream().allMatch(entry -> Math.abs(entry.total() - 1.0) < DELTA),
                "moi tac gia phai duoc tinh dung 1 cho bai nay");
    }

    @Test
    @DisplayName("Bai khong co nam xuat ban bi bo qua")
    void workWithoutYearIsIgnored() {
        TrendAggregator aggregator = new TrendAggregator();

        aggregator.add(new OpenAlexWorksResponse.Work("W1", null, List.of(authorship("A1", "IN"))));

        assertEquals(0, aggregator.getTotalWorks());
    }

    // ==========================================
    // Ho tro dung du lieu mau
    // ==========================================

    private OpenAlexWorksResponse.Work work(Integer year, OpenAlexWorksResponse.Authorship... authorships) {
        return new OpenAlexWorksResponse.Work("W-" + year, year, Arrays.asList(authorships));
    }

    private OpenAlexWorksResponse.Authorship authorship(String authorId, String... countryCodes) {
        OpenAlexWorksResponse.Author author = new OpenAlexWorksResponse.Author(authorId, "Author " + authorId);
        return new OpenAlexWorksResponse.Authorship(author, List.of(), Arrays.asList(countryCodes));
    }

    private double totalOf(TrendAggregator aggregator, String countryKey) {
        JournalTrendResponse response = aggregator.toResponse("S1", "1234-5678", "Test Journal", 2016, 2016);
        return response.countries().stream().filter(entry -> entry.key().equals(countryKey))
                .mapToDouble(JournalTrendResponse.TrendEntry::total).sum();
    }

    private double sumForYear(JournalTrendResponse response, int year) {
        return response.countries().stream().flatMap(entry -> entry.byYear().stream())
                .filter(value -> value.year() == year).mapToDouble(JournalTrendResponse.YearValue::value).sum();
    }
}
