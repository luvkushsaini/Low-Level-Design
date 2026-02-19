package CarPoolingSystem;

import java.time.LocalDateTime;
import java.util.List;

public class CarPoolingSystem {
    private UserService userService=new UserService();
    private RideService rideService=new RideService();
    private BookingService bookingService=new BookingService();
    

    public  User createUser(String name,String emailId){
       return  userService.createUser(name, emailId);
    }
     public Ride offerRide(User driver,Vehicle vehicle, String src, String dst,LocalDateTime dateAndTime , int price){
        return rideService.offerRide(driver,vehicle,src,dst,dateAndTime,price);
     }

     public void cancelRide(String rideId,String userId){
        Ride ride=rideService.getRide(rideId);
        if(ride!=null){
            if(ride.getDriver().getUserId().equals(userId)){
            rideService.cancelRide(rideId);
            System.out.println("Ride cancelled successfully");
        }
        else{
            System.out.println("This ride doesnot belong to you");
        }            
        }
     }


     public Booking bookRide(User user, Ride ride, int seatsRequired){
        return bookingService.createBooking(ride,user,seatsRequired);
     }
     public void calcelBooking(String bookingId){

        bookingService.cancelBooking(bookingId);
     }

     public List<Ride>searchRides(String src ,String dst, LocalDateTime dateAndTime,VehicleType vehicleType, int seatsRequired,VehicleType type){
        List<Ride>result= rideService.searchRides(src, dst, dateAndTime, vehicleType, seatsRequired,type);
        if(result.size()==0){
            System.out.println("No Ride Found ");
        }
       return result;
     }
}
