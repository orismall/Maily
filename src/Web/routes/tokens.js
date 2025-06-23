const express = require('express');
const router = express.Router();
const tokensController = require('../controllers/tokens');

// Login user
router.post('/tokens', tokensController.login);

module.exports = router;