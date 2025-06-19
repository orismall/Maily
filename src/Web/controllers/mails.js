const User = require('../models/users');
const { Mail, addLabelToMail, removeLabelFromMail } = require('../models/mails');
const TrashMail = require('../models/trash');
const SpamMail = require('../models/spam');
const extractLinks = require('../utils/linkExtractor');
const isLoggedIn = require('../utils/isLoggedIn');
const { sendCommandToServer } = require('../models/blacklist');
const LabelModel = require('../models/labels');


// GET /api/mails - Returns 50 last mails
function getMails(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const combinedMails = [...user.inbox, ...user.sent];
    const uniqueMails = Array.from(new Map(combinedMails.map(item => [item.mail.id, item])).values());
    uniqueMails.sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const start = (page - 1) * PAGE_SIZE;
    const paginated = uniqueMails.slice(start, start + PAGE_SIZE);
    res.json(paginated);
  });
}

// GET /api/inbox
function getInboxMails(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    sorted = [...user.inbox].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const start = (page - 1) * PAGE_SIZE;
    const paginated = sorted.slice(start, start + PAGE_SIZE);
    res.json(paginated);
  });
}


// GET /api/sent
function getSentMails(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const sorted = [...user.sent].sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const start = (page - 1) * PAGE_SIZE;
    const paginated = sorted.slice(start, start + PAGE_SIZE);
    res.json(paginated);
  });
}

// POST /api/mails - Sends a new mail
async function sendMail(req, res) {
  isLoggedIn(req, res, async () => {
    const sender = req.user;
    const { receiver, subject, content } = req.body;

    // Validates that we will receive an array and in the right format
    const receivers = Array.isArray(receiver)
      ? receiver.map(email => email.trim().toLowerCase())
      : [receiver.trim().toLowerCase()];
    // Validate and collect receiver users

    const receiverUsers = [];
    for (const email of receivers) {
      const user = await User.findByEmail(email);
      if (!user) {
        return res.status(404).json({ error: `Receiver not found: ${email}` });
      }
      receiverUsers.push(user);
    }

    // Blacklist check - extract links from mail content and subject
    const contentLinks = extractLinks(content);
    const subjectLinks = extractLinks(subject);
    const links = [...contentLinks, ...subjectLinks];

    for (const link of links) {
      try {
        const response = await sendCommandToServer(`GET ${link}`);
        if (response.includes('true true')) {
          // Send to spam if blacklisted
          const receiverEmails = receiverUsers.map(u => u.email);
          const spamMail = new SpamMail(sender.email, receiverEmails, subject || "(No subject)", content || '');
          sender.sent.unshift({ mail: spamMail, isRead: true, isStarred: false });

          for (const user of receiverUsers) {
            user.spam.unshift({ mail: spamMail, isRead: false, isStarred: false });
          }
          return res.status(201).json({ warning: 'Mail sent to spam due to blacklisted content' });
        }
      } catch (err) {
        return res.sendStatus(500);
      }
    }
    // Create and store the new mail
    const newMail = new Mail(sender.email, receivers, subject || "(No subject)", content || '');

    // Sender gets it as read
    sender.sent.unshift({ mail: newMail, isRead: true, isStarred: false });

    for (const user of receiverUsers) {
      user.inbox.unshift({ mail: newMail, isRead: false, isStarred: false });
    }
    res.status(201).location(`/api/mails/${newMail.id}`).end();
  });
}

// GET /api/mails/:id - Get a specific mail by ID
function getMailById(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);
    const allMails = [...user.inbox, ...user.sent];
    const item = allMails.find(item => item.mail.id === mailId);
    if (!item) {
      return res.status(404).json({ error: 'Mail not found' });
    }
    res.json(item);
  });
}

// PATCH /api/mails/:id - Update mail
async function updateMail(req, res) {
  isLoggedIn(req, res, async () => {
    const user = req.user;
    const mailId = Number(req.params.id);

    if ('isRead' in req.body) {
      const folders = [user.inbox, user.sent, user.spam, user.trash, user.drafts];
      let updated = false;

      for (const folder of folders) {
        const mailItem = folder.find(item => item.mail.id === mailId);
        if (mailItem) {
          mailItem.isRead = req.body.isRead === true;
          updated = true;
          break;
        }
      }
      if (!updated) {
        return res.status(404).json({ error: 'Mail not found in any folder' });
      }

      return res.status(204).end();
    }

    if ('isStarred' in req.body) {
      const { isStarred, mailType } = req.body;
      const folders = [user.inbox, user.sent, user.spam, user.trash, user.drafts];
      let updated = false;

      for (const folder of folders) {
        for (const item of folder) {
          const type = item.mail.type || 'mail';
          if (item.mail && item.mail.id === mailId && type === mailType) {
            item.isStarred = isStarred === true;
            updated = true;
          }
        }
      }

      if (!updated) {
        return res.status(404).json({ error: 'Mail not found in any folder' });
      }

      return res.status(204).end();
    }


    // Check in sent mails
    const sentItem = user.sent.find(item => item.mail.id === mailId);
    if (!sentItem) {
      return res.status(404).json({ error: 'Access denied - only sent mails can be edited' });
    }
    const allowedFields = ['subject', 'content'];
    const updateFields = Object.keys(req.body);
    const invalidFields = updateFields.filter(field => !allowedFields.includes(field));
    if (invalidFields.length > 0) {
      return res.status(400).json({ error: 'Invalid fields in update', invalidFields });
    }
    const subjectLinks = req.body.subject ? extractLinks(req.body.subject) : [];
    const contentLinks = req.body.content ? extractLinks(req.body.content) : [];
    const links = [...subjectLinks, ...contentLinks];
    for (const link of links) {
      try {
        const response = await sendCommandToServer(`GET ${link}`);
        if (response.includes('true true')) {
          return res.status(400).json({ error: 'Update contains blacklisted link', link });
        }
      } catch (err) {
        return res.sendStatus(500);
      }
    }
    // Apply updates
    const mailToEdit = sentItem.mail;
    updateFields.forEach(field => {
      mailToEdit[field] = req.body[field];
    });
    mailToEdit.date = new Date();
    return res.status(204).end();
  });
}

