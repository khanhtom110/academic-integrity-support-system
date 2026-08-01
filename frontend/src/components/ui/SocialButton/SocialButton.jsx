import "./SocialButton.css";

import googleIcon from "../../../assets/icons/google.svg";
import outlookIcon from "../../../assets/icons/Outlook.svg";
import facebookIcon from "../../../assets/icons/facebook.svg";

const icons = {
  google: googleIcon,
  outlook: outlookIcon,
  facebook: facebookIcon,
};

const labels = {
  google: "Google",
  outlook: "Outlook",
  facebook: "Facebook",
};

function SocialButton({ provider }) {
  return (
    <button className="social-btn">
      <img src={icons[provider]} alt={labels[provider]} />
      <span>{labels[provider]}</span>
    </button>
  );
}

export default SocialButton;
