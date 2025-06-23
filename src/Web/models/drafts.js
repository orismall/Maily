let draftId = 0;

class Draft {
  constructor(sender, receiver , subject, content) {
    this.id = ++draftId;
    this.sender = sender;
    this.receiver = receiver;
    this.subject = subject;
    this.content = content;
    this.type = 'draft';
    this.date = new Date();
  }
}

module.exports = Draft;