// DELETE /api/mails/:id - Delete a mail by ID
function deleteMail(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = Number(req.params.id);

    const inboxIndex = user.inbox.findIndex(item => item.mail.id === mailId);
    if (inboxIndex !== -1) {
      const { mail, isRead, isStarred } = user.inbox.splice(inboxIndex, 1)[0];
      if (mail.sender === mail.receiver[0]) {
        const sentIndex = user.sent.findIndex(item => item.mail.id === mailId);
        if (sentIndex !== -1) user.sent.splice(sentIndex, 1);
      }
      user.trash.unshift({
        mail: mail,
        isRead: true,
        isStarred: false
      });
      return res.status(204).end();
    }

    const sentIndex = user.sent.findIndex(item => item.mail.id === mailId);
    if (sentIndex !== -1) {
      const { mail, isRead, isStarred } = user.sent.splice(sentIndex, 1)[0];
      if (mail.sender === mail.receiver[0]) {
        const inboxIndex2 = user.inbox.findIndex(item => item.mail.id === mailId);
        if (inboxIndex2 !== -1) user.inbox.splice(inboxIndex2, 1);
      }
      user.trash.unshift({
        mail: mail,
        isRead: true,
        isStarred: false
      });
      return res.status(204).end();
    }

    return res.status(404).json({ error: 'Mail not found' });
  });
}

// GET /api/mails/search/:query
function searchMails(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const query = req.params.query.toLowerCase();
    const allMails = [...user.inbox, ...user.sent];
    const matchedMails = allMails.filter(item => {
      const mail = item.mail;
      return (
        mail.sender.toLowerCase().includes(query) ||
        mail.receiver.some(r => r.toLowerCase().includes(query)) ||
        mail.subject.toLowerCase().includes(query) ||
        mail.content.toLowerCase().includes(query)
      );
    });
    if (matchedMails.length === 0) {
      return res.status(404).json({ error: 'No matching mails found' });
    }

    const uniqueMails = Array.from(new Map(matchedMails.map(item => [item.mail.id, item])).values());
    uniqueMails.sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));
    const recentMails = uniqueMails.slice(0, 50);
    res.json(recentMails);
  });
}

// POST /api/mails/:mailId/:labelId - Add label to mail
function addMailToLabel(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = parseInt(req.params.mailId);
    const labelId = parseInt(req.params.labelId);

    const label = LabelModel.getLabelById(labelId);
    if (!label || label.userId !== user.id) {
      return res.status(404).json({ error: 'Label not found' });
    }
    const allMails = [...user.inbox, ...user.sent];
    const mailEntry = allMails.find(item => item.mail.id === mailId);
    if (!mailEntry) {
      return res.status(404).json({ error: 'Mail not found' });
    }

    addLabelToMail(mailEntry.mail, labelId);
    return res.status(204).end();
  });
}
// DELETE /api/mails/:mailId/:labelId - Remove label from mail
function removeMailFromLabel(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;
    const mailId = parseInt(req.params.mailId);
    const labelId = parseInt(req.params.labelId);

    const label = LabelModel.getLabelById(labelId);
    if (!label || label.userId !== user.id) {
      return res.status(404).json({ error: 'Label not found' });
    }

    const allMails = [...user.inbox, ...user.sent];
    const mailEntry = allMails.find(item => item.mail.id === mailId);
    if (!mailEntry) {
      return res.status(404).json({ error: 'Mail not found' });
    }

    removeLabelFromMail(mailEntry.mail, labelId);
    return res.status(204).end();
  });

}

// GET /api/starred
function getStarred(req, res) {
  isLoggedIn(req, res, () => {
    const user = req.user;

    const allMails = [...user.inbox, ...user.sent, ...user.spam, ...user.trash, ...user.drafts];
    const starred = allMails.filter(item => item.isStarred);

    const uniqueMap = new Map();

    for (const item of starred) {
      const key = `${item.mail.id}_${item.mail.type}`;


      if (uniqueMap.has(key)) {
        const existing = uniqueMap.get(key);
        const preferInbox = existing.folder === 'sent' && item.folder === 'inbox';
        if (preferInbox) {
          uniqueMap.set(key, { ...item, folder: 'inbox' });
        }
      } else {
        // 
        const folder = user.inbox.includes(item) ? 'inbox' :
          user.sent.includes(item) ? 'sent' :
            user.spam.includes(item) ? 'spam' :
              user.trash.includes(item) ? 'trash' :
                'drafts';

        uniqueMap.set(key, { ...item, folder });
      }
    }

    const sorted = Array.from(uniqueMap.values())
      .sort((a, b) => new Date(b.mail.date) - new Date(a.mail.date));

    const page = parseInt(req.query.page) || 1;
    const PAGE_SIZE = 50;
    const start = (page - 1) * PAGE_SIZE;
    const paginated = sorted.slice(start, start + PAGE_SIZE);
    res.json(paginated);
  });
}


// Export all controller functions
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