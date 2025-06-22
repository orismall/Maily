const express = require('express');
const router = express.Router();
const labelsController = require('../controllers/labels');

// GET all labels
router.get('/labels', labelsController.getAllLabels);
    
// POST new label
router.post('/labels', labelsController.createLabel);

// GET label by ID
router.get('/labels/:id', labelsController.getLabelById);

// PATCH label by ID
router.patch('/labels/:id', labelsController.editLabelById);

// DELETE label by ID
router.delete('/labels/:id', labelsController.deleteLabelById);

// Get all mails associated with a label
router.get('/labels/:id/mails', labelsController.getMailsByLabel);

module.exports = router;