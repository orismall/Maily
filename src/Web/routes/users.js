const express = require('express');
const router = express.Router();
const controller = require('../controllers/users');
const isLoggedIn = require('../utils/isLoggedIn')

// Register
router.post('/users', controller.createUser);

router.get('/users/:id', isLoggedIn, controller.getUserById); 

// Get user by ID
router.get('/users/:id', controller.getUserById);



module.exports = router;
