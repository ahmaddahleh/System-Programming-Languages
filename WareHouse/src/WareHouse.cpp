#include "WareHouse.h"
#include "Action.h"
#include "Volunteer.h"
#include "Customer.h"
#include <algorithm>
#include <iostream>
#include <fstream>

// constructor of the class WareHouse : (given an config file as an input)
WareHouse :: WareHouse(const string &configFilePath) : isOpen(false) , actionsLog() , volunteers() , pendingOrders() , inProcessOrders() , completedOrders() , customers() , 
 customerCounter(0) , volunteerCounter (0) , orderIdCounter(0) {
    std::ifstream configFile(configFilePath);
     if (!configFile.is_open()) {
        std::cerr << "Error opening the configuration file." << std::endl;
        return;
    }
    std::string line;
    while (std::getline(configFile, line)){
        std :: size_t findspc1 = line.find(" ");
        std :: string Type = line.substr(0 , findspc1);
        if (Type == "customer"){
            std :: string restofline = line.substr(findspc1 + 1);
            std :: size_t findspc2 = restofline.find(" ");
            std :: string custname = restofline.substr(0 , findspc2);
            std :: string restofline2 = restofline.substr(findspc2 + 1);
            std :: size_t findspc3 = restofline2.find(" ");
            std :: string custtype = restofline2.substr(0 , findspc3);
            std :: string restofline3 = restofline2.substr(findspc3 + 1);
            std :: size_t findspc4 = restofline3.find(" ");
            std :: string custdistance = restofline3.substr(0 , findspc4);
            std :: string restofline4 = restofline3.substr(findspc4 + 1);
            std :: string custmaxOrders = restofline4.substr(0 , 1);
            AddCustomer custtoadd (custname , custtype , std::stoi(custdistance) , std::stoi(custmaxOrders));
            WareHouse &thiswareHouse = *this ; 
            custtoadd.act(thiswareHouse);
        }
        else if (Type == "volunteer"){
            std :: string restofline = line.substr(findspc1 + 1);
            std :: size_t findspc2 = restofline.find(" ");
            std :: string volname = restofline.substr(0 , findspc2);
            std :: string restofline2 = restofline.substr(findspc2 + 1);
            std :: size_t findspc3 = restofline2.find(" ");
            std :: string voltype = restofline2.substr(0 , findspc3);
            std :: string restofline3 = restofline2.substr(findspc3 + 1);
            std :: string volmaxorders = restofline3.substr(0 , 1);
            if (voltype ==  "collector"){
                CollectorVolunteer* voltoadd = new CollectorVolunteer(volunteerCounter , volname , std ::stoi(volmaxorders));
                volunteerCounter++;
                volunteers.push_back(voltoadd);
            }
            else if (voltype == "limited_collector"){
                std :: size_t findspc4 = restofline3.find(" ");
                std :: string voldcooldown = restofline3.substr(0 , findspc4);
                std :: string volmaxOrders = restofline3.substr(findspc4 + 1 , findspc4 + 2);
                LimitedCollectorVolunteer* voltoadd = new LimitedCollectorVolunteer(volunteerCounter , volname , std ::stoi(voldcooldown) , std::stoi(volmaxOrders));
                volunteerCounter++;
                volunteers.push_back(voltoadd);
            }
            else if (voltype == "driver"){
                std :: size_t findspc4 = restofline3.find(" ");
                std :: string voldmaxDistance = restofline3.substr(0 , findspc4);
                std :: string vol_distance_per_step = restofline3.substr(findspc4 + 1 , findspc4 + 2);
                DriverVolunteer* voltoadd = new DriverVolunteer(volunteerCounter ,  volname , std::stoi(voldmaxDistance) , std::stoi(vol_distance_per_step));
                volunteerCounter++;
                volunteers.push_back(voltoadd);
            }
            else if (voltype == "limited_driver"){
                std :: size_t findspc4 = restofline3.find(" ");
                std :: string voldmaxDistance = restofline3.substr(0 , findspc4);
                std :: string restofline4 = restofline3.substr(findspc4 + 1);
                std :: size_t findspc5 = restofline4.find(" ");
                std :: string vol_distance_per_step = restofline4.substr(0 , findspc5);
                std :: string volmaxOrders = restofline4.substr(findspc5 + 1 , findspc5 + 2);
                LimitedDriverVolunteer* voltoadd = new LimitedDriverVolunteer(volunteerCounter , volname ,  std::stoi(voldmaxDistance) , std::stoi(vol_distance_per_step) , std::stoi(volmaxOrders));
                volunteerCounter++;
                volunteers.push_back(voltoadd);
            }
        }
    }
}


