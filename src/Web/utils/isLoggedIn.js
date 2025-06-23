const jwt = require('jsonwebtoken');
const User = require('../models/users');
const JWT_SECRET = 'secretkey';

const isLoggedIn = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(403).json({ error: "Token required" });
  }
  const token = authHeader.split(" ")[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    const userId = decoded.userId;
    const user = User.getUserById(userId);
    if (!user) {
      return res.status(401).json({ error: "Invalid user" });
    }
    req.user = user; 
    next();
  } catch (err) {
    return res.status(401).json({ error: "Invalid or expired token" });
  }
};

module.exports = isLoggedIn;
