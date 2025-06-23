#ifndef THREADPERCLIENTHANDLER_H
#define THREADPERCLIENTHANDLER_H

#include <thread>
#include <unistd.h> 
#include "App.h"
#include "IThreadHandler.h"
#include "SocketInputHandler.h"
#include "SocketOutputHandler.h"

// This class handles multiple clients, while each client runs in a distinct thread
class ThreadPerClientHandler : public IThreadHandler {
    private:
        App* app;
    public:
        // Constructor
        ThreadPerClientHandler(App* app);
        // function for creating and running of the client's thread
        void handleClient(int clientSocket) override;
};

#endif