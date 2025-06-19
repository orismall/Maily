#include "SocketOutputHandler.h"
using namespace std;

SocketOutputHandler::SocketOutputHandler(int clientSocket)
    : clientSocket(clientSocket) {}

// Sends the provided message to the socket
void SocketOutputHandler::print(const string& message) {
    // Add newline so the client can recognize the end of the message
    const char* data = message.c_str();
    int totalSent = 0;
    int dataLength = static_cast<int>(message.length());
    while (totalSent < dataLength) {
        int bytesSent = send(clientSocket, data + totalSent, dataLength - totalSent, 0);
        if (bytesSent < 0) {
            throw runtime_error("Failed to send data to client socket");
        }
        totalSent += bytesSent;
    }
}
