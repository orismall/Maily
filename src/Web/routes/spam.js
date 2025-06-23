const express = require('express');
const router = express.Router();
const spamController = require('../controllers/spam');

// Get all spam mails for the authenticated user
router.get('/', spamController.getSpam);

// Get a specific spam mail by its ID
router.get('/:id', spamController.getSpamById);

// Permanently delete a spam mail by its ID
router.delete('/:id', spamController.deleteSpam);

// Restore a spam mail to the inbox by its ID
router.patch('/:id/restore', spamController.restoreFromSpam);

// Mark a mail as spam and update blacklist accordingly
router.patch('/:id/mark-as-spam', spamController.markAsSpam);

// Mark a mail from spam as not spam and update blacklist accordingly
router.patch('/:id/mark-as-not-spam', spamController.markAsNotSpam);

module.exports = router;
