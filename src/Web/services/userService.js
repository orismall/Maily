const User = require('../models/users');

// Find user by email
async function findUserByEmail(email) {
  return await User.findOne({ email });
}

// Create a new user
async function createUser(data) {
  return await User.create(data);
}

// Find user by userId
async function findUserById(userId) {
  return await User.findById(userId).lean();
}

module.exports = {
  findUserByEmail,
  createUser,
  findUserById
};
