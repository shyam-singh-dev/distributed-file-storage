import { createContext, useContext, useState } from "react";
import {
  getLoggedInUser,
  isLoggedIn,
  loginUser,
  logoutUser,
} from "../services/authService";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(isLoggedIn());
  const [user, setUser] = useState(getLoggedInUser());

  const login = async (loginData) => {
    const response = await loginUser(loginData);

    const token = response.data.token;
    const refreshToken = response.data.refreshToken;
    const userData = response.data.user;

    localStorage.setItem("token", token);
    localStorage.setItem("refreshToken", refreshToken);
    localStorage.setItem("user", JSON.stringify(userData));

    setIsAuthenticated(true);
    setUser(userData);

    return response;
  };

  const logout = async () => {
    await logoutUser();

    setIsAuthenticated(false);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        user,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};