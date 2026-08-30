package com.haui.lms.service.trends;

import com.haui.lms.dto.aggregation.AggregationDTO;
import com.haui.lms.dto.openalex.OpenAlexSourceDTO;
import com.haui.lms.service.aggregation.AggregationService;
import com.haui.lms.service.openalex.OpenAlexClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TrendJobService {
    /**
     * Chỉ chạy 1 job tại một thời điểm. Tránh nhiều job cùng lúc cùng gọi OpenAlex và tiêu quota quá nhanh.
     */
    private static final int MAX_CONCURRENT_JOBS = 1;

    /**
     * Độ sâu tối đa khi chia nhỏ khoảng thời gian.
     *
     * Ví dụ:
     *
     * 2014-01-01 -> 2014-12-31 ↓ 2014-01-01 -> 2014-06-30 2014-07-01 -> 2014-12-31
     *
     * Nếu vẫn timeout thì tiếp tục chia nhỏ.
     */
    private static final int MAX_SPLIT_DEPTH = 6;

    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_JOBS);

    private final Map<String, TrendJob> jobs = new ConcurrentHashMap<>();

    private final OpenAlexClient openAlexClient;

    private final AggregationService aggregationService;

    public TrendJobService(OpenAlexClient openAlexClient, AggregationService aggregationService) {
        this.openAlexClient = openAlexClient;
        this.aggregationService = aggregationService;
    }

    public TrendJob createJob(String issn) {
        String jobId = UUID.randomUUID().toString();
        TrendJob job = new TrendJob();
        job.setJobId(jobId);
        job.setIssn(issn);
        job.setStatus(TrendJob.Status.QUEUED);
        job.setTotalWorks(0);
        job.setProcessedWorks(0);
        job.setBatchCount(0);
        job.setCreatedAt(Instant.now());

        jobs.put(jobId, job);

        executor.submit(() -> processJob(job));

        return job;
    }

    public TrendJob getJob(String jobId) {
        return jobs.get(jobId);
    }

    private void processJob(TrendJob job) {

        try {

            job.setStatus(TrendJob.Status.RUNNING);

            System.out.println("JOB " + job.getJobId() + " | START" + " | ISSN=" + job.getIssn());

            // 1. LOOKUP JOURNAL
            OpenAlexSourceDTO source = openAlexClient.lookupByIssn(job.getIssn());

            if (source == null) {

                failJob(job, "Không tìm thấy journal với ISSN: " + job.getIssn());

                return;
            }

            String sourceId = source.getId();

            if (sourceId == null || sourceId.isBlank()) {

                failJob(job, "OpenAlex source không có ID");

                return;
            }

            Integer firstYear = source.getFirstPublicationYear();

            Integer lastYear = source.getLastPublicationYear();

            if (firstYear == null || lastYear == null) {

                failJob(job, "Journal không có firstPublicationYear/lastPublicationYear");

                return;
            }

            System.out.println(
                    "JOB " + job.getJobId() + " | SOURCE=" + sourceId + " | YEARS=" + firstYear + "-" + lastYear);

            AggregationService.AggregationAccumulator accumulator = aggregationService.createAccumulator();

            for (int year = firstYear; year <= lastYear; year++) {

                processYear(job, accumulator, sourceId, year);
            }

            AggregationDTO result = aggregationService.finish(accumulator);

            job.setResult(result);

            job.setStatus(TrendJob.Status.COMPLETED);

            job.setFinishedAt(Instant.now());

            System.out.println("JOB " + job.getJobId() + " | COMPLETED" + " | totalWorks=" + job.getTotalWorks()
                    + " | batches=" + job.getBatchCount());

        } catch (OpenAlexClient.OpenAlexRateLimitException e) {

            failJob(job, "OPENALEX_RATE_LIMIT: " + e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            failJob(job, e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void processYear(TrendJob job, AggregationService.AggregationAccumulator accumulator, String sourceId,
            int year) {

        String fromDate = year + "-01-01";

        String toDate = year + "-12-31";

        System.out.println(
                "JOB " + job.getJobId() + " | PROCESS YEAR=" + year + " | PERIOD=" + fromDate + " -> " + toDate);

        processDateRange(job, accumulator, sourceId, fromDate, toDate, 0);
    }

    private void processDateRange(TrendJob job, AggregationService.AggregationAccumulator accumulator, String sourceId,
            String fromDate, String toDate, int splitDepth) {

        try {

            fetchRange(job, accumulator, sourceId, fromDate, toDate);

            return;

        } catch (OpenAlexClient.OpenAlexQueryTimeoutException e) {

            System.err.println("JOB " + job.getJobId() + " | QUERY TIMEOUT" + " | PERIOD=" + fromDate + " -> " + toDate
                    + " | depth=" + splitDepth);

            if (splitDepth >= MAX_SPLIT_DEPTH) {

                throw e;
            }

            LocalDate start = LocalDate.parse(fromDate);

            LocalDate end = LocalDate.parse(toDate);

            // Không thể chia nhỏ hơn nữa
            if (!start.isBefore(end)) {

                throw e;
            }

            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);

            long half = days / 2;

            LocalDate middle = start.plusDays(half);

            LocalDate leftEnd = middle;

            LocalDate rightStart = middle.plusDays(1);

            String leftFrom = start.toString();

            String leftTo = leftEnd.toString();

            System.out.println("JOB " + job.getJobId() + " | SPLIT LEFT" + " | " + leftFrom + " -> " + leftTo);

            processDateRange(job, accumulator, sourceId, leftFrom, leftTo, splitDepth + 1);

            // =================================================
            // 6. RIGHT RANGE
            // =================================================

            String rightFrom = rightStart.toString();

            String rightTo = end.toString();

            System.out.println("JOB " + job.getJobId() + " | SPLIT RIGHT" + " | " + rightFrom + " -> " + rightTo);

            processDateRange(job, accumulator, sourceId, rightFrom, rightTo, splitDepth + 1);
        }
    }

    private void fetchRange(TrendJob job, AggregationService.AggregationAccumulator accumulator, String sourceId,
            String fromDate, String toDate) {

        String cursor = "*";

        while (cursor != null) {

            OpenAlexClient.WorksBatch batch = openAlexClient.fetchWorksBatch(sourceId, fromDate, toDate, cursor);

            if (batch == null) {
                break;
            }

            if (batch.getWorks() == null || batch.getWorks().isEmpty()) {
                break;
            }

            aggregationService.aggregateBatch(accumulator, batch.getWorks());

            long currentWorks = job.getTotalWorks() + batch.getWorks().size();

            long currentBatch = job.getBatchCount() + 1;

            job.setTotalWorks(currentWorks);

            job.setProcessedWorks(currentWorks);

            job.setBatchCount(currentBatch);

            System.out.println("JOB " + job.getJobId() + " | ISSN=" + job.getIssn() + " | period=" + fromDate + " -> "
                    + toDate + " | batch=" + currentBatch + " | works=" + currentWorks);

            String nextCursor = batch.getNextCursor();

            if (nextCursor == null || nextCursor.isBlank()) {
                break;
            }

            // Tránh vòng lặp vô hạn
            if ("*".equals(nextCursor)) {
                break;
            }

            cursor = nextCursor;
        }
    }

    private void failJob(TrendJob job, String error) {

        job.setStatus(TrendJob.Status.FAILED);

        job.setError(error);

        job.setFinishedAt(Instant.now());

        System.err.println("=================================================");

        System.err.println("JOB FAILED: " + job.getJobId());

        System.err.println("ISSN: " + job.getIssn());

        System.err.println("ERROR: " + error);

        System.err.println("=================================================");
    }
}
