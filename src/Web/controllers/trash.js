const mailService = require('../services/mailService');
const isLoggedIn = require('../utils/isLoggedIn');

async function getTrash(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const trash = await mailService.getTrashMails(userId);
    const sorted = [...trash].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = +req.query.page || 1;
    res.json(sorted.slice((page - 1) * 50, page * 50));
  });
}

async function getTrashMailById(req, res) {
  await isLoggedIn(req, res, async () => {
    const userId = req.user.userId;
    const mail = await mailService.getTrashMailById(userId, +req.params.id);
    if (!mail) return res.status(404).json({ error: 'Mail not found in trash' });
    res.json(mail);
  });
}

async function restoreFromTrash(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.restoreMailFromTrash(req.user.userId, +req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

async function permanentlyDeleteFromTrash(req, res) {
  await isLoggedIn(req, res, async () => {
    const success = await mailService.deleteFromTrash(req.user.userId, +req.params.id);
    res.status(success ? 204 : 404).end();
  });
}

module.exports = {
  getTrash,
  getTrashMailById,
  restoreFromTrash,
  permanentlyDeleteFromTrash
};
