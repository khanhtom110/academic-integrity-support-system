import { forwardRef } from "react";
import "./Input.css";

const Input = forwardRef(function Input({ label, error, ...props }, ref) {
  return (
    <div className="input-group">
      {label && <label>{label}</label>}

      <input ref={ref} {...props} />

      {error && <p className="input-error">{error}</p>}
    </div>
  );
});

export default Input;
