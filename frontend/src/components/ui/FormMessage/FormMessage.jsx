import "./FormMessage.css";

function FormMessage({ children, variant = "error" }) {
  if (!children) {
    return null;
  }

  const isError = variant === "error";

  return (
    <div
      className={`form-message form-message-${variant}`}
      role={isError ? "alert" : "status"}
      aria-live={isError ? "assertive" : "polite"}
    >
      <span className="form-message-icon" aria-hidden="true">
        {isError ? "!" : "✓"}
      </span>
      <span>{children}</span>
    </div>
  );
}

export default FormMessage;
