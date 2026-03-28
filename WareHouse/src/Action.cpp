#include "Action.h"
#include "Volunteer.h"
#include <iostream>
#include <algorithm>
#include "Customer.h"

extern WareHouse* backup;


BaseAction :: BaseAction () : errorMsg() , status() {}


ActionStatus BaseAction :: getStatus() const {
    return status;
}


void BaseAction :: complete(){
    this->status = ActionStatus::COMPLETED;
    }


void BaseAction :: error(string errorMsg){
    this->status = ActionStatus::ERROR;
    this->errorMsg = errorMsg; 
}


string BaseAction :: getErrorMsg() const {
    return errorMsg;
}


// Constructor for the SimulateStep action :
SimulateStep :: SimulateStep(int numOfSteps) : numOfSteps(numOfSteps) {
    
}


void SimulateStep :: act(WareHouse &wareHouse){
    for (int i = numOfSteps ; i > 0 ; i--){
        for(int j = 0; j < (int)wareHouse.getpendingOrders().size() ; j++) {
            std::vector<Order*> tmpPending = wareHouse.getpendingOrders();
            Order* ord = tmpPending[j];
            for (Volunteer* vol1 : wareHouse.getVolunteers()){
                bool orderok = true;
                if(vol1->canTakeOrder(*ord)){
                    if (ord->getStatus() == OrderStatus::PENDING){
                        vol1->acceptOrder(*ord);
                        ord->setStatus(OrderStatus::COLLECTING);
                        ord->setCollectorId(vol1->getId());
                        wareHouse.transordtoInprocess(*ord);
                        orderok = false;
                        j--;
                    }
                    if ( ord->getStatus() == OrderStatus::COLLECTING && orderok){
                        vol1->acceptOrder(*ord);
                        ord->setStatus(OrderStatus::DELIVERING);
                        ord->setDriverId(vol1->getId());
                        wareHouse.transordtoInprocess(*ord);
                        j--;
                    }
                       break;                 
                }
            }
        }
        for(int m = 0; m< (int)wareHouse.getVolunteers().size();m++) {
            std::vector<Volunteer*> tmpvol = wareHouse.getVolunteers();
            Volunteer* vol2 = tmpvol[m];
            if(vol2->isBusy()){
           vol2->step();
           for(int j = 0; j < (int)wareHouse.getinProcessOrders().size() ; j++){
            std::vector<Order*> tmpinProcess = wareHouse.getinProcessOrders();
            Order* ord = tmpinProcess[j];
                if(ord->getId() == vol2->getCompletedOrderId()){
                    if (ord->getStatus() == OrderStatus::COLLECTING){
                        wareHouse.transordtoPending(*ord);
                        j--;
                    }
                    else if (ord->getStatus() == OrderStatus::DELIVERING){
                        ord->setStatus(OrderStatus::COMPLETED);
                        wareHouse.transordtoCompleted(*ord);
                        j--;
                    }
                }
            }
            }
             if(!vol2->hasOrdersLeft() && !vol2->isBusy()){
                    wareHouse.removeVolunteer(*vol2);
                    m--;
                    delete vol2;
                }
        }
    }
    complete();
    wareHouse.addAction(this->clone());
}


std::string SimulateStep :: toString() const {
    return  "simulateStep "  + std :: to_string(numOfSteps) + " COMPLETED" ;
}


SimulateStep* SimulateStep :: clone() const{
    return new SimulateStep(*this);
}


// Constructor for the addOrder action :
AddOrder :: AddOrder (int id): customerId(id){
}


void AddOrder :: act(WareHouse &wareHouse){
    if(wareHouse.lookforcustomer(customerId) && wareHouse.getCustomer(customerId).canMakeOrder()){
    Order* ordToAdd = new Order(wareHouse.getOrderIdCounter() , customerId , wareHouse.getCustomer(customerId).getCustomerDistance());
    wareHouse.addOrder(ordToAdd);
    wareHouse.getCustomer(customerId).addOrder(ordToAdd->getId());
    wareHouse.incrementOrderIdCounter();
    complete();
    }
    else{
        error("Cannot place this order");
        std :: cout << "Error:" + getErrorMsg() << std::endl;
    }
    wareHouse.addAction(this->clone());
}


