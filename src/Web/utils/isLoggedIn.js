const User = require('../models/users');
const tokenService = require('../services/tokenService');

// Accepts req, res, and a callback to execute on success
const isLoggedIn = async (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(403).json({ error: "Token required" });
  }

  const token = authHeader.split(" ")[1];

  try {
    const decoded = tokenService.verifyToken(token);
    const user = await User.findById(decoded.userId);

    if (!user) return res.status(401).json({ error: "Invalid user" });

    req.user = user;
    next();
  } catch (err) {
    res.status(401).json({ error: "Invalid or expired token" });
  }
};

module.exports = isLoggedIn;
