#ifndef ICOMMANDPARSER_H
#define ICOMMANDPARSER_H

#include <string>
#include <map>
#include "ICommand.h"

using namespace std;

class ICommandParser {
public:
    // Parses the input string
    virtual void parseInput(const string& user_input, map<string, ICommand*> commands) = 0;

    // Returns the user's selected functionality
    virtual string getChoice() const = 0;

    // Returns the user's URL input
    virtual string getLink() const = 0;

    // Validates the input
    virtual bool inputCheck(const string& user_input, map<string, ICommand*> commands) const = 0;

    // Destructor
    virtual ~ICommandParser() = default;
};

#endif
