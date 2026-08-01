import "./PasswordInput.css";

function PasswordInput({ label, ...props }) {
  return (
    <div className="password-group">
      {label && <label>{label}</label>}
      <input type="password" {...props} />
    </div>
  );
}

export default PasswordInput;
