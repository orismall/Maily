import React, { useEffect, useRef, useState } from 'react';

export default function UserView() {
  // State to store the user object fetched from the server
  const [user, setUser] = useState(null);

  // State to toggle visibility of the user dropdown
  const [open, setOpen] = useState(false);

  // Ref to the dropdown wrapper, used to detect clicks outside the dropdown
  const dropdownRef = useRef();

  // Fetch user data once on mount
  useEffect(() => {
    const session = JSON.parse(localStorage.getItem('session'));
    if (!session?.userId) return;

    const fetchUser = async () => {
      try {
        // Fetch the user data from the backend
        const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/users/${session.userId}`, {
          headers: {
            'User-Id': session.userId
          }
        });

        // If response is not OK, throw an error
        if (!res.ok) throw new Error("Failed to fetch user");

        // Parse response and store user in state
        const data = await res.json();
        setUser(data);
      } catch (err) {
        console.error("User fetch error:", err);
      }
    };

    fetchUser();
  }, []);

  // Close dropdown if clicking outside of it
  useEffect(() => {
    const handleClickOutside = (e) => {
      // If click is outside dropdown, close it
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };

    // Add/remove click listener based on dropdown state
    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }

    // Cleanup event listener on unmount
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  // If user has not yet been fetched, render nothing
  if (!user) return null;

  return (
    <div
      className="user-view-wrapper"
      ref={dropdownRef}
      style={{ position: 'relative' }}
    >
      {/* Header area with avatar and greeting */}
      <div className="user-view">
        {/* Toggle dropdown on avatar click */}
        <div className="user-avatar-wrapper" onClick={() => setOpen(!open)}>
          <img
            src={user.avatar}
            alt="User Avatar"
            className="user-avatar"
          />
        </div>
        <span className="user-greeting">Hello, {user.firstName}</span>
      </div>

      {/* Dropdown menu with user info */}
      {open && (
        <div className="user-dropdown">
          <p><strong>First name:</strong> {user.firstName}</p>
          <p><strong>Last name:</strong> {user.lastName}</p>
          <p><strong>Email:</strong> {user.email}</p>
        </div>
      )}
    </div>
  );
}
