#include "Volunteer.h"


//Constructor for the Volunteer class: 
Volunteer :: Volunteer(int id, const string &name) :  completedOrderId(NO_ORDER) , activeOrderId(NO_ORDER) , id(id) , name(name) {

}


int Volunteer :: getId() const {
    return id; 
}


const std :: string& Volunteer :: getName() const {
    return name;
}


int Volunteer :: getActiveOrderId() const {
    return activeOrderId;
}


int Volunteer :: getCompletedOrderId() const {
    return completedOrderId;
}


bool Volunteer :: isBusy() const {
    if(activeOrderId != -1){
      return true;
    }
    return false ;
}


string Volunteer :: toString() const{
    if(isBusy()){
    return "VolunteerID: " + std :: to_string(id) + "\nisBusy:" + " True"
     +  "\nOrderId: " + std :: to_string(activeOrderId) ;
    }
    else return "VolunteerID:" + std :: to_string(id) + "\nisBusy:" + " False"
     +  "\nOrderId: None";
    
}


//Constructor for the CollectorVolunteer class: 
CollectorVolunteer :: CollectorVolunteer(int id , const string& name , int coolDown ) : Volunteer(id , name) , coolDown(coolDown) , timeLeft(coolDown) {

}


 CollectorVolunteer* CollectorVolunteer :: clone() const{
    return new CollectorVolunteer(*this);
 }


void CollectorVolunteer :: step(){
    if(decreaseCoolDown()) {
        completedOrderId = activeOrderId;
        activeOrderId = NO_ORDER;
    }
}


int CollectorVolunteer :: getCoolDown() const {
    return coolDown;
}


int CollectorVolunteer :: getTimeLeft() const {
    return timeLeft;
}


bool CollectorVolunteer :: decreaseCoolDown(){
    timeLeft--;
    if(timeLeft <= 0){
        return true;
    }
    return false;
}


bool CollectorVolunteer :: hasOrdersLeft() const{
    return true;
}


bool CollectorVolunteer :: canTakeOrder(const Order &order) const {
     if (!isBusy() && order.getStatus() == OrderStatus::PENDING ) {
        return true;
    }
    else{
     return false;
    }
}


void CollectorVolunteer :: acceptOrder(const Order &order){
    if(canTakeOrder(order)){
    completedOrderId = activeOrderId;
    activeOrderId = order.getId();
    timeLeft = coolDown;
    }
}


string CollectorVolunteer :: toString() const { 
    if(getTimeLeft() == 0 ){
         return Volunteer::toString() + "\nTimeLeft: None" + "\nOrdersLeft: No Limit";
    }
    else return Volunteer::toString() + "\nTimeLeft: " + std :: to_string(timeLeft) + "\nOrdersLeft: No Limit";
}


//Constructor for the LimitedCollectorVolunteer class:
LimitedCollectorVolunteer ::  LimitedCollectorVolunteer(int id, const string &name, int coolDown ,int maxOrders): 
CollectorVolunteer(id , name , coolDown) , maxOrders(maxOrders) , ordersLeft(maxOrders)  {}


LimitedCollectorVolunteer* LimitedCollectorVolunteer :: clone() const {
    return new LimitedCollectorVolunteer(*this);
}


bool LimitedCollectorVolunteer :: hasOrdersLeft() const {
    if(ordersLeft == 0){
        return false;
    }
    return true;
}


bool LimitedCollectorVolunteer :: canTakeOrder(const Order &order) const{
    if(!isBusy() && hasOrdersLeft() && order.getStatus() == OrderStatus::PENDING){
        return true;
    }
    return false;
}


void LimitedCollectorVolunteer :: acceptOrder(const Order &order){
    if(canTakeOrder(order)){
        CollectorVolunteer :: acceptOrder(order);
        ordersLeft--;
    }
}


int LimitedCollectorVolunteer :: getMaxOrders() const {
    return maxOrders;
}


int LimitedCollectorVolunteer :: getNumOrdersLeft() const{
    return ordersLeft;
}


