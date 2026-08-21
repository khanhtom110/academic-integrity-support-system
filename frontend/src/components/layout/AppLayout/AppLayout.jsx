import { useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

import { getProfile } from "../../../features/user/services/userService";
import { getUserError } from "../../../features/user/utils/profile";
import MainHeader from "../MainHeader/MainHeader";

import "./AppLayout.css";

function AppLayout() {
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState("loading");
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    const loadProfile = async () => {
      setStatus("loading");
      setError("");

      try {
        const response = await getProfile();
        if (!response?.data) throw new Error("Phản hồi hồ sơ không hợp lệ.");

        if (active) {
          setProfile(response.data);
          setStatus("success");
        }
      } catch (requestError) {
        if (active) {
          setError(getUserError(requestError, "Không thể tải thông tin tài khoản."));
          setStatus("error");
        }
      }
    };

    loadProfile();

    return () => {
      active = false;
    };
  }, [reloadKey]);

  return (
    <div className="app-shell">
      <MainHeader profile={profile} />
      <main className="app-main">
        {status === "loading" && (
          <div className="app-state" aria-live="polite" aria-busy="true">
            <span className="auth-loading-spinner" aria-hidden="true" />
            <p>Đang tải thông tin tài khoản...</p>
          </div>
        )}

        {status === "error" && (
          <div className="app-state" role="alert">
            <h1>Không thể tải trang</h1>
            <p>{error}</p>
            <button type="button" className="primary-btn" onClick={() => setReloadKey((key) => key + 1)}>
              Thử lại
            </button>
          </div>
        )}

        {status === "success" && <Outlet context={{ profile, setProfile }} />}
      </main>
    </div>
  );
}

export default AppLayout;
