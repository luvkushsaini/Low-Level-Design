package CarPoolingSystem;
import java.time.LocalDateTime;
import java.util.UUID;

public class Ride {

    private RideStatus rideStatus;
    private final String rideId;
    private String source;
    private String destination;
    private LocalDateTime dateAndTime;
    private User driver;
    private Vehicle vehicle;
    private double pricePerSeat;
    private int availableSeats;

    public Ride(User driver, Vehicle vehicle, String source,
                String destination, LocalDateTime dateAndTime,
                double pricePerSeat) {

        this.rideId = UUID.randomUUID().toString();
        this.rideStatus = RideStatus.CREATED;
        this.driver = driver;
        this.vehicle = vehicle;
        this.pricePerSeat = pricePerSeat;
        this.source = source;
        this.destination = destination;
        this.dateAndTime = dateAndTime;
        this.availableSeats = vehicle.getTotalSeats();
    }

    // -------- Business Methods --------

    public boolean hasSeats(int requested) {
        return this.availableSeats >= requested;
    }

    public void reduceSeats(int seats) {
        if (seats <= 0) return;

        this.availableSeats -= seats;
        if (this.availableSeats == 0) {
            this.rideStatus = RideStatus.FULL;
        }
    }

    public void increaseSeats(int seats) {
        if (seats <= 0) return;

        this.availableSeats += seats;
        if (this.availableSeats > 0 && this.rideStatus == RideStatus.FULL) {
            this.rideStatus = RideStatus.CREATED;
        }
    }

    public void startRide() {
        if (rideStatus == RideStatus.CREATED || rideStatus == RideStatus.FULL) {
            this.rideStatus = RideStatus.ONGOING;
        }
    }

    public void cancelRide() {
        this.rideStatus = RideStatus.CANCELLED;
    }
    public void completeRide() {
    if (rideStatus == RideStatus.ONGOING) {
        this.rideStatus = RideStatus.COMPLETED;
    }
}


    // -------- Getters --------

    public String getRideId() {
        return rideId;
    }

    public RideStatus getRideStatus() {
        return rideStatus;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDateAndTime() {
        return dateAndTime;
    }

    public User getDriver() {
        return driver;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getPricePerSeat() {
        return pricePerSeat;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }
}
