const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);

const mailSchema = new mongoose.Schema({
  mailId: { type: Number}, 
  sender: { type: String, required: true },
  receiver: { type: [String], required: true },
  subject: { type: String, required: false, default: '(No subject)' },
  content: { type: String, required: false, default: '' },
  date: { type: Date, default: Date.now },
  labels: { type: [Number], default: [] }, // Label IDs
  type: { type: String, default: 'mail' }
});

mailSchema.plugin(AutoIncrement, { inc_field: 'mailId' });
module.exports = mailSchema;

