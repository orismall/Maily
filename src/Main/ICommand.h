#ifndef ICOMMAND_H
#define ICOMMAND_H

#include <string>
#include "BloomFilter.h"
#include "IOutputHandler.h"

using namespace std;

// an interface for handling different functionalities of the program
class ICommand {
public:
    // Execute the command
    virtual void execute(const string& url, BloomFilter& bf, IOutputHandler* outputHandler) = 0;
    // Destructor
    virtual ~ICommand() = default;
};

#endif