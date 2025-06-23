#include "HashStrategy1.h"
#include <functional>
#include <iostream>

using namespace std;

// Apply std::hash repeatedly according to values in hashFuncs
vector<size_t> HashStrategy1::runHash(const string& url, int length, const vector<int>& hashFuncs) const {
    vector<size_t> results;
    // runs for every func in the vector
    for (int reps : hashFuncs) {
        size_t hashValue = hash<string>()(url);
        // Apply hash 'reps' times
        for (int i = 1; i < reps; i++) {
            hashValue = hash<string>()(to_string(hashValue));
        }
        // Final result mod array length
        results.push_back(hashValue % length);
    }
    return results;
}