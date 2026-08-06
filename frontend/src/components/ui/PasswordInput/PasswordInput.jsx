import "./PasswordInput.css";
import { forwardRef } from "react";

const PasswordInput = forwardRef(function PasswordInput(
  { label, error, id, ...props },
  ref,
) {
  return (
    <div className="password-group">
      {label && <label htmlFor={id}>{label}</label>}

      <input
        id={id}
        ref={ref}
        type="password"
        className={error ? "input-invalid" : ""}
        {...props}
      />

      {error && <p className="input-error">{error}</p>}
    </div>
  );
});

export default PasswordInput;
