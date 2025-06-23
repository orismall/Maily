#include "App.h"
#include "BloomFilter.h"
#include "InitializationInputParser1.h"
#include "CommandInputParser1.h"
#include "FileDataManager.h"
#include "AddURLCommand.h"
#include "DeleteURLCommand.h"
#include "CheckURLCommand.h"
#include "ConsoleInputHandler.h"
#include "ConsoleOutputHandler.h"
#include "ServerSocket.h"
#include "SocketInputHandler.h"
#include "SocketOutputHandler.h"
#include "HashStrategy1.h"
#include "ThreadPerClientHandler.h"
#include <fstream>
#include <string>
#include <iostream>

using namespace std;

// Constructor implementation
App::App(map<string, ICommand*> commands, IDataManager* dataManager, IInputHandler* inputHandler, IOutputHandler* outputHandler,
    IInitParser* initializationInputParser, ICommandParser* commandInputParser, IHashStrategy* hashStrategy, BloomFilter& bloomfilter, mutex& mutex)
    : commands(commands), dataManager(dataManager), inputHandler(inputHandler), outputHandler(outputHandler),
    initializationInputParser(initializationInputParser), commandInputParser(commandInputParser),hashStrategy(hashStrategy), bf(bloomfilter), mtx(mutex) {}

// Getters
map<string, ICommand*> App::getCommands() const {
    return commands;
}

IDataManager* App::getDataManager() const {
    return dataManager;
}

IInitParser* App::getInitParser() const {
    return initializationInputParser;
}

ICommandParser* App::getCommandParser() const {
    return commandInputParser;
}

IHashStrategy* App::getHashStrategy() const {
    return hashStrategy;
}

BloomFilter& App::getBloomFilter() const {
    return bf;
}

mutex& App::getMutex() const {
    return mtx;
}

// runs the program app
void App::run() {
    // Commands loop
    while (true) {
        string user_input = inputHandler->getInput();
        if (commandInputParser ->inputCheck(user_input, commands)) {
            commandInputParser ->parseInput(user_input, commands);
            // Uses the map to differentiate between functionalities
            // Locks the execute functions to handle shared data access
            lock_guard<mutex> lock(mtx);
            commands[commandInputParser ->getChoice()]->execute(commandInputParser ->getLink(), bf, outputHandler);
            dataManager->saveTo(bf);
        }
        else{
            // invalid command
            outputHandler->print("400 Bad Request");
        } 
    }
}

// Performs as the main function of the program
int main(int argc, char** argv) {
    // Checks if argv isn't in the right size
    if (argc < 3) {
        return 1;
     }

    // Checks if port number is out of valid range [1024, 49151]
    int port = atoi(argv[1]);
    if (port < 1024 || port > 49151) {
        return 1;
    }

    // Assemble init string from argv[2] to argv[argc - 1]
    string initStr;
    for (int i = 2; i < argc; i++) {
         initStr += argv[i];
         if (i < argc - 1) initStr += " ";
     }
     // Checks if argv isn't valid
     IInitParser* iip = new InitializationInputParser1();
     if (!iip->inputCheck(initStr)) {
         delete iip;
         return 1;
     }
    try {
        ServerSocket server_socket(atoi(argv[1]));
        server_socket.bindSocket();
        // defines all necessary fields according to the app needs - can change for maintaing multiple implementations according to choice
        map<string, ICommand*> commands;
        IDataManager* fileManager = new FileDataManager();
        ICommand* add_url = new AddURLCommand();
        ICommand* check_url = new CheckURLCommand();
        ICommand* delete_url = new DeleteURLCommand();
        ICommandParser* cip = new CommandInputParser1();
        IHashStrategy* strategy = new HashStrategy1();
        // defines a mapping of the different functionalities to commands
        commands["POST"] = add_url;
        commands["GET"] = check_url;
        commands["DELETE"] = delete_url;
        // defines global bloom filter
        BloomFilter bf;
        // parses the input to the bloom filter's fields
        iip->parseInput(initStr);
        int size = iip->getArrayLength();
        bf.setArray(size);
        bf.setHashFuncs(iip->getHashFuncs());
        bf.setHashStrategy(strategy);
        mutex mtx; 
        // Check if a blacklist file already exists
        if (fileManager->hasBlackList(bf)) {
            fileManager->loadFrom(bf);
        }   
        // Creates base app for each thread
        App base(commands, fileManager, nullptr, nullptr, iip, cip, strategy, bf, mtx);
        // Creates thread handler
        IThreadHandler* tpch = new ThreadPerClientHandler(&base);
        // Keeps accepting clients
        while (true) {
            int clientSocket = server_socket.acceptClient();
            tpch->handleClient(clientSocket);
        }
        // Cleanup
        delete add_url;
        delete check_url;
        delete delete_url;
        delete fileManager;
        delete iip;
        delete cip;
        delete strategy;
    } catch (const exception& e) {
        return 1;
    }
    return 0;
}