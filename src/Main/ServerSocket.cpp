#include "ServerSocket.h"

using namespace std;
// Constructor for initialization with port
ServerSocket::ServerSocket(int server_port) {
    port = server_port;
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("Error creating socket");
        throw runtime_error("Error creating socket");
        return;
    }
    sid = sock;
}
// Creates the struct for socket adress and port
void ServerSocket::bindSocket() {
    struct sockaddr_in sin;
    memset(&sin, 0, sizeof(sin));
    sin.sin_family = AF_INET;
    sin.sin_addr.s_addr = INADDR_ANY;
    sin.sin_port = htons(port);
    // Bind the socket to the address and port
    if (bind(sid, (struct sockaddr *) &sin, sizeof(sin)) < 0) {
        throw runtime_error("Error binding socket");
       exit(1);
    }
    if (listen(sid, 5) < 0) {
        throw runtime_error("Error listening on socket");
    }
}

// Accept and return client socket
int ServerSocket::acceptClient() {
    struct sockaddr_in client_sin;
    socklen_t addr_len = sizeof(client_sin);
    int client_sock = accept(sid, (struct sockaddr*)&client_sin, &addr_len);
    if (client_sock < 0) {
        throw runtime_error("Error accepting client");
    }
    return client_sock;
}

// Getter for port
int ServerSocket::getPort() {
    return port;
}

// Getter for socket id
int ServerSocket::getSid() {
    return sid;
}
