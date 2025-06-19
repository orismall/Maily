#include "SocketInputHandler.h"

using namespace std;

SocketInputHandler::SocketInputHandler(int clientSocket)
    : clientSocket(clientSocket) {}

// Prompts the user and returns input from client socket
string SocketInputHandler::getInput() {
    string input;
    char ch;

    while (true) {
        int bytesReceived = recv(clientSocket, &ch, 1, 0);
        if (bytesReceived < 0) {
            throw runtime_error("Error receiving data from socket");
        } else if (bytesReceived == 0) {
            // Connection closed
            return "";
        }
        // Stop at newline character
        if (ch == '\n') {
            break;
        }

        input += ch;
    }
    return input;
}

