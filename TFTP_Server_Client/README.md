# SPL TFTP Client-Server 

A Java implementation of an **extended TFTP (Trivial File Transfer Protocol)** client-server system for the **SPL course**.
This project includes a **multi-client TCP server** and a **command-line client** that communicate using a **binary packet-based protocol**.

## ✨ Overview
This project was built as part of **SPL Assignment 3** and implements the main TFTP operations required by the assignment.

### What this project does
- 🔐 Login with a unique username using `LOGRQ`
- 📥 Download files from the server using `RRQ`
- 📤 Upload files to the server using `WRQ`
- 📂 Request a directory listing with `DIRQ`
- 🗑️ Delete files from the server using `DELRQ`
- 📢 Broadcast file updates to connected clients with `BCAST`
- ✅ Handle `ACK` packets
- ❌ Handle `ERROR` packets
- 🔌 Disconnect cleanly using `DISC`

## 🧠 How it works
The repository contains **two separate Maven projects**:

### Server
The server is based on a **Thread-Per-Client** architecture.
Each client connection is handled independently, while the server keeps track of:
- active connections
- logged-in users
- files inside the `server/Files/` directory

The server is responsible for:
- decoding incoming binary packets
- processing TFTP requests
- reading, writing, deleting, and listing files
- sending `DATA`, `ACK`, `ERROR`, and `BCAST` packets
- notifying all logged-in clients when a file is added or removed

### Client
The client is a **console application** with two main flows:
- ⌨️ a keyboard thread for reading user commands
- 👂 a listening thread for receiving responses from the server

The client is responsible for:
- encoding terminal commands into binary packets
- uploading and downloading files
- printing server responses to the terminal
- saving downloaded files locally
- handling multi-packet file transfers

## 🚀 Supported Commands
```text
LOGRQ <username>
RRQ <filename>
WRQ <filename>
DIRQ
DELRQ <filename>
DISC
```

## ⚙️ Build and Run
### Server
From the `server/` directory:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpServer" -Dexec.args="7777"
```

### Client
From the `client/` directory:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.tftp.TftpClient" -Dexec.args="127.0.0.1 7777"
```

## 📦 Packet Flow Idea
This project works with **binary TFTP packets** rather than plain text messages.
A typical flow looks like this:

1. `LOGRQ` → client logs in
2. `RRQ` / `WRQ` / `DIRQ` / `DELRQ` → client sends a request
3. server responds with `ACK`, `DATA`, or `ERROR`
4. file changes may trigger a `BCAST` message to other logged-in clients
5. `DISC` → clean disconnection

## 🛠️ Main Concepts Used
- Java sockets
- multi-threaded client/server communication
- thread-per-client server model
- binary encoding and decoding
- big-endian packet parsing
- concurrent connection management
- chunked file transfer with 512-byte `DATA` packets

##  Example Session
```text
LOGRQ Ahmad
DIRQ
RRQ A.txt
WRQ notes.txt
DELRQ old_file.txt
DISC
```

## 📝 Notes
- The server stores shared files inside `server/Files/`.
- Downloaded files are saved on the client side in the current working directory.
- The project also contains some starter/template files from the SPL skeleton.
- The main implementation for this assignment is inside the `impl/tftp/` packages.

## 👨‍💻 Authors
Developed for the SPL course by **Ahmad Dahleh**.
