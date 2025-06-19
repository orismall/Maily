const User = require('../models/users');
const { Mutex } = require('async-mutex');

const userCreationMutex = new Mutex();

// Create a new user
exports.createUser = async (req, res) => {
    const { email, password, confirmPassword, firstName, lastName, gender, birthdate, avatar } = req.body;

    // Basic validations
    if (!email) return res.status(400).json({ error: 'Email is missing' });
    if (!password) return res.status(400).json({ error: 'Password is missing' });
    if (!firstName) return res.status(400).json({ error: 'First name is missing' });
    if (!lastName) return res.status(400).json({ error: 'Last name is missing' });
    if (!gender) return res.status(400).json({ error: 'Gender is missing' });
    if (!birthdate) return res.status(400).json({ error: 'Birthdate is missing' });
    if (!avatar) return res.status(400).json({ error: 'Image is missing' });

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        return res.status(400).json({ error: 'Invalid email format' });
    }

    const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!strongPasswordRegex.test(password)) {
        return res.status(400).json({ error: 'Password is not strong enough' });
    }

    if (password !== confirmPassword) {
        return res.status(409).json({ error: 'Passwords do not match' });
    }

    const validGenders = ['male', 'female', 'other'];
    if (!validGenders.includes(gender.toLowerCase())) {
        return res.status(400).json({ error: 'Invalid gender' });
    }

    const birthDateObject = new Date(birthdate);
    if (isNaN(birthDateObject.getTime()) || birthDateObject > new Date()) {
        return res.status(400).json({ error: 'Invalid birthdate' });
    }

    if (avatar && !avatar.startsWith("data:image/jpeg") &&
        !avatar.startsWith("data:image/jpg") &&
        !avatar.startsWith("data:image/png")) {
        return res.status(400).json({ error: 'Invalid avatar format' });
    }

    try {
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(409).json({ error: 'Email already exists' });
        }

        // Use mutex to prevent simultaneous same-userId generation
        const newUser = await userCreationMutex.runExclusive(async () => {
            return await User.create({
                email,
                password,
                confirmPassword,
                firstName,
                lastName,
                gender,
                birthdate,
                avatar
            });
        });

        res.status(201).location(`/api/users/${newUser.userId}`).end();
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
    }
};

// Get user info by userId (excluding inbox/sent/password)
exports.getUserById = async (req, res) => {
    const id = parseInt(req.params.id);
    const authenticatedId = parseInt(req.header("User-Id"));

    if (id !== authenticatedId) {
        return res.status(403).json({ error: "Access denied: you can only view your own profile." });
    }

    try {
        const user = await User.findOne({ userId: id }).lean();
        if (!user) {
            return res.status(404).json({ error: "User not found" });
        }

        // Remove sensitive or bulky fields
        const { password, confirmPassword, mails: { inbox, sent, ...otherMails }, mails, ...rest } = user;

        res.json({ ...rest, mails: otherMails });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: 'Internal Server Error' });
    }
};
