#include "CommandInputParser1.h"

using namespace std;

// Constructor
CommandInputParser1::CommandInputParser1() : choice(""), link("") {}

// Validates command user input
bool CommandInputParser1::inputCheck(const string &user_input, map<string, ICommand *> commands) const
{
    if (user_input.length() < 3)
        return false;

    int i = 0;
    // Skip leading spaces
    while (i < user_input.length() && user_input[i] == ' ')
        i++;

    // Read command word
    int start = i;
    while (i < user_input.length() && user_input[i] != ' ')
        i++;
    string command = user_input.substr(start, i - start);
    if (commands.find(command) == commands.end())
    {
        return false;
    }
    // Skip spaces after command
    while (i < user_input.length() && user_input[i] == ' ')
        i++;
    // No URL
    if (i >= user_input.length())
        return false;

    // Read URL
    start = i;
    while (i < user_input.length() && user_input[i] != ' ')
        i++;
    // No URL
    if (start == i)
        return false;
    // URL isnt valid check
    static const regex url_pattern(R"(^(?:(?:file:///(?:[A-Za-z]:)?(?:/[^\s])?)|(?:(?:[A-Za-z][A-Za-z0-9+.\-])://)?(?:localhost|(?:[A-Za-z0-9\-]+\.)+[A-Za-z0-9\-]+|(?:\d{1,3}\.){3}\d{1,3})(?::\d+)?(?:/[^\s]*)?)$)");
    string url = user_input.substr(start, i - start);
    static const regex email_pattern(R"(^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$)");

    if (!regex_match(url, url_pattern) && !regex_match(url, email_pattern))
    {
        return false;
    }

    // Check for anything beyond the URL
    while (i < user_input.length())
    {
        if (user_input[i] != ' ')
            return false;
        i++;
    }

    return true;
}

// Parses command user input
void CommandInputParser1::parseInput(const string &user_input, map<string, ICommand *> commands)
{
    if (!inputCheck(user_input, commands))
    {
        return;
    }
    int i = 0;
    while (i < user_input.length() && user_input[i] == ' ')
        i++;

    // Extract command
    int start = i;
    while (i < user_input.length() && user_input[i] != ' ')
        i++;
    choice = user_input.substr(start, i - start);

    // Skip spaces after command
    while (i < user_input.length() && user_input[i] == ' ')
        i++;

    // Extract URL
    start = i;
    while (i < user_input.length() && user_input[i] != ' ')
        i++;
    link = user_input.substr(start, i - start);
}

// Getter for user's functionality choice
string CommandInputParser1::getChoice() const
{
    return choice;
}

// Getter for user's URL input
string CommandInputParser1::getLink() const
{
    return link;
}