string AddOrder :: toString() const {
    if (getStatus() == ActionStatus::COMPLETED){
        return "order " + std::to_string(customerId) + " COMPLETED" ; 
    }
    else  return "order " + std::to_string(customerId) + " ERROR";

}


AddOrder* AddOrder :: clone() const {
    return new AddOrder(*this);
}


// Constructor for the addCustomer action :
CustomerType AddCustomer :: convertTocustomerType(string customerType) const {
    if (customerType == "civilian"){
        return CustomerType::Civilian;
    }
    else return CustomerType ::Soldier;
}


AddCustomer ::  AddCustomer(const string &customerName, const string &customerType, int distance, int maxOrders) 
: customerName(customerName) , customerType(convertTocustomerType(customerType)) , distance(distance) , maxOrders(maxOrders) {

}


void AddCustomer :: helpAct(WareHouse &wareHouse){
    act(wareHouse);
    complete();
    wareHouse.addAction(this->clone());
}

void AddCustomer :: act(WareHouse &wareHouse) {
    if (customerType == CustomerType ::Civilian){
    Customer* custtoadd = new CivilianCustomer(wareHouse.getcustomerCounter() , customerName , distance , maxOrders);
    wareHouse.getcustomers().push_back(custtoadd);
    }
    else if (customerType == CustomerType ::Soldier){
    Customer* custtoadd = new SoldierCustomer(wareHouse.getcustomerCounter() , customerName , distance , maxOrders);
    wareHouse.getcustomers().push_back(custtoadd);
    }
    wareHouse.incrementcustomerCounter();
    
}


string AddCustomer :: convertcustTypetostring(CustomerType customerType) const{
        if (customerType == CustomerType::Civilian){return "Civilian";}
        else return "Soldier";
}


string AddCustomer :: toString() const {
    return "customer " + customerName + " " + convertcustTypetostring(customerType) + " " +
     std::to_string(distance) + " " + std::to_string(maxOrders);
}

AddCustomer* AddCustomer :: clone() const {
    return new AddCustomer(*this);
}


// Constructor for the PrintOrderStatus action :
PrintOrderStatus :: PrintOrderStatus(int id) : orderId(id) {

}


void PrintOrderStatus :: act (WareHouse &wareHouse){
    if (wareHouse.lookforord(orderId)){
    if(wareHouse.getOrder(orderId).getId() == orderId){
        std::cout << wareHouse.getOrder(orderId).toString() << std::endl;
    }
    }
    else{
        error("Order doesn't exist");
        std::cout << "Error:" + getErrorMsg() << std::endl;
    }
    wareHouse.addAction(this->clone());
    
}


string PrintOrderStatus :: toString() const {
    if (getStatus() == ActionStatus::COMPLETED){
        return "orderStatus " + std:: to_string(orderId) + " COMPLETED" ;
    }
    else return "orderStatus " + std::to_string(orderId) + " ERROR";
}


PrintOrderStatus* PrintOrderStatus :: clone() const {
    return new PrintOrderStatus(*this);
}


// Constructor for the PrintCustomerStatus action :
PrintCustomerStatus :: PrintCustomerStatus(int customerId) : customerId(customerId) {

}


void PrintCustomerStatus :: act(WareHouse &wareHouse){
    if (wareHouse.lookforcustomer(customerId)){
        string str;
        for (int i : wareHouse.getCustomer(customerId).getOrdersIds()){
            str = str + "OrderId: " + std::to_string(i) +  "\n" + "OrderStatus: " + wareHouse.getOrder(i).statusToString() + "\n";
        }
        std :: cout << "CustomerID: " + std::to_string(customerId) + "\n" + str  + "numOrdersLeft: " +  
         std::to_string((wareHouse.getCustomer(customerId).getMaxOrders() - wareHouse.getCustomer(customerId).getNumOrders())) << std::endl;
         complete();
    }
    else{
        error("Customer doesn't exist");
        std::cout << "Error:" + getErrorMsg() << std::endl;
    }
    wareHouse.addAction(this->clone());
}


string PrintCustomerStatus :: toString() const {
    if (getStatus() == ActionStatus::COMPLETED){
        return "customerStatus " + std:: to_string(customerId) + " COMPLETED" ;
    }
    else return "customerStatus " + std::to_string(customerId) + " ERROR";

}

