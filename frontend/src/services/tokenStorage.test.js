
import { beforeEach, describe, expect, it } from "vitest";

import {
  getRefreshToken,
  hasRefreshToken,
  removeRefreshToken,
  saveRefreshToken,
} from "./tokenStorage";

describe("refresh token session storage", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it("stores the refresh token only in sessionStorage", () => {
    saveRefreshToken("refresh-token");

    expect(getRefreshToken()).toBe("refresh-token");
    expect(hasRefreshToken()).toBe(true);
    expect(localStorage.getItem("refreshToken")).toBeNull();
  });

  it("removes the refresh token from the current tab session", () => {
    saveRefreshToken("refresh-token");
    removeRefreshToken();

    expect(getRefreshToken()).toBeNull();
    expect(hasRefreshToken()).toBe(false);
  });
});
