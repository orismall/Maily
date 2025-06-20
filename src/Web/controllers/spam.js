const isLoggedIn = require('../utils/isLoggedIn');
const extractLinks = require('../utils/linkExtractor');
const { sendCommandToServer } = require('../models/blacklist');
const mailService = require('../services/mailService');

async function getSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const spam = await mailService.getSpamMails(userId);
    const sorted = [...spam].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function getSpamById(req, res) {
  await isLoggedIn(req, res, async () => {
    const mail = await mailService.getSpamMailById(req.user.userId, +req.params.id);
    if (!mail) return res.status(404).json({ error: 'Mail not found in spam' });
    res.json(mail);
  });
}

async function deleteSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.deleteSpamMail(req.user.userId, +req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

async function restoreFromSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.restoreFromSpam(req.user.userId, +req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

async function markAsSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const mailId = +req.params.id;

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

async function markAsNotSpam(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const mailId = +req.params.id;

    const removeFn = async mail => {
      const links = [...extractLinks(mail.subject), ...extractLinks(mail.content)];
      if (links.length > 0) {
        for (const link of links) await sendCommandToServer(`DELETE ${link}`);
      } else {
        await sendCommandToServer(`DELETE ${mail.sender}`);
      }
    };

    const success = await mailService.markAsNotSpam(userId, mailId, removeFn);
    res.status(success ? 200 : 404).json(success
      ? { message: "Mail restored and blacklist updated" }
      : { error: "Mail not found in spam" });
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
