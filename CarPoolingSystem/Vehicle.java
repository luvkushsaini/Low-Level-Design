package CarPoolingSystem;

public class Vehicle {
    private String vehicleNumber;
    private VehicleType vehicleType;

    public Vehicle(String vehicleNumber,VehicleType vehicleType){
        this.vehicleNumber=vehicleNumber;
        this.vehicleType=vehicleType;
    }

    public int getTotalSeats(){
        return vehicleType.getTotalSeats();
    }
    public String getVehicleNumber(){
        return vehicleNumber;
    }
    public VehicleType getVehicleType(){
        return vehicleType;
    }
}
