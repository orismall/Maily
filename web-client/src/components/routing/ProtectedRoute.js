import React from "react";
import { Navigate } from "react-router-dom";

// ProtectedRoute: A wrapper component that restricts access to authenticated users only
const ProtectedRoute = ({ children }) => {
  // Get the session from localStorage
  const session = localStorage.getItem("session");

  // If session does not exist, redirect the user to the login page
  if (!session) {
    return <Navigate to="/" replace />;
  }

  // If session exists, allow access to the protected route
  return children;
};

export default ProtectedRoute;
