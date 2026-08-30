import { useState, useRef, useEffect, useCallback } from "react";
import { searchJournals, getJournalDetail } from "../../services/journalService";
import JournalDetail from "../JournalDetail/JournalDetail";
import "./JournalLookup.css";

const DEBOUNCE_MS = 400;
const MIN_CHARS = 2;

function formatNumber(n) {
  if (n == null) return "-";
  return n.toLocaleString("vi-VN");
}

export default function JournalLookup() {
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [results, setResults] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const [selectedSearchItem, setSelectedSearchItem] = useState(null);
  const [selectedJournal, setSelectedJournal] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const wrapperRef = useRef(null);
  const timerRef = useRef(null);
  const abortRef = useRef(null);

  const doSearch = useCallback(async (value) => {
    const trimmed = value.trim();
    if (trimmed.length < MIN_CHARS) {
      setResults(null);
      setError("");
      setShowDropdown(false);
      return;
    }
    setError("");
    setLoading(true);
    setShowDropdown(true);
    if (abortRef.current) abortRef.current.abort();
    const controller = new AbortController();
    abortRef.current = controller;
    try {
      const data = await searchJournals(trimmed, controller.signal);
      if (controller.signal.aborted) return;
      const list = Array.isArray(data) ? data : [];
      setResults(list);
      if (list.length === 0) setError(`Không tìm thấy kết quả cho "${trimmed}".`);
    } catch (err) {
      if (controller.signal.aborted) return;
      setError(err?.response?.data?.message || err?.message || "Đã có lỗi xảy ra.");
      setResults([]);
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }, []);

  const handleChange = (e) => {
    const value = e.target.value;
    setQuery(value);
    if (timerRef.current) clearTimeout(timerRef.current);
    const trimmed = value.trim();
    if (trimmed.length < MIN_CHARS) {
      setResults(null);
      setError("");
      setShowDropdown(false);
      return;
    }
    setShowDropdown(true);
    setLoading(true);
    timerRef.current = setTimeout(() => doSearch(value), DEBOUNCE_MS);
  };

  const handleClear = () => {
    setQuery("");
    setResults(null);
    setError("");
    setShowDropdown(false);
    setLoading(false);
    setSelectedSearchItem(null);
    setSelectedJournal(null);
    if (timerRef.current) clearTimeout(timerRef.current);
    if (abortRef.current) abortRef.current.abort();
  };

  const handleSelect = (journal) => {
    setQuery(journal.displayName || journal.issn || "");
    setShowDropdown(false);
    setSelectedSearchItem(journal);
    if (journal.directDetail) {
      setSelectedJournal(journal.directDetail);
    } else {
      setSelectedJournal(null);
    }
    setError("");
  };

  const handleFetchDetail = async () => {
    if (!selectedSearchItem) return;
    if (selectedSearchItem.directDetail) {
      setSelectedJournal(selectedSearchItem.directDetail);
      return;
    }
    setDetailLoading(true);
    setError("");
    setSelectedJournal(null);
    try {
      const issn = selectedSearchItem.issn;
      if (!issn) { setError("Không tìm thấy ISSN."); setDetailLoading(false); return; }
      const detail = await getJournalDetail(issn);
      setSelectedJournal(detail);
    } catch (err) {
      setError(err?.response?.data?.message || "Không thể tải chi tiết tạp chí.");
    } finally {
      setDetailLoading(false);
    }
  };

  const handleChangeJournal = () => {
    setSelectedSearchItem(null);
    setSelectedJournal(null);
    setQuery("");
    setResults(null);
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setShowDropdown(false);
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => () => {
    if (timerRef.current) clearTimeout(timerRef.current);
    if (abortRef.current) abortRef.current.abort();
  }, []);

  const hasResults = results && results.length > 0;

  return (
    <div className="journal-lookup">
      <div className="journal-lookup__header">
        <h1 className="journal-lookup__title">Tra cứu thông tin tạp chí</h1>

        <div className="journal-searchbar-wrapper" ref={wrapperRef}>
          <div className="journal-searchbar">
            <div className="journal-searchbar__field">
              <input
                className="journal-searchbar__input"
                type="text"
                value={query}
                onChange={handleChange}
                onFocus={() => { if (results?.length > 0) setShowDropdown(true); }}
                placeholder="Nhập tên tạp chí hoặc mã ISSN (ví dụ: Cell, 1420-682X)"
                aria-label="Tên tạp chí hoặc ISSN"
              />
            </div>
            <button
              type="button"
              className="journal-searchbar__btn"
              onClick={() => doSearch(query)}
              disabled={loading || query.trim().length < MIN_CHARS}
            >
              {loading && <span className="journal-btn-spinner" aria-hidden="true" />}
              {loading ? "Đang tra cứu..." : "Tra cứu"}
            </button>
          </div>

          {error && <p className="journal-lookup__message journal-lookup__message--error" role="alert">{error}</p>}

          {showDropdown && hasResults && (
            <ul className="journal-dropdown" role="listbox" aria-label="Kết quả tìm kiếm">
              {results.map((j) => (
                <li key={j.openAlexId || j.issn} className="journal-dropdown__item" role="option" tabIndex={0}
                  onClick={() => handleSelect(j)} onKeyDown={(e) => e.key === "Enter" && handleSelect(j)}>
                  <span className="journal-dropdown__name">{j.displayName}</span>
                  <span className="journal-dropdown__meta">{j.issn} - {j.publisher || "Không rõ nhà xuất bản"}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Card tạp chí đã chọn */}
      {selectedSearchItem && !selectedJournal && (
        <div className="jd-selected-card">
          <div className="jd-selected-card__inner">
            <div className="jd-selected-card__info">
              <h3 className="jd-selected-card__name">{selectedSearchItem.displayName}</h3>
              <p className="jd-selected-card__meta">
                {formatNumber(selectedSearchItem.worksCount)} tác phẩm · {selectedSearchItem.issn} · {selectedSearchItem.publisher || "Không rõ"}
              </p>
            </div>
            <div className="jd-selected-card__actions">
              <button className="jd-selected-card__btn" onClick={handleChangeJournal}>Thay đổi</button>
              <button
                className="jd-selected-card__btn jd-selected-card__btn--primary"
                onClick={handleFetchDetail}
                disabled={detailLoading}
              >
                {detailLoading && <span className="journal-btn-spinner journal-btn-spinner--sm" aria-hidden="true" />}
                {detailLoading ? "Đang tải..." : "Tìm nạp và phân tích"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Card tạp chí đã chọn khi đã có detail */}
      {selectedSearchItem && selectedJournal && (
        <div className="jd-selected-card">
          <div className="jd-selected-card__inner">
            <div className="jd-selected-card__info">
              <h3 className="jd-selected-card__name">{selectedSearchItem.displayName}</h3>
              <p className="jd-selected-card__meta">
                {formatNumber(selectedSearchItem.worksCount)} tác phẩm · {selectedSearchItem.issn} · {selectedSearchItem.publisher || "Không rõ"}
              </p>
            </div>
            <div className="jd-selected-card__actions">
              <button className="jd-selected-card__btn" onClick={handleChangeJournal}>Thay đổi</button>
              <button
                className="jd-selected-card__btn jd-selected-card__btn--primary"
                onClick={handleFetchDetail}
                disabled={detailLoading}
              >
                Tìm nạp và phân tích
              </button>
            </div>
          </div>
        </div>
      )}

      <JournalDetail journal={selectedJournal} loading={detailLoading} />
    </div>
  );
}
