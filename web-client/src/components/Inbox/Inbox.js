import React, { useState, useEffect, useRef, useCallback } from 'react';
import MailList from './MailList/MailList';
import SideBar from './SideBar/SideBar';
import UpperBar from './UpperBar/UpperBar';
import ComposeModal from '../compose/ComposeModal';
import MailView from './MailView/MailView';
import SearchBar from './SearchBar/SearchBar';
import CategoryBar from './CategoryBar/CategoryBar';

import './Inbox.css';
import './LabelsSection/LabelsSection.css';
import './SideBar/SideBar.css';
import './UpperBar/UpperBar.css';
import '../../styles/DarkMode.css';

const Inbox = () => {
  // UI and state management
  const [page, setPage] = useState(1);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mails, setMails] = useState([]);
  const [selectedMail, setSelectedMail] = useState(null);
  const [currentFolder, setCurrentFolder] = useState('inbox');
  const [composeValues, setComposeValues] = useState({ receiver: "", subject: "", body: "" });
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const searchInputRef = useRef(null);
  const [searchEmpty, setSearchEmpty] = useState(false);
  const [searchResults, setSearchResults] = useState(null);
  const [isInSearchMode, setIsInSearchMode] = useState(false);
  const [previousFolder, setPreviousFolder] = useState('inbox');
  const [searchTerm, setSearchTerm] = useState('');
  const [labels, setLabels] = useState([]);

  // Fetch user's labels
  const fetchLabels = async () => {
    try {
      const session = JSON.parse(localStorage.getItem('session'));
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/labels`, {
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${session.token}`
        }
      });
      const data = await res.json();
      setLabels(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to fetch labels", err);
    }
  };

  // Open compose modal or unminimize it
  const handleComposeClick = () => {
    if (isModalOpen && isMinimized) {
      setIsMinimized(false);
    } else if (!isModalOpen) {
      setIsModalOpen(true);
      setIsMinimized(false);
    }
  };

  // Close compose modal
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setIsMinimized(false);
  };

  // Fetch mails from current folder and page
  const fetchMails = useCallback(async () => {
    try {
      const session = JSON.parse(localStorage.getItem('session'));
      if (!session || !session.token || !session._id) return;

      const endpointMap = {
        inbox: 'inbox',
        starred: 'starred',
        sent: 'sent',
        drafts: 'drafts',
        spam: 'spam',
        trash: 'trash'
      };
      const endpoint = endpointMap[currentFolder];
      if (!endpoint) return;

      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/${endpoint}?page=${page}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "user-id": session._id,
          "Authorization": `Bearer ${session.token}`
        }
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Failed to fetch mails");

      const sorted = data.sort((a, b) =>
        new Date(b.mail.date) - new Date(a.mail.date)
      );
      setMails(sorted);
    } catch (err) {
      console.error(err.message || "Something went wrong");
    }
  }, [currentFolder, page]);

  // Load labels on mount
  useEffect(() => {
    fetchLabels();
  }, []);

  // Fetch mails when folder/page changes (unless searching)
  useEffect(() => {
    if (currentFolder === null || isInSearchMode) return;
    fetchMails();
  }, [currentFolder, isInSearchMode, page, fetchMails]);

  // Initialize dark mode based on localStorage
  useEffect(() => {
    const dark = localStorage.getItem("theme") === "dark";
    setIsDarkMode(dark);
    if (dark) {
      document.body.classList.add("dark");
    }
  }, []);

  // Toggle between light/dark mode
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

  // Toggle read/unread status
  const handleToggleRead = async (mailId, newValue) => {
    try {
      const session = JSON.parse(localStorage.getItem('session'));
      await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails/${mailId}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`,
          'user-id': session._id
        },
        body: JSON.stringify({ isRead: newValue })
      });

      setMails(prev =>
        prev.map(mail =>
          mail._id === mailId
            ? { ...mail, isRead: newValue }
            : mail
        )
      );
    } catch (error) {
      console.error('Failed to update read status', error);
    }
  };

  // Handle click on a mail: open view or draft editor
  const handleOpenMail = (mailData) => {
    if (mailData.type === 'draft') {
      setComposeValues({
        receiver: mailData.receiver || "",
        subject: mailData.subject || "",
        content: mailData.content || "",
        id: mailData._id || null
      });
      setIsModalOpen(true);
      setIsMinimized(false);
      setSelectedMail(null);
    } else {
      setSelectedMail(mailData);
      if (!mailData.isRead) {
        handleToggleRead(mailData._id, true);
      }
    }
  };

  // Close mail view
  const handleCloseMail = () => {
    setSelectedMail(null);
  };

  // Change folder
  const handleFolderSelect = (folder) => {
    setSearchResults(null);
    setSearchEmpty(false);
    setIsInSearchMode(false);
    setSelectedMail(null);
    setCurrentFolder(folder);
    setPage(1);
  };

  // Run a search
  const handleSearch = async (query) => {
    setSearchResults(null);
    setSearchEmpty(false);
    setPreviousFolder(currentFolder);
    setSearchTerm(query);
    setIsInSearchMode(true);
    setPage(1);

    try {
      const session = JSON.parse(localStorage.getItem('session'));
      if (!session || !session.token || !session._id) return;

      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails/search/${encodeURIComponent(query)}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "user-id": session._id,
          "Authorization": `Bearer ${session.token}`
        }
      });

      const data = await res.json();

      if (res.status === 404 || data.length === 0) {
        setSearchResults([]);
        setSearchEmpty(true);
        return;
      }

      setSearchResults(data);
    } catch (err) {
      console.error("Search failed", err);
      setSearchResults([]);
      setSearchEmpty(true);
    }
  };

  // Clear search and return to previous folder
  const handleClearSearch = () => {
    if (isInSearchMode) {
      setSearchResults(null);
      setSearchEmpty(false);
      setIsInSearchMode(false);
      setSearchTerm('');
      setCurrentFolder(previousFolder);
    }
  };

  // Toggle star (favorite) for a mail
  const handleToggleStar = async (mailId, mailType) => {
    const session = JSON.parse(localStorage.getItem('session'));
    const targetMail = mails.find(m => m._id === mailId && m.type === mailType);
    if (!targetMail) return;

    const newStarValue = !targetMail.isStarred;

    try {
      const url = mailType === 'draft'
        ? `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/drafts/${mailId}`
        : `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails/${mailId}`;
      await fetch(url, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`,
          'user-id': session._id
        },
        body: JSON.stringify({ isStarred: newStarValue, mailType })
      });

      if (currentFolder === 'starred') {
        const response = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/starred`, {
          headers: {
            'Authorization': `Bearer ${session.token}`,
            'user-id': session._id
          }
        });
        const updatedStarred = await response.json();
        setMails(updatedStarred);
      } else {
        setMails(prev =>
          prev.map(mail =>
            mail._id === mailId && mail.mail.type === mailType
              ? { ...mail, isStarred: newStarValue }
              : mail
          )
        );
      }
    } catch (err) {
      console.error("Error toggling star", err);
    }
  };
  // Filter mails based on a specific label ID
  const filterMailsByLabel = async (labelId) => {
    try {
      const session = JSON.parse(localStorage.getItem('session'));
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails?page=${page}`, {
        headers: {
          "Content-Type": "application/json",
          "user-id": session._id,
          "Authorization": `Bearer ${session.token}`
        }
      });
      if (!res.ok) throw new Error("Failed to fetch mails");

      const all = await res.json();

      // Filter only mails that have the specified label
      const filtered = all.filter(item =>
        item.mail.labels?.includes(labelId) && currentFolder !== 'trash'
      );

      setMails(filtered);
      setSelectedMail(null);
      setIsInSearchMode(false);
      setCurrentFolder(`label-${labelId}`);
      setPage(1);
    } catch (err) {
      console.error("Error filtering mails by label:", err);
    }
  };

  // Mark a mail as spam
  const handleMarkAsSpam = async (mailId) => {
    const confirm = window.confirm("Are you sure you want to report this mail as spam?");
    if (!confirm) return;

    try {
      const session = JSON.parse(localStorage.getItem('session'));
      await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/spam/${mailId}/mark-as-spam`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`
        }
      });
      fetchMails();             // Refresh updated folder
      setSelectedMail(null);    // Close mail view if open
    } catch (err) {
      console.error("Error marking as spam:", err);
    }
  };

  // Mark a mail as not spam
  const handleMarkAsNotSpam = async (mailId) => {
    const confirm = window.confirm("Are you sure you want to mark this mail as not spam?");
    if (!confirm) return;

    try {
      const session = JSON.parse(localStorage.getItem('session'));
      await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/spam/${mailId}/mark-as-not-spam`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`
        }
      });

    fetchMails();
    setSelectedMail(null);

    } catch (err) {
      console.error("Error unmarking spam:", err);
    }
  };

  // Delete a mail or move it to trash depending on folder
  const handleDelete = async (mailId, currentFolder) => {
    const session = JSON.parse(localStorage.getItem('session'));
    const confirmDelete = window.confirm(
      currentFolder === 'trash' || currentFolder === 'spam' || currentFolder === 'drafts'
        ? "Are you sure you want to permanently delete this mail?"
        : "Are you sure you want to move this mail to trash?"
    );
    if (!confirmDelete) return;

    try {
      const url =
        currentFolder === 'trash'
          ? `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/trash/${mailId}`
          : currentFolder === 'spam'
            ? `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/spam/${mailId}`
            : currentFolder === 'drafts'
              ? `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/drafts/${mailId}`
              : `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails/${mailId}`;

      const res = await fetch(url, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${session.token}`,
        }
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || 'Failed to delete mail');
      }

      handleCloseMail();

      // Refresh the view
      if (currentFolder.startsWith("label-")) {
        const labelId = currentFolder.replace("label-", "");
        filterMailsByLabel(labelId);
      } else {
        fetchMails();
      }
      setSelectedMail(null);
    } catch (error) {
      alert('Error: ' + error.message);
    }
  };

  // Restore a deleted mail from trash
  const handleRestore = async (mailId) => {
    const session = JSON.parse(localStorage.getItem('session'));
    const confirmRestore = window.confirm("Are you sure you want to Restore this mail?");
    if (!confirmRestore) return;

    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/trash/${mailId}/restore`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${session.token}`,
          'User-Id': session._id,
        },
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || 'Failed to restore mail.');
      }

      fetchMails();
      handleCloseMail();
    } catch (error) {
      console.error("Restore error:", error.message);
      alert("Failed to restore mail: " + error.message);
    }
  };

  // Main render block
  return (
    <div className="inbox-container">
      <UpperBar>
        <SearchBar
          onSearch={handleSearch}
          onClearSearch={handleClearSearch}
          inputRef={searchInputRef}
          setSelectedMail={setSelectedMail}
          setCurrentFolder={setCurrentFolder}
        />
      </UpperBar>

      {/* Toggle dark mode */}
      <button
        className="dark-mode-button"
        aria-label="Toggle dark mode"
        onClick={toggleDarkMode}
      >
        <span className="icon moon">🌚</span>
        <span className="icon sun">☀</span>
      </button>

      {/* Sidebar with folders and labels */}
      <SideBar
        onComposeClick={handleComposeClick}
        onFolderSelect={handleFolderSelect}
        collapsed={sidebarCollapsed}
        setCollapsed={setSidebarCollapsed}
        selectedFolder={currentFolder}
        fetchLabels={fetchLabels}
        labels={labels}
        setLabels={setLabels}
        onLabelClick={filterMailsByLabel}
      />

      {/* Compose mail modal */}
      <ComposeModal
        isOpen={isModalOpen}
        isMinimized={isMinimized}
        setIsMinimized={setIsMinimized}
        onClose={handleCloseModal}
        values={composeValues}
        setValues={setComposeValues}
        onMailSent={fetchMails}
        currentFolder={currentFolder}
      />

      {/* Main content area */}
      <div className={`main-content ${sidebarCollapsed ? 'collapsed' : ''}`}>
        {!selectedMail && (
          <CategoryBar
            selectedCategory={currentFolder}
            onCategoryChange={handleFolderSelect}
          />
        )}

        {/* Conditionally render the mail view or list */}
        {selectedMail ? (
          <MailView
            mail={selectedMail}
            onClose={handleCloseMail}
            labels={labels}
            refreshLabels={fetchLabels}
            currentFolder={currentFolder}
            onMarkAsSpam={handleMarkAsSpam}
            onMarkAsNotSpam={handleMarkAsNotSpam}
            refreshMails={fetchMails}
            handleDelete={handleDelete}
            handleRestore={handleRestore}
          />
        ) : searchEmpty ? (
          <div className="empty-message">No messages were found matching your search.</div>
        ) : (
          <MailList
            mails={isInSearchMode ? searchResults : mails}
            onToggleRead={handleToggleRead}
            onOpenMail={handleOpenMail}
            collapsed={sidebarCollapsed}
            searchTerm={searchTerm}
            currentFolder={currentFolder}
            onToggleStar={handleToggleStar}
            onMarkAsSpam={handleMarkAsSpam}
            onMarkAsNotSpam={handleMarkAsNotSpam}
            handleDelete={handleDelete}
            handleRestore={handleRestore}
          />
        )}

        {/* Pagination controls */}
        {!selectedMail && !isInSearchMode && !searchEmpty && (
          <div className="pagination">
            <button
              onClick={() => setPage(prev => Math.max(prev - 1, 1))}
              disabled={page === 1}
            >
              Previous
            </button>
            <span style={{ margin: "0 10px" }}>Page {page}</span>
            <button
              onClick={() => setPage(prev => prev + 1)}
              disabled={mails.length < 50}
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Inbox;
