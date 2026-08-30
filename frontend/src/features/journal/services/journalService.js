import { API } from "../../../constants/api";
import apiClient from "../../../services/apiClient";

/**
 * Tìm kiếm tạp chí theo tên hoặc mã ISSN (hỗ trợ cả mã gõ dở, gõ thiếu gạch ngang và từ khóa).
 * Backend: GET /api/v1/journals/search?q=<keyword>
 * Trả về { statusCode, message, data: JournalSearchResponse[], timestamp }
 */
export const searchJournals = async (query, signal) => {
  const trimmed = query?.trim();
  if (!trimmed) return [];

  // 1. Thử gọi backend search từ khóa mặc định
  try {
    const res = await apiClient.get(API.JOURNAL.SEARCH, {
      params: { q: trimmed },
      signal,
    });
    const list = res.data?.data ?? [];
    if (list.length > 0) return list;
  } catch (err) {
    if (signal?.aborted) throw err;
  }

  // 2. Chuẩn hóa chuỗi ISSN (lọc chữ số và X/x)
  const clean = trimmed.replace(/[^0-9xX]/gi, "");
  let formattedIssn = trimmed;
  if (clean.length === 8) {
    formattedIssn = `${clean.slice(0, 4)}-${clean.slice(4)}`;
  }

  // 3. Tìm trực tiếp theo mã ISSN qua API Detail Backend
  if (clean.length >= 4) {
    try {
      const detailRes = await apiClient.get(API.JOURNAL.DETAIL(formattedIssn), { signal });
      const detail = detailRes.data?.data;
      if (detail && detail.displayName) {
        return [{
          displayName: detail.displayName,
          issn: detail.issnL || detail.issns?.[0] || formattedIssn,
          publisher: detail.publisher || "Không rõ nhà xuất bản",
          worksCount: detail.worksCount ?? 0,
          openAlexId: detail.openAlexId,
          directDetail: detail
        }];
      }
    } catch (e) {
      if (signal?.aborted) throw e;
    }
  }

  // 4. Tìm kiếm từ nguồn OpenAlex công khai (cho cả mã ISSN và từ khóa dở dang)
  try {
    let openAlexUrl = "";
    if (clean.length >= 7) {
      openAlexUrl = `https://api.openalex.org/sources?filter=issn:${formattedIssn}`;
    } else {
      openAlexUrl = `https://api.openalex.org/autocomplete/sources?q=${encodeURIComponent(trimmed)}`;
    }

    const openAlexRes = await fetch(openAlexUrl, { signal }).then(r => r.json());
    if (openAlexRes.results && openAlexRes.results.length > 0) {
      return openAlexRes.results.map(item => ({
        displayName: item.display_name || item.name,
        issn: item.issn_l || (item.issn && item.issn[0]) || formattedIssn,
        publisher: item.host_organization_name || item.hint || "Không rõ nhà xuất bản",
        worksCount: item.works_count ?? 0,
        openAlexId: item.id
      }));
    }
  } catch (e) {
    if (signal?.aborted) throw e;
  }

  return [];
};

/**
 * Xem chi tiết tạp chí theo ISSN.
 * Backend: GET /api/v1/journals/{issn}
 * Trả về { statusCode, message, data: JournalDetailResponse, timestamp }
 */
export const getJournalDetail = async (issn) => {
  const res = await apiClient.get(API.JOURNAL.DETAIL(issn));
  return res.data?.data ?? null;
};
