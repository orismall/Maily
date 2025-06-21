const isLoggedIn = require('../utils/isLoggedIn');
const labelService = require('../services/labelService');

exports.createLabel = (req, res) => {
  isLoggedIn(req, res, async () => {
    const { name, color } = req.body;
    const userId = req.user._id; // ✅ use MongoDB _id

    if (!name) return res.status(400).json({ error: 'Name is required' });

    const existing = await labelService.findLabelByName(name, userId);
    if (existing) return res.status(409).json({ error: 'Label already exists' });

    const newLabel = await labelService.createLabel(userId, name, color);
    res.status(201).location(`/api/labels/${newLabel._id}`).end(); // ✅ use _id
  });
};

exports.getLabelById = (req, res) => {
  isLoggedIn(req, res, async () => {
    const labelId = req.params.id; // ✅ keep as string
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

    res.json(labels); // ✅ don't remap `_id`, let frontend receive the true object

  });
};
