cmake_minimum_required(VERSION 3.17)

project(MyExample)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Option to control test building
option(BUILD_TESTS "Build tests" ON)

# Fetch GoogleTest if tests are enabled
if(BUILD_TESTS)
    include(FetchContent)
    FetchContent_Declare(
    googletest
    URL https://github.com/google/googletest/archive/refs/tags/release-1.11.0.zip
    )

    set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
    FetchContent_MakeAvailable(googletest)
endif()

# All files needed to run the main program
set(SRC_FILES
    src/Main/AddURLCommand.cpp
    src/Main/DeleteURLCommand.cpp
    src/Main/App.cpp
    src/Main/BloomFilter.cpp
    src/Main/CheckURLCommand.cpp
    src/Main/CommandInputParser1.cpp
    src/Main/ConsoleInputHandler.cpp
    src/Main/ConsoleOutputHandler.cpp
    src/Main/FileDataManager.cpp
    src/Main/HashStrategy1.cpp
    src/Main/InitializationInputParser1.cpp
    src/Main/ServerSocket.cpp
    src/Main/SocketInputHandler.cpp
    src/Main/SocketOutputHandler.cpp
    src/Main/ThreadPerClientHandler.cpp
)
if(NOT BUILD_TESTS)
    add_executable(Main ${SRC_FILES})
endif()

# Only add tests if enabled
if(BUILD_TESTS)
    # All files needed to run the tests
    set(TEST_FILES
        src/Tests/test.cpp
        src/Main/AddURLCommand.cpp
        src/Main/DeleteURLCommand.cpp
        src/Main/BloomFilter.cpp
        src/Main/CheckURLCommand.cpp
        src/Main/CommandInputParser1.cpp
        src/Main/ConsoleInputHandler.cpp
        src/Main/ConsoleOutputHandler.cpp
        src/Main/FileDataManager.cpp
        src/Main/HashStrategy1.cpp
        src/Main/InitializationInputParser1.cpp
        src/Main/SocketInputHandler.cpp
        src/Main/SocketOutputHandler.cpp
        src/Main/ServerSocket.cpp
    )

    # include directories so that the tests could have access to Main files
    include_directories(src/Main)

    add_executable(Tests ${TEST_FILES})
    target_link_libraries(Tests gtest_main)
endif()