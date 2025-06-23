#ifndef INITIALIZATIONINPUTPARSER1_H
#define INITIALIZATIONINPUTPARSER1_H

#include <vector>
#include <string>
#include "IInitParser.h"

using namespace std;

// Implementation for handling parsing of initialization input
class InitializationInputParser1 : public IInitParser {
private:
    vector<int> hashFuncs;
    int arrayLength;

public:
    // Constructor
    InitializationInputParser1();

    // Parses initialization user input
    void parseInput(const string& input) override;

    // Getter for hash functions
    vector<int> getHashFuncs() const override;

    // Getter for bits array length
    int getArrayLength() const override;

    // Validates initialization user input
    bool inputCheck(const string& user_input) override;
    
    // Helps parse the hash functions in inputCheck and parseInput
    int getNextArgument(const string& str, int& index);
};

#endif
