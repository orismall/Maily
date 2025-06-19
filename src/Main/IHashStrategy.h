#ifndef IHASHSTRATEGY_H
#define IHASHSTRATEGY_H

#include <string>
#include <vector>

using namespace std;

// Interface for hash strategies
class IHashStrategy {
public:
    // Computes hash values based on input, array size, and chosen hash functions
    virtual vector<size_t> runHash(const string& input, int length, const vector<int>& hashFuncs) const = 0;
    // Destructor
    virtual ~IHashStrategy() = default;
};

#endif
