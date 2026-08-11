import "./PasswordInput.css";
import { forwardRef, useId, useState } from "react";
import { EyeIcon } from "../Icons";

const PasswordInput = forwardRef(function PasswordInput(
  {
    label,
    error,
    id,
    className = "",
    "aria-describedby": ariaDescribedBy,
    ...props
  },
  ref,
) {
  const [isVisible, setIsVisible] = useState(false);
  const generatedId = useId();
  const inputId = id || generatedId;
  const errorId = `${inputId}-error`;
  const describedBy = [ariaDescribedBy, error ? errorId : null]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="password-group">
      {label && <label htmlFor={inputId}>{label}</label>}

      <div className="password-control">
        <input
          id={inputId}
          ref={ref}
          type={isVisible ? "text" : "password"}
          className={[className, error ? "input-invalid" : ""]
            .filter(Boolean)
            .join(" ")}
          aria-invalid={error ? "true" : undefined}
          aria-describedby={describedBy || undefined}
          {...props}
        />
        <button
          type="button"
          className="password-toggle"
          onClick={() => setIsVisible((current) => !current)}
          aria-label={isVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
          aria-pressed={isVisible}
        >
          <EyeIcon hidden={!isVisible} />
        </button>
      </div>

      {error && (
        <p id={errorId} className="input-error" role="alert">
          {error}
        </p>
      )}
    </div>
  );
});

export default PasswordInput;
