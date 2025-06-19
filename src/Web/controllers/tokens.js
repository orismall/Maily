const User = require('../models/users');
const jwt = require('jsonwebtoken');
const JWT_SECRET = "secretkey"

// Login user
exports.login = (req, res) => {
    const { email, password } = req.body;

    // Check that both username and password are provided
    if (!email || !password) {
        return res.status(400).json({ error: 'Email and password are required' });
    }

    // Find the user by email
    const user = User.findByEmail(email);

    // Check if user exists and password matches
    if (!user || user.password !== password) {
        return res.status(401).json({ error: 'Invalid email or password' });
    }

    const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '1h' });
    res.json({ token, userId: user.id });
};

