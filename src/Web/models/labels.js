let labelsCounter = 0
let labels = []

// Create and return a new label
const createLabel = (userId, name, color = '#000000') => {
    const newLabel = { id: ++labelsCounter, userId, name , color , mailIds: [] }
    labels.push(newLabel)
    return newLabel
}

// Find label by ID
const getLabelById = (id) => labels.find(l => l.id === id);

// Find label by name
function findLabelByName(name, userId) {
    return labels.find(l => l.name === name && l.userId === userId);
}

// Edit an existing label
const editLabelById = (id, updates) => {
    const label = getLabelById(id);
    if (!label) return null;
    if (updates.name) label.name = updates.name;
    if (updates.color) label.color = updates.color;
    return label;
};

// Delete an existing label
const deleteLabelById = (id) => {
    const index = labels.findIndex(l => l.id === id);
    if (index === -1) {
        return false;
    }
    // Removes the label from the labels array
    labels.splice(index, 1);
    return true;
};

// Returns all labels
const getAllLabels = (userId) => {
    // Returns all labels of a user
    return labels.filter(label => label.userId === userId);
};

module.exports = {
    createLabel,
    getLabelById,
    findLabelByName,
    editLabelById,
    deleteLabelById,
    getAllLabels,
}
