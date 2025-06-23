// routes/trash.js
const express = require('express');
const router = express.Router();
const trashController = require('../controllers/trash');

// Get all mails in trash
router.get('/trash', trashController.getTrash);

// Get a specific trashed mail by its ID
router.get('/trash/:id', trashController.getTrashMailById);

// Restore a mail from trash
router.post('/trash/:id/restore', trashController.restoreFromTrash);

// Permanently delete a mail from trash
router.delete('/trash/:id', trashController.permanentlyDeleteFromTrash);

module.exports = router;
