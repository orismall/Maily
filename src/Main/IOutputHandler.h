#ifndef IOUTPUTHANDLER_H
#define IOUTPUTHANDLER_H

#include <string>
using namespace std;

// interface for output handling
class IOutputHandler {
public:
    // Virtual function for printing a message
    virtual void print(const string& message) = 0;
    // Destructor
    virtual ~IOutputHandler() = default;
};

#endif
