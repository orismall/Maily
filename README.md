# Maily App

## Overview

This project implements **Maily** - a **Gmail-inspired application**, developed as part of an *Advanced Systems Programming* course.

The system includes:  
- **Android Mobile App**  
A Gmail-like mobile application developed with Android Studio. It provides a mobile-friendly interface with screens for Login, Signup, and Inbox. The app communicates with the Node.js web server to perform all email-related operations and manages user sessions locally.

- **MongoDB Database**  
Stores all persistent user data, including account information, mails, labels, and more.
The Node.js server connects to MongoDB to perform all database operations efficiently and securely.

-   **React Web Client**  
A Gmail-like web application developed with React. It provides a user interface via any modern web browser. The web client includes three main screens: Login, Signup, and Inbox. It communicates with the Node.js server to perform all email-related operations.

- **Node.js Web Server (MVC Architecture)**  
  Acts as the RESTful backend for the Gmail-like application. It handles user-related Functionalities such as registration, login, sending and receiving emails, managing labels, and more.  
  It also communicates with the C++ server when validating ,adding or deleting URLs.

- **C++ Multi-threaded TCP Server**  
  Handles blacklist operations (add, check, delete) using a **Bloom Filter** for efficient URL lookups.  
  The server supports multiple clients at the same time, using threads and ensures thread-safe access to shared resources.  
  Blacklisted URLs are persistently stored in `data/Blacklist.txt`.

This part of the project demonstrates:

-	A native **Android mobile application** for full Gmail-like functionality on mobile devices
-	A **MongoDB database**, providing persistent storage for users, mails, labels, and more


### Key Features
-	**Android mobile application**, providing full Gmail-like functionality on mobile devices
-	**Gmail-like React web client** accessible via any modern browser, with a responsive and intuitive UI
-	**Three main UI views** across platforms: Login, Signup, and Inbox
-	**RESTful API** implemented with **Node.js** in an **MVC architecture**, handling all email operations
-	**User authentication** and secure session management across all platforms
-	**Persistent storage** of users, emails, labels, and more using a **MongoDB database**
- **Persistent storage** of blacklisted URLs in `data/Blacklist.txt`
-	**Blacklist management** supporting add/check/delete of URLs
-	Efficient URL filtering using a **Bloom Filter**, protecting users from malicious links
-	**Multi-threaded C++ server** capable of handling multiple concurrent client requests safely
-	**Dockerized environment** – easy setup with no manual dependency installation
-	A modular, *loosely-coupled* architecture designed according to **SOLID** principles and developed with Test-Driven Development (**TDD**) to ensure easy maintenance and future expansion.

---
## Full System Demonstration and Documentation

All setup instructions, build steps, and usage guides — including screenshots — are provided in the **`wiki/`** folder of this repository.

The `wiki/` folder includes:

- Step-by-step instructions for building and running the entire system
- Complete environment setup guide  
- Clear explanation of all application features and functionalities 

---

## Project Structure

- Project
  - android (all android app code)
  - src
      - Main
          - *.cpp
          - *.h
      - Web
          - controllers
              - *.js

          - models
              - *.js

          - routes
              - *.js

          - utils
              - *.js
          - app.js
          - package-lock.json
          - package.json 
      - Tests
          - test.cpp
  - data
      - Blacklist.txt
  - web-client
    - public
      - index.html
      - Maily.png
    - src
      - components
        - *.js
        - *.css
      - styles
        - *.css
      - app.js
      - index.js
    - .env
    - package-lock.json
    - package.json 
  - wiki
    - Screenshots
      - *.png
    - 0.Build-And-Setup.md
    - 1.Android-App-Usage.md
    - 2.Web-App-Usage.md
  - .env
  - .gitignore
  - CMakeLists.txt
  - docker-compose.yml
  - DockerfileClient
  - DockerfileReact
  - DockerfileServer
  - DockerfileTests
  - DockerfileWeb
  - README.md

---

## Authors
- Ori Small
- Ofek Sarusi
- Itay Turiel

---