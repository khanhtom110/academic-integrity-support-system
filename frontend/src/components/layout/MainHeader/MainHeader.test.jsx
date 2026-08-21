// @vitest-environment jsdom

import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ logout: vi.fn() }));

vi.mock("../../../hooks/useAuth", () => ({
  useAuth: () => ({ logout: mocks.logout }),
}));

import MainHeader from "./MainHeader";

const profile = {
  fullName: "Nguyễn Văn A",
  email: "user@haui.edu.vn",
  avatar: "",
};

function LocationProbe() {
  const location = useLocation();
  return <output data-testid="location">{location.pathname}</output>;
}

describe("MainHeader", () => {
  let container;
  let root;

  const renderHeader = () => {
    act(() => {
      root.render(
        <MemoryRouter initialEntries={["/"]}>
          <Routes>
            <Route path="*" element={<><MainHeader profile={profile} /><LocationProbe /></>} />
          </Routes>
        </MemoryRouter>,
      );
    });
  };

  beforeEach(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
    mocks.logout.mockReset();
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("opens the account menu and navigates to the profile", () => {
    renderHeader();
    const trigger = container.querySelector(".account-trigger");
    act(() => trigger.click());

    expect(trigger.getAttribute("aria-expanded")).toBe("true");
    const profileButton = [...container.querySelectorAll('[role="menuitem"]')].find((button) =>
      button.textContent.includes("Thông tin tài khoản"),
    );
    act(() => profileButton.click());

    expect(container.querySelector('[data-testid="location"]').textContent).toBe("/profile");
  });

  it("logs out and redirects to login", async () => {
    mocks.logout.mockResolvedValue();
    renderHeader();
    act(() => container.querySelector(".account-trigger").click());
    const logoutButton = [...container.querySelectorAll('[role="menuitem"]')].find((button) =>
      button.textContent.includes("Đăng xuất"),
    );

    await act(async () => logoutButton.click());

    expect(mocks.logout).toHaveBeenCalledOnce();
    expect(container.querySelector('[data-testid="location"]').textContent).toBe("/login");
  });

  it("keeps unavailable navigation items disabled", () => {
    renderHeader();
    const disabledItems = container.querySelectorAll(".main-nav-item:disabled");

    expect(disabledItems).toHaveLength(2);
    expect([...disabledItems].map((item) => item.textContent)).toEqual(["Tra cứu", "Khóa học"]);
  });
});
