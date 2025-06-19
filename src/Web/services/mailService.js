// services/mailService.js
const User = require('../models/users');

// Find a user by userId
async function findUserById(userId) {
  return await User.findOne({ userId });
}

// Add a mail to a specific folder
async function pushMailToFolder(userId, folder, mailObj, isRead = false) {
  return await User.updateOne(
    { userId },
    { $push: { [`mails.${folder}`]: { mail: mailObj, isRead, isStarred: false } } }
  );
}

// Update mail flags (read/starred)
async function updateMailFlags(userId, mailId, updates) {
  const folders = ['inbox', 'sent', 'drafts', 'trash', 'spam'];
  for (const folder of folders) {
    const base = `mails.${folder}`;
    const setOps = {};
    if (updates.isRead !== undefined) setOps[`${base}.$[m].isRead`] = updates.isRead;
    if (updates.isStarred !== undefined) setOps[`${base}.$[m].isStarred`] = updates.isStarred;

    if (Object.keys(setOps).length > 0) {
      const result = await User.updateOne(
        { userId },
        { $set: setOps },
        { arrayFilters: [{ 'm.mail.id': mailId }] }
      );
      if (result.modifiedCount > 0) return true;
    }
  }
  return false;
}

// Edit the content/subject of a sent mail
async function updateSentMail(userId, mailId, updates) {
  const fields = {};
  if (updates.subject !== undefined) fields['mails.sent.$[m].mail.subject'] = updates.subject;
  if (updates.content !== undefined) fields['mails.sent.$[m].mail.content'] = updates.content;
  fields['mails.sent.$[m].mail.date'] = new Date();

  return await User.updateOne(
    { userId },
    { $set: fields },
    { arrayFilters: [{ 'm.mail.id': mailId }] }
  );
}

// Delete a mail and move to trash
async function moveToTrash(userId, mailId) {
  const user = await findUserById(userId);
  const folder = Object.keys(user.mails).find(f =>
    user.mails[f].some(item => item.mail.id === mailId)
  );
  if (!folder) return false;

  const mail = user.mails[folder].find(item => item.mail.id === mailId).mail;

  await User.updateOne(
    { userId },
    {
      $pull: { [`mails.${folder}`]: { 'mail.id': mailId } },
      $push: { 'mails.trash': { mail, isRead: true, isStarred: false } }
    }
  );

  return true;
}

async function addLabelToMail(userId, mailId, labelId) {
  await User.updateOne(
    { userId },
    { $addToSet: { 'mails.$[].mail.labels': labelId } },
    { arrayFilters: [{ 'm.mail.id': mailId }] }
  );
}

async function removeLabelFromMail(userId, mailId, labelId) {
  await User.updateOne(
    { userId },
    { $pull: { 'mails.$[].mail.labels': labelId } },
    { arrayFilters: [{ 'm.mail.id': mailId }] }
  );
}


module.exports = {
  findUserById,
  pushMailToFolder,
  updateMailFlags,
  updateSentMail,
  moveToTrash,
  addLabelToMail,
  removeLabelFromMail
};
