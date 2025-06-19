const Label = require('./labels');


// mailID tracking
let mailId = 0;

class Mail {
  constructor(sender, receivers, subject, content, labels = []) {
    this.id = ++mailId;
    this.sender = sender;
    this.receiver = receivers;
    this.subject = subject;
    this.content = content;
    this.date = new Date();
    this.labels = labels;
    this.type = 'mail';
  }
}

function addLabelToMail(mail, labelId) {
  if (!mail.labels.includes(labelId)) {
    mail.labels.push(labelId);
    const label = Label.getLabelById(labelId);
    if (label && !label.mailIds.includes(mail.id)) {
      label.mailIds.push(mail.id);
    }
  }
}

function removeLabelFromMail(mail, labelId) {
  const labelIndex = mail.labels.indexOf(labelId);
  if (labelIndex !== -1) {
    mail.labels.splice(labelIndex, 1);
  }
  const label = Label.getLabelById(labelId);
  if (label) {
    const mailIndex = label.mailIds.indexOf(mail.id);
    if (mailIndex !== -1) {
      label.mailIds.splice(mailIndex, 1);
    }
  }
}


module.exports = {
  Mail,
  addLabelToMail,
  removeLabelFromMail
};
