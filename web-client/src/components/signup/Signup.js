import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./Signup.css";
import "../../styles/DarkMode.css";

export default function Signup() {
  // State for form fields and avatar
  const [values, setValues] = useState({
    email: "",
    password: "",
    confirmPassword: "",
    firstName: "",
    lastName: "",
    gender: "",
    birthdate: "",
    avatar: ""
  });

  // State for showing loading state and server errors
  const [loading, setLoading] = useState(false);
  const [serverError, setError] = useState("");

  // Dark mode toggle state
  const [isDarkMode, setIsDarkMode] = useState(false);

  // React Router navigation function
  const navigate = useNavigate();

  // On first render: apply dark mode if previously stored in localStorage
  useEffect(() => {
    const dark = localStorage.getItem("theme") === "dark";
    setIsDarkMode(dark);
    if (dark) {
      document.body.classList.add("dark");
    }
  }, []);

  // Toggle light/dark theme
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

  // Convert selected image to base64 and store in avatar
  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      setValues(v => ({ ...v, avatar: reader.result }));
    };
    reader.readAsDataURL(file);
  };

  // Handle input changes (text, email, select, etc.)
  const handleChange = e =>
    setValues(v => ({ ...v, [e.target.name]: e.target.value }));

  // Submit form: send data to server and handle validation or redirect
  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/users`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: values.email,
          password: values.password,
          confirmPassword: values.confirmPassword,
          firstName: values.firstName,
          lastName: values.lastName,
          gender: values.gender,
          birthdate: values.birthdate,
          avatar: values.avatar
        })
      });
      // On successful signup, redirect to login page
      if (res.status === 201) {
        navigate("/");
        return;
      }
      // If server responds with error message
      const { error } = await res.json();
      setError(error);
    } catch {
      // If fetch fails (network error)
      setError("Network error - please try again");
    } finally {
      setLoading(false);
    }
  };
  
  // Reusable field renderer (for text/email/password inputs)
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
      {/* Container for signup form with dark mode toggle */}
      <div className="signup-container">
        {/* Dark mode toggle button */}
        <button
          className="dark-mode-button"
          aria-label="Toggle dark mode"
          onClick={toggleDarkMode}
        >
          <span className="icon moon">🌚</span>
          <span className="icon sun">☀️</span>
        </button>

        {/* Signup form card */}
        <div className="signup-card">
          <form className="signup-form" onSubmit={handleSubmit} noValidate>
            <img src="/Maily.png" alt="Mail icon" className="signup-icon" />
            <h2>Create your Maily account</h2>

            {/* Form input fields */}
            {field("firstName", "First Name")}
            {field("lastName", "Last Name")}
            {field("email", "Email address", "email")}
            {field("password", "Password", "password")}
            {field("confirmPassword", "Confirm password", "password")}

            {/* Gender select field */}
            <select
              className="signup-input"
              name="gender"
              value={values.gender}
              onChange={handleChange}
              required
            >
              <option value="" disabled>Select gender</option>
              <option value="male">Male</option>
              <option value="female">Female</option>
              <option value="other">Other</option>
            </select>

            {/* Birthdate field */}
            {field("birthdate", "Birthdate (YYYY-MM-DD)", "date")}

            {/* Avatar upload */}
            <input
              className="signup-input"
              type="file"
              accept="image/*"
              onChange={handleFileSelect}
            />

            {/* Avatar preview */}
            {values.avatar && (
              <img
                src={values.avatar}
                alt="avatar preview"
                className="avatar-preview"
              />
            )}

            {/* Submit button */}
            <button className="signup-btn" disabled={loading}>
              {loading ? "Creating…" : "Register"}
            </button>

            {/* Redirect to login if already registered */}
            <p className="signup-login-link">
              Already have an account? <Link to="/">Sign in</Link>
            </p>

            {/* Server error display */}
            {serverError && (
              <div className="signup-error" style={{ marginTop: 12 }}>
                {serverError}
              </div>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}
