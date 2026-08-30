import { useState, useRef } from "react";
import "./JournalDetail.css";

function fmt(n) {
  if (n == null) return "-";
  return n.toLocaleString("vi-VN");
}

const COUNTRY_NAMES = {
  CH: "Thụy Sĩ (Switzerland)",
  US: "Mỹ (United States)",
  GB: "Vương quốc Anh (United Kingdom)",
  DE: "Đức (Germany)",
  FR: "Pháp (France)",
  NL: "Hà Lan (Netherlands)",
  JP: "Nhật Bản (Japan)",
  CN: "Trung Quốc (China)",
  KR: "Hàn Quốc (South Korea)",
  VN: "Việt Nam",
  CA: "Canada",
  AU: "Úc (Australia)",
  SG: "Singapore",
  IN: "Ấn Độ (India)",
};

function getCountryLabel(code) {
  if (!code) return "";
  const upper = code.toUpperCase();
  return COUNTRY_NAMES[upper] || upper;
}

const TOPIC_COLORS = [
  "#3b82f6", "#f97316", "#ef4444", "#a855f7", "#06b6d4",
  "#22c55e", "#eab308", "#ec4899", "#14b8a6", "#6366f1",
  "#f43f5e", "#0ea5e9", "#8b5cf6", "#10b981", "#f59e0b",
  "#2563eb", "#d946ef", "#059669", "#e11d48", "#0891b2"
];

function getBezierPath(points) {
  if (points.length < 2) return "";
  let d = `M ${points[0].x},${points[0].y}`;
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i];
    const p1 = points[i + 1];
    const mx = (p0.x + p1.x) / 2;
    d += ` C ${mx},${p0.y} ${mx},${p1.y} ${p1.x},${p1.y}`;
  }
  return d;
}

function AreaChart({ data }) {
  const [hoveredPoint, setHoveredPoint] = useState(null);
  const svgRef = useRef(null);

  if (!data || data.length === 0) return null;

  const W = 980;
  const H = 200;
  const PAD = { top: 15, right: 15, bottom: 35, left: 45 };
  const cw = W - PAD.left - PAD.right;
  const ch = H - PAD.top - PAD.bottom;

  const maxValRaw = Math.max(...data.map((d) => d.worksCount || 0), 1);
  const maxVal = Math.ceil(maxValRaw / 100) * 100;
  const stepX = data.length > 1 ? cw / (data.length - 1) : cw;

  const points = data.map((d, i) => ({
    x: PAD.left + i * stepX,
    y: PAD.top + ch - ((d.worksCount || 0) / maxVal) * ch,
    year: d.year,
    val: d.worksCount,
  }));

  const linePath = getBezierPath(points);
  const areaPath = linePath + ` L ${points[points.length - 1].x},${PAD.top + ch} L ${points[0].x},${PAD.top + ch} Z`;

  const yTicksCount = 3;
  const yLines = Array.from({ length: yTicksCount + 1 }, (_, i) => {
    const val = (maxVal / yTicksCount) * (yTicksCount - i);
    const y = PAD.top + (ch / yTicksCount) * i;
    let label = Math.round(val).toString();
    if (val >= 1000) {
      const kVal = val / 1000;
      label = kVal % 1 === 0 ? `${kVal}k` : `${kVal.toFixed(1)}k`;
    }
    return { val, y, label };
  });

  const xLabels = points.filter((_, i) => {
    const n = points.length;
    if (n <= 8) return true;
    if (i === 0 || i === n - 1) return true;
    return i % Math.ceil(n / 7) === 0;
  });

  const handleMouseMove = (e) => {
    if (!svgRef.current) return;
    const rect = svgRef.current.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const svgX = (mouseX / rect.width) * W;

    if (svgX < PAD.left - 10 || svgX > W - PAD.right + 10) {
      setHoveredPoint(null);
      return;
    }

    let closest = points[0];
    let minDiff = Math.abs(svgX - points[0].x);
    for (let i = 1; i < points.length; i++) {
      const diff = Math.abs(svgX - points[i].x);
      if (diff < minDiff) {
        minDiff = diff;
        closest = points[i];
      }
    }
    setHoveredPoint(closest);
  };

  const handleMouseLeave = () => {
    setHoveredPoint(null);
  };

  // Tooltip positioning math
  const activePt = hoveredPoint || points[0];
  const tooltipText = hoveredPoint ? `Năm ${hoveredPoint.year}: ${fmt(hoveredPoint.val)} tác phẩm` : "";
  const tooltipX = Math.max(PAD.left + 75, Math.min(W - PAD.right - 75, activePt.x));
  const tooltipY = Math.max(PAD.top + 20, activePt.y - 18);

  return (
    <div className="jd-chart">
      <h3 className="jd-section__title">SỐ LƯỢNG TÁC PHẨM MỖI NĂM</h3>
      <div className="jd-chart__svg-wrap">
        <svg
          ref={svgRef}
          viewBox={`0 0 ${W} ${H}`}
          className="jd-chart__svg"
          aria-label="Biểu đồ số lượng tác phẩm theo năm"
          onMouseMove={handleMouseMove}
          onMouseLeave={handleMouseLeave}
        >
          <defs>
            <linearGradient id="jd-area-grad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#60a5fa" stopOpacity="0.4" />
              <stop offset="100%" stopColor="#93c5fd" stopOpacity="0.03" />
            </linearGradient>
            <filter id="jd-tooltip-shadow" x="-20%" y="-20%" width="140%" height="140%">
              <feDropShadow dx="0" dy="2" stdDeviation="3" floodColor="#000000" floodOpacity="0.15" />
            </filter>
          </defs>

          {/* Grid lines & Y Axis */}
          {yLines.map((t) => (
            <g key={t.val + t.label}>
              <line x1={PAD.left} x2={W - PAD.right} y1={t.y} y2={t.y} stroke="#64748b" strokeWidth="0.75" />
              <text x={PAD.left - 8} y={t.y + 3} textAnchor="end" fontSize="11" fill="#64748b" fontFamily="sans-serif">
                {t.label}
              </text>
            </g>
          ))}

          {/* Area gradient path */}
          <path d={areaPath} fill="url(#jd-area-grad)" />

          {/* Smooth line path */}
          <path d={linePath} fill="none" stroke="#2563eb" strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />

          {/* X Axis labels */}
          {xLabels.map((p) => (
            <text key={p.year} x={p.x} y={H - 8} textAnchor="middle" fontSize="11" fill="#64748b" fontFamily="sans-serif">
              {p.year}
            </text>
          ))}

          {/* Smooth animated Guideline */}
          <line
            className="jd-chart__guideline"
            x1={activePt.x}
            y1={PAD.top}
            x2={activePt.x}
            y2={PAD.top + ch}
            stroke="#2563eb"
            strokeWidth="1.5"
            strokeDasharray="4 4"
            style={{ opacity: hoveredPoint ? 1 : 0 }}
          />

          {/* Smooth animated Point Highlight */}
          <circle
            className="jd-chart__point"
            cx={activePt.x}
            cy={activePt.y}
            r="5"
            fill="#2563eb"
            stroke="#ffffff"
            strokeWidth="2.5"
            style={{ opacity: hoveredPoint ? 1 : 0 }}
          />

          {/* Smooth animated Tooltip Box */}
          <g
            className="jd-chart__tooltip-g"
            transform={`translate(${tooltipX}, ${tooltipY})`}
            filter="url(#jd-tooltip-shadow)"
            style={{ opacity: hoveredPoint ? 1 : 0 }}
          >
            <rect
              x="-75"
              y="-14"
              width="150"
              height="26"
              rx="6"
              fill="#2563eb"
            />
            <text
              x="0"
              y="3"
              textAnchor="middle"
              fill="#ffffff"
              fontSize="11"
              fontWeight="600"
              fontFamily="sans-serif"
            >
              {tooltipText}
            </text>
          </g>
        </svg>
      </div>
    </div>
  );
}

