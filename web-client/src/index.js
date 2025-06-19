import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from "react-router-dom";
import App from './App';
import 'bootstrap/dist/css/bootstrap.min.css';

// Create a root DOM node to render the React app into
const root = ReactDOM.createRoot(document.getElementById('root'));
// Render the application inside a BrowserRouter to enable routing
root.render(
  <BrowserRouter>
    <App />
  </BrowserRouter>
);
