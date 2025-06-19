#ifndef ADDURLCOMMAND_H
#define ADDURLCOMMAND_H

#include <iostream>
#include "ICommand.h"
#include "IOutputHandler.h"

using namespace std;

// Implementation of one of the functionalites of ICommand
class AddURLCommand : public ICommand {
public:
// Handles add url to blacklist functionality of ICommand interface
void execute(const string& url, BloomFilter& bf, IOutputHandler* outputHandler) override;
};

#endif