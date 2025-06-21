const User = require('../models/users');
const mongoose = require('mongoose');
const mailSchema = require('../models/mails');
mongoose.model('Mail', mailSchema);
const Mail = mongoose.model('Mail');
const Label = require('../models/labels');

// Helper: Find user by MongoDB _id
async function findUserById(userId) {
  return await User.findById(userId);
}

// Add a mail to a specific folder
async function pushMailToFolder(userId, folder, mailObj, isRead = false) {
  const user = await findUserById(userId);
  const alreadyExists = user.mails[folder].some(m => m.mail._id?.equals(mailObj._id));

  if (alreadyExists) return false;

  return await User.updateOne(
    { _id: userId },
    { $push: { [`mails.${folder}`]: { mail: mailObj, isRead, isStarred: false } } }
  );
}

// Update mail flags (read/starred)
async function updateMailFlags(userId, mailId, updates) {
  const folders = ['inbox', 'sent', 'drafts', 'trash', 'spam'];
  let found = false; // <-- Declare `found` at the beginning

  for (const folder of folders) {
    const base = `mails.${folder}`;
    const setOps = {};

    if (updates.isRead !== undefined) {
      setOps[`${base}.$[m].isRead`] = updates.isRead;
    }
    if (updates.isStarred !== undefined) {
      setOps[`${base}.$[m].isStarred`] = updates.isStarred;
    }

    if (Object.keys(setOps).length > 0) {
      const result = await User.updateOne(
        { _id: userId },
        { $set: setOps },
        { arrayFilters: [{ 'm.mail._id': new mongoose.Types.ObjectId(mailId) }] }
      );

      if (result.modifiedCount > 0) {
        found = true;
      }
    }
  }
  return found;
}


// Edit the content/subject of a sent mail
async function updateSentMail(userId, mailId, updates) {
  const fields = {};
  if (updates.subject !== undefined) fields['mails.sent.$[m].mail.subject'] = updates.subject;
  if (updates.content !== undefined) fields['mails.sent.$[m].mail.content'] = updates.content;
  fields['mails.sent.$[m].mail.date'] = new Date();

  return await User.updateOne(
    { _id: userId },
    { $set: fields },
    { arrayFilters: [{ 'm.mail._id': new mongoose.Types.ObjectId(mailId) }] }
  );
}

// Delete a mail and move to trash
async function moveToTrash(userId, mailId) {
  const user = await findUserById(userId);
  const allFolders = Object.keys(user.mails);
  let foundMail = null;

  const foldersWithMail = allFolders.filter(folder =>
    user.mails[folder].some(item => item.mail._id?.equals(mailId))
  );

  if (foldersWithMail.length === 0) return false;

  for (const folder of foldersWithMail) {
    const match = user.mails[folder].find(item => item.mail._id?.equals(mailId));
    if (match) {
      foundMail = match.mail;
      break;
    }
  }

  if (!foundMail) return false;

  if (foundMail.labels?.length > 0) {
    await Promise.all(
      foundMail.labels.map(labelId =>
        removeLabelFromMail(userId, mailId, labelId)
      )
    );
  }

  const pullOps = {};
  foldersWithMail.forEach(folder => {
    pullOps[`mails.${folder}`] = { 'mail._id': new mongoose.Types.ObjectId(mailId) };
  });

  const result = await User.updateOne(
    { _id: userId },
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
    const user = await findUserById(userId);
    const mailItem = user.mails[folder].find(m => m.mail._id?.equals(mailId));
    if (!mailItem) continue;

    // Check if label already exists
    const alreadyHasLabel = mailItem.mail.labels?.some(
      l => l.toString() === labelId
    );
    if (alreadyHasLabel) return false;

    // Add label to embedded mail
    const result = await User.updateOne(
      { _id: userId },
      { $addToSet: { [`mails.${folder}.$[m].mail.labels`]: labelId } },
      { arrayFilters: [{ 'm.mail._id': new mongoose.Types.ObjectId(mailId) }] }
    );

    if (result.modifiedCount > 0) {
      // Update Label and Mail documents
      await Label.updateOne(
        { _id: labelId, user: userId },
        { $addToSet: { mailIds: mailId } }
      );

      await Mail.updateOne(
        { _id: mailId },
        { $addToSet: { labels: labelId } }
      );

      return true;
    }
  }

  return false;
}


