// @vitest-environment jsdom

import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter, Outlet, Route, Routes } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({ changePassword: vi.fn() }));

vi.mock("../../services/userService", () => ({
  changePassword: mocks.changePassword,
}));

import ChangePasswordForm from "./ChangePasswordForm";

function Layout() {
  return <Outlet context={{ profile: { email: "user@haui.edu.vn" } }} />;
}

describe("ChangePasswordForm", () => {
  let container;
  let root;

  const renderForm = () => {
    act(() => {
      root.render(
        <MemoryRouter initialEntries={["/change-password"]}>
          <Routes>
            <Route element={<Layout />}>
              <Route path="/change-password" element={<ChangePasswordForm />} />
              <Route path="/profile" element={<p>Hồ sơ</p>} />
            </Route>
          </Routes>
        </MemoryRouter>,
      );
    });
  };

  const fill = (input, value) => {
    const setter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      "value",
    ).set;
    act(() => {
      setter.call(input, value);
      input.dispatchEvent(new Event("input", { bubbles: true }));
    });
  };

  beforeEach(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
    mocks.changePassword.mockReset();
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  it("submits the backend password payload and shows success", async () => {
    mocks.changePassword.mockResolvedValue({ data: null });
    renderForm();
    const inputs = container.querySelectorAll('input[type="password"]');
    fill(inputs[0], "Current123@");
    fill(inputs[1], "NewPassword123@");
    fill(inputs[2], "NewPassword123@");

    await act(async () => container.querySelector("form").requestSubmit());

    expect(mocks.changePassword).toHaveBeenCalledWith({
      oldPassword: "Current123@",
      newPassword: "NewPassword123@",
      confirmPassword: "NewPassword123@",
    });
    expect(container.textContent).toContain("Mật khẩu của bạn đã được thay đổi.");
  });

  it("blocks mismatched confirmation before calling the API", async () => {
    renderForm();
    const inputs = container.querySelectorAll('input[type="password"]');
    fill(inputs[0], "Current123@");
    fill(inputs[1], "NewPassword123@");
    fill(inputs[2], "Different123@");

    await act(async () => container.querySelector("form").requestSubmit());

    expect(mocks.changePassword).not.toHaveBeenCalled();
    expect(container.textContent).toContain("Mật khẩu xác nhận không khớp.");
  });
});
