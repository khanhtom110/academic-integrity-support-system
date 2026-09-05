// @vitest-environment jsdom

import { beforeEach, describe, expect, it, vi } from "vitest";

import { API } from "../../../constants/api";

const mocks = vi.hoisted(() => ({
  get: vi.fn(),
  put: vi.fn(),
  post: vi.fn(),
  delete: vi.fn(),
}));

vi.mock("../../../services/apiClient", () => ({
  default: mocks,
}));

const { changePassword, deleteAvatar, getProfile, updateProfile, uploadAvatar } =
  await import("./userService");

describe("userService", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("gets the authenticated user's profile", async () => {
    const apiResponse = { data: { data: { fullName: "Nguyễn Văn A" } } };
    mocks.get.mockResolvedValue(apiResponse);

    await expect(getProfile()).resolves.toBe(apiResponse.data);
    expect(mocks.get).toHaveBeenCalledWith(API.USER.PROFILE);
  });

  it("updates only the supplied profile payload", async () => {
    const payload = { fullName: "Nguyễn Văn B" };
    mocks.put.mockResolvedValue({ data: { data: payload } });

    await updateProfile(payload);

    expect(mocks.put).toHaveBeenCalledWith(API.USER.PROFILE, payload);
  });

  it("uploads an avatar as the file form field", async () => {
    const file = new File(["avatar"], "avatar.png", { type: "image/png" });
    mocks.post.mockResolvedValue({ data: { data: { avatar: "/avatar.png" } } });

    await uploadAvatar(file);

    const [url, body, config] = mocks.post.mock.calls[0];
    expect(url).toBe(API.USER.AVATAR);
    expect(body).toBeInstanceOf(FormData);
    expect(body.get("file")).toBe(file);
    expect(config).toEqual({ headers: { "Content-Type": undefined } });
  });

  it("deletes the current avatar", async () => {
    mocks.delete.mockResolvedValue({ data: { data: { avatar: null } } });

    await deleteAvatar();

    expect(mocks.delete).toHaveBeenCalledWith(API.USER.AVATAR);
  });

  it("changes the authenticated user's password", async () => {
    const payload = {
      oldPassword: "Current123@",
      newPassword: "NewPassword123@",
      confirmPassword: "NewPassword123@",
    };
    mocks.post.mockResolvedValue({ data: { message: "Password changed successfully" } });

    await changePassword(payload);

    expect(mocks.post).toHaveBeenCalledWith(API.USER.CHANGE_PASSWORD, payload);
  });
});
