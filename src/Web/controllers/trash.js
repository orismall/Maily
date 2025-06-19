const User = require('../models/users');
const isLoggedIn = require('../utils/isLoggedIn');

// GET /api/trash - Retrieve all trashed mails for authenticated user
function getTrash(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const sorted = [...user.trash].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const start = (page - 1) * PAGE_SIZE;
    const paginated = sorted.slice(start, start + PAGE_SIZE);
    res.json(paginated);
  });
}

// POST /api/trash/:id/restore - Restore mail from trash back to inbox or sent
function restoreFromTrash(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);

    // Find by: item.mail.id (not item.original.id)
    const index = user.trash.findIndex(item => item.mail.id === mailId);
    if (index === -1) return res.status(404).json({ error: "Mail not found in trash" });

    const { mail, isRead, isStarred } = user.trash.splice(index, 1)[0];
    const isSender = mail.sender === user.email;
    const isReceiver = mail.receiver.includes(user.email);
    const entry = { mail, isRead, isStarred };

    if (isSender && isReceiver) {
      user.inbox.unshift(entry);
      user.sent.unshift(entry);
    } else if (isSender) {
      user.sent.unshift(entry);
    } else if (isReceiver) {
      user.inbox.unshift(entry);
    }

    res.status(204).end();
  });
}


// DELETE /api/trash/:id - Permanently delete a mail from trash
function permanentlyDeleteFromTrash(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const index = user.trash.findIndex(item => item.mail.id === mailId);
    if (index === -1) return res.status(404).json({ error: "Mail not found in trash" });
    user.trash.splice(index, 1);
    res.status(204).end();
  });
}

// GET /api/trash/:id - Retrieve a specific trashed mail by its ID
function getTrashMailById(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const item = user.trash.find(item => item.mail.id === mailId);
    if (!item) return res.status(404).json({ error: "Mail not found in trash" });
    res.json(item);
  });
}

module.exports = {
  getTrash,
  restoreFromTrash,
  permanentlyDeleteFromTrash,
  getTrashMailById
};