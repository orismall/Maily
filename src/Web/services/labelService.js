const Label = require('../models/labels');

async function createLabel(userId, name, color) {
  const label = new Label({ userId, name, color });
  return await label.save();
}

async function findLabelByName(name, userId) {
  return await Label.findOne({ name, userId });
}

async function findLabelById(id, userId) {
  return await Label.findOne({ id, userId });
}

async function updateLabelById(id, userId, updates) {
  return await Label.findOneAndUpdate({ id, userId }, { $set: updates }, { new: true });
}

async function deleteLabelById(id, userId) {
  return await Label.findOneAndDelete({ id, userId });
}

async function getAllLabels(userId) {
  return await Label.find({ userId });
}

module.exports = {
  createLabel,
  findLabelByName,
  findLabelById,
  updateLabelById,
  deleteLabelById,
  getAllLabels
};
