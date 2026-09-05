package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.openalex.OpenAlexSourceDTO;
import com.haui.lms.dto.response.JournalDetailResponse;
import com.haui.lms.dto.response.JournalSearchResponse;
import com.haui.lms.service.JournalService;
import com.haui.lms.service.openalex.OpenAlexClient;
import com.haui.lms.service.trends.TrendJob;
import com.haui.lms.service.trends.TrendJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPath.API_V1)
@Tag(name = "Journal", description = "Tra cứu thông tin tạp chí khoa học, dữ liệu lấy từ OpenAlex")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class JournalController {

    private final JournalService journalService;
    private final OpenAlexClient openAlexClient;
    private final TrendJobService trendJobService;

    @Operation(summary = "Gợi ý tạp chí theo tên", description = "Trả về tối đa 10 gợi ý kèm ISSN, nhà xuất bản và số bài. "
            + "Rất nhiều tạp chí trùng tên nhau nên frontend cần hiển thị đủ các thông tin này để người dùng phân biệt.")
    @GetMapping(UrlConstant.Journal.SEARCH)
    public ResponseEntity<ApiResponse<List<JournalSearchResponse>>> search(
            @Parameter(description = "Từ khóa tìm kiếm, tối thiểu 2 ký tự", example = "journal of science") @RequestParam(value = "q", required = false, defaultValue = "") String query) {

        List<JournalSearchResponse> results = journalService.search(query);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Journal.SEARCH_SUCCESS, results));
    }

    @Operation(summary = "Xem chi tiết tạp chí theo ISSN", description = "Chấp nhận cả ISSN bản in lẫn bản điện tử. "
            + "Nhiều trường có thể null với tạp chí nhỏ chưa công bố đầy đủ thông tin, frontend cần xử lý trường hợp này.")
    @GetMapping(UrlConstant.Journal.DETAIL)
    public ResponseEntity<ApiResponse<JournalDetailResponse>> getByIssn(
            @Parameter(description = "Mã ISSN, ví dụ 0092-8674", example = "0092-8674") @PathVariable("issn") String issn) {

        JournalDetailResponse detail = journalService.getByIssn(issn);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Journal.GET_DETAIL_SUCCESS, detail));
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam String issn) {
        try {
            OpenAlexSourceDTO source = openAlexClient.lookupByIssn(issn);
            if (source == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(source);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Không tìm thấy journal: " + e.getMessage());
        }
    }

    @PostMapping("/trends")
    public ResponseEntity<?> createTrendsJob(@RequestParam String issn) {
        try {
            TrendJob job = trendJobService.createJob(issn);
            return ResponseEntity.accepted()
                    .body(Map.of("jobId", job.getJobId(), "issn", job.getIssn(), "status", job.getStatus().name()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Không thể tạo trends job: " + e.getMessage());
        }
    }

    @GetMapping("/trends/{jobId}/status")
    public ResponseEntity<?> getTrendStatus(@PathVariable String jobId) {
        TrendJob job = trendJobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("jobId", job.getJobId(), "issn", job.getIssn(), "status",
                job.getStatus().name(), "totalWorks", job.getTotalWorks(), "batchCount", job.getBatchCount()));
    }

    @GetMapping("/trends/{jobId}/result")
    public ResponseEntity<?> getTrendResult(@PathVariable String jobId) {
        TrendJob job = trendJobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        if (job.getStatus() == TrendJob.Status.QUEUED || job.getStatus() == TrendJob.Status.RUNNING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", job.getStatus().name(),
                    "totalWorks", job.getTotalWorks(), "batchCount", job.getBatchCount()));
        }
        if (job.getStatus() == TrendJob.Status.FAILED) {
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "FAILED", "error", job.getError() == null ? "Unknown error" : job.getError()));
        }
        return ResponseEntity.ok(job.getResult());
    }
}