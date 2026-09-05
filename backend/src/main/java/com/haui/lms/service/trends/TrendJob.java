package com.haui.lms.service.trends;

import com.haui.lms.dto.aggregation.AggregationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendJob {

    public enum Status {
        QUEUED, RUNNING, COMPLETED, FAILED
    }

    private String jobId;
    private String issn;
    private TrendJob.Status status;
    private long totalWorks;
    private long processedWorks;
    private long batchCount;
    private String error;
    private AggregationDTO result;
    private Instant createdAt;
    private Instant finishedAt;

    public TrendJob(String jobId, String issn) {
        this.jobId = jobId;
        this.issn = issn;
        this.status = TrendJob.Status.QUEUED;
        this.createdAt = Instant.now();
    }
}
