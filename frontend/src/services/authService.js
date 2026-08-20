import axiosInstance from "../api/axiosInstance";

export const registerUser = async (registerData) => {
  const response = await axiosInstance.post(
    "/api/v1/auth/register",
    registerData
  );
  return response.data;
};

export const loginUser = async (loginData) => {
  const response = await axiosInstance.post(
    "/api/v1/auth/login",
    loginData
  );
  return response.data;
};

export const logoutUser = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
};

export const isLoggedIn = () => {
  return localStorage.getItem("token") !== null;
};

export const getLoggedInUser = () => {
  const user = localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
};