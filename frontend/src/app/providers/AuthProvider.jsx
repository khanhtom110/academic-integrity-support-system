import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "../contexts";
import { setAccessToken, clearAccessToken } from "../../services/tokenManager";
import {
  getRefreshToken,
  saveRefreshToken,
  removeRefreshToken,
} from "../../services/tokenStorage";
import { getCurrentUserProfile } from "../../features/user/services/userService";
import {
  logout as logoutService,
  refreshToken as refreshTokenService,
} from "../../features/auth/services/authService";

const AUTH_STATUS = {
  LOADING: "loading",
  AUTHENTICATED: "authenticated",
  ANONYMOUS: "anonymous",
};

/**
 * API chỉ trả về accessToken/refreshToken/id khi đăng nhập,
 * nên thông tin hồ sơ đầy đủ sẽ được lấy thêm từ GET /user/profile.
 */
function buildUser(authData, profile) {
  return {
    id: profile?.id ?? authData?.id ?? authData?.user?.id ?? null,
    email: profile?.email ?? authData?.user?.email ?? null,
    fullName: profile?.fullName ?? authData?.user?.fullName ?? null,
    phone: profile?.phone ?? null,
    address: profile?.address ?? null,
    avatar: profile?.avatar ?? null,
    role: profile?.role ?? authData?.user?.role ?? null,
    isActive: profile?.isActive ?? null,
  };
}

function AuthProvider({ children }) {
  /**
   * User Information
   */
  const [user, setUser] = useState(null);

  /**
   * Authentication State
   */
  const [authStatus, setAuthStatus] = useState(AUTH_STATUS.LOADING);

  const clearSession = useCallback(() => {
    clearAccessToken();
    removeRefreshToken();
    setUser(null);
    setAuthStatus(AUTH_STATUS.ANONYMOUS);
  }, []);

  /**
   * Lưu phiên đăng nhập và tải hồ sơ người dùng
   */
  const applyAuthenticatedUser = useCallback(async (authData) => {
    setUser(buildUser(authData));
    setAuthStatus(AUTH_STATUS.AUTHENTICATED);

    try {
      const response = await getCurrentUserProfile();

      if (response?.data) {
        setUser(buildUser(authData, response.data));
      }
    } catch {
      // Giữ thông tin tối thiểu nếu không tải được hồ sơ.
    }
  }, []);

  /**
   * Login
   */
  const login = useCallback(
    (authData) => {
      setAccessToken(authData.accessToken);

      saveRefreshToken(authData.refreshToken);

      return applyAuthenticatedUser(authData);
    },
    [applyAuthenticatedUser],
  );
  /**
   * Chỉ cập nhật Access Token
   * Sau khi Refresh Token thành công
   */
  const updateAccessToken = useCallback((token) => {
    setAccessToken(token);
  }, []);

  /**
   * Update User Information
   */
  const updateUser = useCallback((newUserData) => {
    setUser((prev) => ({
      ...prev,
      ...newUserData,
    }));
  }, []);

  /**
   * Logout
   */
  const logout = useCallback(async () => {
    try {
      await logoutService(getRefreshToken());
    } catch {
      // Luôn xóa phiên FE ngay cả khi API logout không phản hồi.
    } finally {
      clearSession();
    }
  }, [clearSession]);

  /**
   * Đăng xuất cục bộ, không gọi API logout.
   * Dùng khi server đã tự vô hiệu hoá token (ví dụ sau đổi mật khẩu),
   * tránh gọi thêm API gây lỗi refresh không cần thiết.
   */
  const logoutLocal = useCallback(() => {
    clearSession();
  }, [clearSession]);

  /**
   * Restore Session
   */
  const restoreSession = useCallback(async () => {
    const storedRefreshToken = getRefreshToken();

    if (!storedRefreshToken) {
      clearSession();
      return;
    }

    setAuthStatus(AUTH_STATUS.LOADING);

    try {
      const response = await refreshTokenService(storedRefreshToken);
      const authData = response?.data;

      if (!authData?.accessToken) {
        throw new Error("Phản hồi làm mới phiên không hợp lệ.");
      }

      setAccessToken(authData.accessToken);

      if (authData.refreshToken) {
        saveRefreshToken(authData.refreshToken);
      }

      await applyAuthenticatedUser(authData);
    } catch {
      clearSession();
    }
  }, [applyAuthenticatedUser, clearSession]);

  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  const isAuthenticated = authStatus === AUTH_STATUS.AUTHENTICATED;
  const value = useMemo(
    () => ({
      user,
      authStatus,
      isAuthenticated,
      login,
      logout,
      logoutLocal,
      updateAccessToken,
      updateUser,
      restoreSession,
    }),
    [
      user,
      authStatus,
      isAuthenticated,
      login,
      logout,
      logoutLocal,
      updateAccessToken,
      updateUser,
      restoreSession,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
