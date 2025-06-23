import ComposeMail from './ComposeMail';

const ComposeModal = ({
  isOpen,          // Whether the modal is open
  isMinimized,     // Whether the modal is minimized
  setIsMinimized,  // Setter to change minimized state
  onClose,         // Callback when modal is closed
  values,          // Mail form values (receiver, subject, content, id)
  setValues,       // Setter to update mail form values
  onMailSent,      // Callback when mail is successfully sent or draft saved
  currentFolder    // Currently active mail folder
}) => {

  // Minimize the modal
  const handleMinimize = () => setIsMinimized(true);

  // Maximize the modal
  const handleMaximize = () => setIsMinimized(false);

  // Close the modal and save mail as a draft (or update existing draft)
  const handleClose = async () => {
    const session = JSON.parse(localStorage.getItem('session'));
    const isEditingDraft = !!values.id; // Check if it's an existing draft

    const url = isEditingDraft
      ? `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/drafts/${values.id}`
      : `http://localhost:${process.env.REACT_APP_WEB_PORT}/api/drafts`;

    const method = isEditingDraft ? "PATCH" : "POST";

    try {
      // Save or update the draft on close
      await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${session?.token || ''}`
        },
        body: JSON.stringify({
          receiver: values.receiver,
          subject: values.subject,
          content: values.content,
          action: "close",
        }),
      });

      onMailSent(); // Refresh mail list
    } catch (err) {
      console.error("Error saving draft:", err);
    } finally {
      // Reset state and close modal
      setValues({ receiver: "", subject: "", body: "" });
      setIsMinimized(false);
      onClose();
    }
  };

  // Close the modal without saving as a draft
  const handleDiscardMessage = () => {
    const confirmDiscard = window.confirm("Are you sure you want to discard this message?");
    if (!confirmDiscard) return;

    setValues({ receiver: "", subject: "", body: "" });
    setIsMinimized(false);
    onClose();
  };

  // If modal is not open, render nothing
  if (!isOpen) return null;

  // Render minimized version of the modal
  if (isMinimized) {
    return (
      <div className="compose-minimized" onClick={handleMaximize}>
        <div className="compose-minimized-bar">
          <span className="compose-minimized-title">New Message</span>
          <div className="compose-minimized-actions">
            {/* Maximize button */}
            <button
              className="compose-dark-icons"
              onClick={(e) => { e.stopPropagation(); handleMaximize(); }}
            >▲</button>

            {/* Close button (saves as draft) */}
            <button
              className="compose-dark-icons"
              onClick={(e) => { e.stopPropagation(); handleClose(); }}
            >✕</button>
          </div>
        </div>
      </div>
    );
  }

  // Render full compose modal
  return (
    <div className="compose-backdrop" onClick={handleMinimize}>
      <div className="compose-wrapper" onClick={(e) => e.stopPropagation()}>
        <div className="compose-container">

          {/* Modal Header with controls */}
          <div className="compose-header">
            <button className="close-button" onClick={handleDiscardMessage}>←</button>
            <span>New Message</span>
            <div>
              <button className="minimize-button" onClick={handleMinimize}>-</button>
              <button className="close-button" onClick={handleClose}>✕</button>
            </div>
          </div>

          {/* ComposeMail form component */}
          <ComposeMail
            onClose={onClose}
            onMinimize={handleMinimize}
            values={values}
            setValues={setValues}
            onMailSent={onMailSent}
            currentFolder={currentFolder}
          />
        </div>
      </div>
    </div>
  );
};

export default ComposeModal;
