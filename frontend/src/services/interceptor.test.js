// @vitest-environment jsdom

import { beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => {
  const apiClient = vi.fn(async (config) => ({ config }));
  const requestUse = vi.fn();
  const responseUse = vi.fn();

  apiClient.interceptors = {
    request: { use: requestUse },
    response: { use: responseUse },
  };
  apiClient.post = vi.fn();

  return {
    apiClient,
    requestUse,
    responseUse,
    getRefreshToken: vi.fn(),
    saveRefreshToken: vi.fn(),
    removeRefreshToken: vi.fn(),
    getAccessToken: vi.fn(),
    setAccessToken: vi.fn(),
    clearAccessToken: vi.fn(),
  };
});

vi.mock("./apiClient", () => ({ default: mocks.apiClient }));
vi.mock("./tokenManager", () => ({
  getAccessToken: mocks.getAccessToken,
  setAccessToken: mocks.setAccessToken,
  clearAccessToken: mocks.clearAccessToken,
}));
vi.mock("./tokenStorage", () => ({
  getRefreshToken: mocks.getRefreshToken,
  saveRefreshToken: mocks.saveRefreshToken,
  removeRefreshToken: mocks.removeRefreshToken,
}));

await import("./interceptor");

const responseErrorHandler = mocks.responseUse.mock.calls[0][1];

describe("authentication interceptor", () => {
  beforeEach(() => {
    mocks.apiClient.mockClear();
    mocks.apiClient.post.mockReset();
    mocks.getRefreshToken.mockReset();
    mocks.saveRefreshToken.mockReset();
    mocks.removeRefreshToken.mockReset();
    mocks.setAccessToken.mockReset();
    mocks.clearAccessToken.mockReset();
  });

  it("does not refresh a public authentication request", async () => {
    const error = {
      response: { status: 401 },
      config: { url: "/api/v1/auth/login", headers: {} },
    };

    await expect(responseErrorHandler(error)).rejects.toBe(error);
    expect(mocks.apiClient.post).not.toHaveBeenCalled();
  });

  it("refreshes once when a 403 response has an empty body", async () => {
    mocks.getRefreshToken.mockReturnValue("old-refresh-token");
    mocks.apiClient.post.mockResolvedValue({
      data: {
        data: {
          accessToken: "new-access-token",
          refreshToken: "new-refresh-token",
        },
      },
    });

    const config = { url: "/api/v1/user/profile", headers: {} };

    await responseErrorHandler({
      response: { status: 403, data: "" },
      config,
    });

    expect(mocks.apiClient.post).toHaveBeenCalledTimes(1);
    expect(mocks.setAccessToken).toHaveBeenCalledWith("new-access-token");
    expect(config.headers.Authorization).toBe("Bearer new-access-token");
  });

  it("does not refresh a business 403 response with an ApiResponse body", async () => {
    const error = {
      response: {
        status: 403,
        data: { statusCode: 403, message: "Access denied", data: null },
      },
      config: { url: "/api/v1/user/profile", headers: {} },
    };

    await expect(responseErrorHandler(error)).rejects.toBe(error);
    expect(mocks.apiClient.post).not.toHaveBeenCalled();
  });

  it("shares one refresh request between simultaneous 401 responses", async () => {
    let resolveRefresh;
    const refreshResponse = new Promise((resolve) => {
      resolveRefresh = resolve;
    });

    mocks.getRefreshToken.mockReturnValue("old-refresh-token");
    mocks.apiClient.post.mockReturnValue(refreshResponse);

    const firstConfig = { url: "/api/v1/user/profile", headers: {} };
    const secondConfig = { url: "/api/v1/user/courses", headers: {} };
    const firstRetry = responseErrorHandler({
      response: { status: 401 },
      config: firstConfig,
    });
    const secondRetry = responseErrorHandler({
      response: { status: 401 },
      config: secondConfig,
    });

    resolveRefresh({
      data: {
        data: {
          accessToken: "new-access-token",
          refreshToken: "new-refresh-token",
        },
      },
    });

    await Promise.all([firstRetry, secondRetry]);

    expect(mocks.apiClient.post).toHaveBeenCalledTimes(1);
    expect(mocks.setAccessToken).toHaveBeenCalledWith("new-access-token");
    expect(mocks.saveRefreshToken).toHaveBeenCalledWith("new-refresh-token");
    expect(mocks.apiClient).toHaveBeenCalledTimes(2);
    expect(firstConfig.headers.Authorization).toBe("Bearer new-access-token");
    expect(secondConfig.headers.Authorization).toBe("Bearer new-access-token");
  });
});
