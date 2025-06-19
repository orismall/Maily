#include <gtest/gtest.h>
#include "BloomFilter.h"
#include "AddURLCommand.h"
#include "CheckURLCommand.h"
#include "DeleteURLCommand.h"
#include "ConsoleInputHandler.h"
#include "ConsoleOutputHandler.h"
#include "InitializationInputParser1.h"
#include "CommandInputParser1.h"
#include "FileDataManager.h"
#include "HashStrategy1.h"
#include "ServerSocket.h"
#include "SocketInputHandler.h"
#include "SocketOutputHandler.h"
#include <string>
#include <iostream>
#include <fstream>
#include <map>
#include <filesystem>

using namespace std;
// Checks the initialization input check - sanity tests
TEST(InitializationLineTest, ValidInitializationLines) {
    InitializationInputParser1 parser;
    EXPECT_TRUE(parser.inputCheck("8 1 2"));
    EXPECT_TRUE(parser.inputCheck("8 1"));
    EXPECT_TRUE(parser.inputCheck("8 2"));
    EXPECT_TRUE(parser.inputCheck("1000 5 10"));
    EXPECT_TRUE(parser.inputCheck("3 2"));
    EXPECT_TRUE(parser.inputCheck(" 3 2"));
    EXPECT_TRUE(parser.inputCheck("3 2 "));
    EXPECT_TRUE(parser.inputCheck("3  2"));
    EXPECT_TRUE(parser.inputCheck("09 2"));
}
// Checks the initialization input check - negative tests
TEST(InitializationLineTest, InvalidInitializationLines) {
    InitializationInputParser1 parser;
    EXPECT_FALSE(parser.inputCheck(""));
    EXPECT_FALSE(parser.inputCheck(" "));
    EXPECT_FALSE(parser.inputCheck("8"));
    EXPECT_FALSE(parser.inputCheck("8 1 a"));
    EXPECT_FALSE(parser.inputCheck("letters"));
    EXPECT_FALSE(parser.inputCheck("8,1,2"));
    EXPECT_FALSE(parser.inputCheck("8 0 2"));
    EXPECT_FALSE(parser.inputCheck("0 1 2"));
}
// Checks the initialization input check and parsing
TEST(InitializationInputParser1Test, ValidInputWithMultipleNumbers) {
    string user_input = "8 1 2 3 4";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    EXPECT_EQ(initParser.getArrayLength(), 8);
    vector<int> expected = {1, 2, 3, 4};
    EXPECT_EQ(initParser.getHashFuncs(), expected);
}
// Checks the initialization input check and parsing
TEST(InitializationInputParser1Test, ValidInputWithOneHashFunction) {
    string user_input = "5 10";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    EXPECT_EQ(initParser.getArrayLength(), 5);
    vector<int> expected = {10};
    EXPECT_EQ(initParser.getHashFuncs(), expected);
}
// Checks the initialization input check and parsing
TEST(InitializationInputParser1Test, ClassesTest1) {
    string user_input = "5 10";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    int length = initParser.getArrayLength();
    HashStrategy1 hs1;
    BloomFilter bf(bl, length, hashf,&hs1);
    EXPECT_EQ(bf.getLength(), 5);
    vector<int> expected = {10};
    EXPECT_EQ(bf.getHashFuncs(), expected);
}
// Checks the initialization input check and parsing
TEST(InitializationInputParser1Test, ClassesTest2) {
    string user_input = "8 1 2 3 4";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    int length = initParser.getArrayLength();
    HashStrategy1 hs1;
    BloomFilter bf(bl, length, hashf,&hs1);
    EXPECT_EQ(bf.getLength(), 8);
    vector<int> expected = {1, 2, 3, 4};
    EXPECT_EQ(bf.getHashFuncs(), expected);
}
// Checks the initialization input check and parsing
TEST(InitializationInputParser1Test, ClassesTest3) {
    string user_input = "8 1  2 3 4 ";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    int length = initParser.getArrayLength();
    HashStrategy1 hs1;
    BloomFilter bf(bl, length, hashf,&hs1);
    EXPECT_EQ(bf.getLength(), 8);
    vector<int> expected = {1, 2, 3, 4};
    EXPECT_EQ(bf.getHashFuncs(), expected);
}
// Checks the command input check and parsing - sanity tests
TEST(CommandLineTest, ValidInitializationLines) {
    map<string, ICommand*> commands = {
        {"1", nullptr},
        {"2", nullptr},
        {"3", nullptr}
    };
    CommandInputParser1 parser;
    EXPECT_TRUE(parser.inputCheck("1 www.orismall.com", commands));   
    EXPECT_TRUE(parser.inputCheck("2 www.orismall.com", commands));
    EXPECT_TRUE(parser.inputCheck(" 2 www.orismall.com", commands));
    EXPECT_TRUE(parser.inputCheck("2 www.orismall.com  ", commands));
    EXPECT_TRUE(parser.inputCheck("2   www.orismall.com", commands));
    EXPECT_TRUE(parser.inputCheck(" 2  www.orismall.com ", commands));
    EXPECT_TRUE(parser.inputCheck("2 www.orismall.com0", commands));
    EXPECT_TRUE(parser.inputCheck("3 www.orismall.com0", commands));
    EXPECT_TRUE(parser.inputCheck("3   www.orismall.com", commands));
}
// Checks the command input check and parsing - negative tests
TEST(CommandLineTest, InvalidInitializationLines) {
    map<string, ICommand*> commands = {
        {"1", nullptr},
        {"2", nullptr},
        {"3", nullptr}
    };
    CommandInputParser1 parser;
    EXPECT_FALSE(parser.inputCheck("", commands));
    EXPECT_FALSE(parser.inputCheck(" ", commands));
    EXPECT_FALSE(parser.inputCheck("1", commands));
    EXPECT_FALSE(parser.inputCheck("1 ", commands));
    EXPECT_FALSE(parser.inputCheck("2 www www", commands));
    EXPECT_FALSE(parser.inputCheck("3 ", commands));
}

