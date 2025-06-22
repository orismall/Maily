import React, { useEffect, useRef, useState } from 'react';

export default function UserView() {
  const [user, setUser] = useState(null);
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef();

  useEffect(() => {
    const session = JSON.parse(localStorage.getItem('session'));
    const userId = session?.userId || session?._id;
    if (!userId) return;

    const fetchUser = async () => {
      try {
        const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/users/${userId}`, {
          headers: {
            'Authorization': `Bearer ${session.token}`
          }
        });

        if (!res.ok) throw new Error("Failed to fetch user");

        const data = await res.json();
        setUser(data);
      } catch (err) {
        console.error("User fetch error:", err);
      }
    };

    fetchUser();
  }, []);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [open]);

  if (!user) return null;

  return (
    <div
      className="user-view-wrapper"
      ref={dropdownRef}
    >
      <div className="user-view">
        <div className="user-avatar-wrapper" onClick={() => setOpen(!open)}>
          <img
            src={user.avatar}
            alt="User Avatar"
            className="user-avatar"
          />
        </div>
        <span className="user-greeting">Hello, {user.firstName}</span>
      </div>

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
