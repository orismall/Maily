#ifndef SOCKETINPUTHANDLER_H
#define SOCKETINPUTHANDLER_H

#include "IInputHandler.h"
#include <string>
#include <iostream>
#include <stdexcept>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/unistd.h>

using namespace std;
// Implementation of IInputHandler that reads input from the socket
class SocketInputHandler : public IInputHandler {
private:
    int clientSocket;
public:
    SocketInputHandler(int clientSocket);
    // Returns user input read from client socket
    string getInput() override;
};

#endif