// Checks the command input check and parsing - sanity tests
TEST(NewCommandLineTests, ValidInitializationLines) {
    map<string, ICommand*> commands = {
        {"POST", nullptr},
        {"GET", nullptr},
        {"DELETE", nullptr}
    };
    CommandInputParser1 parser;
    EXPECT_TRUE(parser.inputCheck("POST www.orismall.com", commands));   
    EXPECT_TRUE(parser.inputCheck("GET www.orismall.com", commands));
    EXPECT_TRUE(parser.inputCheck(" GET www.orismall.com", commands));
    EXPECT_TRUE(parser.inputCheck("GET www.orismall.com  ", commands));
    EXPECT_TRUE(parser.inputCheck("GET   www.orismall.com", commands));
    EXPECT_TRUE(parser.inputCheck(" GET  www.orismall.com ", commands));
    EXPECT_TRUE(parser.inputCheck("GET www.orismall.com0", commands));
    EXPECT_TRUE(parser.inputCheck("DELETE www.orismall.com0", commands));
    EXPECT_TRUE(parser.inputCheck("DELETE   www.orismall.com", commands));
}
// Checks the command input check and parsing - negative tests
TEST(NewCommandLineTests, InvalidInitializationLines) {
    map<string, ICommand*> commands = {
        {"POST", nullptr},
        {"GET", nullptr},
        {"DELETE", nullptr}
    };
    CommandInputParser1 parser;
    EXPECT_FALSE(parser.inputCheck("", commands));
    EXPECT_FALSE(parser.inputCheck(" ", commands));
    EXPECT_FALSE(parser.inputCheck("     ", commands));
    EXPECT_FALSE(parser.inputCheck("POST", commands));
    EXPECT_FALSE(parser.inputCheck("POST ", commands));
    EXPECT_FALSE(parser.inputCheck("GET www www", commands));
    EXPECT_FALSE(parser.inputCheck("DELETE ", commands));
}


// Checks the command input check and parsing
TEST(CommandInputParser1Test, ValidInputWithMultipleNumbers) {
    map<string, ICommand*> commands = {
        {"1", nullptr},
        {"2", nullptr},
        {"3", nullptr}
    };
    CommandInputParser1 cmdParser;
    string user_input = "1 www.orismall.com";
    cmdParser.parseInput(user_input, commands);
    EXPECT_EQ(cmdParser.getChoice(), "1");
    string expected = "www.orismall.com";
    EXPECT_EQ(cmdParser.getLink(), expected);
}
// Checks the command input check and parsing
TEST(CommandInputParser1Test, ValidInputWithOneHashFunction) {
    map<string, ICommand*> commands = {
        {"1", nullptr},
        {"2", nullptr},
        {"3", nullptr}
    };
    CommandInputParser1 cmdParser;
    string user_input = "2 www.itayturiel.com";
    cmdParser.parseInput(user_input, commands);
    EXPECT_EQ(cmdParser.getChoice(), "2");
    string expected = "www.itayturiel.com";
    EXPECT_EQ(cmdParser.getLink(), expected);
}
// Checks the command input check and parsing
TEST(CommandInputParser1Test, AnotherCheck) {
    map<string, ICommand*> commands = {
        {"1", nullptr},
        {"2", nullptr},
        {"3", nullptr}
    };
    CommandInputParser1 cmdParser;
    string user_input = "3  www.itayturiel.com ";
    cmdParser.parseInput(user_input, commands);
    EXPECT_EQ(cmdParser.getChoice(), "3");
    string expected = "www.itayturiel.com";
    EXPECT_EQ(cmdParser.getLink(), expected);
}

