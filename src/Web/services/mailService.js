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
        { arrayFilters: [{ 'm.mail.mailId': mailId }] }
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
    { arrayFilters: [{ 'm.mail.mailId': mailId }] }
  );
}

// Delete a mail and move to trash
async function moveToTrash(userId, mailId) {
  const user = await findUserById(userId);
  const allFolders = Object.keys(user.mails);
  let foundMail = null;

  // Find all folders containing the mail
  const foldersWithMail = allFolders.filter(folder =>
    user.mails[folder].some(item => item.mail.mailId === mailId)
  );

  // If not found in any folder, exit
  if (foldersWithMail.length === 0) return false;

  // Use the first instance to extract mail object
  for (const folder of foldersWithMail) {
    const match = user.mails[folder].find(item => item.mail.mailId === mailId);
    if (match) {
      foundMail = match.mail;
      break;
    }
  }
  if (!foundMail) return false;

  // Optionally remove labels (label cleanup)
  if (foundMail.labels?.length > 0) {
    await Promise.all(
      foundMail.labels.map(labelId =>
        removeLabelFromMail(userId, mailId, labelId)
      )
    );
  }

  // Prepare pull object for all folders
  const pullOps = {};
  foldersWithMail.forEach(folder => {
    pullOps[`mails.${folder}`] = { 'mail.mailId': mailId };
  });

  const result = await User.updateOne(
    { userId },
    {
      $pull: pullOps,
      $push: {
        'mails.trash': {
          mail: foundMail,
          isRead: true,
          isStarred: false
        }
      }
    }
  );

  return result.modifiedCount > 0;
}



async function addLabelToMail(userId, mailId, labelId) {
  const folders = ['inbox', 'sent', 'drafts', 'trash', 'spam'];
  for (const folder of folders) {
    const result = await User.updateOne(
      { userId },
      { $addToSet: { [`mails.${folder}.$[m].mail.labels`]: labelId } },
      { arrayFilters: [{ 'm.mail.mailId': mailId }] }
    );
    if (result.modifiedCount > 0) return true;
  }
  return false;
}

async function removeLabelFromMail(userId, mailId, labelId) {
  const folders = ['inbox', 'sent', 'drafts', 'trash', 'spam'];
  for (const folder of folders) {
    const result = await User.updateOne(
      { userId },
      { $pull: { [`mails.${folder}.$[m].mail.labels`]: labelId } },
      { arrayFilters: [{ 'm.mail.mailId': mailId }] }
    );
    if (result.modifiedCount > 0) return true;
  }
  return false;
}


async function getTrashMails(userId) {
  const user = await findUserById(userId);
  return user.mails.trash;
}

async function restoreMailFromTrash(userId, mailId) {
  const user = await findUserById(userId);
  const index = user.mails.trash.findIndex(item => item.mail.mailId === mailId);
  if (index === -1) return false;

  const { mail, isRead, isStarred } = user.mails.trash[index];
  const entry = { mail, isRead, isStarred };

  const isSender = mail.sender === user.email;
  const isReceiver = mail.receiver.includes(user.email);

  const updates = { $pull: { 'mails.trash': { 'mail.mailId': mailId } } };
  if (isSender && isReceiver) {
    updates.$push = {
      'mails.inbox': entry,
      'mails.sent': entry
    };
  } else if (isSender) {
    updates.$push = { 'mails.sent': entry };
  } else if (isReceiver) {
    updates.$push = { 'mails.inbox': entry };
  }

  await User.updateOne({ userId }, updates);
  return true;
}

async function deleteFromTrash(userId, mailId) {
  const result = await User.updateOne(
    { userId },
    { $pull: { 'mails.trash': { 'mail.mailId': mailId } } }
  );
  return result.modifiedCount > 0;
}

async function getTrashMailById(userId, mailId) {
  const user = await findUserById(userId);
  return user.mails.trash.find(item => item.mail.mailId === mailId);
}

// Get all spam mails
async function getSpamMails(userId) {
  const user = await findUserById(userId);
  return user.mails.spam;
}

// Get specific spam mail
async function getSpamMailById(userId, mailId) {
  const user = await findUserById(userId);
  return user.mails.spam.find(item => item.mail.mailId === mailId);
}

// Delete spam mail
async function deleteSpamMail(userId, mailId) {
  const result = await User.updateOne(
    { userId },
    { $pull: { 'mails.spam': { 'mail.mailId': mailId } } }
  );
  return result.modifiedCount > 0;
}

