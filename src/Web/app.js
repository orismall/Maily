const express = require('express');
const app = express();
const cors = require('cors');
const userRoutes = require('./routes/users');
const blacklistRoutes = require('./routes/blacklist');
const tokenRoutes = require('./routes/tokens');
const mailRoutes = require('./routes/mails');
const labelRoutes = require('./routes/labels');
const draftRoutes = require('./routes/drafts');
const trashRoutes = require('./routes/trash');
const spamRouter = require('./routes/spam');
const mongoose = require('mongoose');

require('dotenv').config();
mongoose.connect(process.env.MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  console.log('✅ Connected to MongoDB');
}).catch((err) => {
  console.error('❌ MongoDB connection error:', err);
});

app.use(cors());

// Limits images to 5mb
app.use(express.json({ limit: "5mb" }));


// Handle user-related routes under /api/users
app.use('/api', userRoutes);

// Handle user-related routes under /api/blacklist
app.use('/api', blacklistRoutes);

// Handle token-related routes under /api/tokens
app.use('/api', tokenRoutes)

// Handle mail-related routes under /api/mails
app.use('/api', mailRoutes);

// Handle label-related routes under /api/labels
app.use('/api', labelRoutes)

// Handle draft-related routes under /api/drafts
app.use('/api', draftRoutes)

// Handle trash-related routes under /api/trash
app.use('/api', trashRoutes);

// Handle spam-related routes under /api/spam
app.use('/api/spam', spamRouter);

// Get the port number from environment variables
const PORT = process.env.PORT;

// JSON formatting
app.set('json spaces', 2);
// Start the server and listen on the specified port
app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));