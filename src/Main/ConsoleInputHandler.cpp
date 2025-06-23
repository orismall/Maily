#include "ConsoleInputHandler.h"
using namespace std;

// Prompts the user and returns input from std::cin
string ConsoleInputHandler::getInput() {
    string input;
    getline(cin, input);
    return input;
}
