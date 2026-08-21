// @vitest-environment jsdom

import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter, Outlet, Route, Routes } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({
  updateProfile: vi.fn(),
  uploadAvatar: vi.fn(),
  deleteAvatar: vi.fn(),
}));

vi.mock("../../services/userService", () => mocks);

import ProfileForm from "./ProfileForm";

const baseProfile = {
  id: "user-1",
  email: "user@haui.edu.vn",
  fullName: "Nguyễn Văn A",
  phone: "",
  address: "",
  avatar: "",
  role: "USER",
  isActive: true,
};

function ProfileLayout({ profile, setProfile }) {
  return <Outlet context={{ profile, setProfile }} />;
}

describe("ProfileForm", () => {
  let container;
  let root;
  let setProfile;

  const renderForm = (profile = baseProfile) => {
    act(() => {
      root.render(
        <MemoryRouter initialEntries={["/profile"]}>
          <Routes>
            <Route element={<ProfileLayout profile={profile} setProfile={setProfile} />}>
              <Route path="/profile" element={<ProfileForm />} />
              <Route path="/" element={<p>Trang chủ</p>} />
            </Route>
          </Routes>
        </MemoryRouter>,
      );
    });
  };

  const changeInput = (input, value) => {
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
    setProfile = vi.fn();
    vi.clearAllMocks();
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
    vi.restoreAllMocks();
  });

  it("validates a blank full name without calling the API", () => {
    renderForm();
    const input = container.querySelector("#profile-full-name");
    changeInput(input, "   ");

    act(() => container.querySelector("form").requestSubmit());

    expect(container.textContent).toContain("Vui lòng nhập họ và tên.");
    expect(mocks.updateProfile).not.toHaveBeenCalled();
  });

  it("links password changes to the authenticated change-password page", () => {
    renderForm();
    const link = [...container.querySelectorAll("a")].find((item) =>
      item.textContent.includes("Đổi mật khẩu"),
    );

    expect(link.getAttribute("href")).toBe("/change-password");
  });

  it("updates the full name and shared profile", async () => {
    const updated = { ...baseProfile, fullName: "Nguyễn Văn B" };
    mocks.updateProfile.mockResolvedValue({ data: updated });
    renderForm();
    changeInput(container.querySelector("#profile-full-name"), "  Nguyễn Văn B  ");

    await act(async () => {
      container.querySelector("form").requestSubmit();
    });

    expect(mocks.updateProfile).toHaveBeenCalledWith({ fullName: "Nguyễn Văn B" });
    expect(setProfile).toHaveBeenCalledWith(updated);
    expect(container.textContent).toContain("được lưu thành công");
  });

  it("rejects an unsupported avatar before uploading", () => {
    renderForm();
    const fileInput = container.querySelector('input[type="file"]');
    const file = new File(["text"], "avatar.gif", { type: "image/gif" });
    Object.defineProperty(fileInput, "files", { configurable: true, value: [file] });

    act(() => fileInput.dispatchEvent(new Event("change", { bubbles: true })));

    expect(container.textContent).toContain("JPEG, PNG hoặc WebP");
    expect(mocks.uploadAvatar).not.toHaveBeenCalled();
  });

  it("rejects an avatar larger than 5 MB before uploading", () => {
    renderForm();
    const fileInput = container.querySelector('input[type="file"]');
    const file = new File(["image"], "avatar.webp", { type: "image/webp" });
    Object.defineProperty(file, "size", { value: 5 * 1024 * 1024 + 1 });
    Object.defineProperty(fileInput, "files", { configurable: true, value: [file] });

    act(() => fileInput.dispatchEvent(new Event("change", { bubbles: true })));

    expect(container.textContent).toContain("không được vượt quá 5 MB");
    expect(mocks.uploadAvatar).not.toHaveBeenCalled();
  });

  it("uploads and deletes an avatar while updating shared profile", async () => {
    const withAvatar = { ...baseProfile, avatar: "/uploads/avatar.png" };
    mocks.uploadAvatar.mockResolvedValue({ data: withAvatar });
    mocks.deleteAvatar.mockResolvedValue({ data: baseProfile });
    vi.spyOn(window, "confirm").mockReturnValue(true);
    renderForm();

    const fileInput = container.querySelector('input[type="file"]');
    const file = new File(["image"], "avatar.png", { type: "image/png" });
    Object.defineProperty(fileInput, "files", { configurable: true, value: [file] });

    await act(async () => {
      fileInput.dispatchEvent(new Event("change", { bubbles: true }));
    });

    expect(mocks.uploadAvatar).toHaveBeenCalledWith(file);
    expect(setProfile).toHaveBeenCalledWith(withAvatar);

    act(() => root.unmount());
    root = createRoot(container);
    renderForm(withAvatar);
    const deleteButton = [...container.querySelectorAll("button")].find((button) =>
      button.textContent.includes("Xóa ảnh"),
    );

    await act(async () => deleteButton.click());

    expect(mocks.deleteAvatar).toHaveBeenCalledOnce();
    expect(setProfile).toHaveBeenCalledWith(baseProfile);
  });
});
