import React from 'react';

// A button to open the compose modal; changes label when collapsed
const ComposeButton = ({ onClick, collapsed = false }) => (
  <button
    type="button"
    className={`compose-button ${collapsed ? 'collapsed' : ''}`}
    onClick={onClick}
  >
    {collapsed ? '+' : '+ Compose'}
  </button>
);

export default ComposeButton;
