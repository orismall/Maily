#ifndef ITHREADHANDLER_H
#define ITHREADHANDLER_H

using namespace std;

// interface for output handling
class IThreadHandler {
public:
    // Virtual function for creating and running of the client's thread
    virtual void handleClient(int clientSocket) = 0;
    // Destructor
    virtual ~IThreadHandler() = default;
};

#endif
