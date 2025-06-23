const User = require('../models/users');
const Draft = require('../models/drafts');
const { sendMail } = require('./mails');
const isLoggedIn = require('../utils/isLoggedIn');
const TrashMail = require('../models/trash');


// POST /api/drafts - Create a new draft
function createDraft(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const { receiver, subject, content } = req.body;
    // Do not save an empty draft
    if (!receiver && !subject && !content) {
      return res.status(400).json({ error: "Cannot create an empty draft" });
    }
    const newDraft = new Draft(
      user.email,
      receiver || '',
      subject || "(No subject)",
      content || ''
    );
    newDraft.date = new Date();
    user.drafts.unshift({ mail: newDraft, isRead: true, isStarred: false });
    res.status(201).location(`/api/drafts/${newDraft.id}`).end();
  });
};

async function sendDraftAsMail(req, res) {
  isLoggedIn(req, res, async () => {
    const user = req.user;
    const draftId = Number(req.params.id);
    const draftIndex = user.drafts.findIndex(d => d.mail.id === draftId);
    if (draftIndex === -1) return res.status(404).json({ error: "Draft not found" });
    const draft = user.drafts[draftIndex].mail;
    // Simulate request body like a normal mail
    req.body = {
      receiver: draft.receiver,
      subject: draft.subject || "(No subject)",
      content: draft.content || ""
    };
    await sendMail(req, res);
    // If sent successfully, delete the draft
    if (res.statusCode === 201) { ////////////////////////////////////////////////////////
      user.drafts.splice(draftIndex, 1);
    }
  });
};

// GET /api/drafts - Get all drafts of the authenticated user
function getDrafts(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const sorted = [...user.drafts].sort((a, b) => new Date(b.date) - new Date(a.date));
    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const start = (page - 1) * PAGE_SIZE;
    const paginated = sorted.slice(start, start + PAGE_SIZE);
    res.json(paginated);

  });
};

// GET /api/drafts/:id - Get a specific draft
function getDraftById(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const draftId = Number(req.params.id);
    const item  = user.drafts.find(d => d.mail.id === draftId);
    if (!item ) return res.status(404).json({ error: "Draft not found" });
    res.json(item);
  });
};

// PATCH /api/drafts/:id - Update a draft
function updateDraft(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const draftId = Number(req.params.id);
    const item  = user.drafts.find(d => d.mail.id === draftId);
    if (!item ) return res.status(404).json({ error: "Draft not found" });
    const draft = item.mail;
    const { receiver, subject, content, isStarred } = req.body;
    if (receiver !== undefined) draft.receiver = receiver;
    if (subject !== undefined) draft.subject = subject;
    if (content !== undefined) draft.content = content;
    if (isStarred !== undefined) item.isStarred = isStarred
    draft.date = new Date(); 
    res.status(204).end();
  });
};

// DELETE /api/drafts/:id - Permanently delete a draft
function deleteDraft(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const draftId = Number(req.params.id);
    const index = user.drafts.findIndex(d => d.mail.id === draftId);
    if (index === -1) {
      return res.status(404).json({ error: "Draft not found" });
    }
    user.drafts.splice(index, 1);
    res.status(204).end();
  });
}

// Export all controller functions
module.exports = {
  createDraft,
  sendDraftAsMail,
  getDrafts,
  getDraftById,
  updateDraft,
  deleteDraft
};