package com.haui.lms.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Trang thai cua mot lan phan tich. Phan tich mot tap chi lon phai keo hang tram trang du lieu tu OpenAlex nen khong
 * chay kip trong mot HTTP request, vi vay client tao job roi hoi tien do dinh ky.
 */
public record TrendJobResponse(@Schema(description = "Mã job, dùng để hỏi tiến độ và lấy kết quả") String jobId,

        @Schema(description = "ISSN đang phân tích") String issn,

        @Schema(description = "QUEUED, RUNNING, COMPLETED hoặc FAILED") String status,

        @Schema(description = "Năm bắt đầu") Integer fromYear,

        @Schema(description = "Năm kết thúc") Integer toYear,

        @Schema(description = "Số bài đã xử lý") Integer processedWorks,

        @Schema(description = "Tổng số bài, chỉ có khi status là COMPLETED") Integer totalWorks,

        @Schema(description = "Phần trăm hoàn thành, null khi chưa ước lượng được") Integer percent,

        @Schema(description = "Kết quả có sẵn trong cache nên job xong ngay, không phải gọi OpenAlex") Boolean fromCache,

        @Schema(description = "Lý do thất bại, chỉ có khi status là FAILED") String error,

        Instant createdAt,

        Instant finishedAt) {
}
