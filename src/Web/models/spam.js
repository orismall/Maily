let spamId = 0;

class SpamMail {
  constructor(sender, receiver, subject, content) {
    this.id = ++spamId;
    this.sender = sender;
    this.receiver = receiver;
    this.subject = subject;
    this.content = content;
    this.date = new Date();
  }
}

module.exports = SpamMail;
