package CarPoolingSystem;

import java.util.UUID;

public class Booking {

    private final String bookingId;
    private final Ride ride;
    private final User user;
    private BookingStatus bookingStatus;
    private final int seatsRequired;

    public Booking(Ride ride, User user, int seatsRequired) {
        this.bookingId = UUID.randomUUID().toString();
        this.ride = ride;
        this.user = user;
        this.seatsRequired = seatsRequired;
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    // -------- Behavior Methods --------

    public void cancel() {
        if (bookingStatus == BookingStatus.CONFIRMED) {
            this.bookingStatus = BookingStatus.CANCELLED;
        }
    }
    // -------- Getters --------

    public String getBookingId() {
        return bookingId;
    }

    public Ride getRide() {
        return ride;
    }

    public User getUser() {
        return user;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public int getSeatsRequired() {
        return seatsRequired;
    }
}
