#ifndef HASHSTRATEGY1_H
#define HASHSTRATEGY1_H

#include "IHashStrategy.h"
#include <vector>
#include <string>

using namespace std;

// A simple hash strategy using std::hash and repeated hashing for exercise 1
class HashStrategy1 : public IHashStrategy {
    public:
        // Compute multiple hashes by repeating base hash
        vector<size_t> runHash(const string& input, int length, const vector<int>& hashFuncs) const override;
};
#endif
