#ifndef IDATAMANAGER_H
#define IDATAMANAGER_H

#include "BloomFilter.h"
using namespace std;

// an interface for handling different way to store the data of the program
class IDataManager {
public:
    // Load the bloom filter's current state 
    virtual void loadFrom(BloomFilter& bf) = 0;
    // Saves the bloom filter's current state 
    virtual void saveTo(const BloomFilter& bf) = 0;
    // Checks if a blacklist exists
    virtual bool hasBlackList(const BloomFilter& bf) = 0;
    // Destructor
    virtual ~IDataManager() = default;
};

#endif