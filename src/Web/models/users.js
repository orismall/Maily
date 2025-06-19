const mongoose = require('mongoose');
const AutoIncrement = require('mongoose-sequence')(mongoose);
const mailSchema = require('./mails');

const userSchema = new mongoose.Schema({
    userId: { type: Number, unique: true },
    email: {type: String, required: true},
    password: {type: String, required: true},
    confirmPassword: {type: String, required: true},
    firstName: {type: String, required: true},
    lastName: {type: String, required: true},
    gender: {type: String, required: true},
    birthdate: {type: Date, required: true},
    avatar: {type: String, required: true},
    mails: {
        inbox: {type: [mailSchema], default: [] },
        sent: {type: [mailSchema], default: [] },
        drafts: {type: [mailSchema], default: [] },
        trash: {type: [mailSchema], default: [] },
        spam: {type: [mailSchema], default: [] }
    }
});

userSchema.plugin(AutoIncrement, { inc_field: 'userId' });
const User = mongoose.model('User', userSchema);
module.exports = User;
