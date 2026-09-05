// @vitest-environment jsdom

import { act } from "react";
import { createRoot } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import SocialButton from "./SocialButton";

vi.mock("../../../constants/oauthConfig", () => ({
  createOAuthAuthorizationUrl: vi.fn(
    (provider) => `#oauth-${provider}`,
  ),
}));

describe("SocialButton", () => {
  let container;
  let root;

  beforeEach(() => {
    globalThis.IS_REACT_ACT_ENVIRONMENT = true;
    window.history.replaceState(null, "", "/");
    container = document.createElement("div");
    document.body.appendChild(container);
    root = createRoot(container);
  });

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
    vi.restoreAllMocks();
  });

  const startRedirect = (provider) => {
    act(() => root.render(<SocialButton provider={provider} />));

    const button = container.querySelector("button");
    act(() => button.click());

    expect(button.disabled).toBe(true);
    expect(button.textContent).toContain("Đang chuyển...");

    return button;
  };

  it.each(["google", "outlook"])(
    "resets the %s button when a cached page is shown again",
    (provider) => {
      const button = startRedirect(provider);

      act(() => window.dispatchEvent(new Event("pageshow")));

      expect(button.disabled).toBe(false);
      expect(button.textContent).not.toContain("Đang chuyển...");
    },
  );

  it("resets the button when the window regains focus", () => {
    const button = startRedirect("google");

    act(() => window.dispatchEvent(new Event("focus")));

    expect(button.disabled).toBe(false);
    expect(button.textContent).toContain("Google");
  });

  it("resets the button when the tab becomes visible", () => {
    const button = startRedirect("outlook");
    vi.spyOn(document, "visibilityState", "get").mockReturnValue("visible");

    act(() => document.dispatchEvent(new Event("visibilitychange")));

    expect(button.disabled).toBe(false);
    expect(button.textContent).toContain("Outlook");
  });
});
