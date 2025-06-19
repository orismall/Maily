const User = require('../models/users');
const { Mutex } = require('async-mutex');

const userCreationMutex = new Mutex();

// Create a new user
exports.createUser = async (req, res) => {
    const { email, password, confirmPassword, firstName, lastName, gender, birthdate, avatar } = req.body
    
    // Validate required fields
    if (!email)
        return res.status(400).json({ error: 'Email is missing' })
    if (!password)
        return res.status(400).json({ error: 'Password is missing' })
    if (!firstName)
        return res.status(400).json({ error: 'First name is missing' })
    if (!lastName)
        return res.status(400).json({ error: 'Last name is missing' })
    if (!gender)
        return res.status(400).json({ error: 'Gender is missing' })
    if (!birthdate)
        return res.status(400).json({ error: 'Birthdate is missing' })
    if (!avatar)
        return res.status(400).json({ error: 'Image is missing' })

    // Email format validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        return res.status(400).json({ error: 'Invalid email format' });
    }

    // Check if the password is strong - should contain at least 8 characters, one lowercase, one uppercase, one number and one special character 
    const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!strongPasswordRegex.test(password)) {
        return res.status(400).json({ error: 'Password is not strong enough' });
    }

    // Check if passwords match
    if (password !== confirmPassword) {
        return res.status(409).json({ error: 'Passwords do not match' })
    }
    // Check if gender is valid
    const validGenders = ['male', 'female', 'other'];
    if (!validGenders.includes(gender.toLowerCase())) {
        return res.status(400).json({ error: 'Invalid gender' });
    }

    // Check if birthdate is valid
    const birthDateObject = new Date(birthdate);
    if (isNaN(birthDateObject.getTime())) {
        return res.status(400).json({ error: 'Invalid birthdate' });
    }
    // Check if birthdate is in the future
    const now = new Date();
    if (birthDateObject > now) {
        return res.status(400).json({ error: 'Invalid birthdate' });
    }

    // Validate avatar format if provided
    if (avatar) {
        if (!avatar.startsWith("data:image/jpeg") &&
            !avatar.startsWith("data:image/jpg") &&
            !avatar.startsWith("data:image/png")) {
            return res.status(400).json({ error: 'Invalid avatar format - must end with .jpg, .jpeg or .png' });
        }
    }

    // Check if email already exists
        if (User.findByEmail(email)) {
            throw { status: 409, message: 'Email already exists' };
        }

    // Added mutex to avoid creating users with the same id
    await userCreationMutex.runExclusive(() => {
        // Create and return new user
        const user = User.createUser(email, password, confirmPassword, firstName, lastName, gender, birthdate, avatar || null)
        return user;
    }).then((user) => {
        res.status(201).location(`/api/users/${user.id}`).end();
    }).catch(err => {
        if (err.status && err.message) {
            res.status(err.status).json({ error: err.message });
        } else {
            res.status(500).json({ error: 'Internal Server Error' });
        }
    });
}

// Get user details by ID (excluding password)
exports.getUserById = (req, res) => {
    // Get the requested user ID from the URL parameters
    const id = parseInt(req.params.id);
    // Get the authenticated user ID from the HTTP header
    const authenticatedId = parseInt(req.header("User-Id"));
    // Check if the authenticated user is trying to access another user's data
    if (id !== authenticatedId) {
        return res.status(403).json({ error: "Access denied: you can only view your own profile." });
    }
    const user = User.getUserInfoById(id);
    if (!user) {
        return res.status(404).json({ error: "User not found" });
    }
    // Return the user data
    res.json(user);
};
