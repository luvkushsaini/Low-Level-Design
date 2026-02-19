package CarPoolingSystem;

public class Main {
    
}
/*
===============================================================
                CAR POOLING SYSTEM – LLD
===============================================================

PROBLEM STATEMENT (Interview Style)
---------------------------------------------------------------
Design a Car Pooling / Ride Sharing system where users can:
1. Create an account.
2. Offer a ride as a driver with source, destination, date-time, vehicle and price.
3. Search for available rides based on source, destination, date-time, vehicle type and seats.
4. Book seats in a ride if seats are available.
5. Cancel their booking.
6. Driver can cancel or start the ride.
7. System should manage seat availability and ride status lifecycle.
8. In-memory storage is sufficient. No DB/UI required.

The goal is to design clean object-oriented code following SOLID
principles with proper separation of concerns.

---------------------------------------------------------------
APPROACH / DESIGN THINKING
---------------------------------------------------------------
1. Identify Core Entities:
   - User      → Represents system users.
   - Vehicle   → Represents vehicle type and capacity.
   - Ride      → Represents a ride offered by a driver.
   - Booking   → Represents a passenger’s reservation.

2. Identify Responsibilities:
   - Entities hold data + small business behavior.
   - Services handle business logic and storage.
   - Facade class provides a simple interface for interaction.

3. State Management:
   Ride has lifecycle states:
   CREATED → FULL → ONGOING → COMPLETED
   CREATED → CANCELLED

4. Seat Management:
   - On booking → reduce seats.
   - On cancellation → increase seats.
   - When seats = 0 → mark ride FULL.

5. Encapsulation:
   - Fields are private.
   - State changes via behavior methods (startRide, cancelRide, etc.)
   - No generic setters for status.

---------------------------------------------------------------
ARCHITECTURE / CLASS STRUCTURE
---------------------------------------------------------------

ENTITIES (Domain Models)
-------------------------
User
 - userId, name, email

Vehicle
 - vehicleNumber, vehicleType
 - seat capacity derived from enum

Ride
 - rideId, source, destination, dateTime
 - driver, vehicle, pricePerSeat
 - availableSeats, rideStatus
 - Methods: reduceSeats, increaseSeats, startRide, cancelRide, completeRide

Booking
 - bookingId, ride, user, seatsRequired, bookingStatus
 - Methods: cancel()

ENUMS
-----
RideStatus → CREATED, FULL, ONGOING, COMPLETED, CANCELLED
BookingStatus → CONFIRMED, CANCELLED
VehicleType → TWO_SEATER, FOUR_SEATER, SIX_SEATER (with seat count)

SERVICES (Business Logic + Storage)
------------------------------------
UserService
 - createUser, getUser
 - Map<userId, User>

RideService
 - offerRide, searchRides, startRide, cancelRide, getRide
 - Map<rideId, Ride>

BookingService
 - createBooking, cancelBooking
 - Map<bookingId, Booking>

FACADE / ENTRY POINT
---------------------
CarPoolingSystem
 - Acts as a controller layer.
 - Interacts with all services.
 - Provides simple APIs for UI / Main class.

---------------------------------------------------------------
KEY DESIGN PRINCIPLES FOLLOWED
---------------------------------------------------------------
- Encapsulation
- Single Responsibility Principle
- Separation of Concerns
- Behavior-Driven State Changes
- Extensibility (easy to add payment, rating, notifications)
- In-Memory Data Management

---------------------------------------------------------------
POSSIBLE FUTURE IMPROVEMENTS
---------------------------------------------------------------
- Concurrency control using locks.
- Booking history per user.
- Payment and wallet integration.
- Notifications (SMS/Email).
- Scheduler to auto-complete rides.
- Logging framework instead of console prints.

===============================================================
*/


/*
===============================================================
            LEARNING NOTES / MISTAKES & IMPROVEMENTS
===============================================================

During implementation, the following logical and design mistakes
were identified and corrected. These notes help avoid repeating
them in future LLD or machine-coding rounds.

---------------------------------------------------------------
1. Using '==' Instead of '.equals()' for Objects
---------------------------------------------------------------
Mistake:
    if(source == src)

Why Wrong:
    '==' compares memory references, not actual values.
    Works sometimes for Strings due to interning, but unreliable.

Fix:
    source.equals(src)

Rule:
    Always use .equals() for String, LocalDateTime, UUID, Integer etc.

---------------------------------------------------------------
2. Making Fields Public Instead of Private
---------------------------------------------------------------
Mistake:
    public String source;

Why Wrong:
    Breaks encapsulation. Any class can modify data freely,
    leading to inconsistent state.

Fix:
    private fields + public getters.
    Modify data only through behavior methods.

Rule:
    Data private, behavior public.

---------------------------------------------------------------
3. Generic Setters for Status
---------------------------------------------------------------
Mistake:
    setRideStatus(RideStatus status)

Why Wrong:
    Allows illegal transitions like COMPLETED → CREATED.
    Breaks business rules and state machine.

Fix:
    Use intent-based methods:
        startRide()
        cancelRide()
        completeRide()

Rule:
    Status change = behavior, not plain setter.

---------------------------------------------------------------
4. Seat Update Logic Initially Reversed
---------------------------------------------------------------
Mistake:
    Booking → increaseSeats()

Why Wrong:
    Booking should reduce seats, not increase.

Fix:
    Booking → reduceSeats()
    Cancel → increaseSeats()

Rule:
    Booking decreases capacity, cancellation restores it.

---------------------------------------------------------------
5. Removing Ride Instead of Updating Status
---------------------------------------------------------------
Mistake:
    rides.remove(rideId);

Why Suboptimal:
    Loses history and audit trail.
    Harder to debug or extend with analytics/refunds.

Fix:
    ride.cancelRide();  // change status to CANCELLED

Rule:
    Prefer logical deletion (status change) over physical deletion.

---------------------------------------------------------------
6. Missing Null Checks
---------------------------------------------------------------
Mistake:
    ride.startRide(); // without checking null

Why Wrong:
    Causes NullPointerException if ride not found.

Fix:
    if(ride != null) ride.startRide();

Rule:
    Always validate object existence before operations.

---------------------------------------------------------------
7. Redundant Fields
---------------------------------------------------------------
Mistake:
    Storing Vehicle inside Booking while Ride already has it.

Why Wrong:
    Duplication leads to inconsistency and unnecessary memory use.

Fix:
    Access via booking.getRide().getVehicle()

Rule:
    Avoid storing the same data in multiple places.

---------------------------------------------------------------
8. Constructor Visibility
---------------------------------------------------------------
Mistake:
    Package-private constructors unintentionally.

Why Wrong:
    Restricts object creation unexpectedly.

Fix:
    Use public constructors unless intentionally restricted.

---------------------------------------------------------------
9. Returning null Instead of Empty Collections
---------------------------------------------------------------
Mistake:
    return null;

Why Risky:
    Caller must handle null → potential NPE.

Fix:
    return new ArrayList<>();

Rule:
    Prefer empty collections over null.

---------------------------------------------------------------
10. Business Logic Inside Entities vs Services
---------------------------------------------------------------
Learning:
    Entities → data + small behavior.
    Services → orchestration and storage logic.

This separation improves maintainability and scalability.

---------------------------------------------------------------
FINAL TAKEAWAY
---------------------------------------------------------------
- Encapsulation is critical.
- Use behavior methods instead of setters.
- Think in terms of state machines.
- Avoid duplication.
- Validate inputs and nulls.
- Prefer logical deletion.
- Services handle orchestration, entities handle state.

These corrections significantly improved code quality,
extensibility, and interview readiness.

===============================================================
*/
