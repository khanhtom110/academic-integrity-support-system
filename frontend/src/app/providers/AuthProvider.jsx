import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "../contexts";
import { setAccessToken, clearAccessToken } from "../../services/tokenManager";
import {
  getRefreshToken,
  saveRefreshToken,
  removeRefreshToken,
} from "../../services/tokenStorage";
import {
  logout as logoutService,
  refreshToken as refreshTokenService,
} from "../../features/auth/services/authService";

const AUTH_STATUS = {
  LOADING: "loading",
  AUTHENTICATED: "authenticated",
  ANONYMOUS: "anonymous",
};

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
   * Login
   */
  const login = useCallback((authData) => {
    setAccessToken(authData.accessToken);

    saveRefreshToken(authData.refreshToken);

    setUser({
      id: authData.id ?? null,
      tokenType: authData.tokenType ?? "Bearer",
    });

    setAuthStatus(AUTH_STATUS.AUTHENTICATED);
  }, []);
  /**
   * Chỉ cập nhật Access Token
   * Sau khi Refresh Token thành công
   */
  const updateAccessToken = useCallback((token) => {
    setAccessToken(token);
  }, []);
  /**
   * Logout
   */
  const logout = useCallback(async () => {
    try {
      await logoutService();
    } catch {
      // Luôn xóa phiên FE ngay cả khi API logout không phản hồi.
    } finally {
      clearSession();
    }
  }, [clearSession]);

  /**
   * Restore Session
   *
   * (Làm ở Module tiếp theo)
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

      setUser({
        id: authData.id ?? null,
        tokenType: authData.tokenType ?? "Bearer",
      });
      setAuthStatus(AUTH_STATUS.AUTHENTICATED);
    } catch {
      clearSession();
    }
  }, [clearSession]);

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
      updateAccessToken,
      restoreSession,
    }),
    [
      user,
      authStatus,
      isAuthenticated,
      login,
      logout,
      updateAccessToken,
      restoreSession,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