string LimitedCollectorVolunteer :: toString() const {
     if(getTimeLeft() == 0 ){
         return Volunteer::toString() + "\nTimeLeft: None" + "\nOrdersLeft: " + std :: to_string(ordersLeft);
    }
    else return Volunteer::toString() + "\nTimeLeft: " + std :: to_string(getTimeLeft()) + "\nOrdersLeft: " + std :: to_string(ordersLeft);
}


//Constructor for the DriverVolunteer class:
DriverVolunteer :: DriverVolunteer(int id, const string &name, int maxDistance, int distancePerStep) : 
Volunteer(id , name) , maxDistance(maxDistance) , distancePerStep(distancePerStep) , distanceLeft(NO_ORDER) {

}


DriverVolunteer* DriverVolunteer :: clone() const {
    return new DriverVolunteer(*this);
}


int DriverVolunteer :: getDistanceLeft() const {
    return distanceLeft;
}


int DriverVolunteer :: getMaxDistance() const {
    return maxDistance;
}


int DriverVolunteer :: getDistancePerStep() const {
    return distancePerStep;
}


bool DriverVolunteer :: decreaseDistanceLeft() {
    distanceLeft = distanceLeft - distancePerStep ; 
    if (distanceLeft <= 0 ){
        distanceLeft = 0 ;
        return true;
    }
    return false;
}


bool DriverVolunteer :: hasOrdersLeft() const {
    return true ; 
}


bool DriverVolunteer :: canTakeOrder(const Order &order) const {
    if(!isBusy() && order.getStatus() == OrderStatus::COLLECTING && order.getOrderDistance() <= maxDistance){
        return true ;
    }
    return false;
}


void DriverVolunteer :: acceptOrder(const Order &order){
    if(canTakeOrder(order)){
        completedOrderId = activeOrderId;
        activeOrderId = order.getId();
        distanceLeft = order.getOrderDistance();
    }
}


void DriverVolunteer :: step(){
    if(decreaseDistanceLeft()){
        completedOrderId = activeOrderId;
        activeOrderId = NO_ORDER;
    }
}


string DriverVolunteer :: toString() const {
    if(distanceLeft <= 0){
    return Volunteer :: toString() + "\nDistanceLeft: None"  + "\nOrdersLeft: No Limit";
    }
    return Volunteer :: toString() + "\nDistanceLeft: " + std :: to_string(distanceLeft) + "\nOrdersLeft: No Limit";

}


//Constructor for the LimitedDriverVolunteer class:
LimitedDriverVolunteer :: LimitedDriverVolunteer(int id, const string &name, int maxDistance, int distancePerStep,int maxOrders) : 
DriverVolunteer(id, name , maxDistance , distancePerStep) , maxOrders(maxOrders) , ordersLeft(maxOrders) {

}


LimitedDriverVolunteer* LimitedDriverVolunteer :: clone() const{
    return new LimitedDriverVolunteer(*this);
}


int LimitedDriverVolunteer :: getMaxOrders() const {
    return maxOrders;
}


int LimitedDriverVolunteer :: getNumOrdersLeft() const {
    return ordersLeft;
}


bool LimitedDriverVolunteer :: hasOrdersLeft() const{
    if (ordersLeft != 0){
        return true;
    }
    return false;
}


bool LimitedDriverVolunteer :: canTakeOrder(const Order &order) const{
    if (!isBusy() && hasOrdersLeft() && order.getOrderDistance() <= getMaxDistance()&& order.getStatus() == OrderStatus::COLLECTING){
        return true;
    }
    return false;
}


void LimitedDriverVolunteer :: acceptOrder(const Order &order){
    if(canTakeOrder(order)){
    DriverVolunteer :: acceptOrder(order);
    ordersLeft--;
    }
}


string LimitedDriverVolunteer :: toString() const {
    if(getDistanceLeft() <= 0){
    return Volunteer :: toString() + "\nDistanceLeft: None"  + "\nOrdersLeft: " + std :: to_string(ordersLeft);
    }
    return Volunteer :: toString() + "\nDistanceLeft: " + std :: to_string(getDistanceLeft()) + "\nOrdersLeft: " + std :: to_string(ordersLeft);
}






