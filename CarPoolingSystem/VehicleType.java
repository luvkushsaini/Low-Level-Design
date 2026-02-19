package CarPoolingSystem;

public enum VehicleType {
    TWO_SEATER(2),
    FOUR_SEATER(4),
    SIX_SEATER(6);

   private final int  seats;
    VehicleType(int seats){
        this.seats=seats;
    }

    public int getTotalSeats(){
        return seats;
    }

}
