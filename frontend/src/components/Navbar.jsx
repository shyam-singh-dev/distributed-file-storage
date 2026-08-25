import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getLoggedInUser, isLoggedIn, logoutUser } from "../services/authService";

const Navbar = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();

   const handleLogout = async () => {
   await logoutUser();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">Distributed File Storage</Link>
      </div>

      <div className="navbar-links">
        {isLoggedIn() ? (
          <>
            <span className="user-info">
              {user?.fullName} ({user?.role})
            </span>
            <Link to="/dashboard">Dashboard</Link>
            <button onClick={handleLogout} className="btn btn-danger">
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;