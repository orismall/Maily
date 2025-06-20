import React from 'react';
import MailItem from './MailItem';
import './MailList.css';

// Displays a list of emails by rendering MailItem components
const MailList = ({
  mails,
  onToggleRead,
  onToggleStar,
  onOpenMail,
  collapsed,
  searchTerm,
  onMarkAsSpam,
  onMarkAsNotSpam,
  currentFolder,
  handleDelete,
  handleRestore
}) => {
  // Show message when there are no mails to display
  if (!mails || mails.length === 0) {
    return <div className={`mail-list ${collapsed ? 'collapsed' : ''}`}>No mails found.</div>;
  }

  // Render a list of MailItem components, one for each mail
  return (
    <div className={`mail-list ${collapsed ? 'collapsed' : ''}`}>
      {mails.map(item => (
        <MailItem
          key={item.mail._id} // ✅ changed from mailId to _id
          mail={item}
          onToggleRead={onToggleRead}
          onToggleStar={onToggleStar}
          onOpenMail={onOpenMail}
          searchTerm={searchTerm}
          currentFolder={currentFolder}
          onMarkAsSpam={onMarkAsSpam}
          onMarkAsNotSpam={onMarkAsNotSpam}
          handleDelete={handleDelete}
          handleRestore={handleRestore}
        />
      ))}
    </div>
  );
};

export default MailList;
