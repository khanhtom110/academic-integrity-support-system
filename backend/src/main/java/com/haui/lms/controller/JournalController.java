package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.response.JournalDetailResponse;
import com.haui.lms.dto.response.JournalSearchResponse;
import com.haui.lms.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
