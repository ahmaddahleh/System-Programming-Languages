# 🎮 Set Card Game – SPL Assignment 2

A multithreaded implementation of the **Set** card game developed as part of **SPL Assignment 2**.

This project focuses on **Java concurrency and synchronization**, with the main goal of implementing the game logic of a competitive Set game using multiple threads, shared state, timers, penalties, and score tracking.

---

## 📌 Overview

This project implements the core logic of the **Set** card game in Java.

The system is built around concurrent game entities:

- a **Dealer** thread that controls the game flow
- multiple **Player** threads that compete simultaneously
- a shared **Table** object that stores the visible cards and tokens

The project emphasizes:

- thread coordination
- synchronization on shared resources
- fairness in handling player claims
- responsive concurrent gameplay

---

## 🃏 What is the Game?

**Set** is a card game where players try to find a **legal set of 3 cards** from the cards currently displayed on the table.

Each card is defined by **4 features**, and each feature has **3 possible values**:

- color
- number
- shape
- shading

A group of 3 cards is considered a **legal set** if, for **each feature**, the values are either:

- **all the same**, or
- **all different**

If for even one feature there are **two the same and one different**, then it is **not** a legal set.

### In this implementation:

- The deck contains **81 cards**
- The table displays cards in a **3x4 grid**
- Players place tokens on cards to claim a possible set
- Once a player marks 3 cards, the **dealer checks** whether the set is valid
- If the set is valid:
  - the player gets a **point**
  - the cards are removed and replaced
- If the set is invalid:
  - the player gets a **penalty**
  - the player is frozen for a short time

The game continues until no more legal sets can be formed from the remaining cards.

---

## 🧵 Concurrency Focus

This project was designed to practice concurrent programming in Java.

Main concurrency ideas used in the project:

- **one dealer thread** manages the game lifecycle
- **one thread per player**
- optional **AI thread** for non-human players
- synchronized interaction with the shared table
- communication between players and dealer for set validation
- freeze logic for point and penalty handling
- fair processing of player claims

---

## 🏗️ Main Components

### `Dealer`
Responsible for:

- starting and managing the game
- creating and running player threads
- placing and removing cards from the table
- checking claimed sets
- updating the countdown timer
- reshuffling when needed
- announcing the winner(s)
- terminating the game gracefully

### `Player`
Responsible for:

- handling key presses / input actions
- placing and removing tokens
- submitting a claimed set to the dealer
- receiving points or penalties
- tracking personal score
- simulating random input for computer players

### `Table`
Shared game structure that stores:

- the cards currently on the table
- slot-to-card / card-to-slot mapping
- tokens placed by players
- UI updates for card and token changes

---

## ✨ Features

- Multithreaded gameplay
- Dealer / player synchronization
- Token placement and removal
- Legal set validation
- Score tracking
- Penalty and point freeze system
- Countdown timer
- Human and computer player support
- Configurable gameplay settings
- Maven project structure

---

## 🛠️ Technologies

- **Java**
- **Maven**
- **JUnit 5**

## 🚀 Build and Run

From the project root:

mvn clean compile

mvn exec:java

To run tests:

mvn test
## ⚙️ Configuration

Game settings are configured through:

src/main/resources/config.properties

-Examples of configurable settings:

-number of human players

-number of computer players

-timeout duration

-warning time

-point freeze duration

-penalty freeze duration

-table delay

-player key mappings

## 📝 Notes
This repository contains the implementation of the game logic layer of the assignment.

The graphical UI and some infrastructure components were provided as part of the assignment skeleton.

The main work in this project is the synchronization and coordination logic between concurrent entities.
## 👥 Authors
Ahmad Dahleh

Ebrahim Taha

## 🎓 Academic Context
This project was developed as part of the Systems Programming Laboratory (SPL) course assignment on Java Concurrency and Synchronization.
