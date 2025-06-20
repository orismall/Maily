import React, { useState, useEffect, useRef } from 'react';
import { MdReportGmailerrorred } from 'react-icons/md';
import { FaTrash } from 'react-icons/fa';

import './MailView.css';

// Main mail view component for reading, replying, forwarding, labeling, and managing a mail
const MailView = ({
  mail,
  onClose,
  labels,
  refreshLabels,
  refreshMails,
  currentFolder,
  onMarkAsSpam,
  onMarkAsNotSpam,
  handleDelete,
  handleRestore
}) => {
  // State for reply and forward functionality
  const [isReplying, setIsReplying] = useState(false);
  const [isForwarding, setIsForwarding] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [forwardContent, setForwardContent] = useState('');
  const [forwardTo, setForwardTo] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // State for label management
  const [attachedLabels, setAttachedLabels] = useState(mail.labels || []);
  const [showDropdown, setShowDropdown] = useState(false);
  const [labelList, setLabelList] = useState(labels || []);
  const dropdownRef = useRef();

  // Session storage for auth
  const session = JSON.parse(localStorage.getItem('session'));

  // Keep label list in sync with props
  useEffect(() => {
    setLabelList(labels);
  }, [labels]);

  // Close label dropdown if clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const [userEmail, setUserEmail] = useState('');

  // Fetch current user email
  useEffect(() => {
    if (!session.userId) return;
    const fetchUser = async () => {
      try {
        const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/users/${session.userId}`, {
          headers: {
            'User-Id': session.userId,
            'Authorization': `Bearer ${session.token}`,
          },
        });
        if (!res.ok) throw new Error('Failed to fetch user');
        const data = await res.json();
        setUserEmail(data.email || '');
      } catch (err) {
        console.error(err);
      }
    };
    fetchUser();
  }, [session.userId, session.token]);

  // Don't render anything if mail is null
  if (!mail) return null;

  // Format date in Hebrew locale
  const formatDate = (date) => {
    const dateString = new Date(date);
    return dateString.toLocaleString('he-IL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  // Enable reply mode
  const handleReply = () => {
    setIsReplying(true);
    setIsForwarding(false);
    setReplyContent('');
    setErrorMessage('');
  };

  // Enable forward mode and prefill forward content
  const handleForward = () => {
    setIsForwarding(true);
    setIsReplying(false);
    setForwardTo('');
    setErrorMessage('');
    setForwardContent(
      `\n\n----------Forwarded message----------\nFrom: ${mail.sender} \nDate: ${formatDate(mail.date)} \nSubject: ${mail.subject} \nTo: ${mail.receiver ? mail.receiver.join(', ') : userEmail} \n\n${mail.content}`
    );
  };

  // Send reply to original sender
  const handleSendReply = async () => {
    setErrorMessage('');
    const newReply = replyContent.trim();
    if (!newReply) {
      setErrorMessage("Reply content cannot be empty.");
      return;
    }

    const fullReply = `${newReply}\n\n---------------------------------------\nFrom: ${mail.sender} \nDate: ${formatDate(mail.date)} \nSubject: ${mail.subject} \nTo: ${mail.receiver ? mail.receiver.join(', ') : userEmail} \n\n${mail.content}`;
    const replyMail = {
      sender: userEmail,
      receiver: [mail.sender],
      subject: "Re: " + mail.subject,
      content: fullReply,
    };

    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`,
        },
        body: JSON.stringify(replyMail),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to send reply.');
      }

      setIsReplying(false);
      setReplyContent('');
      refreshMails();
      onClose();
    } catch (error) {
      setErrorMessage("Failed to send reply: " + error.message);
    }
  };

  // Send forwarded email to provided recipients
  const handleSendForward = async () => {
    setErrorMessage('');
    const forwardText = forwardContent.trim();
    const forwardRecipients = forwardTo.split(',').map(email => email.trim()).filter(Boolean);

    if (!forwardText || forwardRecipients.length === 0) {
      setErrorMessage("Please provide both recipients and content for the forwarded message.");
      return;
    }

    const forwardMail = {
      sender: userEmail,
      receiver: forwardRecipients,
      subject: "Fwd: " + mail.subject,
      content: forwardText,
    };

    try {
      const res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`,
        },
        body: JSON.stringify(forwardMail),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to send forward.');
      }

      setIsForwarding(false);
      setForwardContent('');
      setForwardTo('');
      refreshMails();
      onClose();
    } catch (error) {
      setErrorMessage("Failed to send: " + error.message);
    }
  };

  // Discard reply/forward state and clear fields
  const handleDiscard = () => {
    const confirmDiscard = window.confirm("Are you sure you want to discard this message?");
    if (!confirmDiscard) return;

    setIsReplying(false);
    setIsForwarding(false);
    setReplyContent('');
    setForwardContent('');
    setForwardTo('');
    setErrorMessage('');
  };

  // Add/remove label from mail
  const toggleLabel = async (labelId) => {
    const hasLabel = attachedLabels.includes(labelId);
    const url = `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails/${mail.mailId}/labels/${labelId}`;
    const method = hasLabel ? 'DELETE' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${session.token}`,
        },
      });

      if (!res.ok) throw new Error('Failed to toggle label');

      setAttachedLabels(prev =>
        hasLabel ? prev.filter(id => id !== labelId) : [...prev, labelId]
      );
    } catch (err) {
      console.error(err);
    }
  };
