package CarPoolingSystem;

import java.util.HashMap;
import java.util.Map;

public class BookingService {
    private Map<String, Booking>bookings=new HashMap<>();

    public Booking createBooking(Ride ride,User user,int seats){
        if(ride.hasSeats(seats) && ride.getRideStatus()==RideStatus.CREATED){
        Booking booking =new Booking(ride, user,seats);   
        bookings.put(booking.getBookingId(), booking);
        ride.reduceSeats(seats);
        return booking;
        }
        else{
            System.out.println("Not enough seats available or ride already Started");
            return null;
        }
    }

public void cancelBooking(String bookingId){
    Booking booking = bookings.get(bookingId);

    if (booking == null) {
        System.out.println("No such booking found");
        return;
    }

    booking.cancel();
    booking.getRide().increaseSeats(booking.getSeatsRequired());
    bookings.remove(bookingId);
}

}
