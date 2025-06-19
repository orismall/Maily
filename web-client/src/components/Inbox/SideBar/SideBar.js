import React from 'react';
import {
  FaInbox, FaStar, FaRegEnvelope, FaTrash,
  FaRegEdit, FaRegPaperPlane, FaBars
} from 'react-icons/fa';
import LabelsSection from '../LabelsSection/LabelsSection';
import ComposeButton from '../../compose/ComposeButton';
import './SideBar.css';

// Sidebar component that displays navigation buttons and label controls
const SideBar = ({
  onComposeClick,     
  onFolderSelect,     
  collapsed,         
  setCollapsed,       
  labels,             
  setLabels,          
  onLabelClick,       
  selectedFolder      
}) => {

  // Handle built-in folder button clicks (Inbox, Sent, etc.)
  const handleClick = (folder) => {
    onFolderSelect(folder);
  };

  // Handle clicks on custom user-defined labels
  const handleLabelSelect = (labelId) => {
    onLabelClick(labelId);
  };

  return (
    <div className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      {/* Top section with Compose button and sidebar toggle */}
      <div className="sidebar-compose-toggle">
        <ComposeButton onClick={onComposeClick} collapsed={collapsed} />

        <button
          className="sidebar-toggle-button"
          onClick={() => setCollapsed(!collapsed)}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          <FaBars />
        </button>
      </div>

      {/* Built-in folder navigation buttons */}
      <div className="sidebar-buttons">
        <button
          onClick={() => handleClick('inbox')}
          className={`sidebar-button ${selectedFolder === 'inbox' ? 'active' : ''}`}
        >
          <FaInbox /> {!collapsed && <span>Inbox</span>}
        </button>

        <button
          onClick={() => handleClick('starred')}
          className={`sidebar-button ${selectedFolder === 'starred' ? 'active' : ''}`}
        >
          <FaStar /> {!collapsed && <span>Starred</span>}
        </button>

        <button
          onClick={() => handleClick('sent')}
          className={`sidebar-button ${selectedFolder === 'sent' ? 'active' : ''}`}
        >
          <FaRegPaperPlane /> {!collapsed && <span>Sent</span>}
        </button>

        <button
          onClick={() => handleClick('drafts')}
          className={`sidebar-button ${selectedFolder === 'drafts' ? 'active' : ''}`}
        >
          <FaRegEdit /> {!collapsed && <span>Drafts</span>}
        </button>

        <button
          onClick={() => handleClick('spam')}
          className={`sidebar-button ${selectedFolder === 'spam' ? 'active' : ''}`}
        >
          <FaRegEnvelope /> {!collapsed && <span>Spam</span>}
        </button>

        <button
          onClick={() => handleClick('trash')}
          className={`sidebar-button ${selectedFolder === 'trash' ? 'active' : ''}`}
        >
          <FaTrash /> {!collapsed && <span>Trash</span>}
        </button>

        {/* Custom user-created labels section */}
        <LabelsSection
          collapsed={collapsed}
          setCollapsed={setCollapsed}
          labels={labels}
          setLabels={setLabels}
          onLabelClick={handleLabelSelect}
          selectedFolder={selectedFolder}
        />
      </div>
    </div>
  );
};

export default SideBar;
