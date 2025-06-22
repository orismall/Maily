const Label = require('../models/labels');
const { removeLabelFromMail } = require('./mailService');

// Create a new label associated with a user
async function createLabel(userObjectId, name, color) {
  const label = new Label({ user: userObjectId, name, color });
  return await label.save();
}

// Find label by name and user
async function findLabelByName(name, userObjectId) {
  return await Label.findOne({ name, user: userObjectId });
}

// Find label by _id and user
async function findLabelById(labelId, userObjectId) {
  return await Label.findOne({ _id: labelId, user: userObjectId });
}

// Update label by _id and user
async function updateLabelById(labelId, userObjectId, updates) {
  return await Label.findOneAndUpdate(
    { _id: labelId, user: userObjectId },
    { $set: updates },
    { new: true }
  );
}

// Delete label by _id and user
async function deleteLabelById(labelId, userObjectId) {
  const label = await Label.findOne({ _id: labelId, user: userObjectId });
  if (!label) return null;

  const mailIds = label.mailIds || [];

  // Remove label from each mail using the existing logic
  await Promise.all(
    mailIds.map(mailId =>
      removeLabelFromMail(userObjectId, mailId.toString(), labelId.toString())
    )
  );

  // Finally delete the label itself
  return await Label.findOneAndDelete({ _id: labelId, user: userObjectId });
}

// Get all labels for a specific user
async function getAllLabels(userObjectId) {
  return await Label.find({ user: userObjectId });
}

module.exports = {
  createLabel,
  findLabelByName,
  findLabelById,
  updateLabelById,
  deleteLabelById,
  getAllLabels
};
