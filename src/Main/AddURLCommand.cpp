#include "AddURLCommand.h"

using namespace std;

// Handles add url to blacklist functionality of ICommand interface
void AddURLCommand::execute(const string& url, BloomFilter& bf, IOutputHandler* outputHandler) {
    // Use the hash strategy hash
    vector<size_t> hashIndices = bf.applyHash(url);
    // Set the bits in the Bloom filter
    for (size_t index : hashIndices) {
        bf.setBit(index, 1);
    }
    // Add the URL to the blacklist
    bf.add(url);
    
    if (outputHandler != nullptr)
    {
        outputHandler->print("201 Created");
    }
}