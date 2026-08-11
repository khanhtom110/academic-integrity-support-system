import "./OTPInput.css";

import { useEffect, useRef, useState } from "react";

function OTPInput({
  value = "",
  onChange,
  hasError = false,
  errorId,
  length = 6,
}) {
  const [otp, setOtp] = useState(Array(length).fill(""));

  const inputRefs = useRef([]);

  useEffect(() => {
    if (!value) {
      setOtp(Array(length).fill(""));
      return;
    }

    const values = value.split("").slice(0, length);

    while (values.length < length) {
      values.push("");
    }

    setOtp(values);
  }, [value, length]);

  const updateOtp = (newOtp) => {
    setOtp(newOtp);
    onChange?.(newOtp.join(""));
  };

  const handleChange = (e, index) => {
    const value = e.target.value;

    if (!/^\d?$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;

    updateOtp(newOtp);

    if (value && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === "Backspace" && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();

    const pasted = e.clipboardData
      .getData("text")
      .replace(/\D/g, "")
      .slice(0, length);

    if (!pasted) return;

    const newOtp = Array(length).fill("");

    pasted.split("").forEach((char, index) => {
      newOtp[index] = char;
    });

    updateOtp(newOtp);

    inputRefs.current[Math.min(pasted.length, length) - 1]?.focus();
  };

  return (
    <div className="otp-input" onPaste={handlePaste}>
      {otp.map((digit, index) => (
        <input
          key={index}
          ref={(el) => (inputRefs.current[index] = el)}
          value={digit}
          maxLength={1}
          inputMode="numeric"
          autoComplete={index === 0 ? "one-time-code" : "off"}
          className={`otp-box ${hasError ? "otp-invalid" : ""}`}
          aria-label={`Chữ số OTP ${index + 1}`}
          aria-invalid={hasError ? "true" : undefined}
          aria-describedby={hasError ? errorId : undefined}
          onChange={(e) => handleChange(e, index)}
          onKeyDown={(e) => handleKeyDown(e, index)}
        />
      ))}
    </div>
  );
}

export default OTPInput;
