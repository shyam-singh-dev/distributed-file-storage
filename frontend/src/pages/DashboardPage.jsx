import { getLoggedInUser } from "../services/authService";

const DashboardPage = () => {
  const user = getLoggedInUser();

  return (
    <div className="page-container">
      <div className="dashboard-card">
        <h2>Dashboard</h2>

        <p className="subtitle">
          Welcome to your Distributed File Storage System.
        </p>

        <div className="user-card">
          <h3>Logged-in User</h3>
          <p>
            <strong>Name:</strong> {user?.fullName}
          </p>
          <p>
            <strong>Email:</strong> {user?.email}
          </p>
          <p>
            <strong>Role:</strong> {user?.role}
          </p>
        </div>

        <div className="info-box">
          <h3>What is completed?</h3>
          <ul>
            <li>Backend registration is connected</li>
            <li>Backend login is connected</li>
            <li>JWT token is stored in browser</li>
            <li>Protected dashboard route is working</li>
          </ul>
        </div>

        <div className="info-box">
          <h3>Coming next</h3>
          <p>
            On Day 7, this dashboard will show file upload, file list,
            and download options.
          </p>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;