#ifndef IINPUTHANDLER_H
#define IINPUTHANDLER_H
#include <string>
using namespace std;

// interface for input handling
class IInputHandler {
    public:
        // Virtual function for getting an input
        virtual string getInput() = 0;
        // Destructor
        virtual ~IInputHandler() = default;
        };

#endif
    