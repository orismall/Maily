const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const mailSchema = new mongoose.Schema({
  sender: { type: String, required: true },
  receiver: { type: [String], required: true },
  subject: { type: String, required: false, default: '(No subject)' },
  content: { type: String, required: false, default: '' },
  date: { type: Date, default: Date.now },
  labels: { type: [Schema.Types.ObjectId], ref: 'Label', default: [] },
  type: { type: String, default: 'mail' }
});

module.exports = mailSchema;

