const isLoggedIn = require('../utils/isLoggedIn');
const labelService = require('../services/labelService');
const User = require('../models/users');

exports.createLabel = (req, res) => {
  isLoggedIn(req, res, async () => {
    const { name, color } = req.body;
    const userId = req.user._id;
    if (!name) return res.status(400).json({ error: 'Name is required' });
    const existing = await labelService.findLabelByName(name, userId);
    if (existing) return res.status(409).json({ error: 'Label already exists' });
    const newLabel = await labelService.createLabel(userId, name, color);
    res.status(201).location(`/api/labels/${newLabel._id}`).end();
  });
};

exports.getLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = req.params.id;
    const userId = req.user._id;
    const label = await labelService.findLabelById(labelId, userId);
    if (!label) return res.status(404).json({ error: 'Label not found' });
    res.json({ id: label._id, name: label.name, color: label.color });
  });
};

exports.editLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = req.params.id;
    const userId = req.user._id;
    const updated = await labelService.updateLabelById(labelId, userId, req.body);
    if (!updated) return res.status(404).json({ error: 'Label not found or not updated' });
    res.status(204).end();
  });
};

exports.deleteLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = req.params.id;
    const userId = req.user._id;
    const deleted = await labelService.deleteLabelById(labelId, userId);
    if (!deleted) return res.status(404).json({ error: 'Label not found' });
    res.status(204).end();
  });
};

exports.getAllLabels = (req, res) => {
  isLoggedIn(req, res, async () => {
    const userId = req.user._id;
    const labels = await labelService.getAllLabels(userId);
    res.json(labels);
  });
};

exports.getMailsByLabel = async (req, res) => {
  isLoggedIn(req, res, async () => {
    const userId = req.user._id;
    const labelId = req.params.id;

    try {
      const user = await User.findById(userId);
      if (!user) return res.status(404).json({ error: 'User not found' });

      // Collect all trash/spam mail IDs
      const trashIds = new Set(user.mails.trash.map(m => m.mail._id.toString()));
      const spamIds = new Set(user.mails.spam.map(m => m.mail._id.toString()));

      const allMails = Object.values(user.mails).flat();
      const seen = new Set();
      const filtered = [];

      for (const item of allMails) {
        const mailId = item.mail._id.toString();
        const isLabeled = item.mail.labels?.some(l => l.toString() === labelId);
        const isTrashedOrSpam = trashIds.has(mailId) || spamIds.has(mailId);

        if (isLabeled && !isTrashedOrSpam && !seen.has(mailId)) {
          filtered.push(item);
          seen.add(mailId);
        }
      }

      // Sort and paginate
      filtered.sort((a, b) => b.mail.date - a.mail.date);
      const page = +req.query.page || 1;
      res.json(filtered.slice((page - 1) * 50, page * 50));
    } catch (err) {
      console.error('Error fetching mails by label:', err);
      res.status(500).json({ error: 'Failed to fetch labeled mails' });
    }
  });
};
