import "./AuthSteps.css";

const steps = ["Email", "OTP", "Mật khẩu mới"];

function AuthSteps({ currentStep }) {
  return (
    <ol className="auth-steps" aria-label="Tiến trình khôi phục mật khẩu">
      {steps.map((step, index) => {
        const stepNumber = index + 1;
        const isCurrent = stepNumber === currentStep;
        const isCompleted = stepNumber < currentStep;

        return (
          <li
            key={step}
            className={isCompleted ? "completed" : isCurrent ? "current" : ""}
            aria-current={isCurrent ? "step" : undefined}
          >
            <span className="auth-step-number">{isCompleted ? "✓" : stepNumber}</span>
            <span className="auth-step-label">{step}</span>
          </li>
        );
      })}
    </ol>
  );
}

export default AuthSteps;
