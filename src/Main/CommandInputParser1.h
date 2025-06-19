#ifndef COMMANDINPUTPARSER1_H
#define COMMANDINPUTPARSER1_H

#include <string>
#include "ICommandParser.h"
#include <regex>

using namespace std;


// Implementation for handling parsing a command input
class CommandInputParser1 : public ICommandParser {
private:
    string choice;
    string link;

public:
    // Constructor
    CommandInputParser1();

    // Parses command user input
    void parseInput(const string& user_input, map<string, ICommand*> commands) override;

    // Getter for user's functionality choice
    string getChoice() const override;

    // Getter for user's URL input
    string getLink() const override;

    // Validates command user input
    bool inputCheck(const string& user_input, map<string, ICommand*> commands) const override;
};

#endif
