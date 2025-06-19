#ifndef BLOOMFILTER_H
#define BLOOMFILTER_H

#include <set>
#include <string>
#include <vector>
#include <fstream>
#include <functional>
#include <stdexcept>
#include "IHashStrategy.h"

using namespace std;

// Class for maintaining the bloom filter
class BloomFilter {
private:
    set<string> blacklist;
    vector<int> array;
    vector<int> hashFuncs;
    int length;
    IHashStrategy* hashStrategy;

public:
    // Default constructor
    BloomFilter();

    // Constructor with parameters
    BloomFilter(set<string> list, int count, vector<int>& funcs, IHashStrategy* hashStrat);

    // Getter for urls
    set<string> getBlacklist() const;

    // Add a URL to the blacklist
    void add(const string& url);

    // Remove a URL from the blacklist
    void remove(const string& url);

    // Check if the blacklist contains a URL
    bool contains(const string& url) const;

    // Getter for bits array
    vector<int> getArray() const;

    // Setter for bits array
    void setArray(int length);

    // Getter for bits array length
    int getLength() const;

    // Returns the value of the bit at the specified index in the bits array.
    int getBit(size_t index) const;

    // Set the bits array in a specific index
    void setBit(size_t index, int value);

    // Getter for hash funcs
    vector<int> getHashFuncs() const;

    // Setter for hash funcs
    void setHashFuncs(vector<int> other);

    // Set the hash strategy 
    void setHashStrategy(IHashStrategy* strategy);

    // Get all hash values
    vector<size_t> applyHash(const string& url) const;
};

#endif