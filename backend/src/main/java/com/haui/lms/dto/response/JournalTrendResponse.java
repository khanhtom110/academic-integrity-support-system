package com.haui.lms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Ket qua phan tich xu huong cua mot tap chi, du de ve ca ba tab quoc gia / to chuc / tac gia.
 * <p>
 * Cach dem khong giong nhau giua cac tab, day la chu y quan trong khi hien thi:
 * <ul>
 * <li>Quoc gia va to chuc dung <b>fractional counting</b>: mot bai luon dong gop tong cong 1.0, chia deu cho cac dong
 * tac gia roi chia tiep cho cac quoc gia hoac to chuc trong dong do. Nho vay bai hop tac quoc te khong bi dem lap.</li>
 * <li>Tac gia dung <b>presence counting</b>: moi tac gia duoc tinh 1 cho moi bai ho tham gia, khong chia nho.</li>
 * </ul>
 * Vi vay tong cua cac quoc gia se bang so bai, con tong cua cac tac gia thi lon hon.
 */
public record JournalTrendResponse(@Schema(description = "Mã OpenAlex của tạp chí") String openAlexId,

        @Schema(description = "ISSN đã dùng để phân tích") String issn,

        @Schema(description = "Tên tạp chí") String displayName,

        @Schema(description = "Năm bắt đầu phân tích") Integer fromYear,

        @Schema(description = "Năm kết thúc phân tích") Integer toYear,

        @Schema(description = "Tổng số bài đã xử lý trong khoảng năm trên") Integer totalWorks,

        @Schema(description = "Số quốc gia khác nhau xuất hiện") Integer uniqueCountries,

        @Schema(description = "Số tổ chức khác nhau xuất hiện") Integer uniqueInstitutions,

        @Schema(description = "Số tác giả khác nhau xuất hiện") Integer uniqueAuthors,

        @Schema(description = "Các năm có dữ liệu, tăng dần. Frontend dùng làm trục hoành") List<Integer> years,

        @Schema(description = "Số bài theo năm") List<YearCount> worksByYear,

        @Schema(description = "Top quốc gia, fractional counting, giảm dần theo tổng") List<TrendEntry> countries,

        @Schema(description = "Top tổ chức, fractional counting, giảm dần theo tổng") List<TrendEntry> institutions,

        @Schema(description = "Top tác giả, presence counting, giảm dần theo số bài") List<TrendEntry> authors,

        @Schema(description = "Thời điểm hoàn tất phân tích") Instant fetchedAt) {

    public record YearCount(Integer year, Integer count) {
    }

    /**
     * @param key
     *            ma quoc gia (VN, US) hoac ma OpenAlex cua to chuc / tac gia
     * @param total
     *            tong tren toan khoang nam
     * @param byYear
     *            chuoi theo nam, chi chua nhung nam thuc su co so lieu
     */
    public record TrendEntry(String key, String name, Double total, List<YearValue> byYear) {
    }

    public record YearValue(Integer year, Double value) {
    }
}
