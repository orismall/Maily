import React from 'react';
import './MailItem.css';
import { MdMarkEmailRead, MdMarkEmailUnread } from 'react-icons/md';
import { FaStar, FaRegStar, FaTrash } from 'react-icons/fa';
import { MdReportGmailerrorred } from 'react-icons/md';

// Represents a single mail preview item in the list
const MailItem = ({
  mail,
  currentFolder,
  onToggleRead,
  onToggleStar,
  onMarkAsSpam,
  onMarkAsNotSpam,
  handleDelete,
  onOpenMail,
  searchTerm,
  handleRestore
}) => {
  const { mail: mailData, isRead } = mail;
  const isStarred = mail.isStarred;

  // Toggle read/unread without opening the mail
  const handleToggleRead = (e) => {
    e.stopPropagation();
    onToggleRead(mailData.mailId, !isRead);
  };

  // When clicking the mail item, open it and mark as read if needed
  const handleClick = () => {
    if (!isRead) {
      onToggleRead(mailData.mailId, true);
    }
    onOpenMail(mailData);
  };

  // Highlight matching search term in sender/subject/content
  const highlightText = (text) => {
    if (!searchTerm) return text;
    const regex = new RegExp(`(${searchTerm})`, 'gi');
    return text.split(regex).map((part, index) =>
      regex.test(part) ? <mark key={index}>{part}</mark> : part
    );
  };

  // Format date nicely (Hebrew locale, full timestamp)
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

  return (
    <div className={`mail-item ${isRead ? '' : 'unread'}`} onClick={handleClick}>
      {/* Star and Draft section, not shown for trash/spam */}
      {currentFolder !== 'trash' && currentFolder !== 'spam' && (
        <div className="mail-item-checkbox">
          <div className="checkbox-star-draft">
            <button
              className={`star-button ${isStarred ? 'starred' : ''}`}
              onClick={(e) => {
                e.stopPropagation();
                onToggleStar(mailData.mailId, mailData.type);
              }}
              title={isStarred ? 'Unstar' : 'Star'}
            >
              {isStarred ? <FaStar /> : <FaRegStar />}
            </button>

            {/* Show "Draft" label only in drafts folder */}
            {mailData.type === 'draft' && (
              <span className="draft-label">Draft</span>
            )}
          </div>
        </div>
      )}

      {/* Main mail info section: sender, subject/content snippet, date */}
      <div className="mail-item-details">
        <span className="mail-item-sender">{highlightText(mailData.sender)}</span>
        <span className={`mail-item-subject ${!isRead ? 'unread-subject' : ''}`}>
          <strong>{highlightText(mailData.subject)}</strong>
          {mailData.content && (
            <>
              {" - "}
              {highlightText(
                mailData.content.length > 70
                  ? mailData.content.slice(0, 70) + "..."
                  : mailData.content
              )}
            </>
          )}
        </span>
        <span className="mail-item-date">{formatDate(mailData.date)}</span>

        {/* Conditional actions based on folder */}
        {currentFolder === 'trash' ? (
          <>
            {/* Restore from Trash */}
            <button
              className="icon-button restore-button"
              onClick={(e) => {
                e.stopPropagation();
                handleRestore(mailData.mailId);
              }}
              title="Restore"
            >
              🔄
            </button>

            {/* Permanently delete */}
            <button
              className="icon-button delete-button"
              onClick={(e) => {
                e.stopPropagation();
                handleDelete(mailData.mailId, currentFolder);
              }}
              title="Delete Permanently"
            >
              <FaTrash />
            </button>
          </>
        ) : currentFolder === 'drafts' ? (
          <>
            {/* Delete draft */}
            <button
              className="icon-button delete-button"
              onClick={(e) => {
                e.stopPropagation();
                handleDelete(mailData.mailId, currentFolder);
              }}
              title="Delete Draft"
            >
              <FaTrash />
            </button>
          </>
        ) : (
          <>
            {/* Toggle read/unread status */}
            <button
              className={`toggle-read-btn ${isRead ? 'read' : 'unread'}`}
              onClick={handleToggleRead}
              title={isRead ? 'Mark as Unread' : 'Mark as Read'}
            >
              {isRead ? <MdMarkEmailRead /> : <MdMarkEmailUnread />}
            </button>

            {/* Spam/Not Spam button */}
            <button
              className={`icon-button spam-button ${currentFolder === 'spam' ? 'not-spam' : 'is-spam'}`}
              onClick={(e) => {
                e.stopPropagation();
                if (currentFolder === 'spam') {
                  onMarkAsNotSpam(mailData.mailId, mailData.type);
                } else {
                  onMarkAsSpam(mailData.mailId, mailData.type);
                }
              }}
              title={currentFolder === 'spam' ? "Mark as Not Spam" : "Mark as Spam"}
            >
              <MdReportGmailerrorred />
            </button>

            {/* Move to Trash or Delete */}
            <button
              className="icon-button delete-button"
              onClick={(e) => {
                e.stopPropagation();
                handleDelete(mailData.mailId, currentFolder);
              }}
              title={currentFolder === 'spam' ? "Delete Permanently" : "Move to Trash"}
            >
              <FaTrash />
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default MailItem;