void WareHouse :: start(){
    WareHouse &thiswareHouse = *this;
    std :: string accepteddata;
    open();
    while (isOpen){
            std::getline(std::cin, accepteddata);
            std :: size_t findspc1 = accepteddata.find(" ");
            std :: string actionType = accepteddata.substr(0 , findspc1);
            if (actionType == "step"){
                std :: string restofaccd1 = accepteddata.substr(findspc1 + 1);
                std :: size_t findspc2 = restofaccd1.find(" ");
                std :: string numOfSteps = restofaccd1.substr(0 , findspc2);
                SimulateStep steptosim (std::stoi(numOfSteps));
                steptosim.act(thiswareHouse); 
            }
            else if (actionType == "order"){
                std :: string restofaccd1 = accepteddata.substr(findspc1 + 1);
                std :: size_t findspc2 = restofaccd1.find(" ");
                std :: string custId = restofaccd1.substr(0 , findspc2);
                AddOrder ordtoadd (std::stoi(custId));
                ordtoadd.act(thiswareHouse);
            }
            else if (actionType == "customer"){
                 std :: string restofaccd1 = accepteddata.substr(findspc1 + 1);
                 std :: size_t findspc2 = restofaccd1.find(" ");
                 std :: string custname = restofaccd1.substr(0 , findspc2);
                 std :: string restofaccd2 = restofaccd1.substr(findspc2 + 1);
                 std :: size_t findspc3 = restofaccd2.find(" ");
                 std :: string custtype = restofaccd2.substr(0 , findspc3);
                 std :: string restofaccd3 = restofaccd2.substr(findspc3 + 1);
                 std :: size_t findspc4 = restofaccd3.find(" ");
                 std :: string custdistance = restofaccd3.substr(0 , findspc4);
                 std :: string custmaxOrders = restofaccd3.substr(findspc4 + 1 , findspc4 + 2);
                 AddCustomer custtoadd (custname , custtype , std::stoi(custdistance) , std::stoi(custmaxOrders));
                 custtoadd.helpAct(thiswareHouse);
            }
            else if (actionType == "orderStatus"){
                std :: string restofaccd1 = accepteddata.substr(findspc1 + 1);
                std :: size_t findspc2 = restofaccd1.find(" ");
                std :: string orderId = restofaccd1.substr(0 , findspc2);
                PrintOrderStatus ordstatustoprint (std::stoi(orderId));
                ordstatustoprint.act(thiswareHouse);
            }
            else if (actionType == "customerStatus"){
                std :: string restofaccd1 = accepteddata.substr(findspc1 + 1);
                std :: size_t findspc2 = restofaccd1.find(" ");
                std :: string custId = restofaccd1.substr(0 , findspc2);
                PrintCustomerStatus custstatustoprint (std::stoi(custId));
                custstatustoprint.act(thiswareHouse);
            }
            else if (actionType == "volunteerStatus"){
                std :: string restofaccd1 = accepteddata.substr(findspc1 + 1);
                std :: size_t findspc2 = restofaccd1.find(" ");
                std :: string volId = restofaccd1.substr(0 , findspc2);
                PrintVolunteerStatus volstatustoprint (std::stoi(volId));
                volstatustoprint.act(thiswareHouse);
            }
            else if (actionType == "log"){
                PrintActionsLog log;
                log.act(thiswareHouse);
            }
            else if (actionType == "close"){
                Close close;
                close.act(thiswareHouse);
            }
            else if (actionType == "backup"){
                BackupWareHouse backup;
                backup.act(thiswareHouse);
            }
            else if (actionType == "restore"){
                RestoreWareHouse restore;
                restore.act(thiswareHouse);
            }
    }
}


void WareHouse :: addOrder(Order* order) {
    this->pendingOrders.push_back(order);
}


void WareHouse :: addAction(BaseAction* action){
    this->actionsLog.push_back(action);
}


bool WareHouse :: lookforcustomer(int customerId) const{
    for(Customer* findcust : customers){
    if ( findcust->getId() == customerId){
        return true;
    }
 }
 return false;
}


