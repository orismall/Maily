import React from 'react';
import { useNavigate } from 'react-router-dom';
import "../UpperBar.css";


const LogoutButton = () => {
  // React Router hook for programmatic navigation
  const navigate = useNavigate();

  // Function that handles the logout process
  const handleLogout = () => {
    // Ask user to confirm logout
    const confirmed = window.confirm('Are you sure you want to logout?');
    if (confirmed) {
      // Remove session data from local storage
      localStorage.removeItem('session');

      // Navigate back to the login screen
      navigate('/');
    }
  };

  // Render the logout button
  return (
    <button onClick={handleLogout} className="logout-button">
      Log out
    </button>
  );
};

export default LogoutButton;