async function removeLabelFromMail(userId, mailId, labelId) {
  const folders = ['inbox', 'sent', 'drafts', 'trash', 'spam'];
  let removedFromMail = false;
  for (const folder of folders) {
    const result = await User.updateOne(
      { _id: userId },
      { $pull: { [`mails.${folder}.$[m].mail.labels`]: labelId } },
      { arrayFilters: [{ 'm.mail._id': new mongoose.Types.ObjectId(mailId) }] }
    );
    if (result.modifiedCount > 0) {
      removedFromMail = true;
      break;
    }
  }
  if (removedFromMail) {
    await Label.updateOne(
      { _id: labelId, user: userId },
      { $pull: { mailIds: mailId } }
    );
  }
  await Mail.updateOne(
    { _id: mailId },
    { $pull: { labels: labelId } }
  );

  return removedFromMail;
}

async function getTrashMails(userId) {
  const user = await findUserById(userId);
  return user.mails.trash;
}

async function restoreMailFromTrash(userId, mailId) {
  const user = await findUserById(userId);
  const index = user.mails.trash.findIndex(item => item.mail._id?.equals(mailId));
  if (index === -1) return false;

  const { mail, isRead, isStarred } = user.mails.trash[index];
  const entry = { mail, isRead, isStarred };

  const isSender = mail.sender === user.email;
  const isReceiver = mail.receiver.includes(user.email);

  const updates = { $pull: { 'mails.trash': { 'mail._id': new mongoose.Types.ObjectId(mailId) } } };
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

  await User.updateOne({ _id: userId }, updates);
  if (mail.labels?.length > 0) {
    await Promise.all(
      mail.labels.map(labelId =>
        Label.updateOne(
          { _id: labelId, user: userId },
          { $addToSet: { mailIds: mail._id } }
        )
      )
    );
    // Also ensure mail document has correct labels (in case it's restored without them)
    await Mail.updateOne(
      { _id: mail._id },
      { $addToSet: { labels: { $each: mail.labels || [] } } }
    );
  }
  return true;
}

async function deleteFromTrash(userId, mailId) {
  const result = await User.updateOne(
    { _id: userId },
    { $pull: { 'mails.trash': { 'mail._id': new mongoose.Types.ObjectId(mailId) } } }
  );

  if (result.modifiedCount > 0) {
    await deleteMailDocument(mailId);
    return true;
  }
  return false;
}


async function getTrashMailById(userId, mailId) {
  const user = await findUserById(userId);
  return user.mails.trash.find(item => item.mail._id?.equals(mailId));
}

async function getSpamMails(userId) {
  const user = await findUserById(userId);
  return user.mails.spam;
}

async function getSpamMailById(userId, mailId) {
  const user = await findUserById(userId);
  return user.mails.spam.find(item => item.mail._id?.equals(mailId));
}

async function deleteSpamMail(userId, mailId) {
  const result = await User.updateOne(
    { _id: userId },
    { $pull: { 'mails.spam': { 'mail._id': new mongoose.Types.ObjectId(mailId) } } }
  );
  return result.modifiedCount > 0;
}

async function markAsSpam(userId, mailId, blacklistUpdateFn) {
  const user = await findUserById(userId);
  const folders = ['inbox', 'sent'];
  let found = null;
  for (const folder of folders) {
    const item = user.mails[folder].find(m => m.mail._id?.equals(mailId));
    if (item) {
      found = item;
      break;
    }
  }
  if (!found) return false;

  const { mail, isRead, isStarred } = found;
  const mailObjectId = new mongoose.Types.ObjectId(mailId);

  // Remove label references
  const originalLabels = [...(mail.labels || [])];

  if (originalLabels.length > 0) {
    await Label.updateMany(
      { _id: { $in: originalLabels }, user: userId },
      { $pull: { mailIds: mailObjectId } }
    );
    await Mail.updateOne(
      { _id: mailObjectId },
      { $pull: { labels: { $in: originalLabels } } }
    );
  }

  // Remove from inbox/sent
  const updates = {
    $pull: {
      'mails.inbox': { 'mail._id': mailObjectId },
      'mails.sent': { 'mail._id': mailObjectId }
    },
    $addToSet: {
      'mails.spam': { mail, isRead, isStarred }
    }
  };

  await User.updateOne({ _id: userId }, updates);
  await blacklistUpdateFn(mail);
  return true;
}


