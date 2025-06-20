import React, { useEffect, useState } from 'react';

export default function UserView() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const session = JSON.parse(localStorage.getItem('session'));
    if (!session?.userId) return;

    const fetchUser = async () => {
      try {
        const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/users/${session.userId}`, {
          headers: {
            'User-Id': session.userId
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
  
  return <div>{user ? `Hello, ${user.firstName}` : "Loading..."}</div>;
}
