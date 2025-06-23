const express = require('express');
const router = express.Router();
const controller = require('../controllers/users');

// Register
router.post('/users', controller.createUser);

// Get user by ID
router.get('/users/:id', controller.getUserById);


module.exports = router;
