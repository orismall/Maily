#include "CheckURLCommand.h"
#include "ConsoleOutputHandler.h"
#include <iostream>

using namespace std;

// Handles check url functionality of ICommand interface
void CheckURLCommand::execute(const string& url, BloomFilter& bf, IOutputHandler* outputHandler) {
    // Get the bit indices using the hash strategy
    vector<size_t> hashIndices = bf.applyHash(url);
    bool bitsCheck = true;
    // Check if all bits are set
    for (size_t index : hashIndices) {
        if (!bf.getBit(index)) {
            bitsCheck = false;
            break;
        }
    }
    // Output based on Bloom filter and blacklist
    if (!bitsCheck) {
        outputHandler->print("200 Ok\n\nfalse");
    } else if (bf.contains(url)) {
        outputHandler->print("200 Ok\n\ntrue true");
    } else {
        outputHandler->print("200 Ok\n\ntrue false");
    }
}