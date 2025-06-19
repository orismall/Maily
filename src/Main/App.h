#ifndef APP_H
#define APP_H

#include <thread>
#include <mutex>
#include <map>
#include "ICommand.h"
#include "IDataManager.h"
#include "IInputHandler.h"
#include "IOutputHandler.h"
#include "BloomFilter.h"
#include "IInitParser.h"
#include "ICommandParser.h"
#include "IHashStrategy.h"


// Main application class - responsible for executing commands and managing the program lifecycle
class App {
    private:
    // Map that connects between a functionality and its execution implementation 
    map<string, ICommand*> commands;
    IDataManager* dataManager;
    IInputHandler* inputHandler;
    IOutputHandler* outputHandler;
    IInitParser* initializationInputParser;
    ICommandParser* commandInputParser;
    IHashStrategy* hashStrategy;
    BloomFilter& bf;
    mutex& mtx;

    public:
    // Constructor 
    App(map<string, ICommand*> commands, IDataManager* dataManager, IInputHandler* inputHandler, IOutputHandler* outputHandler,
        IInitParser* initializationInputParser, ICommandParser* commandInputParser, IHashStrategy* hashStrategy, BloomFilter& bf, mutex& mtx);

    // Getters
    map<string, ICommand*> getCommands() const;
    IDataManager* getDataManager() const;
    IInitParser* getInitParser() const;
    ICommandParser* getCommandParser() const;
    IHashStrategy* getHashStrategy() const;
    BloomFilter& getBloomFilter() const;
    mutex& getMutex() const;

    void run();
};

#endif