// Restore mail from spam
async function restoreFromSpam(userId, mailId) {
  const user = await findUserById(userId);
  const index = user.mails.spam.findIndex(item => item.mail.mailId === mailId);
  if (index === -1) return false;

  const { mail, isRead, isStarred } = user.mails.spam[index];
  const isSender = mail.sender === user.email;
  const isReceiver = mail.receiver.includes(user.email);
  const entry = { mail, isRead, isStarred };

  const updates = { $pull: { 'mails.spam': { 'mail.mailId': mailId } } };
  if (isSender && isReceiver) {
    updates.$push = {
      'mails.inbox': entry,
      'mails.sent': entry
    };
  } else if (isSender) {
    updates.$push = { 'mails.sent': entry };
  } else if (isReceiver) {
    updates.$push = { 'mails.inbox': entry };
  }

  await User.updateOne({ userId }, updates);
  return true;
}

// Mark a mail as spam
async function markAsSpam(userId, mailId, blacklistUpdateFn) {
  const user = await findUserById(userId);
  const allFolders = Object.keys(user.mails);
  let foundItem = null, folder = null;

  for (const f of ['inbox', 'sent']) {
    const item = user.mails[f].find(m => m.mail.mailId === mailId);
    if (item) {
      foundItem = item;
      folder = f;
      break;
    }
  }

  if (!foundItem) return false;
  const { mail, isRead } = foundItem;

  // Run blacklist update logic
  await blacklistUpdateFn(mail);

  await User.updateOne(
    { userId },
    {
      $pull: {
        [`mails.${folder}`]: { 'mail.mailId': mailId }
      },
      $push: {
        'mails.spam': { mail, isRead, isStarred: false }
      }
    }
  );
  return true;
}

// Mark a mail as not spam
async function markAsNotSpam(userId, mailId, blacklistRemoveFn) {
  const user = await findUserById(userId);
  const item = user.mails.spam.find(m => m.mail.mailId === mailId);
  if (!item) return false;

  const { mail, isRead, isStarred } = item;

  await blacklistRemoveFn(mail);

  const isSender = mail.sender === user.email;
  const isReceiver = mail.receiver.includes(user.email);
  const entry = { mail, isRead, isStarred };

  const updates = { $pull: { 'mails.spam': { 'mail.mailId': mailId } } };
  if (isSender && isReceiver) {
    updates.$push = {
      'mails.inbox': entry,
      'mails.sent': entry
    };
  } else if (isSender) {
    updates.$push = { 'mails.sent': entry };
  } else if (isReceiver) {
    updates.$push = { 'mails.inbox': entry };
  }

  await User.updateOne({ userId }, updates);
  return true;
}

// Create new draft
async function createDraft(userId, draftData) {
  const result = await User.updateOne(
    { userId },
    { $push: { 'mails.drafts': { mail: draftData, isRead: true, isStarred: false } } }
  );
  return result.acknowledged;
}

// Get all drafts
async function getDrafts(userId) {
  const user = await findUserById(userId);
  return user.mails.drafts;
}

// Get single draft
async function getDraftById(userId, draftId) {
  const user = await findUserById(userId);
  return user.mails.drafts.find(d => d.mail.mailId === draftId);
}

// Update draft
async function updateDraft(userId, draftId, updates) {
  const setOps = {};
  if (updates.receiver !== undefined) setOps['mails.drafts.$[d].mail.receiver'] = updates.receiver;
  if (updates.subject !== undefined) setOps['mails.drafts.$[d].mail.subject'] = updates.subject;
  if (updates.content !== undefined) setOps['mails.drafts.$[d].mail.content'] = updates.content;
  if (updates.isStarred !== undefined) setOps['mails.drafts.$[d].isStarred'] = updates.isStarred;

  // Always update date
  setOps['mails.drafts.$[d].mail.date'] = new Date();

  const result = await User.updateOne(
    { userId },
    { $set: setOps },
    { arrayFilters: [{ 'd.mail.mailId': draftId }] }
  );
  return result.modifiedCount > 0;
}

// Delete draft
async function deleteDraft(userId, draftId) {
  const result = await User.updateOne(
    { userId },
    { $pull: { 'mails.drafts': { 'mail.mailId': draftId } } }
  );
  return result.modifiedCount > 0;
}

// Send draft as mail (and delete)
async function sendDraftAsMail(userId, draftId) {
  const user = await findUserById(userId);
  const draftEntry = user.mails.drafts.find(d => d.mail.mailId === draftId);
  if (!draftEntry) return null;

  const mail = draftEntry.mail;
  await deleteDraft(userId, draftId);
  return mail;
}

module.exports = {
  findUserById,
  pushMailToFolder,
  updateMailFlags,
  updateSentMail,
  moveToTrash,
  addLabelToMail,
  removeLabelFromMail,
  getTrashMails,
  restoreMailFromTrash,
  deleteFromTrash,
  getTrashMailById,
  getSpamMails,
  getSpamMailById,
  deleteSpamMail,
  restoreFromSpam,
  markAsSpam,
  markAsNotSpam,
  createDraft,
  getDrafts,
  getDraftById,
  updateDraft,
  sendDraftAsMail
};
