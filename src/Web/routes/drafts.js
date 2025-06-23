// routes/drafts.js
const express = require('express');
const router = express.Router();
const draftsController = require('../controllers/drafts');

// Create a new draft
router.post('/drafts', draftsController.createDraft);

// POST /api/drafts/:id/send - Send a draft as an email
router.post('/drafts/:id/send', draftsController.sendDraftAsMail);

// Get all drafts for the authenticated user
router.get('/drafts', draftsController.getDrafts);

// Get a specific draft by ID
router.get('/drafts/:id', draftsController.getDraftById);

// Update a draft by ID
router.patch('/drafts/:id', draftsController.updateDraft);

// Delete a draft by ID
router.delete('/drafts/:id', draftsController.deleteDraft);

module.exports = router;
