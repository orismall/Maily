#include "ThreadPerClientHandler.h"

using namespace std;

// Constructor
ThreadPerClientHandler::ThreadPerClientHandler(App* app) : app(app) {}

// Handles a client by running the App in a new thread
void ThreadPerClientHandler::handleClient(int clientSocket) {
    thread([this, clientSocket]() {
        try {
            // Creates input/output handlers for the client
            IInputHandler* inputHandler = new SocketInputHandler(clientSocket);
            IOutputHandler* outputHandler = new SocketOutputHandler(clientSocket);
            // Constructs App for a client
            App app(
                this->app->getCommands(),
                this->app->getDataManager(),
                inputHandler,
                outputHandler,
                this->app->getInitParser(),
                this->app->getCommandParser(),
                this->app->getHashStrategy(),
                this->app->getBloomFilter(),
                this->app->getMutex()
            );
            app.run();
            //cleanup
            delete inputHandler;
            delete outputHandler;
            close(clientSocket);
        } catch (const exception& e) {
            close(clientSocket);
        }
    }).detach();
}