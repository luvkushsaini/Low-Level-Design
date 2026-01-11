import java.util.*;

// ---------------- ENUMS ----------------

enum VehicleType {
    BIKE, CAR, TRUCK
}

enum SlotType {
    SMALL, MEDIUM, LARGE
}

// ---------------- VEHICLE ----------------

class Vehicle {
    private String licensePlate;
    private VehicleType type;
    private Slot assignedSlot;
    private long entryTime;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }

    public void assignSlot(Slot slot) {
        this.assignedSlot = slot;
        this.entryTime = System.currentTimeMillis();
    }

    public Slot getAssignedSlot() { return assignedSlot; }
    public long getEntryTime() { return entryTime; }
}

// ---------------- SLOT ----------------

class Slot {
    private String id;
    private SlotType type;
    private boolean occupied;
    private Vehicle vehicle;

    public Slot(String id, SlotType type) {
        this.id = id;
        this.type = type;
        this.occupied = false;
    }

    public String getId() { return id; }
    public SlotType getType() { return type; }
    public boolean isOccupied() { return occupied; }

    public void assignVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.occupied = true;
    }

    public void removeVehicle() {
        this.vehicle = null;
        this.occupied = false;
    }
}

// ---------------- PARKING TICKET ----------------

class ParkingTicket {
    private String ticketId;
    private Vehicle vehicle;
    private Slot slot;
    private long entryTime;
    private long exitTime;
    private double amount;
    private boolean active;

    public ParkingTicket(Vehicle vehicle, Slot slot) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.slot = slot;
        this.entryTime = System.currentTimeMillis();
        this.active = true;
    }

    public void closeTicket(double amount) {
        this.exitTime = System.currentTimeMillis();
        this.amount = amount;
        this.active = false;
    }

    public Vehicle getVehicle() { return vehicle; }
    public Slot getSlot() { return slot; }
    public long getEntryTime() { return entryTime; }
    public double getAmount() { return amount; }
    public boolean isActive() { return active; }
}

// ---------------- PAYMENT STRATEGY ----------------

interface PaymentStrategy {
    void processPayment(double amount);
}

class CashPayment implements PaymentStrategy {
    public void processPayment(double amount) {
        System.out.println("Paid ₹" + amount + " using Cash");
    }
}

class CardPayment implements PaymentStrategy {
    public void processPayment(double amount) {
        System.out.println("Paid ₹" + amount + " using Card");
    }
}

class UPIPayment implements PaymentStrategy {
    public void processPayment(double amount) {
        System.out.println("Paid ₹" + amount + " using UPI");
    }
}

// ---------------- PAYMENT FACTORY ----------------

class PaymentFactory {
    public static PaymentStrategy getPaymentMethod(String type) {
        switch (type.toUpperCase()) {
            case "CASH": return new CashPayment();
            case "CARD": return new CardPayment();
            case "UPI": return new UPIPayment();
            default: throw new IllegalArgumentException("Invalid payment type: " + type);
        }
    }
}

// ---------------- FEE CALCULATOR ----------------

class FeeCalculator {
    public static double calculateFee(ParkingTicket ticket) {
        long durationMillis = System.currentTimeMillis() - ticket.getEntryTime();
        double hours = Math.ceil(durationMillis / (1000.0 * 60 * 60)); // convert ms → hrs

        switch (ticket.getVehicle().getType()) {
            case BIKE: return hours * 10;
            case CAR:  return hours * 20;
            case TRUCK:return hours * 30;
            default:   return hours * 10;
        }
    }
}

// ---------------- PARKING LOT (SINGLETON) ----------------

class ParkingLot {
    private static ParkingLot instance;
    private Map<SlotType, List<Slot>> slotsByType;
    private Map<String, ParkingTicket> activeTickets;

    private ParkingLot() {
        slotsByType = new HashMap<>();
        activeTickets = new HashMap<>();
        initializeSlots();
    }

    public static synchronized ParkingLot getInstance() {
        if (instance == null)
            instance = new ParkingLot();
        return instance;
    }

