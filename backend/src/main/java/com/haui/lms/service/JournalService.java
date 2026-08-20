package com.haui.lms.service;

import com.haui.lms.dto.response.JournalDetailResponse;
import com.haui.lms.dto.response.JournalSearchResponse;

import java.util.List;

public interface JournalService {

    /**
     * Goi y tap chi theo ten. Ket qua kem ISSN va nha xuat ban de nguoi dung phan biet cac tap chi trung ten.
     */
    List<JournalSearchResponse> search(String query);

    /**
     * Chi tiet tap chi theo ISSN, uu tien lay tu cache Redis.
     */
    JournalDetailResponse getByIssn(String issn);
}
