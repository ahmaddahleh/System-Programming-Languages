#pragma once
#include <string>
#include <vector>

#include "Order.h"
#include "Customer.h"

class BaseAction;
class Volunteer;

// Warehouse responsible for Volunteers, Customers Actions, and Orders.

class WareHouse {

    public:
        WareHouse(const string &configFilePath);
        void start();
        void addOrder(Order* order);
        void addAction(BaseAction* action);
        Customer &getCustomer(int customerId) const;
        Volunteer &getVolunteer(int volunteerId) const;
        Order &getOrder(int orderId) const;
        const vector<BaseAction*> &getActions() const;
        void close();
        void open();
        vector<Volunteer*> &getVolunteers();
        vector<Order*> &getpendingOrders();
        vector<Order*> &getinProcessOrders();
        vector<Order*> &getCompletedOrders();
        vector<Customer*> &getcustomers();
        //added methods :
        int getOrderIdCounter() const;
        void incrementOrderIdCounter();
        bool CustomerisThere(int id) const;
        int getcustomerCounter() const;
        void incrementcustomerCounter();
        bool lookforord(int orderId) const;
        bool lookforcustomer(int customerId) const;
        bool lookforvolunteer(int volunteerId) const;
        void transordtoPending(Order& order);
        void transordtoInprocess(Order& order);
        void transordtoCompleted(Order& order);
        void removeVolunteer(Volunteer& volunteer);
        //rule of 5 implementation : 
        WareHouse(const WareHouse &other);
        WareHouse &operator=(const WareHouse& other);
        ~WareHouse();
        WareHouse& operator=(WareHouse&& other);
        WareHouse(WareHouse&& other);


    private:
        bool isOpen;
        vector<BaseAction*> actionsLog;
        vector<Volunteer*> volunteers;
        vector<Order*> pendingOrders;
        vector<Order*> inProcessOrders;
        vector<Order*> completedOrders;
        vector<Customer*> customers;
        int customerCounter; //For assigning unique customer IDs
        int volunteerCounter; //For assigning unique volunteer IDs
        //added fields: 
        int orderIdCounter; //For assigning unique Order IDs
};