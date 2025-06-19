const express = require('express');
const router = express.Router();
const mailsController = require('../controllers/mails');

// Sending a new mail
router.post('/mails', mailsController.sendMail);

// Get 50 last mails
router.get('/mails', mailsController.getMails);

// Get 50 last Inbox mails
router.get('/inbox', mailsController.getInboxMails);

// Get 50 last Sent mails
router.get('/sent', mailsController.getSentMails);

// Searching mails by query string
router.get('/mails/search/:query', mailsController.searchMails);

// Get mail by ID
router.get('/mails/:id', mailsController.getMailById);

// Update mail by ID
router.patch('/mails/:id', mailsController.updateMail);

// Delete mail by ID
router.delete('/mails/:id', mailsController.deleteMail);

// Add mail to label
router.post('/mails/:mailId/labels/:labelId', mailsController.addMailToLabel);

// Remove mail from label
router.delete('/mails/:mailId/labels/:labelId', mailsController.removeMailFromLabel);

router.get('/starred', mailsController.getStarred);

module.exports = router;