    // initialize slots
    private void initializeSlots() {
        slotsByType.put(SlotType.SMALL, new ArrayList<>());
        slotsByType.put(SlotType.MEDIUM, new ArrayList<>());
        slotsByType.put(SlotType.LARGE, new ArrayList<>());

        // Add a few slots
        slotsByType.get(SlotType.SMALL).add(new Slot("S1", SlotType.SMALL));
        slotsByType.get(SlotType.SMALL).add(new Slot("S2", SlotType.SMALL));
        slotsByType.get(SlotType.MEDIUM).add(new Slot("M1", SlotType.MEDIUM));
        slotsByType.get(SlotType.MEDIUM).add(new Slot("M2", SlotType.MEDIUM));
        slotsByType.get(SlotType.LARGE).add(new Slot("L1", SlotType.LARGE));
    }

    public ParkingTicket enter(Vehicle vehicle) {
        SlotType required = getRequiredSlotType(vehicle.getType());
        List<Slot> slots = slotsByType.get(required);

        for (Slot slot : slots) {
            if (!slot.isOccupied()) {
                slot.assignVehicle(vehicle);
                vehicle.assignSlot(slot);

                ParkingTicket ticket = new ParkingTicket(vehicle, slot);
                activeTickets.put(vehicle.getLicensePlate(), ticket);
                System.out.println("Vehicle " + vehicle.getLicensePlate() +
                                   " parked at slot " + slot.getId());
                return ticket;
            }
        }
        System.out.println("No available slot for " + vehicle.getLicensePlate());
        return null;
    }

    public void exit(String licensePlate, String paymentType) {
        ParkingTicket ticket = activeTickets.get(licensePlate);
        if (ticket == null) {
            System.out.println("No active ticket found for " + licensePlate);
            return;
        }

        double fee = FeeCalculator.calculateFee(ticket);
        PaymentStrategy payment = PaymentFactory.getPaymentMethod(paymentType);
        payment.processPayment(fee);

        ticket.closeTicket(fee);
        Slot slot = ticket.getSlot();
        slot.removeVehicle();
        activeTickets.remove(licensePlate);

        System.out.println("Vehicle " + licensePlate + " exited. Payment successful.");
    }

    private SlotType getRequiredSlotType(VehicleType type) {
        switch (type) {
            case BIKE:  return SlotType.SMALL;
            case CAR:   return SlotType.MEDIUM;
            case TRUCK: return SlotType.LARGE;
            default:    return SlotType.MEDIUM;
        }
    }
}

// ---------------- DRIVER ----------------

