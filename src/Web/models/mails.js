const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);

const mailSchema = new mongoose.Schema({
  mailId: { type: Number}, 
  sender: { type: String, required: true },
  receiver: { type: [String], required: true },
  subject: { type: String, required: true },
  content: { type: String, required: true },
  date: { type: Date, default: Date.now },
  labels: { type: [Number], default: [] }, // Label IDs
  type: { type: String, default: 'mail' }
}, { _id: false }); // _id will be part of the parent document (e.g. inside User.mails)

mailSchema.plugin(AutoIncrement, { inc_field: 'mailId' });
module.exports = mailSchema;

