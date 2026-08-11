import { useCallback, useMemo, useState } from "react";
import { AuthContext } from "../contexts";
import { setAccessToken, clearAccessToken } from "../../services/tokenManager";
import {
  saveRefreshToken,
  removeRefreshToken,
} from "../../services/tokenStorage";

function AuthProvider({ children }) {
  /**
   * User Information
   */
  const [user, setUser] = useState(null);

  /**
   * Authentication State
   */
  const [isAuthenticated, setIsAuthenticated] = useState(false);

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

    setIsAuthenticated(true);
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
  const logout = useCallback(() => {
    clearAccessToken();
    removeRefreshToken();
    setUser(null);
    setIsAuthenticated(false);
  }, []);

  /**
   * Restore Session
   *
   * (Làm ở Module tiếp theo)
   */
  const restoreSession = useCallback(() => {}, []);
  const value = useMemo(
    () => ({
      user,
      isAuthenticated,
      login,
      logout,
      updateAccessToken,
      restoreSession,
    }),
    [user, isAuthenticated, login, logout, updateAccessToken, restoreSession],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
