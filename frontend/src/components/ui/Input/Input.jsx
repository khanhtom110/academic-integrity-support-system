import "./Input.css";

function Input({ label, id, ...props }) {
  return (
    <div className="input-group">
      {label && <label htmlFor={id}>{label}</label>}

      <input id={id} {...props} />
    </div>
  );
}

export default Input;
