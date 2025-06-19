import React from 'react';
import './ComposeMail.css';
import { useNavigate } from 'react-router-dom';

const ComposeMail = ({ values, setValues, onClose, onMinimize, onMailSent, currentFolder }) => {
  const navigate = useNavigate();

  // Error message from the server
  const [serverError, setError] = React.useState('');

  // Toast notification state
  const [showToast, setShowToast] = React.useState(false);

  // Handles sending the email (or sending a draft)
  const handleSend = async (e) => {
    e.preventDefault();

    // Split comma-separated receivers and clean them up
    const receivers = values.receiver
      .split(',')
      .map(email => email.trim().toLowerCase())
      .filter(email => email.length > 0);

    // Require at least one valid recipient
    if (receivers.length === 0) {
      alert('Please specify at least one recipient');
      return;
    }

    // Warn if subject and body are empty
    if (!values.subject.trim() && !values.content.trim()) {
      const confirmed = window.confirm('Send this message without a subject or text in the body?');
      if (!confirmed) return;
    }

    // Check if the user is authenticated
    const session = JSON.parse(localStorage.getItem('session'));
    if (!session || !session.token) {
      alert("You are not logged in.");
      navigate("/Login");
      return;
    }

    try {
      let res;

      if (values.id) {
        // If this is a saved draft, send it
        res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/drafts/${values.id}/send`, {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${session.token}`,
            "Content-Type": "application/json"
          }
        });
      } else {
        // Otherwise, send a new email
        res = await fetch(`http://localhost:${process.env.REACT_APP_WEB_PORT}/api/mails`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${session.token}`
          },
          body: JSON.stringify({
            receiver: receivers,
            subject: values.subject,
            content: values.content
          })
        });
      }

      // If the mail was sent successfully
      if (res.status === 201) {
        setShowToast(true); // Show confirmation toast
        setTimeout(() => setShowToast(false), 3000); // Hide toast after 3 seconds
        setValues({ receiver: "", subject: "", content: "" }); // Clear compose fields
        onMailSent(); // Refresh parent (e.g., refresh inbox)
        onClose(); // Close the modal
        return;
      }

      // Show server error if the request failed
      const { error } = await res.json();
      setError(error);
    } catch {
      // Handle network failure
      setError("Network error - please try again");
    }
  };

  return (
    <>
      <div className="compose-body">
        {/* Show any server error */}
        {serverError && <div className="compose-error">{serverError}</div>}

        {/* Recipient input */}
        <input
          className="compose-input"
          type="text"
          placeholder="To (comma separated)"
          value={values.receiver}
          onChange={(e) => setValues({ ...values, receiver: e.target.value })}
        />

        {/* Subject input */}
        <input
          className="compose-input"
          type="text"
          placeholder="Subject"
          value={values.subject}
          onChange={(e) => setValues({ ...values, subject: e.target.value })}
        />

        {/* Message body input */}
        <textarea
          className="compose-textarea"
          placeholder="Write your message..."
          value={values.content}
          onChange={(e) => setValues({ ...values, content: e.target.value })}
        />

        {/* Send button */}
        <div className="compose-footer">
          <button className="send-button" onClick={handleSend}>Send</button>
        </div>
      </div>

      {/* Toast for success feedback */}
      {showToast && (
        <div className="toast-container position-fixed bottom-0 end-0 p-3">
          <div className="toast show" role="alert">
            <div className="toast-header bg-success text-white">
              <strong className="me-auto">Mail App</strong>
              <button type="button" className="btn-close btn-close-white" onClick={() => setShowToast(false)}></button>
            </div>
            <div className="toast-body">Email sent successfully!</div>
          </div>
        </div>
      )}
    </>
  );
};

export default ComposeMail;