// Checks the command input check and parsing
TEST(NewCommandInputParserTest, ValidInputWithMultipleNumbers) {
    map<string, ICommand*> commands = {
        {"POST", nullptr},
        {"GET", nullptr},
        {"DELETE", nullptr}
    };
    CommandInputParser1 cmdParser;
    string user_input = "POST www.orismall.com";
    cmdParser.parseInput(user_input, commands);
    EXPECT_EQ(cmdParser.getChoice(), "POST");
    string expected = "www.orismall.com";
    EXPECT_EQ(cmdParser.getLink(), expected);
}

// Checks the command input check and parsing
TEST(NewCommandInputParserTest, AnotherCheck) {
    map<string, ICommand*> commands = {
        {"POST", nullptr},
        {"GET", nullptr},
        {"DELETE", nullptr}
    };
    CommandInputParser1 cmdParser;
    string user_input = "DELETE  www.itayturiel.com ";
    cmdParser.parseInput(user_input, commands);
    EXPECT_EQ(cmdParser.getChoice(), "DELETE");
    string expected = "www.itayturiel.com";
    EXPECT_EQ(cmdParser.getLink(), expected);
}

// Checks the add url functionality - sanity tests
TEST(addURLTest, oneHashOneURL) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url ="www.example.com0";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf,&hs1);
    AddURLCommand add;
    add.execute(url, bf, nullptr);
    EXPECT_TRUE(bf.contains(url));
    vector<size_t> bits = bf.applyHash(url);
    for (size_t bit : bits) {
        EXPECT_TRUE(bf.getBit(bit));
    }
}
// Checks the add url functionality - sanity tests
TEST(addURLTest, multipeleHashOneURL) {
    string user_input = "8 1 2";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url ="www.example.com0";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf,&hs1);
    AddURLCommand add;
    add.execute(url, bf, nullptr);
    EXPECT_TRUE(bf.contains(url));
    vector<size_t> bits = bf.applyHash(url);
    for (size_t bit : bits) {
        EXPECT_TRUE(bf.getBit(bit));
    }
}
// Checks the add url functionality - sanity tests
TEST(addURLTest, oneHashMultipleURLs) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url1 ="www.example.com1";
    string url2 ="www.example.com2";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf,&hs1);
    AddURLCommand add;
    add.execute(url1, bf, nullptr);
    add.execute(url2, bf, nullptr);
    EXPECT_TRUE(bf.contains(url1));
    EXPECT_TRUE(bf.contains(url2));
    vector<size_t> bits1 = bf.applyHash(url1);
    for (size_t bit : bits1) {
        EXPECT_TRUE(bf.getBit(bit));
    }
    vector<size_t> bits2 = bf.applyHash(url2);
    for (size_t bit : bits2) {
        EXPECT_TRUE(bf.getBit(bit));
    }
}
// Checks the add url functionality - sanity tests
TEST(addURLTest, multipleHashMultipleURLs) {
    string user_input = "8 2 3";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url1 ="www.example.com1";
    string url2 ="www.example.com2";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf,&hs1);
    AddURLCommand add;
    add.execute(url1, bf, nullptr);
    add.execute(url2, bf, nullptr);
    EXPECT_TRUE(bf.contains(url1));
    EXPECT_TRUE(bf.contains(url2));
    vector<size_t> bits1 = bf.applyHash(url1);
    for (size_t bit : bits1) {
        EXPECT_TRUE(bf.getBit(bit));
    }
    vector<size_t> bits2 = bf.applyHash(url2);
    for (size_t bit : bits2) {
        EXPECT_TRUE(bf.getBit(bit));
    }
}

// Checks the check url functionality - sanity tests
TEST(checkURLTest, inList) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url = "www.example.com0";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf,&hs1);
    AddURLCommand add;
    ConsoleOutputHandler coh;
    CheckURLCommand check;
    add.execute(url, bf, nullptr);
    // catches the print and check if CheckURLCommand.execute worked well
    stringstream buffer;
    streambuf* oldCout = cout.rdbuf();
    cout.rdbuf(buffer.rdbuf()); 
    check.execute("www.example.com0", bf, &coh);
    cout.rdbuf(oldCout);
    string output = buffer.str();
    if (!output.empty() && output.back() == '\n') {
        output.pop_back();
    }
    EXPECT_EQ(output, "200 Ok\n\ntrue true");
}