public class Main {
    public static void main(String[] args) {
        ParkingLot lot = ParkingLot.getInstance();

        Vehicle car = new Vehicle("MH12AB1234", VehicleType.CAR);
        Vehicle bike = new Vehicle("MH14XY6789", VehicleType.BIKE);
        Vehicle truck = new Vehicle("MH15ZZ9999", VehicleType.TRUCK);

        ParkingTicket t1 = lot.enter(car);
        ParkingTicket t2 = lot.enter(bike);
        ParkingTicket t3 = lot.enter(truck);

        // simulate exit
        lot.exit("MH12AB1234", "CARD");
        lot.exit("MH14XY6789", "UPI");
        lot.exit("MH15ZZ9999", "CASH");
    }
}
/*
----------------------------------------------------------
🎯 QUESTION:
Design a Parking Lot System (single floor version).

The system should:
- Allow vehicles (bike, car, truck) to enter and exit.
- Assign an available parking slot based on vehicle type.
- Generate a parking ticket on entry.
- Calculate the parking fee on exit based on duration.
- Accept different payment methods (cash, card, UPI).
- Display "Payment Successful" after completion.

----------------------------------------------------------
🧠 INITIAL APPROACH & INTUITION BUILDING:

1️⃣ Understanding the system:
   We observed that a Parking Lot is a *centralized manager* 
   that coordinates vehicles, slots, and payments. 
   Vehicles come and go, slots stay fixed — so ParkingLot 
   must control all interactions (not Vehicle).

2️⃣ Identifying entities:
   - Vehicle → represents what is being parked.
   - Slot → represents a physical space (Small/Medium/Large).
   - ParkingTicket → tracks entry time, exit time, fee.
   - Payment → handles how fee is paid.
   - FeeCalculator → calculates the total cost.

   Each of these entities was separated into its own class
   to maintain *Single Responsibility Principle (SRP)* — 
   each class has exactly one reason to change.

3️⃣ Basic flow:
   - Vehicle enters → ParkingLot finds a free Slot.
   - Ticket is created → entry time recorded.
   - Vehicle exits → fee calculated → payment processed.
   - Slot becomes free again.

----------------------------------------------------------
🏗️ DESIGN PATTERNS USED & WHY:

1️⃣ **Singleton Pattern (ParkingLot)**
   - Ensures only one instance of ParkingLot exists in the system.
   - ParkingLot is the central controller; multiple instances
     could lead to inconsistent state.
   - Common for global system managers (e.g., DatabaseConnection, Logger).

   ✅ Future use:
   In large systems, a Singleton *Manager* can handle multiple lots
   (e.g., ParkingLotManager controlling multiple ParkingLot objects).

----------------------------------------------------------

2️⃣ **Factory Pattern (PaymentFactory)**
   - Simplifies object creation of different payment methods
     (UPI, Card, Cash) without if/else logic scattered in business code.
   - Factory returns the correct PaymentStrategy object based on user input.

   ✅ Future use:
   Whenever object creation varies based on a string or type
   (like NotificationFactory → Email, SMS, Push),
   we can reuse this pattern.

----------------------------------------------------------

3️⃣ **Strategy Pattern (PaymentStrategy)**
   - Each payment method has a different way to process payment,
     but the ParkingLot should not know those details.
   - Common interface → `processPayment(double amount)`
   - Concrete strategies: CashPayment, CardPayment, UPIPayment.

   ✅ Future use:
   Whenever you have interchangeable behaviors (e.g., 
   PaymentStrategy, SortingStrategy, DiscountStrategy),
   Strategy Pattern provides flexibility.

----------------------------------------------------------
⚙️ DESIGN DECISIONS & CLASS RESPONSIBILITIES:

- `Vehicle` → stores type (BIKE/CAR/TRUCK) and license info.
  Each vehicle knows what size slot it requires.

- `Slot` → represents a parking space with type and occupancy.
  Slots are fixed resources controlled by the ParkingLot.

- `ParkingTicket` → represents a parking session.
  Stores vehicle, slot, entry/exit time, and final amount.

- `ParkingLot` → the orchestrator.
  Handles entering, assigning slots, generating tickets, 
  calculating fees, and coordinating payments.

- `FeeCalculator` → encapsulates the logic of fee calculation.
  Makes pricing rules easy to modify later.

- `PaymentFactory` + `PaymentStrategy` → handle payment creation 
  and processing logic separately for clean extensibility.

----------------------------------------------------------
💡 EXTENSIBILITY & SCALABILITY:

- **Add Floors** → Introduce a `Floor` class with slots per floor,
  and update ParkingLot to manage multiple floors.

- **Add new Vehicle types** → Just extend VehicleType enum and 
  update slot assignment logic.

- **Add new Payment methods** → Create a new class implementing 
  PaymentStrategy and add one line in PaymentFactory.

- **Change Fee Rules** → Create new classes implementing a 
  FeeCalculationStrategy (e.g., HourlyFee, FlatRateFee).

- **Multiple Parking Lots** → Introduce a `ParkingLotManager` Singleton
  to handle multiple ParkingLot instances.

----------------------------------------------------------
📘 WHY THIS DESIGN IS INTERVIEW-STANDARD:

- Demonstrates key OOP principles:
  Encapsulation, SRP, OCP (Open/Closed Principle), and Liskov Substitution.
- Combines multiple patterns correctly and justifies each.
- Clean, modular, readable — easy to extend without rewriting core logic.
- Closely matches real-world parking system behavior.

----------------------------------------------------------
💬 SUMMARY:
Start → Think about real-world flow → Identify entities → Assign responsibilities → 
Apply patterns only where behavior or creation varies → Ensure extensibility.

This design is extensible, modular, and uses real-world reasoning.
Perfect balance between simplicity and scalability.

----------------------------------------------------------
*/
