const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const labelSchema = new mongoose.Schema({
  user: { type: Schema.Types.ObjectId, ref: 'User', required: true },
  name: { type: String, required: true },
  color: { type: String, default: '#000000' },
  mailIds: { type: [Schema.Types.ObjectId], default: [] } 
});

const Label = mongoose.model('Label', labelSchema);
module.exports = Label;
