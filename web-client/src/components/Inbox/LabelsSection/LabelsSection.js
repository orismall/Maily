import React, { useState, useEffect, useRef } from 'react';
import './LabelsSection.css';

const LabelsSection = ({ collapsed, setCollapsed, labels, setLabels, onLabelClick, selectedFolder , fetchLabels }) => {
  // State for editing, creating, and displaying label inputs
  const [editingLabelId, setEditingLabelId] = useState(null);
  const [editedLabelName, setEditedLabelName] = useState('');
  const [editedColor, setEditedColor] = useState('#000000');
  const [newLabel, setNewLabel] = useState('');
  const [newColor, setNewColor] = useState('#000000');
  const [showInput, setShowInput] = useState(false);
  const inputRef = useRef(null);

  // Fetch labels on initial mount
  useEffect(() => {
    if (fetchLabels) {
      fetchLabels();
    }
  }, [fetchLabels]);


  // Hide input form when sidebar collapses
  useEffect(() => {
    if (collapsed) {
      setShowInput(false);
    }
  }, [collapsed]);

  // Close input form when clicking outside of it
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (inputRef.current && !inputRef.current.contains(event.target)) {
        setShowInput(false);
        setNewLabel('');
        setNewColor('#000000');
      }
    };
    if (showInput) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showInput]);

  // Open label input form
  const handleAddClick = () => {
    if (collapsed) setCollapsed(false);
    setShowInput(true);
  };

  // Submit new label to the server
  const handleLabelSubmit = async (e) => {
    e.preventDefault();
    const trimmed = newLabel.trim();
    if (!trimmed) return;

    const session = JSON.parse(localStorage.getItem("session") || "{}");
    const token = session.token;
    if (!token) {
      alert("Missing session token. Please log in again.");
      return;
    }

    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/labels`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ name: trimmed, color: newColor , userId: session._id})
      });

      if (res.status === 201 && fetchLabels) {
        await fetchLabels();
      } else {
        const { error } = await res.json();
        alert(error || "Failed to create label.");
      }
    } catch (err) {
      console.error("Network error:", err);
      alert("Network error - please try again.");
    } finally {
      setNewLabel('');
      setNewColor('#000000');
      setShowInput(false);
    }
  };

  // Start editing an existing label
  const handleEditClick = (label) => {
    setEditingLabelId(label._id);
    setEditedLabelName(label.name);
    setEditedColor(label.color);
    setShowInput(false);
  };

  // Submit edited label to the server
  const handleEditSubmit = async (e) => {
    e.preventDefault();
    const trimmed = editedLabelName.trim();
    if (!trimmed) return;

    const session = JSON.parse(localStorage.getItem("session") || "{}");
    const token = session.token;
    if (!token) {
      alert("Missing session token. Please log in again.");
      return;
    }

    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/labels/${editingLabelId}`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ name: trimmed, color: editedColor }),
      });

      if (res.status === 204 && fetchLabels) {
        await fetchLabels(); 
        setEditingLabelId(null);
        setEditedLabelName('');
        setEditedColor('#000000');
      } else {
        const data = await res.json();
        alert(data.error);
      }
    } catch (err) {
      console.error("Edit label error:", err);
      alert("Network error - please try again.");
    }
  };

  // Delete label after confirmation
  const handleDeleteLabel = async (labelId) => {
    const confirmed = window.confirm("Are you sure you want to delete this label?");
    if (!confirmed) return;

    const session = JSON.parse(localStorage.getItem("session") || "{}");
    const token = session.token;
    if (!token) {
      alert("Missing session token. Please log in again.");
      return;
    }

    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/labels/${labelId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.status === 204 && fetchLabels) {
        await fetchLabels();
      } else {
        const data = await res.json();
        alert(data.error);
      }
    } catch (err) {
      console.error("Delete label error:", err);
      alert("Network error - please try again.");
    }
  };

  return (
    <div className="labels-section">
      <div className="labels-header">
        {!collapsed && <span className="labels-title">Labels</span>}
        <button className="labels-add-button" onClick={handleAddClick}>+</button>
      </div>

      {showInput && (
        <form ref={inputRef} className="label-input-form" onSubmit={handleLabelSubmit}>
          <div className="label-input-row">
            <input
              type="text"
              value={newLabel}
              onChange={(e) => setNewLabel(e.target.value)}
              placeholder="Label name"
              className="label-input"
              autoFocus
            />
            <input
              type="color"
              value={newColor}
              onChange={(e) => setNewColor(e.target.value)}
              className="label-color-picker"
              title="Choose label color"
            />
          </div>
          <button type="submit" className="label-submit-btn">Add</button>
        </form>
      )}

      <ul className="labels-list">
        {labels.map((label, index) => (
          <li
            key={index}
            className={`label-item ${selectedFolder === `label-${label._id}` ? 'active' : ''}`}
            title={label.name}
          >
            {editingLabelId === label._id ? (
              <form onSubmit={handleEditSubmit} className="label-edit-form">
                <div className="label-input-row">
                  <input
                    type="text"
                    value={editedLabelName}
                    onChange={(e) => setEditedLabelName(e.target.value)}
                    className="label-input"
                    autoFocus
                  />
                  <input
                    type="color"
                    value={editedColor}
                    onChange={(e) => setEditedColor(e.target.value)}
                    className="label-color-picker"
                    title="Choose label color"
                  />
                </div>
                <div style={{ display: 'flex', gap: '8px', marginTop: '6px' }}>
                  <button type="submit" className="label-submit-btn">Save</button>
                  <button
                    type="button"
                    className="label-submit-btn"
                    onClick={() => setEditingLabelId(null)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <>
                <span className="label-color-circle" style={{ backgroundColor: label.color }}></span>
                <span
                  className="label-name"
                  onClick={() => onLabelClick(label._id)}
                  style={{ cursor: 'pointer' }}
                >
                  {collapsed ? label.name.charAt(0).toUpperCase() : label.name}
                </span>
                {!collapsed && (
                  <div className="label-actions">
                    <button className="label-edit-btn" title="Edit label" onClick={() => handleEditClick(label)}>✏</button>
                    <button className="label-delete-btn" title="Delete label" onClick={() => handleDeleteLabel(label._id)}>🗑</button>
                  </div>
                )}
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default LabelsSection;
