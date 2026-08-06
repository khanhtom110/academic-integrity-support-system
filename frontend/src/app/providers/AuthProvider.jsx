import { useMemo, useState } from "react";

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
  const login = (authData) => {
    /**
     * authData
     * {
     *    accessToken,
     *    refreshToken,
     *    id,
     *    tokenType
     * }
     */

    setAccessToken(authData.accessToken);

    saveRefreshToken(authData.refreshToken);

    setUser({
      id: authData.id,
      tokenType: authData.tokenType,
    });

    setIsAuthenticated(true);
    console.log("===== AUTH PROVIDER =====");
    console.log(authData);
  };

  /**
   * Chỉ cập nhật Access Token
   * Sau khi Refresh Token thành công
   */
  const updateAccessToken = (token) => {
    setAccessToken(token);
  };

  /**
   * Logout
   */
  const logout = () => {
    clearAccessToken();

    removeRefreshToken();

    setUser(null);

    setIsAuthenticated(false);
  };

  /**
   * Restore Session
   *
   * (Làm ở Module tiếp theo)
   */
  const restoreSession = () => {};

  const value = useMemo(
    () => ({
      user,

      isAuthenticated,

      login,

      logout,

      updateAccessToken,

      restoreSession,
    }),
    [user, isAuthenticated],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
