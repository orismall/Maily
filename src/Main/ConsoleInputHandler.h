#ifndef CONSOLEINPUTHANDLER_H
#define CONSOLEINPUTHANDLER_H

#include "IInputHandler.h"
#include <string>
#include <iostream>

using namespace std;
// Implementation of IInputHandler that reads input from the console
class ConsoleInputHandler : public IInputHandler {
public:
// Returns user input read from standard input
    string getInput() override;
};

#endif
