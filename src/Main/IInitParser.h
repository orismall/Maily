#ifndef IINITPARSER_H
#define IINITPARSER_H

#include <vector>
#include <string>

using namespace std;

class IInitParser {
public:

    // Parses the input string
    virtual void parseInput(const string& input) = 0;

    // Returns the parsed hash function indices
    virtual vector<int> getHashFuncs() const = 0;

    // Returns the length of the bit array
    virtual int getArrayLength() const = 0;

    // Validates initialization user input
    virtual bool inputCheck(const string& user_input) = 0;

    // Destructor
    virtual ~IInitParser() = default;
};

#endif
