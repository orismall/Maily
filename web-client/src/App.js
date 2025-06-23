import React from "react";
import { Routes, Route } from "react-router-dom";
import Login from "./components/login/Login";
import Signup from "./components/signup/Signup";
import ComposeMail from "./components/compose/ComposeMail";
import Inbox from "./components/Inbox/Inbox";
// Wrapper to protect private routes
import ProtectedRoute from "./components/routing/ProtectedRoute";

function App() {
  return (
    <Routes>
      {/* Public Routes (accessible without login) */}
      <Route path="/" element={<Login />} />         {/* Login page */}
      <Route path="/signup" element={<Signup />} /> {/* Signup page */}

      {/* Protected Routes (require authentication) */}
      <Route
        path="/inbox"
        element={
          <ProtectedRoute>
            <Inbox /> {/* Main inbox screen */}
          </ProtectedRoute>
        }
      />

      <Route
        path="/composemail"
        element={
          <ProtectedRoute>
            <ComposeMail /> {/* Compose mail screen */}
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;