Customer& WareHouse :: getCustomer(int customerId) const {
Customer* getcust;
 for(Customer* findcust : customers){
    if ( findcust->getId() == customerId){
        getcust = findcust;
    }
 }
  return *getcust ;
}


bool WareHouse :: lookforvolunteer(int volunteerId) const{
    for(Volunteer* findvol : volunteers){
    if ( findvol->getId() == volunteerId){
        return true;
    }
 }
 return false;
}


Volunteer& WareHouse :: getVolunteer(int volunteerId) const {
Volunteer* getvol;
for(Volunteer* findvol : volunteers){
    if ( findvol->getId() == volunteerId){
        getvol  = findvol;
    }
 }
  return *getvol;
}


 bool WareHouse ::  lookforord(int orderId) const {
    for(Order* findord : pendingOrders){
    if ( findord->getId() == orderId){
        return true;
    }
 }
    for(Order* findord : inProcessOrders){
    if ( findord->getId() == orderId){
        return true;
    }
    }  
    for(Order* findord : completedOrders){
    if ( findord->getId() == orderId){
        return true;
    }
 }
 return false;
 }


Order& WareHouse:: getOrder(int orderId) const {
    Order* getord;
    for(Order* findord : pendingOrders){
    if ( findord->getId() == orderId){
        getord = findord;
    }
 }
    for(Order* findord : inProcessOrders){
    if ( findord->getId() == orderId){
        getord = findord;
    }
    }  
    for(Order* findord : completedOrders){
    if ( findord->getId() == orderId){
        getord = findord;
    }
 }
 return *getord;
}


const :: vector<BaseAction*>& WareHouse:: getActions() const{
    return actionsLog;
}


void WareHouse:: close() { 
    isOpen = false;
  }


void WareHouse:: open() {
    isOpen = true; 
    std :: cout <<  "WareHouse is open!" << std::endl;
}


vector<Volunteer*>& WareHouse:: getVolunteers(){
    return volunteers;
}


vector<Order*>& WareHouse:: getpendingOrders(){
    return pendingOrders;
}


vector<Order*>& WareHouse:: getinProcessOrders(){
    return inProcessOrders;
}


vector<Order*>& WareHouse:: getCompletedOrders(){
    return completedOrders;
}


vector<Customer*>& WareHouse:: getcustomers(){
    return customers;
}

//added methods implementation : 
int WareHouse ::  getOrderIdCounter() const {
    return orderIdCounter ;
}


void WareHouse :: incrementOrderIdCounter(){
 orderIdCounter++ ;
}



int WareHouse :: getcustomerCounter() const{
    return customerCounter;
}


void WareHouse :: incrementcustomerCounter(){
    customerCounter++;
}


void WareHouse :: transordtoPending(Order& order) {
    auto tofind = std::find(inProcessOrders.begin(), inProcessOrders.end(), &order);
    if (tofind != inProcessOrders.end()){
    inProcessOrders.erase(tofind);
    pendingOrders.push_back(&order);
    }
}


void WareHouse :: transordtoInprocess(Order& order) {
      auto tofind = std::find(pendingOrders.begin() , pendingOrders.end(), &order);
      if (tofind != pendingOrders.end()){
         pendingOrders.erase(tofind);
         inProcessOrders.push_back(&order);
         }       
}


void WareHouse :: transordtoCompleted(Order& order) {
      auto tofind = std::find(inProcessOrders.begin(), inProcessOrders.end(), &order);
      if (tofind != inProcessOrders.end()){
         inProcessOrders.erase(tofind);
         completedOrders.push_back(&order);
         }       
}


void WareHouse :: removeVolunteer(Volunteer& volunteer) {
    auto tofind = std::find(volunteers.begin(),volunteers.end(), &volunteer);
    if (tofind != volunteers.end()){
    volunteers.erase(tofind);
    }
}


//Rule of 5 implementation : 

//copy constructor: 
WareHouse::WareHouse(const WareHouse &other): isOpen(other.isOpen) , actionsLog() , volunteers() , pendingOrders() , 
inProcessOrders() , completedOrders() , customers() , customerCounter(other.customerCounter) , 
 volunteerCounter(other.volunteerCounter) , orderIdCounter(other.orderIdCounter) {

    for(Customer* cus : other.customers){
        customers.push_back(cus->clone());
    }
    for(Volunteer* vol : other.volunteers){
        volunteers.push_back(vol->clone());
    }
    for(Order* ord : other.pendingOrders){
        pendingOrders.push_back(new Order(*ord));
    }
    for(Order* ord : other.inProcessOrders){
        inProcessOrders.push_back(new Order(*ord));
    }
    for(Order* ord : other.completedOrders){
        completedOrders.push_back(new Order(*ord));
    }
    for(BaseAction* act : other.actionsLog){
        actionsLog.push_back(act->clone());
    }
}


