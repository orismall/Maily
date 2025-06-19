#ifndef SOCKETOUTPUTHANDLER_H
#define SOCKETOUTPUTHANDLER_H

#include "IOutputHandler.h"
#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdexcept>
#include <string>
#include <sys/unistd.h>

using namespace std;

// Implementation of IOutputHandler that sends the message to the client from the socket
class SocketOutputHandler : public IOutputHandler {
private:
    int clientSocket;
public:
    SocketOutputHandler(int clientSocket);
    // prints the output message to the socket
    void print(const string& message) override;
};

#endif
