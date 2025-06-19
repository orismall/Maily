#include "DeleteURLCommand.h"

using namespace std;

// Executes the delete command on a given URL using the provided BloomFilter instance
void DeleteURLCommand::execute(const string& url, BloomFilter& bf, IOutputHandler* outputHandler) {
    // If the BloomFilter contains the URL, remove it and return 204 status
    if (bf.contains(url)) {
        bf.remove(url);
        outputHandler->print("204 No Content");
    } else {
        // URL was not in the blacklist, return 404
        outputHandler->print("404 Not Found");
    }
}
