const mongoose = require('mongoose');
const extractLinks = require('../utils/linkExtractor');
const isLoggedIn = require('../utils/isLoggedIn');
const { sendCommandToServer } = require('../models/blacklist');
const mailService = require('../services/mailService');
const mailSchema = require('../models/mails');
const Mail = mongoose.model('Mail', mailSchema);
const User = require('../models/users');


async function getMails(req, res) {
  await isLoggedIn(req, res, async () => {
    const user = await mailService.findUserById(req.user.userId);
    const all = [...user.mails.inbox, ...user.mails.sent];
    const unique = Array.from(new Map(all.map(m => [m.mail.mailId, m])).values());
    unique.sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(unique.slice((page - 1) * 50, page * 50));
  });
}

async function getInboxMails(req, res) {
  await isLoggedIn(req, res, async () => {
    const user = await mailService.findUserById(req.user.userId);
    const sorted = [...user.mails.inbox].sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function getSentMails(req, res) {
  await isLoggedIn(req, res, async () => {
    const user = await mailService.findUserById(req.user.userId);
    const sorted = [...user.mails.sent].sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function sendMail(req, res) {
  await isLoggedIn(req, res, async () => {
    const sender = await mailService.findUserById(req.user.userId);
    const { receiver, subject = '(No subject)', content = '' } = req.body;
    const receivers = Array.isArray(receiver) ? receiver : [receiver];

    const users = await User.find({ email: { $in: receivers } });
    const missing = receivers.filter(r => !users.some(u => u.email === r));
    if (missing.length) return res.status(404).json({ error: `Receiver(s) not found: ${missing.join(', ')}` });

    const links = [...extractLinks(subject), ...extractLinks(content)];
    for (const link of links) {
      const resp = await sendCommandToServer(`GET ${link}`);
      if (resp.includes('true true')) {
        const mailDoc = new Mail({
          sender: sender.email,
          receiver: receivers,
          subject,
          content,
          labels: [],
          type: 'mail'
        });
        await mailDoc.save();
        const mailObj = mailDoc.toObject();
        await mailService.pushMailToFolder(sender.userId, 'sent', mailObj, true);
        for (const u of users) await mailService.pushMailToFolder(u.userId, 'spam', mailObj);
        return res.status(201).json({ warning: 'Sent to spam (blacklisted content)' });
      }
    }

    const mailDoc = new Mail({
      sender: sender.email,
      receiver: receivers,
      subject,
      content,
      labels: [],
      type: 'mail'
    });
    await mailDoc.save();
    const mailObj = mailDoc.toObject();

    await mailService.pushMailToFolder(sender.userId, 'sent', mailObj, true);
    for (const u of users) await mailService.pushMailToFolder(u.userId, 'inbox', mailObj);
    res.status(201).location(`/api/mails/${mailObj.mailId}`).end();

  });
}

async function getMailById(req, res) {
  await isLoggedIn(req, res, async () => {
    const user = await mailService.findUserById(req.user.userId);
    const id = +req.params.id;
    const mail = [...user.mails.inbox, ...user.mails.sent].find(m => m.mail.mailId === id);
    if (!mail) return res.status(404).json({ error: 'Mail not found' });
    res.json(mail);
  });
}

async function updateMail(req, res) {
  await isLoggedIn(req, res, async () => {
    const { isRead, isStarred, subject, content } = req.body;
    const userId = req.user.userId;
    const mailId = +req.params.id;

    if (isRead !== undefined || isStarred !== undefined) {
      const success = await mailService.updateMailFlags(userId, mailId, { isRead, isStarred });
      return success ? res.sendStatus(204) : res.status(404).json({ error: 'Mail not found' });
    }

    const user = await mailService.findUserById(userId);
    const sentIds = user.mails.sent.map(i => i.mail.mailId);
    if (!sentIds.includes(mailId)) return res.status(403).json({ error: 'Only sent mails can be edited' });

    const links = [...extractLinks(subject || ''), ...extractLinks(content || '')];
    for (const link of links) {
      const resp = await sendCommandToServer(`GET ${link}`);
      if (resp.includes('true true')) return res.status(400).json({ error: 'Update contains blacklisted link', link });
    }

    await mailService.updateSentMail(userId, mailId, { subject, content });
    res.sendStatus(204);
  });
}

async function deleteMail(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.moveToTrash(req.user.userId, +req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

async function searchMails(req, res) {
  await isLoggedIn(req, res, async () => {
    const user = await mailService.findUserById(req.user.userId);
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
    if (!matches.length) return res.status(404).json({ error: 'No mails found' });

    const unique = Array.from(new Map(matches.map(m => [m.mail.mailId, m])).values());
    unique.sort((a, b) => b.mail.date - a.mail.date);
    res.json(unique.slice(0, 50));
  });
}

async function addMailToLabel(req, res) {
  await isLoggedIn(req, res, async () => {
    const { mailId, labelId } = req.params;
    const userId = req.user.userId;

    const user = await mailService.findUserById(userId);
    const folders = Object.keys(user.mails);
    const mailItem = folders
      .flatMap(folder => user.mails[folder])
      .find(m => m.mail.mailId === +mailId);

    if (!mailItem) return res.status(404).json({ error: 'Mail not found' });

    // Add labelId if not already present
    if (!mailItem.mail.labels.includes(+labelId)) {
      await mailService.addLabelToMail(userId, +mailId, +labelId);
    }

    res.sendStatus(204);
  });
}

async function removeMailFromLabel(req, res) {
  await isLoggedIn(req, res, async () => {
    const { mailId, labelId } = req.params;
    const userId = req.user.userId;

    const user = await mailService.findUserById(userId);
    const folders = Object.keys(user.mails);
    const mailItem = folders
      .flatMap(folder => user.mails[folder])
      .find(m => m.mail.mailId === +mailId);

    if (!mailItem) return res.status(404).json({ error: 'Mail not found' });

    await mailService.removeLabelFromMail(userId, +mailId, +labelId);
    res.sendStatus(204);
  });
}

async function getStarred(req, res) {
  await isLoggedIn(req, res, async () => {
    const user = await mailService.findUserById(req.user.userId);
    const allMails = Object.values(user.mails).flat();
    const starred = allMails.filter(m => m.isStarred);
    starred.sort((a, b) => b.mail.date - a.mail.date);
    const page = +req.query.page || 1;
    res.json(starred.slice((page - 1) * 50, page * 50));
  });
}


module.exports = {
  getMails,
  getInboxMails,
  getSentMails,
  sendMail,
  getMailById,
  updateMail,
  deleteMail,
  searchMails,
  addMailToLabel,
  removeMailFromLabel,
  getStarred
};
