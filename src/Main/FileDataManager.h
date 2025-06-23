#ifndef FILEDATAMANAGER_H
#define FILEDATAMANAGER_H

#include "IDataManager.h"
#include <fstream>
#include <string>

// Class to load/save BloomFilter data from/to a file
class FileDataManager : public IDataManager {
public:
    // Load the bloom filter's current state from a file
    void loadFrom(BloomFilter& bf) override;
    // Saves the bloom filter's current state to a file
    void saveTo(const BloomFilter& bf) override;
    // Checks if a blacklist file exists
    bool hasBlackList(const BloomFilter& bf) override;
};

#endif
