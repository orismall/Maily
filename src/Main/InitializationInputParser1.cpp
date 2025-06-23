#include "InitializationInputParser1.h"
using namespace std;

// Constructor
InitializationInputParser1::InitializationInputParser1() : arrayLength(0) {}

// Helps parsing the hash functions in inputCheck and parseInput
int InitializationInputParser1::getNextArgument(const string& str, int& index) {
    int num = 0;
    while (index < str.size() && str[index] >= '0' && str[index] <= '9') {
        // Converts char to int 
        num = num * 10 + (str[index] - '0'); 
        index++;
    }
    return num;
}

// Validates initialization user input
bool InitializationInputParser1::inputCheck(const string& user_input) {
    if (user_input.empty()) {
        return false;
    }
    // Check if all characters are digits or spaces
    for (char c : user_input) {
        if (!((c >= '0' && c <= '9') || c == ' ')) {
            return false;
        }
    }
    int argc = 0;
    int index = 0;
    int number;
    // Counts how many valid numbers are in the input
    while (index < user_input.size()) {
        if (user_input[index] == ' ')
        {
            index++;
            continue;
        }
        number = getNextArgument(user_input, index);
        if (number == 0) {
            return false;
        }
        argc++;
    }
    // Must have at least array length and one hash function
    return argc >= 2; 
}

// Parses initialization user input
void InitializationInputParser1::parseInput(const string& input) {
    if (!inputCheck(input)) {
        return;
    }
    hashFuncs.clear();
    arrayLength = 0;
    int arguments = 0;
    int index = 0;
    int number;
    // Extract the numbers and assign them in the vector
    while (index < input.size()) {
        if (input[index] == ' ')
        {
            index++;
            continue;
        }
        number = getNextArgument(input, index);
        if (arguments == 0) {
            arrayLength = number;
        } else {
            hashFuncs.push_back(number);
        }
        arguments++;
    }
}

// Getter for hash functions
vector<int> InitializationInputParser1::getHashFuncs() const {
    return hashFuncs;
}

// Getter for bits array length
int InitializationInputParser1::getArrayLength() const {
    return arrayLength;
}
