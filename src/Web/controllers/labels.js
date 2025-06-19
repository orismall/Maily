const Label = require('../models/labels');
const isLoggedIn = require('../utils/isLoggedIn');

// Create a new label
exports.createLabel = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const { name, color } = req.body;
    const userId = req.user.userId;

    if (!name) {
      return res.status(400).json({ error: 'Name is required' });
    }

    const existing = await Label.findOne({ name, userId });
    if (existing) {
      return res.status(409).json({ error: 'Label already exists' });
    }

    const newLabel = new Label({ userId, name, color });
    await newLabel.save();

    res.status(201).location(`/api/labels/${newLabel.id}`).end();
  });
};

// Get label by ID
exports.getLabelById = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = parseInt(req.params.id);
    const userId = req.user.userId;

    const label = await Label.findOne({ id: labelId, userId });
    if (!label) {
      return res.status(404).json({ error: 'Label not found' });
    }

    res.json({ id: label.id, name: label.name });
  });
};

// Edit label
exports.editLabelById = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = parseInt(req.params.id);
    const userId = req.user.userId;

    const updated = await Label.findOneAndUpdate(
      { id: labelId, userId },
      { $set: req.body },
      { new: true }
    );

    if (!updated) {
      return res.status(404).json({ error: 'Label not found or not updated' });
    }

    res.status(204).end();
  });
};

// Delete label
exports.deleteLabelById = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = parseInt(req.params.id);
    const userId = req.user.userId;

    const label = await Label.findOneAndDelete({ id: labelId, userId });
    if (!label) {
      return res.status(404).json({ error: 'Label not found' });
    }

    // Remove label ID from all mails manually if needed in memory or propagate in DB
    const user = req.user;
    const allMails = [...user.inbox, ...user.sent];
    for (const item of allMails) {
      if (item && item.mail?.labels) {
        item.mail.labels = item.mail.labels.filter(id => id !== labelId);
      }
    }

    res.status(204).end();
  });
};

// Get all labels
exports.getAllLabels = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const labels = await Label.find({ userId });

    const formatted = labels.map(label => ({
      id: label.id,
      name: label.name,
      color: label.color
    }));

    res.json(formatted);
  });
};
