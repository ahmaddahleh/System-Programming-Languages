#include "Customer.h"

// Constructor :
Customer :: Customer(int id, const string &name, int locationDistance, int maxOrders): 
id(id) , name(name) , locationDistance(locationDistance) , maxOrders(maxOrders) , ordersId(){

}


const std :: string &Customer:: getName() const {
    return name;
    }


int Customer :: getId() const {
    return id;
    }


int Customer :: getCustomerDistance() const {
    return locationDistance;
    }


int Customer :: getMaxOrders() const {
    return maxOrders;
    }


int Customer :: getNumOrders() const {
    return ordersId.size();
    }


bool Customer :: canMakeOrder()const {
    if(getNumOrders() < maxOrders){
        return true;
    }
    return false;
    }


const std :: vector<int> &Customer:: getOrdersIds() const{
    return ordersId;
}


int Customer :: addOrder(int orderId){
    int toret(-1);
    if(canMakeOrder()){
        ordersId.push_back(orderId);
        toret = orderId;
    }
    return toret;
}

// SoldierCustomer Constructor :
SoldierCustomer :: SoldierCustomer(int id, const string &name, int locationDistance, int maxOrders) : 
Customer(id , name , locationDistance ,maxOrders){}


SoldierCustomer* SoldierCustomer :: clone () const {
    return new SoldierCustomer(*this);
} 


// CivilianCustomer Constructor :
CivilianCustomer :: CivilianCustomer(int id, const string &name, int locationDistance, int maxOrders) :
Customer(id, name , locationDistance , maxOrders){}


CivilianCustomer* CivilianCustomer :: clone() const {
    return new CivilianCustomer(*this);
}




