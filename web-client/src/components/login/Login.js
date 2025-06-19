import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./Login.css";
import "../../styles/DarkMode.css";

export default function Login() {
  // State for form input values
  const [values, setValues] = useState({
    email: "",
    password: ""
  });

  // State for error message display
  const [error, setError] = useState("");

  // State for loading spinner while logging in
  const [loading, setLoading] = useState(false);

  // State for dark mode toggle
  const [isDarkMode, setIsDarkMode] = useState(false);

  // Used for navigating to other routes
  const navigate = useNavigate();

  // On initial render: check localStorage for theme preference and apply dark mode
  useEffect(() => {
    const dark = localStorage.getItem("theme") === "dark";
    setIsDarkMode(dark);
    if (dark) {
      document.body.classList.add("dark");
    }
  }, []);

  // Toggle dark mode and store preference in localStorage
  const toggleDarkMode = () => {
    const newMode = !isDarkMode;
    setIsDarkMode(newMode);

    if (newMode) {
      document.body.classList.add("dark");
      localStorage.setItem("theme", "dark");
    } else {
      document.body.classList.remove("dark");
      localStorage.setItem("theme", "light");
    }
  };

  // Handle form field changes and update corresponding state
  const handleChange = e =>
    setValues(v => ({ ...v, [e.target.name]: e.target.value }));

  // Handle login form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);  
    setError("");      

    try {
      // Send login credentials to the server
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/tokens`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: values.email,
          password: values.password
        }),
      });

      const data = await res.json();

      // If login fails, show server-provided error
      if (!res.ok) {
        setError(data.error);
        return;
      }

      // Save the session (token and userId) in localStorage
      localStorage.setItem("session", JSON.stringify({
        userId: data.userId,
        token: data.token
      }));

      // Navigate to the protected inbox route
      navigate("/Inbox");
    } catch (err) {
      // If the network request fails
      setError("Network error - please try again");
    } finally {
      // Stop showing loading indicator
      setLoading(false);
    }
  };

  // Helper function to render an input field
  const field = (name, placeholder, type = "text") => (
    <input
      className="signup-input"
      name={name}
      type={type}
      placeholder={placeholder}
      value={values[name]}
      onChange={handleChange}
    />
  );

  return (
    <div>
      <div className="login-container">
        {/* Dark mode toggle button */}
        <button
          className="dark-mode-button"
          aria-label="Toggle dark mode"
          onClick={toggleDarkMode}
        >
          <span className="icon moon">🌚</span>
          <span className="icon sun">☀️</span>
        </button>

        {/* Login form card */}
        <div className="login-card">
          <form className="login-form" onSubmit={handleSubmit} noValidate>
            <img src="/Maily.png" alt="Mail icon" className="login-icon" />
            <h2>Login to your Maily account</h2>

            {/* Email and Password fields */}
            {field("email", "Email address", "email")}
            {field("password", "Password", "password")}

            {/* Submit button */}
            <button className="login-btn" type="submit" disabled={loading}>
              {loading ? "Logging in..." : "Log In"}
            </button>

            {/* Link to signup page */}
            <p className="login-signup-link">
              Don’t have an account? <Link to="/signup">Create account</Link>
            </p>

            {/* Error message */}
            {error && <div className="login-error">{error}</div>}
          </form>
        </div>
      </div>
    </div>
  );
}
