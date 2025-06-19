#include "FileDataManager.h"
#include "AddURLCommand.h"
#include <iostream>
#include <filesystem>

using namespace std;

// Load the bloom filter's current state from a file
void FileDataManager::loadFrom(BloomFilter& bf) {
    // Open file for reading
    ifstream blFile("/usr/data/Blacklist.txt");
    string url;
    AddURLCommand addcmd;
    // Read each line (URL) and add to the BloomFilter
    while (getline(blFile, url)) {
        bf.add(url);
        addcmd.execute(url, bf, nullptr);
    }
    // Close the file
    blFile.close();
}

// Saves the bloom filter's current state to a file
void FileDataManager::saveTo(const BloomFilter& bf) {
    // Open file for writing
    ofstream blacklistFile("/usr/data/Blacklist.txt");
    blacklistFile.exceptions(ofstream::failbit | ofstream::badbit);
    // Write each URL in the blacklist to the file
    for (const string& url : bf.getBlacklist()) {
        blacklistFile << url << '\n';
    }
    // Close the file
    blacklistFile.close();  
}

// Checks if a blacklist file exists
bool FileDataManager::hasBlackList(const BloomFilter& bf) {
    bool blacklistFileExists = ifstream("/usr/data/Blacklist.txt").good();
    return blacklistFileExists;
}