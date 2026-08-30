package com.haui.lms.controller;

import com.haui.lms.constant.ApiPath;
import com.haui.lms.dto.openalex.OpenAlexSourceDTO;
import com.haui.lms.service.openalex.OpenAlexClient;
import com.haui.lms.service.trends.TrendJob;
import com.haui.lms.service.trends.TrendJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPath.API_V1)
public class JournalController {

    private final OpenAlexClient openAlexClient;
    private final TrendJobService trendJobService;

    public JournalController(OpenAlexClient openAlexClient, TrendJobService trendJobService) {

        this.openAlexClient = openAlexClient;
        this.trendJobService = trendJobService;
    }

    // =====================================================
    // LOOKUP
    // =====================================================

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

            return ResponseEntity.accepted().body(Map.of("jobId", job.getJobId(),

                    "issn", job.getIssn(),

                    "status", job.getStatus().name()));

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

        return ResponseEntity.ok(Map.of("jobId", job.getJobId(),

                "issn", job.getIssn(),

                "status", job.getStatus().name(),

                "totalWorks", job.getTotalWorks(),

                "batchCount", job.getBatchCount()));
    }

    @GetMapping("/trends/{jobId}/result")
    public ResponseEntity<?> getTrendResult(@PathVariable String jobId) {

        TrendJob job = trendJobService.getJob(jobId);

        if (job == null) {

            return ResponseEntity.notFound().build();
        }

        if (job.getStatus() == TrendJob.Status.QUEUED || job.getStatus() == TrendJob.Status.RUNNING) {

            return ResponseEntity.status(org.springframework.http.HttpStatus.ACCEPTED)
                    .body(Map.of("status", job.getStatus().name(),

                            "totalWorks", job.getTotalWorks(),

                            "batchCount", job.getBatchCount()));
        }

        if (job.getStatus() == TrendJob.Status.FAILED) {

            return ResponseEntity.internalServerError().body(Map.of("status", "FAILED",

                    "error", job.getError() == null ? "Unknown error" : job.getError()));
        }

        return ResponseEntity.ok(job.getResult());
    }

}