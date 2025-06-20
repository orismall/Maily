const isLoggedIn = require('../utils/isLoggedIn');
const extractLinks = require('../utils/linkExtractor');
const { sendCommandToServer } = require('../models/blacklist');
const mailService = require('../services/mailService');

// GET /api/spam
async function getSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user._id;
    const spam = await mailService.getSpamMails(userId);
    const sorted = [...spam].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

// GET /api/spam/:id
async function getSpamById(req, res) {
  await isLoggedIn(req, res, async () => {
    const mail = await mailService.getSpamMailById(req.user._id, req.params.id);
    if (!mail) return res.status(404).json({ error: 'Mail not found in spam' });
    res.json(mail);
  });
}

// DELETE /api/spam/:id
async function deleteSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.deleteSpamMail(req.user._id, req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

// POST /api/spam/:id/restore
async function restoreFromSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.restoreFromSpam(req.user._id, req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

// PATCH /api/spam/:id/mark-as-spam
async function markAsSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user._id;
    const mailId = req.params.id;

    const updateFn = async mail => {
      const links = [...extractLinks(mail.subject), ...extractLinks(mail.content)];
      if (links.length > 0) {
        for (const link of links) await sendCommandToServer(`POST ${link}`);
      } else {
        await sendCommandToServer(`POST ${mail.sender}`);
      }
    };

    const success = await mailService.markAsSpam(userId, mailId, updateFn);
    res.status(success ? 200 : 404).json(success
      ? { message: "Mail moved to spam and blacklist updated" }
      : { error: "Mail not found" });
  });
}

// PATCH /api/spam/:id/mark-as-not-spam
async function markAsNotSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user._id;
    const mailId = req.params.id;

    const removeFn = async mail => {
      const links = [...extractLinks(mail.subject), ...extractLinks(mail.content)];
      if (links.length > 0) {
        for (const link of links) await sendCommandToServer(`DELETE ${link}`);
      } else {
        await sendCommandToServer(`DELETE ${mail.sender}`);
      }
    };

    const restoredMail = await mailService.markAsNotSpam(userId, mailId, removeFn);
    if (!restoredMail) {
      return res.status(404).json({ error: "Mail not found in spam" });
    }

    res.status(200).json(restoredMail); // ✅ Return restored mail to update frontend
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
