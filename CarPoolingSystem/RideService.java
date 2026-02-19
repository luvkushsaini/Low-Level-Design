package CarPoolingSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideService {
   private Map<String,Ride>rides=new HashMap<>();

    public Ride offerRide(User driver,Vehicle vehicle, String src, String dst,LocalDateTime dateAndTime , int price){
        Ride ride=new  Ride(driver,vehicle,src,dst,dateAndTime,price);
        rides.put(ride.getRideId(),ride);
        return ride;
    }

    public List<Ride>searchRides(String src ,String dst, LocalDateTime dateAndTime,VehicleType vehicleType, int seatsRequired){
            List<Ride>result=new  ArrayList<>();
            for(Map.Entry<String , Ride>entry:rides.entrySet()){
                Ride ride =entry.getValue();
                if(ride.getDateAndTime().equals(dateAndTime)&&ride.getDestination().equals(dst) && ride.getSource().equals(src) && ride.hasSeats(seatsRequired) && ride.getRideStatus()==RideStatus.CREATED && ride.getVehicle().getVehicleType()==vehicleType){
                    result.add(ride);
                }
            }
            return result;
    }

    void cancelRide(String rideId){
        Ride ride=rides.get(rideId);
        if(ride!=null)ride.cancelRide();
        
    }
    void startRide(String rideId){
        Ride ride=rides.get(rideId);
        if(ride!=null)ride.startRide();
        
    }

    Ride getRide(String rideId){

        Ride ride =rides.getOrDefault(rideId,null);
        if(ride !=null){
            return ride;
        }
        else {
            System.out.println("No such ride found");
            return null;
        }
    }


}
