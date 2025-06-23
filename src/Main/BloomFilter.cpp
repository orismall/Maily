#include "BloomFilter.h"
#include <iostream>
#include <stdexcept>

using namespace std;

// Default constructor
BloomFilter::BloomFilter() {
    length = 0;
    array = vector<int>();
    hashFuncs = vector<int>();
    hashStrategy = nullptr;
}
// Constructor with parameters
BloomFilter::BloomFilter(set<string> list, int count, vector<int>& funcs, IHashStrategy* hashStrat) {
    blacklist = list;
    length = count;
    hashFuncs = funcs;
    hashStrategy = hashStrat;
    array = vector<int>(count, 0);
}

// Getter for blacklist
set<string> BloomFilter::getBlacklist() const {
    return blacklist;
}

// Add a URL to the blacklist
void BloomFilter::add(const string& url) {
    blacklist.insert(url);
}

// Remove a URL from the blacklist
void BloomFilter::remove(const string& url) {
    blacklist.erase(url);
}

// Check if URL exists in blacklist
bool BloomFilter::contains(const string& url) const {
    return blacklist.find(url) != blacklist.end();
}

// Getter for bit array
vector<int> BloomFilter::getArray() const {
    return array;
}

// Setter for array and size
void BloomFilter::setArray(int size) {
    array = vector<int>(size);
    length = size;
}

// Getter for array length
int BloomFilter::getLength() const {
    return length;
}

// Get bit at specific index
int BloomFilter::getBit(size_t index) const {
    return array[index];
}

// Set bit at specific index
void BloomFilter::setBit(size_t index, int value) {
    if (index >= 0 && index < length) {
        array[index] = value;
    }
}

// Getter for hash functions
vector<int> BloomFilter::getHashFuncs() const {
    return hashFuncs;
}

// Setter for hash functions
void BloomFilter::setHashFuncs(vector<int> other) {
    hashFuncs = other;
}

// Set the hash strategy
void BloomFilter::setHashStrategy(IHashStrategy* strategy) {
    hashStrategy = strategy;
}

// Apply all hash functions to the input string
vector<size_t> BloomFilter::applyHash(const string& url) const {
    return hashStrategy->runHash(url, length, hashFuncs);
}