// Checks the check url functionality - sanity tests
TEST(checkURLTest, notInList) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url = "www.example.com0";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf,&hs1);
    AddURLCommand add;
    ConsoleOutputHandler coh;
    CheckURLCommand check;
    add.execute(url, bf, nullptr);
    // catches the print and check if CheckURLCommand.execute worked well
    stringstream buffer;
    streambuf* oldCout = cout.rdbuf();
    cout.rdbuf(buffer.rdbuf());
    check.execute("www.example.com2", bf, &coh);
    cout.rdbuf(oldCout);
    string output = buffer.str();
    if (!output.empty() && output.back() == '\n') {
        output.pop_back();
    }
    EXPECT_EQ(output, "200 Ok\n\nfalse");
}

// Tests deleting an existing URL - ensures the URL is removed from the Bloom filter and correct output is printed
TEST(deleteURLTest, deleteExistingURL) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url = "www.example.com0";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf, &hs1);
    AddURLCommand add;
    ConsoleOutputHandler coh;
    DeleteURLCommand del;
    add.execute(url, bf,nullptr);
    // Capture printed output
    stringstream buffer;
    streambuf* oldCout = cout.rdbuf();
    cout.rdbuf(buffer.rdbuf());
    del.execute(url, bf,&coh);
    cout.rdbuf(oldCout);
    string output = buffer.str();
    if (!output.empty() && output.back() == '\n') output.pop_back();
    EXPECT_FALSE(bf.contains(url));
    EXPECT_EQ(output, "204 No Content");
}

// Tests trying to delete a URL that doesn't exist - expects 404 response
TEST(deleteURLTest, deleteNonExistingURL) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url = "www.notexists.com";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf, &hs1);
    ConsoleOutputHandler coh;
    DeleteURLCommand del;
    // Capture printed output
    stringstream buffer;
    streambuf* oldCout = cout.rdbuf();
    cout.rdbuf(buffer.rdbuf());
    del.execute(url, bf,&coh);
    cout.rdbuf(oldCout);
    string output = buffer.str();
    if (!output.empty() && output.back() == '\n') output.pop_back();
    EXPECT_EQ(output, "404 Not Found");
}

// Ensures that deleting a URL does not change Bloom filter's bit array
TEST(deleteURLTest, deleteDoesNotChangeBloomFilterBits) {
    string user_input = "8 1";
    InitializationInputParser1 initParser;
    initParser.parseInput(user_input);
    string url = "www.example.com0";
    set<string> bl;
    vector<int> hashf = initParser.getHashFuncs();
    HashStrategy1 hs1;
    BloomFilter bf(bl, initParser.getArrayLength(), hashf, &hs1);
    AddURLCommand add;
    add.execute(url, bf, nullptr);
    // Save Bloom filter bit state before deletion
    vector<bool> bitsBefore;
    for (int i = 0; i < initParser.getArrayLength(); ++i) {
        bitsBefore.push_back(bf.getBit(i));
    }
    ConsoleOutputHandler coh;
    DeleteURLCommand del;
    // Capture output to suppress it from appearing in test results
    stringstream buffer;
    streambuf* oldCout = cout.rdbuf();
    cout.rdbuf(buffer.rdbuf());
    del.execute(url, bf,&coh);
    cout.rdbuf(oldCout); // Restore original cout buffer
    // Compare Bloom filter bit state after deletion
    for (int i = 0; i < initParser.getArrayLength(); ++i) {
        EXPECT_EQ(bitsBefore[i], bf.getBit(i));
    }
}

// Tests that a deleted URL is actually removed from the Blacklist file
TEST(deleteURLTest, deleteURLRemovesFromBlacklistFile) {
    filesystem::create_directory("data");
    // Write a single URL to the blacklist file
    ofstream blOut("data/Blacklist.txt");
    blOut << "www.keep.com\n www.delete.com\n";
    blOut.close();
    BloomFilter bf;
    HashStrategy1 hs1;
    bf.setHashStrategy(&hs1);
    FileDataManager fdm;
    fdm.loadFrom(bf);
    ConsoleOutputHandler coh;
    DeleteURLCommand del;
    // Delete the URL and capture output
    stringstream buffer;
    streambuf* oldCout = cout.rdbuf();
    cout.rdbuf(buffer.rdbuf());
    del.execute("www.delete.com", bf,&coh);
    cout.rdbuf(oldCout);
    // Save updated state to file
    fdm.saveTo(bf);
    // Check that the URL was removed from the file
    ifstream updated("data/Blacklist.txt");
    string line;
    bool deleted = false;
    bool keep = false;
    while (getline(updated, line)) {
        if (line == "www.delete.com") {
            deleted = true;
        }
        if (line == "www.keep.com") {
            keep = true;
        }
    }
    updated.close();
    EXPECT_FALSE(deleted);
    EXPECT_TRUE(keep);
}

