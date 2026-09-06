package com.haui.lms.service;

import com.haui.lms.dto.response.JournalTrendResponse;
import com.haui.lms.dto.response.TrendJobResponse;

/**
 * Phan tich xu huong xuat ban cua mot tap chi theo quoc gia, to chuc va tac gia.
 * <p>
 * Phan tich mot tap chi lon phai keo hang tram trang du lieu tu OpenAlex, mat vai phut, nen khong the tra ket qua ngay
 * trong mot HTTP request. Vi vay quy trinh gom ba buoc: tao job, hoi tien do, lay ket qua.
 */
public interface JournalTrendService {

    /**
     * Tao mot lan phan tich moi. Neu ket qua da co san trong cache thi job tra ve ngay o trang thai COMPLETED ma khong
     * goi OpenAlex.
     *
     * @param fromYear
     *            nam bat dau, null thi tu suy ra tu khoang mac dinh
     * @param toYear
     *            nam ket thuc, null thi lay nam xuat ban gan nhat cua tap chi
     * @param inferUnknown
     *            co suy ra quoc gia cho nhung bai OpenAlex bo trong hay khong, null thi lay theo cau hinh
     */
    TrendJobResponse createJob(String issn, Integer fromYear, Integer toYear, Boolean inferUnknown);

    /**
     * Hoi trang thai va tien do cua mot lan phan tich.
     */
    TrendJobResponse getJob(String jobId);

    /**
     * Lay ket qua phan tich. Chi goi khi job da o trang thai COMPLETED.
     */
    JournalTrendResponse getResult(String jobId);
}
