package com.haui.lms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Du lieu day du cua mot tap chi, da duoc rut gon tu response goc cua OpenAlex (khoang 30KB xuong con vai KB).
 * <p>
 * Gan nhu moi truong deu co the null: cac tap chi nho thuong khong cong bo APC, khong co quoc gia, chua du bai de tinh
 * h-index. Frontend can hien thi "Khong co thong tin" thay vi de trong hoac hien so 0.
 */
public record JournalDetailResponse(@Schema(description = "Mã OpenAlex") String openAlexId,

        @Schema(description = "ISSN đại diện do OpenAlex chọn") String issnL,

        @Schema(description = "Tất cả ISSN của tạp chí, gồm bản in và bản điện tử") List<String> issns,

        @Schema(description = "Tên tạp chí") String displayName,

        @Schema(description = "Tên gọi khác") List<String> alternateTitles,

        @Schema(description = "Nhà xuất bản") String publisher,

        @Schema(description = "Trang chủ tạp chí") String homepageUrl,

        @Schema(description = "Mã quốc gia ISO, thường null với tạp chí nhỏ") String countryCode,

        @Schema(description = "Loại nguồn: journal, conference, repository") String type,

        @Schema(description = "Tổng số bài") Integer worksCount,

        @Schema(description = "Số bài truy cập mở") Integer oaWorksCount,

        @Schema(description = "Tổng lượt trích dẫn") Long citedByCount,

        @Schema(description = "Chỉ số h") Integer hIndex,

        @Schema(description = "Số bài có ít nhất 10 trích dẫn") Integer i10Index,

        @Schema(description = "Trích dẫn trung bình 2 năm, tương đương Impact Factor. Không so sánh được giữa các ngành") Double twoYearMeanCitedness,

        @Schema(description = "Tạp chí có truy cập mở toàn phần hay không") Boolean isOa,

        @Schema(description = "Có trong danh mục DOAJ") Boolean isInDoaj,

        @Schema(description = "Được OpenAlex xếp vào nhóm nguồn cốt lõi") Boolean isCore,

        @Schema(description = "Dùng phần mềm Open Journal Systems") Boolean isOjs,

        @Schema(description = "Năm xuất bản đầu tiên OpenAlex ghi nhận") Integer firstPublicationYear,

        @Schema(description = "Năm xuất bản gần nhất") Integer lastPublicationYear,

        @Schema(description = "Phí xuất bản hiện tại (USD)") Integer apcUsd,

        @Schema(description = "Lịch sử phí xuất bản theo năm, tăng dần") List<ApcByYear> apcHistory,

        @Schema(description = "Số liệu theo năm, sắp xếp tăng dần để vẽ biểu đồ") List<YearlyStat> yearlyStats,

        @Schema(description = "Chủ đề, sắp xếp giảm dần theo mức độ ảnh hưởng") List<TopicShare> topics,

        @Schema(description = "Thời điểm dữ liệu được lấy từ OpenAlex") Instant fetchedAt) {

    public record YearlyStat(Integer year, Integer worksCount, Integer oaWorksCount, Long citedByCount) {
    }

    public record ApcByYear(Integer year, Integer priceUsd) {
    }

    /**
     * @param worksCount
     *            so bai cua tap chi thuoc chu de nay
     * @param worksPercent
     *            chiem bao nhieu phan tram so bai cua tap chi, tra loi cau "tap chi nay chu yeu dang ve cai gi"
     * @param influencePercent
     *            ty le da chuan hoa trong pham vi cac chu de tra ve, tra loi cau "tap chi nay co anh huong lon nhat o
     *            chu de nao". Day la cach journaltrends.com hien thi
     */
    public record TopicShare(String name, String subfield, String field, String domain, Integer worksCount,
            Double worksPercent, Double influencePercent) {
    }
}
