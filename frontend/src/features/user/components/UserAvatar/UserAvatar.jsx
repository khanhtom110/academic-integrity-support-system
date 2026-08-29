import { useEffect, useState } from "react";

import { UserIcon } from "../../../../components/ui/Icons";
import { resolveAvatarUrl } from "../../utils/profile";

import "./UserAvatar.css";

function UserAvatar({ avatar, fullName, size = "medium", className = "" }) {
  const source = resolveAvatarUrl(avatar);
  const [hasImageError, setHasImageError] = useState(false);

  useEffect(() => {
    setHasImageError(false);
  }, [source]);

  const showImage = source && !hasImageError;

  return (
    <span className={`user-avatar user-avatar-${size} ${className}`.trim()}>
      {showImage ? (
        <img
          src={source}
          alt={fullName ? `Ảnh đại diện của ${fullName}` : "Ảnh đại diện"}
          onError={() => setHasImageError(true)}
        />
      ) : (
        <UserIcon size={size === "large" ? 48 : 20} />
      )}
    </span>
  );
}

export default UserAvatar;
