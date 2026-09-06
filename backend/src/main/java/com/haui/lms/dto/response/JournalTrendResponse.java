package com.haui.lms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Ket qua phan tich xu huong cua mot tap chi, du de ve ca ba tab quoc gia / to chuc / tac gia.
 * <p>
 * institutionsByCountry da duoc nhom san theo quoc gia, frontend chi can render dropdown va bang tuong ung, khong phai
 * tu loc lai tu danh sach institutions chung. Neu chi loc tu top chung thi tap chi co nhieu quoc gia dong deu se ra
 * sai: mot to chuc thuc su lon cua mot nuoc co the khong lot vao top chung nhung van la top cua rieng nuoc do.
 * <p>
 * Cach dem khong giong nhau giua cac tab, day la chu y quan trong khi hien thi:
 * <ul>
 * <li>Quoc gia va to chuc dung <b>fractional counting</b> theo cong thuc
 * <code>1 / so quoc gia rieng biet cua bai</code>, giong journaltrends. Mot bai co 4 tac gia An Do va 1 tac gia Duc thi
 * moi nuoc duoc 0.5.</li>
 * <li>Tac gia dung <b>presence counting</b>: moi tac gia duoc tinh 1 cho moi bai ho tham gia, khong chia nho.</li>
 * </ul>
 * Bai khong co thong tin quoc gia hoac to chuc duoc gom vao muc <b>Unknown</b>, nen tong cua cac quoc gia trong mot nam
 * luon bang dung so bai cua nam do. Frontend tu suy ra muc "Other" cho phan ngoai top 20 bang cach lay so bai cua nam
 * tru di tong cac muc tra ve. Tong cua cac tac gia thi lon hon so bai vi moi bai co nhieu tac gia.
 * <p>
 * Luu y ve vi tri cua Unknown: no xep hang binh thuong theo tong nhu moi muc khac, khong bi day xuong cuoi. Neu Unknown
 * co tong lon thi no co the dung ngay dau danh sach. journaltrends luon ghim Unknown o cuoi legend kem dau *, neu can
 * giong het vay thi frontend phai tu tach rieng muc Unknown ra khoi mang truoc khi hien thi, vi countryLabels giu
 * nguyen thu tu xep hang, khong tach rieng Unknown.
 */
public record JournalTrendResponse(@Schema(description = "Mã OpenAlex của tạp chí") String openAlexId,

        @Schema(description = "ISSN đã dùng để phân tích") String issn,

        @Schema(description = "Tên tạp chí") String displayName,

        @Schema(description = "Năm bắt đầu phân tích") Integer fromYear,

        @Schema(description = "Năm kết thúc phân tích") Integer toYear,

        @Schema(description = "Có suy luận quốc gia cho những bài OpenAlex bỏ trống hay không. Bật thì số liệu đầy đủ hơn nhưng là ước lượng, tắt thì phần thiếu nằm hết ở mục Unknown") Boolean inferUnknown,

        @Schema(description = "Tổng số bài đã xử lý trong khoảng năm trên") Integer totalWorks,

        @Schema(description = "Số quốc gia khác nhau xuất hiện") Integer uniqueCountries,

        @Schema(description = "Số tổ chức khác nhau xuất hiện") Integer uniqueInstitutions,

        @Schema(description = "Số tác giả khác nhau xuất hiện") Integer uniqueAuthors,

        @Schema(description = "Các năm có dữ liệu, tăng dần. Frontend dùng làm trục hoành") List<Integer> years,

        @Schema(description = "Số bài theo năm") List<YearCount> worksByYear,

        @Schema(description = "Top quốc gia, fractional counting, giảm dần theo tổng") List<TrendEntry> countries,

        @Schema(description = "Chỉ tên các quốc gia trong trường countries, đúng theo thứ tự đó. "
                + "Tiện cho frontend dựng chú thích (legend) mà không phải tự trích tên từ mảng chi tiết") List<String> countryLabels,

        @Schema(description = "Top tổ chức, fractional counting, giảm dần theo tổng") List<TrendEntry> institutions,

        @Schema(description = "Chỉ tên các tổ chức trong trường institutions, đúng theo thứ tự đó") List<String> institutionLabels,

        @Schema(description = "Top tổ chức của từng quốc gia, để frontend vẽ dropdown lọc theo quốc gia mà không phải tự nhóm lại. "
                + "Khác với trường institutions ở trên: mỗi quốc gia có bộ top riêng của chính nó, không phải lọc lại từ top chung, "
                + "nên tạp chí có nhiều quốc gia đồng đều vẫn ra đúng top tổ chức của từng nước") List<CountryInstitutions> institutionsByCountry,

        @Schema(description = "Top tác giả, presence counting, giảm dần theo số bài") List<TrendEntry> authors,

        @Schema(description = "Chỉ tên các tác giả trong trường authors, đúng theo thứ tự đó") List<String> authorLabels,

        @Schema(description = "Thời điểm hoàn tất phân tích") Instant fetchedAt) {

    public record YearCount(Integer year, Integer count) {
    }

    /**
     * @param countryKey
     *            ma quoc gia cua chinh to chuc, khong phai quoc gia cua tac gia
     * @param totalWeight
     *            tong trong so cua tat ca to chuc thuoc quoc gia nay, dung de sap xep quoc gia nao hien truoc
     * @param institutions
     *            top to chuc cua rieng quoc gia nay, giam dan theo trong so
     */
    public record CountryInstitutions(String countryKey, String countryName, Double totalWeight,
            List<TrendEntry> institutions) {
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
