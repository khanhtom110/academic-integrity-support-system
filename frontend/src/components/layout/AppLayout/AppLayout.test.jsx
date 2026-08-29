// @vitest-environment jsdom

import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter, Route, Routes, useOutletContext } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ getProfile: vi.fn() }));

vi.mock("../../../features/user/services/userService", () => ({
  getProfile: mocks.getProfile,
}));
vi.mock("../MainHeader/MainHeader", () => ({
  default: ({ profile }) => <header>{profile?.fullName || "Tài khoản"}</header>,
}));

import AppLayout from "./AppLayout";

function ProfileConsumer() {
  const { profile } = useOutletContext();
  return <p data-testid="profile">{profile.email}</p>;
}

describe("AppLayout", () => {
  let container;
  let root;

  const renderLayout = () => {
    root.render(
      <MemoryRouter initialEntries={["/"]}>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<ProfileConsumer />} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );
  };

  beforeEach(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
    mocks.getProfile.mockReset();
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("loads the profile once and shares it with header and page", async () => {
    mocks.getProfile.mockResolvedValue({
      data: { fullName: "Nguyễn Văn A", email: "user@haui.edu.vn" },
    });

    await act(async () => renderLayout());

    expect(mocks.getProfile).toHaveBeenCalledOnce();
    expect(container.querySelector("header").textContent).toBe("Nguyễn Văn A");
    expect(container.querySelector('[data-testid="profile"]').textContent).toBe("user@haui.edu.vn");
  });

  it("shows an error and retries loading", async () => {
    mocks.getProfile
      .mockRejectedValueOnce({ response: { status: 500, data: {} } })
      .mockResolvedValueOnce({ data: { fullName: "Nguyễn Văn A", email: "user@haui.edu.vn" } });

    await act(async () => renderLayout());
    expect(container.textContent).toContain("Máy chủ đang gặp sự cố");

    const retryButton = [...container.querySelectorAll("button")].find((button) =>
      button.textContent.includes("Thử lại"),
    );
    await act(async () => retryButton.click());

    expect(mocks.getProfile).toHaveBeenCalledTimes(2);
    expect(container.textContent).toContain("user@haui.edu.vn");
  });
});
