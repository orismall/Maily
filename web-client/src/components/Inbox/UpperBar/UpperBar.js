import React from 'react';
import { FaEnvelope } from 'react-icons/fa'; 
import './UpperBar.css';
import Logout from './logout/Logout';
import UserView from './userView/UserView';

// Functional component representing the top bar of the app
const UpperBar = ({ children }) => {
  return (
    // Container for the upper bar
    <div className="upper-bar">
      
      {/* Left side: Logo section with envelope icon and image */}
      <div className="gmail-logo">
        <FaEnvelope style={{ marginRight: '10px', fontSize: '24px' }} />
        <img
          src="/Maily.png"              
          alt="Maily Logo"              
          className="gmail-logo-img"    
        />
      </div>
      {children}

      {/* User profile avatar and dropdown */}
      <UserView />

      {/* Logout button */}
      <Logout />
    </div>
  );
};

export default UpperBar;