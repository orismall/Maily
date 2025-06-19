const Label = require('../models/labels');
const User = require('../models/users');
const isLoggedIn = require('../utils/isLoggedIn');
const { removeLabelFromMail } = require('../models/mails');

// Creates a new label
exports.createLabel = (req, res) => {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const userId = user.id;
    const { name, color } = req.body;
    if (!name) {
      return res.status(400).json({ error: 'Name is required' });
    }
    if (Label.findLabelByName(name, userId)) {
      return res.status(409).json({ error: 'Label already exists' });
    }
    const label = Label.createLabel(userId, name, color);
    res.status(201).location(`/api/labels/${label.id}`).end();
  });
};


// Get label details by ID 
exports.getLabelById = (req, res) => {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const userId = user.id;
    const labelId = parseInt(req.params.id);
    const label = Label.getLabelById(labelId);
    // Validate that the label exists and belongs to the user
    if (!label || label.userId !== userId) {
      return res.status(404).json({ error: "Label not found" });
    }
    res.json({ id: label.id, name: label.name });
  });
};


// Edits an existing label
exports.editLabelById = (req, res) => {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const userId = user.id;
    const labelId = parseInt(req.params.id);
    const updates = req.body;
    const label = Label.getLabelById(labelId);
    if (!label || label.userId !== userId) {
      return res.status(404).json({ error: "Label not found" });
    }
    const editedLabel = Label.editLabelById(labelId, updates);
    if (!editedLabel) {
      return res.status(500).json({ error: "Failed to edit label" });
    }
    res.status(204).end();
  });
};


// Delete an existing label
exports.deleteLabelById = (req, res) => {
  isLoggedIn(req, res, () => {
    try {
      const user = req.user;
      const userId = user.id;
      const labelId = parseInt(req.params.id);
      const label = Label.getLabelById(labelId);
      if (!label || label.userId !== userId) {
        return res.status(404).json({ error: "Label not found" });
      }
      const deleted = Label.deleteLabelById(labelId);
      if (!deleted) {
        return res.status(500).json({ error: "Failed to delete label" });
      }
      // remove label from mails
      const allMails = [...user.inbox, ...user.sent];
      for (const item of allMails) {
        if (item && item.mail && Array.isArray(item.mail.labels)) {
          item.mail.labels = item.mail.labels.filter(id => id !== labelId);
        }
      }
      res.status(204).end();
    } catch (err) {
      console.error("Unexpected error in deleteLabelById:", err);
      // Let isLoggedIn handle the response
      res.status(500).json({ error: "Unexpected error deleting label" });
    }
  });
};


// Get all labels of specific user
exports.getAllLabels = (req, res) => {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const userId = user.id;
    const labels = Label.getAllLabels(userId);
    const filteredLabels = labels.map(label => ({
      id: label.id,
      name: label.name,
      color: label.color
    }));
    res.json(filteredLabels);
  });
};

