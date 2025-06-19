const isLoggedIn = require('../utils/isLoggedIn');
const labelService = require('../services/labelService');

exports.createLabel = (req, res) => {
  isLoggedIn(req, res, async () => {
    const { name, color } = req.body;
    const userId = req.user.userId;

    if (!name) return res.status(400).json({ error: 'Name is required' });

    const existing = await labelService.findLabelByName(name, userId);
    if (existing) return res.status(409).json({ error: 'Label already exists' });

    const newLabel = await labelService.createLabel(userId, name, color);
    res.status(201).location(`/api/labels/${newLabel.id}`).end();
  });
};

exports.getLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = parseInt(req.params.id);
    const userId = req.user.userId;

    const label = await labelService.findLabelById(labelId, userId);
    if (!label) return res.status(404).json({ error: 'Label not found' });

    res.json({ id: label.id, name: label.name });
  });
};

exports.editLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = parseInt(req.params.id);
    const userId = req.user.userId;

    const updated = await labelService.updateLabelById(labelId, userId, req.body);
    if (!updated) return res.status(404).json({ error: 'Label not found or not updated' });

    res.status(204).end();
  });
};

exports.deleteLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = parseInt(req.params.id);
    const userId = req.user.userId;

    const deleted = await labelService.deleteLabelById(labelId, userId);
    if (!deleted) return res.status(404).json({ error: 'Label not found' });

    // Optionally clean up mail label references (if handled in memory)
    res.status(204).end();
  });
};

exports.getAllLabels = (req, res) => {
  isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const labels = await labelService.getAllLabels(userId);

    res.json(labels.map(label => ({
      id: label.id,
      name: label.name,
      color: label.color
    })));
  });
};
