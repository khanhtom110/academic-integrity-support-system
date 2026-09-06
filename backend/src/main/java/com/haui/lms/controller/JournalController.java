package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.response.JournalDetailResponse;
import com.haui.lms.dto.response.JournalSearchResponse;
import com.haui.lms.dto.response.JournalTrendResponse;
import com.haui.lms.dto.response.TrendJobResponse;
import com.haui.lms.service.JournalService;
import com.haui.lms.service.JournalTrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Toan bo validate duoc dat trong JournalService thay vi dung annotation o day, de moi loi deu tra ve ma 400 kem thong
 * bao ro rang. Neu dung annotation thi ConstraintViolationException se tra ve 422, khong dong nhat voi phan con lai.
 */
@RestController
@RequestMapping(ApiPath.API_V1)
@Tag(name = "Journal", description = "Tra cứu thông tin tạp chí khoa học, dữ liệu lấy từ OpenAlex")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class JournalController {

    private final JournalService journalService;
    private final JournalTrendService journalTrendService;

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

    @Operation(summary = "Bắt đầu phân tích xu hướng của một tạp chí", description = "Phân tích một tạp chí lớn phải kéo hàng trăm trang dữ liệu từ OpenAlex nên "
            + "không trả kết quả ngay được. Endpoint này chỉ tạo job và trả về jobId, frontend dùng jobId để hỏi tiến độ. "
            + "Mặc định chỉ phân tích 25 năm gần nhất vì mỗi năm là ít nhất một lần gọi OpenAlex. "
            + "Nếu khoảng năm này đã được phân tích trước đó thì job trả về COMPLETED ngay lập tức.")
    @PostMapping(UrlConstant.Journal.TRENDS)
    public ResponseEntity<ApiResponse<TrendJobResponse>> createTrendJob(
            @Parameter(description = "Mã ISSN, ví dụ 2002-441X", example = "2002-441X") @PathVariable("issn") String issn,
            @Parameter(description = "Năm bắt đầu, bỏ trống thì lấy 25 năm gần nhất") @RequestParam(value = "fromYear", required = false) Integer fromYear,
            @Parameter(description = "Năm kết thúc, bỏ trống thì lấy năm xuất bản gần nhất của tạp chí") @RequestParam(value = "toYear", required = false) Integer toYear) {

        TrendJobResponse job = journalTrendService.createJob(issn, fromYear, toYear);
        return ResponseEntity.accepted()
                .body(ApiResponse.accepted(SuccessMessage.Journal.CREATE_TREND_JOB_SUCCESS, job));
    }

    @Operation(summary = "Hỏi tiến độ phân tích", description = "Gọi định kỳ vài giây một lần cho tới khi status là COMPLETED rồi mới lấy kết quả. "
            + "Trường percent tính theo số năm đã xử lý xong.")
    @GetMapping(UrlConstant.Journal.TREND_JOB)
    public ResponseEntity<ApiResponse<TrendJobResponse>> getTrendJob(
            @Parameter(description = "Mã job nhận được khi tạo") @PathVariable("jobId") String jobId) {

        TrendJobResponse job = journalTrendService.getJob(jobId);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Journal.GET_TREND_JOB_SUCCESS, job));
    }

    @Operation(summary = "Lấy kết quả phân tích", description = "Chỉ gọi khi job đã COMPLETED. Job chưa xong sẽ trả về 409 kèm thông báo, "
            + "job thất bại trả về 500 kèm lý do. Kết quả gồm đủ dữ liệu cho ba biểu đồ quốc gia, tổ chức và tác giả.")
    @GetMapping(UrlConstant.Journal.TREND_RESULT)
    public ResponseEntity<ApiResponse<JournalTrendResponse>> getTrendResult(
            @Parameter(description = "Mã job nhận được khi tạo") @PathVariable("jobId") String jobId) {

        JournalTrendResponse result = journalTrendService.getResult(jobId);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Journal.GET_TREND_RESULT_SUCCESS, result));
    }
}
