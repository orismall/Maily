const { sendCommandToServer } = require('../models/blacklist');
const User = require('../models/users');
const isLoggedIn = require('../utils/isLoggedIn');

// Extract status code from response
function extractStatusCode(response) {
  // Get 3 first letters from response
  const match = response.trim().match(/^(\d{3})/);
  // Check if there is a match to a status code and return it, otherwise return 500
  return match ? parseInt(match[1]) : 500;
}

// Add a url to the blacklist
exports.addToBlacklist = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const user = req.user;
    const { url } = req.body;
    if (!url) {
      return res.status(400).json({ error: 'URL is required' });
    }
    try {
      const response = await sendCommandToServer(`POST ${url}`);
      const statusCode = extractStatusCode(response);
      res.status(statusCode).end();
    } catch (err) {
      res.status(500).end();
    }
  });
};

// Remove a url from the blacklist
exports.removeFromBlacklist = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const user = req.user;
    const url = req.params.id;
    if (!url) {
      return res.status(400).json({ error: 'URL is required' });
    }
    try {
      const response = await sendCommandToServer(`DELETE ${url}`);
      const statusCode = extractStatusCode(response);
      res.status(statusCode).end();
    } catch (err) {
      res.status(500).end();
    }
  });
};
