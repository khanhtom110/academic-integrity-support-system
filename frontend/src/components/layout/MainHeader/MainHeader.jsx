import { useEffect, useRef, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";

import logo from "../../../assets/images/logo.webp";
import { ROUTES } from "../../../constants/routes";
import { useAuth } from "../../../hooks/useAuth";
import UserAvatar from "../../../features/user/components/UserAvatar/UserAvatar";
import {
  ChevronDownIcon,
  CourseIcon,
  HomeIcon,
  LogoutIcon,
  SearchIcon,
  UserIcon,
} from "../../ui/Icons";

import "./MainHeader.css";

function MainHeader({ profile }) {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const menuRef = useRef(null);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const displayName = profile?.fullName?.trim() || profile?.email || "Tài khoản";

  useEffect(() => {
    if (!isMenuOpen) return undefined;

    const closeOnOutsideClick = (event) => {
      if (!menuRef.current?.contains(event.target)) setIsMenuOpen(false);
    };
    const closeOnEscape = (event) => {
      if (event.key === "Escape") setIsMenuOpen(false);
    };

    document.addEventListener("pointerdown", closeOnOutsideClick);
    document.addEventListener("keydown", closeOnEscape);

    return () => {
      document.removeEventListener("pointerdown", closeOnOutsideClick);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, [isMenuOpen]);

  const handleLogout = async () => {
    if (isLoggingOut) return;
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      navigate(ROUTES.LOGIN, { replace: true });
    }
  };

  return (
    <header className="main-header">
      <div className="main-header-inner">
        <Link className="main-brand" to={ROUTES.HOME} aria-label="Về trang chủ HaUI">
          <img src={logo} alt="" />
          <span>HaUI</span>
        </Link>

        <nav className="main-nav" aria-label="Điều hướng chính">
          <NavLink to={ROUTES.HOME} className={({ isActive }) => `main-nav-item${isActive ? " active" : ""}`}>
            <HomeIcon />
            <span>Trang chủ</span>
          </NavLink>
          <button className="main-nav-item" type="button" disabled title="Tính năng đang phát triển">
            <SearchIcon />
            <span>Tra cứu</span>
          </button>
          <button className="main-nav-item" type="button" disabled title="Tính năng đang phát triển">
            <CourseIcon />
            <span>Khóa học</span>
          </button>
        </nav>

        <div className="account-menu" ref={menuRef}>
          <button
            type="button"
            className="account-trigger"
            aria-haspopup="menu"
            aria-expanded={isMenuOpen}
            onClick={() => setIsMenuOpen((open) => !open)}
          >
            <UserAvatar avatar={profile?.avatar} fullName={displayName} size="small" />
            <span className="account-name">{displayName}</span>
            <ChevronDownIcon />
          </button>

          {isMenuOpen && (
            <div className="account-dropdown" role="menu">
              <button
                type="button"
                role="menuitem"
                onClick={() => {
                  setIsMenuOpen(false);
                  navigate(ROUTES.PROFILE);
                }}
              >
                <UserIcon />
                <span>Thông tin tài khoản</span>
              </button>
              <button type="button" role="menuitem" onClick={handleLogout} disabled={isLoggingOut}>
                <LogoutIcon />
                <span>{isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default MainHeader;
