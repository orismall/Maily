#include <iostream>
#include <string.h>
#include <stdexcept>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/unistd.h>

using namespace std;
// This class manages the creation and maintenance of TCP server socket connection
class ServerSocket {
    private:
    int sid;
    int port;
    public:
    // Constructor for initialization with port
    ServerSocket(int server_port);
    // Getter for socket id
    int getSid();
    // Getter for port
    int getPort();
    // Socket binding
    void bindSocket();
    // Client acceptance
    int acceptClient();
};

