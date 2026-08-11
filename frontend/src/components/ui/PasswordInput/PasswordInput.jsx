import "./PasswordInput.css";
import { forwardRef, useId } from "react";

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
  const generatedId = useId();
  const inputId = id || generatedId;
  const errorId = `${inputId}-error`;
  const describedBy = [ariaDescribedBy, error ? errorId : null]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="password-group">
      {label && <label htmlFor={inputId}>{label}</label>}

      <input
        id={inputId}
        ref={ref}
        type="password"
        className={[className, error ? "input-invalid" : ""]
          .filter(Boolean)
          .join(" ")}
        aria-invalid={error ? "true" : undefined}
        aria-describedby={describedBy || undefined}
        {...props}
      />

      {error && (
        <p id={errorId} className="input-error" role="alert">
          {error}
        </p>
      )}
    </div>
  );
});

export default PasswordInput;
