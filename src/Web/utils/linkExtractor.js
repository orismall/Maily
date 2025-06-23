function extractLinks(text) {
    const regex = /(?:(?:file:\/\/\/(?:[A-Za-z]:)?(?:\/[^\s])?)|(?:(?:[A-Za-z][A-Za-z0-9+.\-]*):\/\/)?(?:localhost|(?:[A-Za-z0-9\-]+\.)+[A-Za-z0-9\-]+|(?:\d{1,3}\.){3}\d{1,3})(?::\d+)?(?:\/[^\s]*)?)/g;
    return typeof text === 'string' ? text.match(regex) || [] : [];
}

module.exports = extractLinks;