const User = require('../models/users');
const extractLinks = require('../utils/linkExtractor');
const isLoggedIn = require('../utils/isLoggedIn');
const { sendCommandToServer } = require('../models/blacklist');

// Helpers

async function findUserById(userId) {
  return await User.findOne({ userId });
}

async function pushMailToFolder(userId, folder, mailObj, read = false) {
  await User.updateOne(
    { userId },
    { $push: { [`mails.${folder}`]: { mail: mailObj, isRead: read, isStarred: false } } }
  );
}

async function getMails(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const combined = [...user.mails.inbox, ...user.mails.sent];
    const unique = Array.from(new Map(combined.map(i => [i.mail.id, i])).values());
    unique.sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(unique.slice((page - 1) * 50, page * 50));
  });
}

async function getInboxMails(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const sorted = [...user.mails.inbox].sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function getSentMails(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const sorted = [...user.mails.sent].sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function sendMail(req, res) {
  isLoggedIn(req, res, async () => {
    const sender = await findUserById(req.user.userId);
    const { receiver, subject = '(No subject)', content = '' } = req.body;
    const receivers = Array.isArray(receiver) ? receiver : [receiver];

    const users = await User.find({ email: { $in: receivers } });
    const missing = receivers.filter(r => !users.some(u => u.email === r));
    if (missing.length) return res.status(404).json({ error: `Receiver(s) not found: ${missing.join(', ')}` });

    const links = [...extractLinks(subject), ...extractLinks(content)];
    for (const link of links) {
      const resp = await sendCommandToServer(`GET ${link}`);
      if (resp.includes('true true')) {
        const mailObj = { id: undefined, sender: sender.email, receiver: receivers, subject, content, labels: [], type: 'mail' };
        await pushMailToFolder(sender.userId, 'sent', mailObj, true);
        for (const u of users) await pushMailToFolder(u.userId, 'spam', mailObj);
        return res.status(201).json({ warning: 'Sent to spam (blacklisted content)' });
      }
    }

    const mailObj = { id: undefined, sender: sender.email, receiver: receivers, subject, content, labels: [], type: 'mail' };
    await pushMailToFolder(sender.userId, 'sent', mailObj, true);
    for (const u of users) await pushMailToFolder(u.userId, 'inbox', mailObj);
    res.status(201).location(`/api/mails/${mailObj.id}`).end();
  });
}

async function getMailById(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const id = +req.params.id;
    const found = [...user.mails.inbox, ...user.mails.sent].find(it => it.mail.id === id);
    if (!found) return res.status(404).json({ error: 'Mail not found' });
    res.json(found);
  });
}

async function updateMail(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const mailId = +req.params.id;
    const { isRead, isStarred, subject, content } = req.body;

    if (isRead !== undefined || isStarred !== undefined) {
      const folders = Object.keys(user.mails);
      for (const folder of folders) {
        const pathBase = `mails.${folder}`;
        const op = {};
        if (isRead !== undefined) op[`${pathBase}.$[m].isRead`] = isRead;
        if (isStarred !== undefined) op[`${pathBase}.$[m].isStarred`] = isStarred;
        if (Object.keys(op).length) {
          await User.updateOne(
            { userId: user.userId },
            { $set: op },
            { arrayFilters: [{ "m.mail.id": mailId }] }
          );
          return res.sendStatus(204);
        }
      }
      return res.status(404).json({ error: 'Mail not found' });
    }

    const sentIds = user.mails.sent.map(i => i.mail.id);
    if (!sentIds.includes(mailId)) {
      return res.status(403).json({ error: 'Only sent mails can be edited' });
    }

    const links = [...extractLinks(subject || ''), ...extractLinks(content || '')];
    for (const link of links) {
      const resp = await sendCommandToServer(`GET ${link}`);
      if (resp.includes('true true')) {
        return res.status(400).json({ error: 'Update contains blacklisted link', link });
      }
    }

    const updates = {};
    if (subject !== undefined) updates['mails.sent.$[m].mail.subject'] = subject;
    if (content !== undefined) updates['mails.sent.$[m].mail.content'] = content;
    updates['mails.sent.$[m].mail.date'] = new Date();

    await User.updateOne(
      { userId: user.userId },
      { $set: updates },
      { arrayFilters: [{ "m.mail.id": mailId }] }
    );
    res.sendStatus(204);
  });
}

async function deleteMail(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const mailId = +req.params.id;
    const category = Object.keys(user.mails).find(folder =>
      user.mails[folder].some(it => it.mail.id === mailId)
    );
    if (!category) return res.status(404).json({ error: 'Mail not found' });

    const mailItem = user.mails[category].find(it => it.mail.id === mailId).mail;
    await User.updateOne(
      { userId: user.userId },
      {
        $pull: { [`mails.${category}`]: { 'mail.id': mailId } },
        $push: { 'mails.trash': { mail: mailItem, isRead: true, isStarred: false } }
      }
    );
    res.sendStatus(204);
  });
}

async function searchMails(req, res) {
  isLoggedIn(req, res, async () => {
    const user = await findUserById(req.user.userId);
    const q = req.params.query.toLowerCase();
    const all = [...user.mails.inbox, ...user.mails.sent];
    const matches = all.filter(it => {
      const m = it.mail;
      return (
        m.sender.toLowerCase().includes(q) ||
        m.receiver.some(r => r.toLowerCase().includes(q)) ||
        m.subject.toLowerCase().includes(q) ||
        m.content.toLowerCase().includes(q)
      );
    });
    if (matches.length === 0) return res.status(404).json({ error: 'No mails found' });
    const unique = Array.from(new Map(matches.map(it => [it.mail.id, it])).values());
    unique.sort((a, b) => b.mail.date - a.mail.date);
    res.json(unique.slice(0, 50));
  });
}

// Export
module.exports = {
  getMails,
  getInboxMails,
  getSentMails,
  sendMail,
  getMailById,
  updateMail,
  deleteMail,
  searchMails
};
