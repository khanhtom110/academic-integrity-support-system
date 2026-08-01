import "./Button.css";

function Button({ children, type = "button", onClick, className = "" }) {
  return (
    <button
      type={type}
      className={`primary-btn ${className}`}
      onClick={onClick}
    >
      {children}
    </button>
  );
}

export default Button;
