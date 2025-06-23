import React from 'react';
import { FaInbox, FaPaperPlane, FaTrash, FaExclamationCircle } from 'react-icons/fa';
import './CategoryBar.css';

// Renders a category bar with buttons for Inbox, Sent, Spam, and Trash
const CategoryBar = ({ selectedCategory, onCategoryChange }) => {
  return (
    <div className="mail-header-bar">
      {/* Inbox Button */}
      <button
        onClick={() => onCategoryChange('inbox')} // Change folder to inbox
        className={selectedCategory === 'inbox' ? 'active' : ''} // Highlight if selected
      >
        <FaInbox style={{ marginRight: '6px' }} />
        Inbox
      </button>

      {/* Sent Button */}
      <button
        onClick={() => onCategoryChange('sent')} // Change folder to sent
        className={selectedCategory === 'sent' ? 'active' : ''} // Highlight if selected
      >
        <FaPaperPlane style={{ marginRight: '6px' }} />
        Sent
      </button>

      {/* Spam Button */}
      <button
        onClick={() => onCategoryChange('spam')} // Change folder to spam
        className={selectedCategory === 'spam' ? 'active' : ''} // Highlight if selected
      >
        <FaExclamationCircle style={{ marginRight: '6px' }} />
        Spam
      </button>

      {/* Trash Button */}
      <button
        onClick={() => onCategoryChange('trash')} // Change folder to trash
        className={selectedCategory === 'trash' ? 'active' : ''} // Highlight if selected
      >
        <FaTrash style={{ marginRight: '6px' }} />
        Trash
      </button>
    </div>
  );
};

export default CategoryBar;
