const mongoose = require('mongoose');

const labelSchema = new mongoose.Schema({
  name: { type: String, required: true },
  color: { type: String, default: '#000000' },
  mailIds: { type: [Number], default: [] }
});

const Label = mongoose.model('Label', labelSchema);
module.exports = Label;
