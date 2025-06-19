const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);

const labelSchema = new mongoose.Schema({
  labelId: { type: Number, required: true },
  name: { type: String, required: true },
  color: { type: String, default: '#000000' },
  mailIds: { type: [Number], default: [] }
});

labelSchema.plugin(AutoIncrement, { inc_field: 'labelId' });

const Label = mongoose.model('Label', labelSchema);
module.exports = Label;
