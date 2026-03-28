# Food Warehouse Management System 📦

A C++ object-oriented simulation project developed for the **Systems Programming Languages (SPL)** course.

This project models a food warehouse that receives customer orders, assigns volunteers to process them, advances the system through simulation steps, and tracks the full lifecycle of each order — from creation to delivery.

---

## ✨ Overview

The system simulates a warehouse in which:

- Customers place food package orders
- Collector volunteers process incoming orders
- Driver volunteers deliver completed packages
- The warehouse tracks all orders through different processing stages
- The user interacts with the system through a command-line interface

The program loads its initial state from a configuration file and then runs in an interactive loop, accepting commands that modify and inspect the warehouse state.

---

## 🚀 Key Features

- Object-oriented design in **C++**
- Interactive command-line simulation
- Configuration-file based initialization
- Full order lifecycle management
- Support for multiple customer and volunteer types
- Action logging system
- Backup and restore functionality
- Dynamic memory management
- Rule of Five implementation where required

---

## 🧠 Core Model

### Customers
The system supports different customer types, each with:

- a unique ID
- name
- distance from the warehouse
- maximum number of allowed orders

### Volunteers
Orders are handled in two main stages:

- **Collectors** receive and prepare pending orders
- **Drivers** deliver collected orders to customers

The system also supports **limited volunteers**, which can only process a fixed number of orders before being removed from the warehouse.

### Orders
Each order moves through the following states:

- **Pending**
- **Collecting**
- **Delivering**
- **Completed**

---

## 🛠️ Supported Commands

The simulator supports commands such as:

- `step <number_of_steps>`
- `order <customer_id>`
- `customer <name> <type> <distance> <max_orders>`
- `orderStatus <order_id>`
- `customerStatus <customer_id>`
- `volunteerStatus <volunteer_id>`
- `log`
- `backup`
- `restore`
- `close`

These commands allow the user to create customers, place orders, advance the simulation, inspect the system state, and manage warehouse snapshots.

## ▶️ Run the Program

Run the simulator by providing a configuration file as a command-line argument:

./bin/warehouse <config_file_path>

Example:

./bin/warehouse configFileExample.txt


## 📝 Configuration File Format

The program starts by reading a configuration file that defines the initial warehouse state.

Customer format
customer <customer_name> <customer_type> <customer_distance> <max_orders>

Example:

customer Moshe soldier 3 2

customer Ron civilian 2 1

Volunteer format

volunteer <volunteer_name> <volunteer_role> <coolDown/maxDistance> <distance_per_step> <max_orders>

Examples:

volunteer Tamar collector 2

volunteer Ron limited_collector 3 2

volunteer Tal driver 7 4

volunteer Din limited_driver 3 2 3


## 🔄 Simulation Flow

A typical run of the program looks like this:

1)Load customers and volunteers from the configuration file

2)Open the warehouse

3)Accept user commands interactively

4)Create and manage orders

5)Advance the simulation using step

6)Track order, customer, and volunteer status

7)Print the actions log when needed

8)Backup or restore warehouse state

9)Close the warehouse and print final order statuses


## 🏗️ Technical Highlights

*This project demonstrates practical use of:

*inheritance and polymorphism

*abstract base classes

*dynamic allocation and manual memory management

*deep copying and Rule of Five

*STL containers

*state-based simulation logic

*modular system design in C++


## 📚 Academic Context

This project was developed as part of an SPL assignment focused on building a complete object-oriented warehouse simulation in C++, while emphasizing:

*correct class design

*efficient state management

*simulation behavior

*memory safety

*clean modular implementation


## 👨‍💻 Authors
Ahmad Dahleh

Ebrahim Taha