function Topics({ topics }) {
  const [showAll, setShowAll] = useState(false);

  if (!topics || topics.length === 0) return null;

  const total = topics.reduce((s, t) => s + (t.influencePercent || 0), 0) || 1;
  const visibleTopics = topics.slice(0, 25);
  const initialTopics = visibleTopics.slice(0, 5);
  const extraTopics = visibleTopics.slice(5);

  return (
    <div className="jd-topics">
      <div className="jd-topics__header">
        <span className="jd-topics__title-main">TỔNG HỢP CHỦ ĐỀ</span>
        <span className="jd-topics__title-sub"> - {visibleTopics.length} chủ đề của OpenAlex, xem thanh bên</span>
      </div>

      <div className="jd-topics__bar">
        {visibleTopics.map((t, i) => {
          const pct = ((t.influencePercent || 0) / total) * 100;
          return (
            <div
              key={t.name + i}
              className="jd-topics__bar-seg"
              style={{ width: `${pct}%`, backgroundColor: TOPIC_COLORS[i % TOPIC_COLORS.length] }}
              title={`${t.name}: ${pct.toFixed(1)}%`}
            />
          );
        })}
      </div>

      <div className="jd-topics__legend-wrap">
        <div className="jd-topics__legend">
          {initialTopics.map((t, i) => {
            const pct = ((t.influencePercent || 0) / total) * 100;
            const color = TOPIC_COLORS[i % TOPIC_COLORS.length];
            const pctFormatted = pct.toFixed(1).replace(".", ",");
            return (
              <div key={t.name + i} className="jd-topic-legend">
                <span className="jd-topic-legend__dot" style={{ backgroundColor: color }} />
                <span className="jd-topic-legend__name">{t.name}</span>
                <span className="jd-topic-legend__pct">{pctFormatted}%</span>
              </div>
            );
          })}
        </div>

        {extraTopics.length > 0 && (
          <div className={`jd-topics__legend-extra ${showAll ? "jd-topics__legend-extra--expanded" : ""}`}>
            <div className="jd-topics__legend-extra-inner">
              {extraTopics.map((t, i) => {
                const globalIndex = i + 5;
                const pct = ((t.influencePercent || 0) / total) * 100;
                const color = TOPIC_COLORS[globalIndex % TOPIC_COLORS.length];
                const pctFormatted = pct.toFixed(1).replace(".", ",");
                return (
                  <div key={t.name + globalIndex} className="jd-topic-legend">
                    <span className="jd-topic-legend__dot" style={{ backgroundColor: color }} />
                    <span className="jd-topic-legend__name">{t.name}</span>
                    <span className="jd-topic-legend__pct">{pctFormatted}%</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {extraTopics.length > 0 && (
          <div className="jd-topics__more-row">
            <button
              type="button"
              className="jd-topic-legend__more"
              onClick={() => setShowAll(!showAll)}
            >
              {showAll ? "Thu gọn ▲" : "Tất cả ▼"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default function JournalDetail({ journal, loading }) {
  if (loading) {
    return (
      <div className="jd-loading">
        <span className="jd-spinner" aria-hidden="true" />
        <p>Đang tải chi tiết tạp chí...</p>
      </div>
    );
  }
  if (!journal) return null;

  const issnDisplay = journal.issns?.length ? journal.issns.join(" · ") : journal.issnL || "-";
  const yearRange = [journal.firstPublicationYear, journal.lastPublicationYear].filter(Boolean).join(" - ");

  return (
    <div className="jd-detail">
      {/* Header */}
      <div className="jd-header">
        <div className="jd-header__left">
          <div className="jd-header__top-row">
            <span className="jd-source-badge">OPENALEX</span>
            <h2 className="jd-header__name">{journal.displayName}</h2>
            <span className="jd-type-label">Tạp chí</span>
          </div>
          <div className="jd-header__meta">
            <span>{journal.publisher || "Không rõ"}</span>
            <span className="jd-header__meta-sep">·</span>
            <span>ISSN {issnDisplay}</span>
          </div>
        </div>

        {/* Badges with interactive hover UI Tooltips */}
        <div className="jd-header__badges">
          {journal.countryCode && (
            <div className="jd-badge-tooltip-wrap">
              <span className="jd-badge jd-badge--country">{journal.countryCode}</span>
              <div className="jd-badge-tooltip">
                <strong className="jd-badge-tooltip__title">Quốc gia xuất bản</strong>
                <span className="jd-badge-tooltip__desc">{getCountryLabel(journal.countryCode)}</span>
              </div>
            </div>
          )}

          {journal.isOa && (
            <div className="jd-badge-tooltip-wrap">
              <span className="jd-badge jd-badge--oa">OA</span>
              <div className="jd-badge-tooltip">
                <strong className="jd-badge-tooltip__title">Truy cập Mở (Open Access)</strong>
                <span className="jd-badge-tooltip__desc">Tất cả bài báo được đọc và tải về miễn phí, không bị khóa trả phí.</span>
              </div>
            </div>
          )}

          {journal.isInDoaj && (
            <div className="jd-badge-tooltip-wrap">
              <span className="jd-badge jd-badge--doaj">DOAJ</span>
              <div className="jd-badge-tooltip">
                <strong className="jd-badge-tooltip__title">Thư mục Tạp chí Uy tín (DOAJ)</strong>
                <span className="jd-badge-tooltip__desc">Chứng nhận tạp chí chính thống, đạt tiêu chuẩn liêm chính học thuật & phản biện nghiêm túc.</span>
              </div>
            </div>
          )}

          {journal.homepageUrl && (
            <div className="jd-badge-tooltip-wrap">
              <a className="jd-badge jd-badge--link" href={journal.homepageUrl} target="_blank" rel="noopener noreferrer">
                Tạp chí ↗
              </a>
              <div className="jd-badge-tooltip">
                <strong className="jd-badge-tooltip__title">Trang chủ Tạp chí</strong>
                <span className="jd-badge-tooltip__desc">Mở liên kết trực tiếp đến trang web chính thức của nhà xuất bản.</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Stats */}
      <div className="jd-stats-card">
        <div className="jd-stat">
          <span className="jd-stat__value">{fmt(journal.worksCount)}</span>
          <span className="jd-stat__label">Tác phẩm</span>
        </div>
        <div className="jd-stats-card__divider" />
        <div className="jd-stat">
          <span className="jd-stat__value">{yearRange || "-"}</span>
          <span className="jd-stat__label">Năm</span>
        </div>
        <div className="jd-stats-card__divider" />
        <div className="jd-stat">
          <span className="jd-stat__value">{journal.apcUsd != null ? `${fmt(journal.apcUsd)} Đô la` : "-"}</span>
          <span className="jd-stat__label">APC (USD)</span>
        </div>
      </div>

      {/* Chart */}
      <AreaChart data={journal.yearlyStats} />

      {/* Topics */}
      <Topics topics={journal.topics} />
    </div>
  );
}
