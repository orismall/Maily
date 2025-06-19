#ifndef CHECKURLCOMMAND_H
#define CHECKURLCOMMAND_H

#include "ICommand.h"
#include "IOutputHandler.h"

// Implementation of one of the functionalites of ICommand
class CheckURLCommand : public ICommand {
    public:
    // Handles check url functionality of ICommand interface
    void execute(const std::string& url, BloomFilter& bf, IOutputHandler* outputHandler) override;
};

#endif