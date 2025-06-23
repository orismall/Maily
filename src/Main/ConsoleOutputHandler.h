#ifndef CONSOLEOUTPUTHANDLER_H
#define CONSOLEOUTPUTHANDLER_H

#include "IOutputHandler.h"
#include <iostream>
using namespace std;

// Implementation of IOutputHandler that prints to the console
class ConsoleOutputHandler : public IOutputHandler {
public:
    // Overrides the print method to output the message to the standard console
    void print(const string& message) override;
};

#endif
