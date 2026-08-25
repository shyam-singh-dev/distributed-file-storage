import axiosInstance from "../api/axiosInstance";

// LOGIN
export const loginUser = async (loginData) => {
  const response = await axiosInstance.post(
    "/api/v1/auth/login",
    loginData
  );

  return response.data;
};

// REFRESH ACCESS TOKEN
export const refreshAccessToken = async (refreshToken) => {
  const response = await axiosInstance.post(
    "/api/v1/auth/refresh",
    { refreshToken }
  );

  return response.data;
};

// LOGOUT
export const logoutUser = async () => {
  try {
    await axiosInstance.post("/api/v1/auth/logout");
  } catch (err) {
    console.log("Logout API failed, clearing locally");
  }

  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("user");
};

// REGISTER
export const registerUser = async (registerData) => {
  const response = await axiosInstance.post(
    "/api/v1/auth/register",
    registerData
  );

  return response.data;
};

// CHECK LOGIN
export const isLoggedIn = () => {
  return localStorage.getItem("token") !== null;
};

// GET LOGGED-IN USER
export const getLoggedInUser = () => {
  const user = localStorage.getItem("user");

  return user ? JSON.parse(user) : null;
};