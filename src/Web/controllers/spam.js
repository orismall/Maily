const User = require('../models/users');
const Mail = require('../models/mails');
const isLoggedIn = require('../utils/isLoggedIn');
const extractLinks = require('../utils/linkExtractor');
const { sendCommandToServer } = require('../models/blacklist');


// GET /api/spam - Retrieve all spam mails
function getSpam(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const sorted = [...user.spam].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const start = (page - 1) * PAGE_SIZE;
    const paginated = sorted.slice(start, start + PAGE_SIZE);
    res.json(paginated);
  });
}

// GET /api/spam/:id - Retrieve specific spam mail
function getSpamById(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const item = user.spam.find(item => item.mail.id === mailId);
    if (!item) return res.status(404).json({ error: "Mail not found in spam" });
    res.json(item);
  });
}

// DELETE /api/spam/:id - Permanently delete mail from spam
function deleteSpam(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const index = user.spam.findIndex(item => item.mail.id === mailId);
    if (index === -1) return res.status(404).json({ error: "Mail not found in spam" });
    user.spam.splice(index, 1);
    res.status(204).end();
  });
}

// PATCH /api/spam/:id/restore - Move spam mail to inbox
function restoreFromSpam(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const index = user.spam.findIndex(item => item.mail.id === mailId);
    if (index === -1) return res.status(404).json({ error: "Mail not found in spam" });
    const { mail, isRead, isStarred } = user.spam.splice(index, 1)[0];
    const isSender = mail.sender === user.email;
    const isReceiver = mail.receiver.includes(user.email)
    if (isSender && isReceiver) {
      user.inbox.unshift({ mail, isRead, isStarred });
      user.sent.unshift({ mail, isRead, isStarred });
    } else if (isSender) {
      user.sent.unshift({ mail, isRead, isStarred });
    } else if (isReceiver) {
      user.inbox.unshift({ mail, isRead, isStarred });
    }
    res.status(204).end();
  });
}

// PATCH /api/spam/:id/mark-as-spam
async function markAsSpam(req, res) {
  isLoggedIn(req, res, async () => {
    const user = req.user;
    const mailId = Number(req.params.id);

    const allMails = [...user.inbox, ...user.sent];
    const item = allMails.find(item => item.mail.id === mailId);
    if (!item) return res.status(404).json({ error: "Mail not found" });

    const { mail, isRead, isStarred } = item;
    const contentLinks = extractLinks(mail.content);
    const subjectLinks = extractLinks(mail.subject);
    const links = [...contentLinks, ...subjectLinks];

    if (links.length > 0) {
      for (const link of links) {
        const response = await sendCommandToServer(`POST ${link}`);
      }
    } else {
      const response = await sendCommandToServer(`POST ${mail.sender}`);
    }

    // Remove from inbox/sent and move to spam
    user.inbox = user.inbox.filter(item => item.mail.id !== mailId);
    user.sent = user.sent.filter(item => item.mail.id !== mailId);
    user.spam.unshift({ mail, isRead, isStarred: false });

    res.status(200).json({ message: "Mail moved to spam and blacklist updated" });
  });
}

// PATCH /api/spam/:id/mark-as-not-spam
async function markAsNotSpam(req, res) {
  isLoggedIn(req, res, async () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const index = user.spam.findIndex(item => item.mail.id === mailId);
    if (index === -1) return res.status(404).json({ error: "Mail not found in spam" });
    const { mail, isRead, isStarred } = user.spam.splice(index, 1)[0];
    const contentLinks = extractLinks(mail.content);
    const subjectLinks = extractLinks(mail.subject);
    const links = [...contentLinks, ...subjectLinks];
    if (links.length > 0) {
      for (const link of links) {
        await sendCommandToServer(`DELETE ${link}`);
      }
    } else {
      await sendCommandToServer(`DELETE ${mail.sender}`);
    }
    const isSender = mail.sender === user.email;
    const isReceiver = mail.receiver.includes(user.email)
    if (isSender && isReceiver) {
      user.inbox.unshift({ mail, isRead, isStarred });
      user.sent.unshift({ mail, isRead, isStarred });
    } else if (isSender) {
      user.sent.unshift({ mail, isRead, isStarred });
    } else if (isReceiver) {
      user.inbox.unshift({ mail, isRead, isStarred });
    }
    res.status(200).json({ message: "Mail restored and blacklist updated" });
  });
}


module.exports = {
  getSpam,
  getSpamById,
  deleteSpam,
  restoreFromSpam,
  markAsSpam,
  markAsNotSpam
};
