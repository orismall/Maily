let idCounter = 0
let users = []

// Create and return a new user object
const createUser = (email, password, confirmPassword, firstName, lastName, gender, birthdate, avatar) => {
    const newUser = { 
        id: ++idCounter,
        email,
        password,
        confirmPassword,
        firstName,
        lastName,
        gender,
        birthdate,
        avatar,
        inbox: [],  
        sent: [],
        drafts: [],
        trash: [],
        spam: []
    }
    users.push(newUser)
    return newUser
}
// Find user by ID
const getUserById = (id) => {
    return users.find(u => u.id === id) || null;
};

// Find user info by ID
const getUserInfoById = (id) => {
    const user = users.find(u => u.id === id);
    if (!user) return null;
    const { inbox, sent, ...filteredUser } = user;
    return filteredUser;
};


// Find user by email
function findByEmail(email) {
    return users.find(u => u.email === email)
}

module.exports = {
    createUser,
    getUserById,
    getUserInfoById,
    findByEmail
}