//copy Assignment operator= : 
 WareHouse& WareHouse :: operator=(const WareHouse& other){
    if (&other != this){
      this->isOpen = other.isOpen;

   for (BaseAction* action : this->actionsLog ){
        delete action;
    }
    actionsLog.clear();

    for (Volunteer* volunteer : this->volunteers ){
        delete volunteer;
    }
    volunteers.clear();

    for (Order* ordertodel : this->pendingOrders ){
        delete ordertodel;
    }
    pendingOrders.clear();

    for (Order* ordertodel : this->inProcessOrders ){
        delete ordertodel;
    }
     inProcessOrders.clear();

    for (Order* ordertodel : this->completedOrders ){
        delete ordertodel;
    }
     completedOrders.clear();

    for (Customer* custtodel : this->customers ){
        delete custtodel;
    }
     customers.clear();


    for(BaseAction* action : other.actionsLog){
        this->actionsLog.push_back(action->clone());
    }

    for(Volunteer* volunteer : other.volunteers){
        this->volunteers.push_back(volunteer->clone());
    }
    
    for (Order* ordertopush : other.pendingOrders){
        this->pendingOrders.push_back(new Order(*ordertopush));
    }
    
    for (Order* ordertopush : other.inProcessOrders){
        this->inProcessOrders.push_back(new Order(*ordertopush));
    }
    
    for (Order* ordertopush : other.completedOrders){
        this->completedOrders.push_back(new Order(*ordertopush));
    }
    
    for (Customer* custtopush : other.customers){
        this->customers.push_back(custtopush->clone());
    }

    this->customerCounter = other.customerCounter;
    this->volunteerCounter = other.volunteerCounter;
    this->orderIdCounter = other.orderIdCounter;

    }
    return *this;
} 


//~ destructor :
WareHouse :: ~ WareHouse(){

    for (BaseAction* action : actionsLog ){
        delete action;
    }

    for (Volunteer* volunteer : volunteers ){
        delete volunteer;
    }

    for (Order* ordertodel : pendingOrders ){
        delete ordertodel;
    }

    for (Order* ordertodel : inProcessOrders ){
        delete ordertodel;
    }

    for (Order* ordertodel : completedOrders ){
        delete ordertodel;
    }

    for (Customer* custtodel : customers ){
        delete custtodel;
    }    
}


//Move assignment operator=
WareHouse &WareHouse::operator=(WareHouse &&other)
{
    if (this != &other)
    {
        for (BaseAction *action : actionsLog)
        {
            delete action;
        }

        for (Volunteer *volunteer : volunteers)
        {
            delete volunteer;
        }

        for (Order *order : pendingOrders)
        {
            delete order;
        }

        for (Order *order : inProcessOrders)
        {
            delete order;
        }

        for (Order *order : completedOrders)
        {
            delete order;
        }
        completedOrders.clear();

        for (Customer *customer : customers)
        {
            delete customer;
        }

        isOpen = other.isOpen;
        actionsLog = std::move(other.actionsLog);
        volunteers = std::move(other.volunteers);
        pendingOrders = std::move(other.pendingOrders);
        inProcessOrders = std::move(other.inProcessOrders);
        completedOrders = std::move(other.completedOrders);
        customers = std::move(other.customers);
        customerCounter = other.customerCounter;
        volunteerCounter = other.volunteerCounter;
        orderIdCounter = other.orderIdCounter;
    }
    return *this;
}


//Move Constructor :
WareHouse::WareHouse(WareHouse&& other)
    : isOpen(other.isOpen),
      actionsLog(std::move(other.actionsLog)),
      volunteers(std::move(other.volunteers)),
      pendingOrders(std::move(other.pendingOrders)),
      inProcessOrders(std::move(other.inProcessOrders)),
      completedOrders(std::move(other.completedOrders)),
      customers(std::move(other.customers)),
      customerCounter(other.customerCounter),
      volunteerCounter(other.volunteerCounter),
      orderIdCounter(other.orderIdCounter) {

}