PrintCustomerStatus* PrintCustomerStatus :: clone() const {
    return new PrintCustomerStatus(*this);
}


// Constructor for the PrintVolunteersStatus action :
PrintVolunteerStatus :: PrintVolunteerStatus(int id) : volunteerId(id) {

}


void PrintVolunteerStatus :: act(WareHouse &wareHouse) {
    if(wareHouse.lookforvolunteer(volunteerId)){
        std :: cout << wareHouse.getVolunteer(volunteerId).toString() << std::endl;
        complete();
    }
    else {
        error("Volunteer doesn't exist");
        std :: cout << "Error:" + getErrorMsg() << std:: endl;
    }
    wareHouse.addAction(this->clone());
}


string PrintVolunteerStatus :: toString() const {
     if (getStatus() == ActionStatus::COMPLETED){
        return "volunteerStatus " + std:: to_string(volunteerId) + " COMPLETED" ;
    }
    else return "volunteerStatus " + std::to_string(volunteerId) + " ERROR";
}


PrintVolunteerStatus* PrintVolunteerStatus :: clone() const{
    return new PrintVolunteerStatus(*this);
}


// Constructor for the PrintActionsLog action :
PrintActionsLog :: PrintActionsLog() {}


void PrintActionsLog :: act(WareHouse &wareHouse) {
    string str="";
     for (BaseAction* i : wareHouse.getActions()){
         str = str +  i->toString() + "\n"; 
         }
         std :: cout << str;
         complete();
         wareHouse.addAction(this->clone());
}


string PrintActionsLog :: toString() const {
    return "log COMPLETED";
}


PrintActionsLog* PrintActionsLog :: clone() const {
    return new PrintActionsLog(*this);
}


// Constructor for the Close action :
Close :: Close(){ 

}

void Close :: act(WareHouse &wareHouse) {
string str = "";
    for(Order* findord : wareHouse.getpendingOrders()){
        str = str + "OrderID: " + std::to_string(findord->getId()) + ", CustomerID: " + std::to_string(findord->getCustomerId()) + ",Status: "+ findord->statusToString() + "\n" ;
 }
    for(Order* findord : wareHouse.getinProcessOrders()){
        str = str + "OrderID: " + std::to_string(findord->getId()) + ", CustomerID: " + std::to_string(findord->getCustomerId()) + ",Status: "+ findord->statusToString() + "\n" ;

    }  
    for(Order* findord : wareHouse.getCompletedOrders()){
         str=  str + "OrderID: " + std::to_string(findord->getId()) + ", CustomerID: " + std::to_string(findord->getCustomerId()) + ",Status: "+ findord->statusToString() + "\n";  
 }  
     std::cout << str << std::endl;
     complete();
     wareHouse.addAction(this->clone());
     wareHouse.close();
     }


Close* Close :: clone() const {
    return new Close(*this);
}


string Close :: toString() const {
    return "close COMPLETED";
}


// Constructor for the BackupWareHouse action :
BackupWareHouse :: BackupWareHouse(){

}


void BackupWareHouse :: act(WareHouse &wareHouse){
    wareHouse.addAction(this->clone());
    complete();
    if(backup != nullptr){
        delete backup;
    }
    backup = new WareHouse(wareHouse);
}


BackupWareHouse* BackupWareHouse :: clone() const {
    return new BackupWareHouse(*this);
}


string BackupWareHouse :: toString() const {
    return "backup COMPLETED";
}


// Constructor for the RestWareHouse action :
RestoreWareHouse :: RestoreWareHouse() {

}


void RestoreWareHouse :: act(WareHouse &wareHouse){
    if (backup == nullptr){
        error("No backup available");
        std :: cout << "Error:" + getErrorMsg() << std ::endl ; 
    }
    else {
    wareHouse = *backup;
    complete();
    }
    wareHouse.addAction(this->clone());
}


RestoreWareHouse* RestoreWareHouse :: clone() const {
    return new RestoreWareHouse(*this);
}


string RestoreWareHouse :: toString() const {
    if (getStatus() == ActionStatus::COMPLETED){
        return "restore COMPLETED" ;
    }
    else return "restore ERROR";
    
}
















