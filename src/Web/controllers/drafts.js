const mailService = require('../services/mailService');
const isLoggedIn = require('../utils/isLoggedIn');
const { sendMail } = require('./mails');
const mongoose = require('mongoose');
const mailSchema = require('../models/mails');
const Mail = mongoose.model('Mail', mailSchema);

async function createDraft(req, res) {
  await isLoggedIn(req, res, async () => {
    const { receiver, subject, content } = req.body;
    if (!receiver && !subject && !content)
      return res.status(400).json({ error: 'Cannot create an empty draft' });

    const mail = {
      sender: req.user.email,
      receiver: receiver || '',
      subject: subject || '(No subject)',
      content: content || '',
      type: 'draft',
      labels: [],
    };

    try {
      const mailDoc = new Mail(mail);
      await mailDoc.save();

      const pushSuccess = await mailService.pushMailToFolder(req.user._id, 'drafts', mailDoc.toObject(), true);
      if (!pushSuccess) {
        return res.status(409).json({ error: 'Draft already exists' });
      }

      res.status(201).json(mailDoc);
    } catch (err) {
      console.error('Failed to create draft:', err);
      res.status(500).json({ error: 'Failed to create draft' });
    }
  });
}

async function getDrafts(req, res) {
  await isLoggedIn(req, res, async () => {
    const drafts = await mailService.getDrafts(req.user._id);
    const sorted = [...drafts].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function getDraftById(req, res) {
  await isLoggedIn(req, res, async () => {
    const draft = await mailService.getDraftById(req.user._id, req.params.id);
    if (!draft) return res.status(404).json({ error: 'Draft not found' });
    res.json(draft);
  });
}

async function updateDraft(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.updateDraft(req.user._id, req.params.id, req.body);
    res.status(success ? 204 : 404).end();
  });
}

async function deleteDraft(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.deleteDraft(req.user._id, req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

async function sendDraftAsMail(req, res) {
  await isLoggedIn(req, res, async () => {
    const mail = await mailService.sendDraftAsMail(req.user._id, req.params.id);
    if (!mail) return res.status(404).json({ error: 'Draft not found' });

    req.body = {
      receiver: mail.receiver,
      subject: mail.subject,
      content: mail.content
    };

    await sendMail(req, res);
  });
}

module.exports = {
  createDraft,
  getDrafts,
  getDraftById,
  updateDraft,
  deleteDraft,
  sendDraftAsMail
};