// Checks the loading of previous data saved - sanity tests
TEST(LoadDataTest, example) {
    filesystem::create_directory("data");
    // create test data files
    ofstream blOut("/usr/data/Blacklist.txt");
    blOut << "ofeksarusi.com\nitayturiel.com\norismall.com\n";
    blOut.close();
    // Create objects to load data into
    BloomFilter bf;
    HashStrategy1 hs1;
    bf.setHashStrategy(&hs1);
    FileDataManager fdm;
    // Load the data
    fdm.loadFrom(bf);
    // blacklist check
    EXPECT_TRUE(bf.contains("ofeksarusi.com"));
    EXPECT_TRUE(bf.contains("itayturiel.com"));
    EXPECT_TRUE(bf.contains("orismall.com"));
}

// Checks the loading of previous data saved - sanity tests
TEST(LoadDataTest, EmptyFiles) {
    filesystem::create_directory("data");
    // Create empty files
    ofstream("/usr/data/Blacklist.txt").close();
    BloomFilter bf;
    HashStrategy1 hs1;
    bf.setHashStrategy(&hs1);
    FileDataManager fdm;
    fdm.loadFrom(bf);
    // Blacklist should be empty
    EXPECT_FALSE(bf.contains("example.com"));
}

// Checks the saving of data
TEST(SaveStateTest, SaveToFile) {
    filesystem::create_directory("data");
    vector<int> hashf = {2, 3};
    int length = 8;
    set<string> bl;
    HashStrategy1 hs1;
    BloomFilter bf(bl, length, hashf,&hs1);
    bf.setBit(0, 1);
    bf.setBit(3, 1);
    bf.add("orismall.com");
    bf.add("itayturipesh.yahoo");
    FileDataManager fdm;
    fdm.saveTo(bf);
    ifstream blFile("/usr/data/Blacklist.txt");
    string line1, line2;
    getline(blFile, line1);
    getline(blFile, line2);
    blFile.close();
    EXPECT_TRUE(bf.contains("orismall.com"));
    EXPECT_TRUE(bf.contains("itayturipesh.yahoo"));
}

// Checks socket server binding
TEST(SocketTest, ServerBindsSuccessfully) {
    int testPort = 12345;
    ServerSocket serverSocket(testPort);
    EXPECT_NO_THROW(serverSocket.bindSocket());
    close(serverSocket.getSid());   
}

// Checks binding on the same port
TEST(ServerSocketTest, ServerThrowsOnRebindingSamePort) {
    int testPort = 12346;
    ServerSocket serverSocket1(testPort);
    serverSocket1.bindSocket();
    ServerSocket serverSocket2(testPort);
    EXPECT_THROW(serverSocket2.bindSocket(), runtime_error);
    close(serverSocket1.getSid());
}

// Checks for message receiving
TEST(ServerSocketTest, ServerReceivesMessage) {
    int testPort = 12348;
    ServerSocket serverSocket(testPort);
    serverSocket.bindSocket();
    int clientSock = socket(AF_INET, SOCK_STREAM, 0);
    ASSERT_NE(clientSock, -1);
    sockaddr_in serverAddr{};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(testPort);
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    int connectResult = connect(clientSock, (struct sockaddr*)&serverAddr, sizeof(serverAddr));
    ASSERT_NE(connectResult, -1);
    int serverClientSock = serverSocket.acceptClient();
    string messageToSend = "Hello Server!";
    send(clientSock, messageToSend.c_str(), messageToSend.size(), 0);
    char buffer[1024] = {0};
    ssize_t bytesRead = recv(serverClientSock, buffer, sizeof(buffer), 0);
    ASSERT_GT(bytesRead, 0);
    string receivedMessage(buffer, bytesRead);
    EXPECT_EQ(receivedMessage, messageToSend);
    // Cleanup
    close(clientSock);
    close(serverClientSock);
    close(serverSocket.getSid());
}

int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}