package com.haui.lms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Mot dong trong danh sach goi y khi go ten tap chi.
 * <p>
 * Co rat nhieu tap chi trung ten nhau (rieng "Journal of Science and Technology" da co 6 tap chi khac nhau) nen bat
 * buoc phai hien thi kem ISSN, nha xuat ban va so bai de nguoi dung phan biet.
 */
public record JournalSearchResponse(@Schema(description = "Mã OpenAlex, ví dụ S110447773") String openAlexId,

        @Schema(description = "ISSN dùng để xem chi tiết") String issn,

        @Schema(description = "Tên tạp chí") String displayName,

        @Schema(description = "Nhà xuất bản, có thể không xác định") String publisher,

        @Schema(description = "Tổng số bài đã xuất bản") Integer worksCount,

        @Schema(description = "Tổng lượt trích dẫn") Long citedByCount) {
}