console.log("MailView mailId", mail.mailId);

  return (
    <div className="mail-view">
      {/* Header and label controls */}
      <div className="mail-header">
        <button onClick={onClose} className="back-button">← Back</button>
        {currentFolder !== 'trash' && currentFolder !== 'spam' && (
          <div className="label-dropdown-wrapper" ref={dropdownRef}>
            <button className="label-toggle" onClick={() => setShowDropdown(prev => !prev)}>
              🔖 Labels
            </button>
            {showDropdown && (
              <div className="label-dropdown">
                {labelList.map(label => {
                  const isActive = attachedLabels.includes(label.id);
                  return (
                    <div
                      key={label.id}
                      className={`label-option ${isActive ? 'selected' : ''}`}
                      onClick={() => toggleLabel(label.id)}
                    >
                      <span className="label-color-circle" style={{ backgroundColor: label.color }}></span>
                      {label.name}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Mail content */}
      <h2>{mail.subject}</h2>
      <p><strong>From:</strong> {mail.sender}</p>
      <p><strong>To:</strong> {mail.receiver ? mail.receiver.join(', ') : userEmail}</p>
      <p><strong>Date:</strong> {formatDate(mail.date)}</p>
      <hr style={{ margin: "20px 0", borderTop: "1px solid #ccc" }} />
      <div className="mail-body">{mail.content}</div>

      {/* Footer actions: reply, forward, spam, delete, restore */}
      <div className="mail-view-footer">
        <div className="left-buttons">
          {currentFolder !== 'trash' && (
            <>
              <button className="mail-action-button" onClick={handleReply}>Reply</button>
              <button className="mail-action-button" onClick={handleForward}>Forward</button>
            </>
          )}
        </div>

        <div className="right-buttons">
          {currentFolder === 'spam' ? (
            <button className="mail-action-button spam-button" onClick={() => onMarkAsNotSpam(mail.mailId)}>
              <MdReportGmailerrorred style={{ marginRight: '4px' }} />
              Not Spam
            </button>
          ) : currentFolder === 'trash' ? (
            <button className="mail-action-button restore-button" onClick={() => handleRestore(mail.mailId)}>
              Restore
            </button>
          ) : (
            <button className="mail-action-button spam-button" onClick={() => onMarkAsSpam(mail.mailId)}>
              <MdReportGmailerrorred style={{ marginRight: '4px' }} />
              Mark as Spam
            </button>
          )}

          <button className="delete-button"onClick={() => handleDelete(mail.mailId, currentFolder)}>
            <FaTrash style={{ marginRight: '4px' }} />
            {currentFolder === 'trash' || currentFolder === 'spam' ? 'Delete Permanently' : 'Move to Trash'}
          </button>
        </div>
      </div>

      {/* Compose box for reply/forward */}
      {(isReplying || isForwarding) && (
        <div className="compose-box">
          <div className="compose-top-bar">
            <h3 className="compose-title">{isReplying ? `Reply to ${mail.sender}` : 'Forward Message'}</h3>
          </div>

          {isForwarding && (
            <input
              type="text"
              placeholder="To (comma separated)"
              value={forwardTo}
              onChange={(e) => setForwardTo(e.target.value)}
            />
          )}

          <textarea
            value={isReplying ? replyContent : forwardContent}
            onChange={(e) => isReplying ? setReplyContent(e.target.value) : setForwardContent(e.target.value)}
            rows={8}
          />

          <div className="compose-actions">
            <button className="send-button" onClick={isReplying ? handleSendReply : handleSendForward}>
              Send
            </button>
            {errorMessage && <div className="error-message">{errorMessage}</div>}
            <button className="icon-button trash-button" onClick={handleDiscard}>🗑️</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default MailView;
