import { API } from "../../../constants/api";
import apiClient from "../../../services/apiClient";

export const getCurrentUserProfile = async () => {
  const response = await apiClient.get(API.USER.PROFILE);
  return response.data;
};

export const updateProfile = async (data) => {
  const response = await apiClient.put(API.USER.PROFILE, data);
  return response.data;
};

export const changePassword = async (data) => {
  const response = await apiClient.post(API.USER.CHANGE_PASSWORD, data);
  return response.data;
};

export const updateAvatar = async (file) => {
  const formData = new FormData();
  formData.append("file", file);

  /**
   * Không set Content-Type thủ công: browser sẽ tự thêm
   * multipart/form-data kèm boundary cho FormData.
   */
  const response = await apiClient.post(API.USER.AVATAR, formData);
  return response.data;
};

export const deleteAvatar = async () => {
  const response = await apiClient.delete(API.USER.AVATAR);
  return response.data;
};
