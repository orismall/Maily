const express = require('express');
const router = express.Router();
const controller = require('../controllers/blacklist');

// Add a url to blacklist
router.post('/blacklist', controller.addToBlacklist);

// delete a url from blacklist
router.delete('/blacklist/:id', controller.removeFromBlacklist);


module.exports = router;
