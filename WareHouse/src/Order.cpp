#include "Order.h"


//Constructor:
Order :: Order(int id, int customerId, int distance) : id(id) , customerId(customerId) , distance(distance) , status(OrderStatus::PENDING) , collectorId(NO_VOLUNTEER) ,
 driverId(NO_VOLUNTEER) {

}


int Order :: getId() const {
    return id;
}


int Order :: getCustomerId() const {
    return customerId;
}


void Order :: setStatus(OrderStatus status) {
    this->status = status;
}


void Order :: setCollectorId(int collectorId) {
   this->collectorId = collectorId;
}


void Order :: setDriverId(int driverId) {
   this->driverId = driverId;
}


int Order :: getCollectorId() const {
    return collectorId;
}


int Order :: getDriverId() const {
    return driverId;
}


OrderStatus Order :: getStatus() const {
    return status;
}


int Order :: getOrderDistance()const {
    return distance;
}


// this function returns a string representing the Orderstatus :
string Order :: statusToString() const {
       if (getStatus() == OrderStatus::PENDING){return "Pending";}
       else if(getStatus() == OrderStatus::COLLECTING){return "Collecting";}
       else if (getStatus() == OrderStatus::DELIVERING){return "Delivering";}
       else return "Completed";
}


//returns to string representation to an Order instance :
const std :: string Order :: toString() const {
    if(collectorId == NO_VOLUNTEER){
    return "OrderId: " + std::to_string(id) + "\nOrderStatus: " + statusToString() +  "\nCustomerID: " + std::to_string(customerId) 
    + "\nCollector: None"  + "\nDriver: None" ;
}
if(driverId == NO_VOLUNTEER){
    return "OrderId: " + std::to_string(id) + "\nOrderStatus: " + statusToString() + "\nCustomerID: " + std::to_string(customerId)  
    + "\nCollector: " + std::to_string(collectorId) + "\nDriver: None ";
}
return  "OrderId: " + std::to_string(id) + "\nOrderStatus: " + statusToString() + "\nCustomerID: " + std::to_string(customerId)  
    + "\nCollector: " + std::to_string(collectorId) + "\nDriver: " + std::to_string(driverId);
}