async function markAsNotSpam(userId, mailId, blacklistRemoveFn) {
  const user = await findUserById(userId);
  const item = user.mails.spam.find(m => m.mail._id?.equals(mailId));
  if (!item) return false;

  const { mail, isRead, isStarred } = item;
  const mailObjectId = new mongoose.Types.ObjectId(mailId);

  await blacklistRemoveFn(mail);

  const isSender = mail.sender === user.email;
  const isReceiver = mail.receiver.includes(user.email);
  const entry = { mail, isRead, isStarred };

  const updates = { $pull: { 'mails.spam': { 'mail._id': mailObjectId } } };

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

  await User.updateOne({ _id: userId }, updates);

  // Re-add label references if needed
  const restoredLabels = mail.labels || [];
  if (restoredLabels.length > 0) {
    await Label.updateMany(
      { _id: { $in: restoredLabels }, user: userId },
      { $addToSet: { mailIds: mailObjectId } }
    );
    await Mail.updateOne(
      { _id: mailObjectId },
      { $addToSet: { labels: { $each: restoredLabels } } }
    );
  }

  return mail; // return for frontend update
}



async function restoreFromSpam(userId, mailId) {
  const user = await findUserById(userId);
  const index = user.mails.spam.findIndex(item => item.mail._id?.equals(mailId));
  if (index === -1) return false;

  const { mail, isRead, isStarred } = user.mails.spam[index];
  const mailObjectId = new mongoose.Types.ObjectId(mailId);

  const isSender = mail.sender === user.email;
  const isReceiver = mail.receiver.includes(user.email);
  const entry = { mail, isRead, isStarred };

  const updates = { $pull: { 'mails.spam': { 'mail._id': mailObjectId } } };
  const pushOps = {};
  if (isSender) pushOps['mails.sent'] = entry;
  if (isReceiver) pushOps['mails.inbox'] = entry;
  if (Object.keys(pushOps).length > 0) {
    updates.$push = pushOps;
  }

  await User.updateOne({ _id: userId }, updates);

  // Sync labels to Mail and Label documents
  if (mail.labels?.length > 0) {
    await Promise.all([
      Mail.updateOne(
        { _id: mailObjectId },
        { $addToSet: { labels: { $each: mail.labels } } }
      ),
      ...mail.labels.map(labelId =>
        Label.updateOne(
          { _id: labelId, user: userId },
          { $addToSet: { mailIds: mailId } }
        )
      )
    ]);
  }

  return mail; // returning mail to update frontend state
}

async function createDraft(userId, draftData) {
  const result = await User.updateOne(
    { _id: userId },
    { $push: { 'mails.drafts': { mail: draftData, isRead: true, isStarred: false } } }
  );
  return result.acknowledged;
}

async function getDrafts(userId) {
  const user = await findUserById(userId);
  return user.mails.drafts;
}

async function getDraftById(userId, draftId) {
  const user = await findUserById(userId);
  return user.mails.drafts.find(d => d.mail._id?.equals(draftId));
}

async function updateDraft(userId, draftId, updates) {
  const setOps = {};
  if (updates.receiver !== undefined) setOps['mails.drafts.$[d].mail.receiver'] = updates.receiver;
  if (updates.subject !== undefined) setOps['mails.drafts.$[d].mail.subject'] = updates.subject;
  if (updates.content !== undefined) setOps['mails.drafts.$[d].mail.content'] = updates.content;
  if (updates.isStarred !== undefined) setOps['mails.drafts.$[d].isStarred'] = updates.isStarred;
  setOps['mails.drafts.$[d].mail.date'] = new Date();

  const result = await User.updateOne(
    { _id: userId },
    { $set: setOps },
    { arrayFilters: [{ 'd.mail._id': new mongoose.Types.ObjectId(draftId) }] }
  );
  return result.modifiedCount > 0;
}

async function deleteDraft(userId, draftId) {
  const result = await User.updateOne(
    { _id: userId },
    { $pull: { 'mails.drafts': { 'mail._id': new mongoose.Types.ObjectId(draftId) } } }
  );
  if (result.modifiedCount > 0) {
    await deleteMailDocument(draftId);
    return true;
  }
  return false;
}


async function sendDraftAsMail(userId, draftId) {
  const user = await findUserById(userId);
  const draftEntry = user.mails.drafts.find(d => d.mail._id?.equals(draftId));
  if (!draftEntry) return null;

  const mail = draftEntry.mail;
  await deleteDraft(userId, draftId);
  return mail;
}

async function deleteMailDocument(mailId) {
  await Mail.deleteOne({ _id: mailId });
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
  sendDraftAsMail,
  deleteMailDocument,
  deleteDraft
};
