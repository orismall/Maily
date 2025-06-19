#ifndef DELETEURLCOMMAND_H
#define DELETEURLCOMMAND_H

#include "ICommand.h"
#include "IOutputHandler.h"

using namespace std;

// Implementation of one of the functionalities of ICommand
class DeleteURLCommand : public ICommand {

public:
    // Handles delete url from blacklist functionality of ICommand interface
    void execute(const string& url, BloomFilter& bf, IOutputHandler* outputHandler) override;
};

#endif