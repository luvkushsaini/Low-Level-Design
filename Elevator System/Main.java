package Questions.Elevator;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * =========================
 * ENUMS
 * =========================
 */

enum Direction {
    UP, DOWN, IDLE
}

enum RequestSource {
    INTERNAL, EXTERNAL
}

/*
 * =========================
 * REQUEST MODEL
 * =========================
 * Immutable-like object that represents a button press
 */
class Request {
    private final Direction direction;
    private final RequestSource source;
    private final int targetFloor;

    public Request(int targetFloor, Direction direction, RequestSource source) {
        this.targetFloor = targetFloor;
        this.direction = direction;
        this.source = source;
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public RequestSource getSource() {
        return source;
    }
}

/*
 * =========================
 * STRATEGY PATTERN
 * =========================
 * Used to decide WHICH elevator should handle an external request
 */
interface ElevatorSelectionStrategy {
    Optional<Elevator> selectElevator(List<Elevator> elevators, Request request);
}

/*
 * Nearest elevator based on distance
 */
class NearestElevatorStrategy implements ElevatorSelectionStrategy {

    @Override
    public Optional<Elevator> selectElevator(List<Elevator> elevators, Request request) {
        Elevator best = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator e : elevators) {
            int distance = Math.abs(e.getCurrentFloor() - request.getTargetFloor());
            if (distance < minDistance) {
                minDistance = distance;
                best = e;
            }
        }
        // Optional used to avoid null handling by caller
        return Optional.ofNullable(best);
    }
}

/*
 * =========================
 * OBSERVER PATTERN
 * =========================
 * Used for display/logging without coupling to Elevator logic
 */
interface Observer {
    void update(Elevator elevator);
}

class Display implements Observer {

    @Override
    public void update(Elevator elevator) {
        System.out.println(
            "[DISPLAY] Elevator " + elevator.getId() +
            " | Floor: " + elevator.getCurrentFloor() +
            " | Direction: " + elevator.getDirection()
        );
    }
}

/*
 * =========================
 * STATE PATTERN
 * =========================
 * Elevator behavior changes based on its current state
 */
interface State {
    void move(Elevator elevator);
    void addRequest(Elevator elevator, Request request);
    Direction getDirection();
}

/*
 * Elevator is idle (not moving)
 */
class IdleState implements State {

    @Override
    public void move(Elevator elevator) {
        if (!elevator.getUpRequests().isEmpty()) {
            elevator.setState(new UpState());
        } else if (!elevator.getDownRequests().isEmpty()) {
            elevator.setState(new DownState());
        }
    }

    @Override
    public void addRequest(Elevator elevator, Request request) {
        if (request.getTargetFloor() > elevator.getCurrentFloor()) {
            elevator.getUpRequests().add(request.getTargetFloor());
        } else if (request.getTargetFloor() < elevator.getCurrentFloor()) {
            elevator.getDownRequests().add(request.getTargetFloor());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.IDLE;
    }
}

/*
 * Elevator moving UP
 */
class UpState implements State {

    @Override
    public void move(Elevator elevator) {
        if (elevator.getUpRequests().isEmpty()) {
            elevator.setState(new IdleState());
            return;
        }

        int nextFloor = elevator.getUpRequests().first();
        elevator.setCurrentFloor(elevator.getCurrentFloor() + 1);

        if (elevator.getCurrentFloor() == nextFloor) {
            System.out.println("Elevator " + elevator.getId() +
                    " stopped at floor " + nextFloor);
            elevator.getUpRequests().pollFirst();
        }
    }

    @Override
    public void addRequest(Elevator elevator, Request request) {
        if (request.getTargetFloor() > elevator.getCurrentFloor()) {
            elevator.getUpRequests().add(request.getTargetFloor());
        } else {
            elevator.getDownRequests().add(request.getTargetFloor());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.UP;
    }
}

/*
 * Elevator moving DOWN
 */
class DownState implements State {

    @Override
    public void move(Elevator elevator) {
        if (elevator.getDownRequests().isEmpty()) {
            elevator.setState(new IdleState());
            return;
        }

        int nextFloor = elevator.getDownRequests().first();
        elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);

        if (elevator.getCurrentFloor() == nextFloor) {
            System.out.println("Elevator " + elevator.getId() +
                    " stopped at floor " + nextFloor);
            elevator.getDownRequests().pollFirst();
        }
    }

    @Override
    public void addRequest(Elevator elevator, Request request) {
        if (request.getTargetFloor() < elevator.getCurrentFloor()) {
            elevator.getDownRequests().add(request.getTargetFloor());
        } else {
            elevator.getUpRequests().add(request.getTargetFloor());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.DOWN;
    }
}

/*
 * =========================
 * ELEVATOR (CORE ENTITY)
 * =========================
 * Each Elevator runs on its OWN THREAD
 */
class Elevator implements Runnable {

    private final int id;

    // AtomicInteger used because multiple threads read/write currentFloor
    private final AtomicInteger currentFloor = new AtomicInteger(1);

    // volatile ensures stop signal is visible to elevator thread
    private volatile boolean running = true;

    private State currentState = new IdleState();
    private Direction direction = Direction.IDLE;

    private final TreeSet<Integer> upRequests = new TreeSet<>();
    private final TreeSet<Integer> downRequests = new TreeSet<>(Collections.reverseOrder());

    private final List<Observer> observers = new ArrayList<>();

    public Elevator(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor.get();
    }

    public Direction getDirection() {
        return direction;
    }

    public TreeSet<Integer> getUpRequests() {
        return upRequests;
    }

    public TreeSet<Integer> getDownRequests() {
        return downRequests;
    }

    // synchronized because multiple threads may add requests
    public synchronized void addRequest(Request request) {
        currentState.addRequest(this, request);
    }

    public void setCurrentFloor(int floor) {
        currentFloor.set(floor);
        notifyObservers();
    }

    public void setState(State state) {
        this.currentState = state;
        this.direction = state.getDirection();
        notifyObservers();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
        observer.update(this);
    }

    private void notifyObservers() {
        observers.forEach(o -> o.update(this));
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        // Continuous life-cycle of elevator
        while (running) {
            currentState.move(this);
            try {
                Thread.sleep(1000); // simulate real time movement
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}

/*
 * =========================
 * FACADE + SINGLETON
 * =========================
 * Only entry point for clients
 */
class ElevatorSystem {

    private static ElevatorSystem instance;

    private final Map<Integer, Elevator> elevators = new HashMap<>();
    private final ElevatorSelectionStrategy strategy = new NearestElevatorStrategy();

    // ExecutorService manages all elevator threads
    private final ExecutorService executor;

    private ElevatorSystem(int count) {
        executor = Executors.newFixedThreadPool(count);
        Display display = new Display();

        for (int i = 1; i <= count; i++) {
            Elevator elevator = new Elevator(i);
            elevator.addObserver(display);
            elevators.put(i, elevator);
        }
    }

    public static synchronized ElevatorSystem getInstance(int count) {
        if (instance == null) {
            instance = new ElevatorSystem(count);
        }
        return instance;
    }

    public void start() {
        elevators.values().forEach(executor::submit);
    }

    public void shutdown() {
        elevators.values().forEach(Elevator::stop);
        executor.shutdown();
    }

    // External hall button
    public void requestElevator(int floor, Direction direction) {
        Request request = new Request(floor, direction, RequestSource.EXTERNAL);
        strategy
            .selectElevator(new ArrayList<>(elevators.values()), request)
            .ifPresent(e -> e.addRequest(request));
    }

    // Internal cabin button
    public void selectFloor(int elevatorId, int floor) {
        Elevator elevator = elevators.get(elevatorId);
        if (elevator == null) return;

        Request request = new Request(floor, Direction.IDLE, RequestSource.INTERNAL);
        elevator.addRequest(request);
    }
}

/*
 * =========================
 * DEMO / MAIN
 * =========================
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        ElevatorSystem system = ElevatorSystem.getInstance(2);
        system.start();

        system.requestElevator(3, Direction.UP);
        Thread.sleep(1500);

        system.selectFloor(1, 7);
        Thread.sleep(3000);

        system.shutdown();
    }
}


/*
===============================================================================
ELEVATOR SYSTEM – REVISION NOTES (READ THIS FIRST IN FUTURE)
===============================================================================

PROBLEM STATEMENT
-----------------
Design a multi-elevator system for a building where:
- Multiple elevators operate independently and simultaneously
- Users can make:
  1) EXTERNAL requests (hall buttons: floor + direction)
  2) INTERNAL requests (inside elevator: destination floor)
- System should decide which elevator serves an external request
- Elevators should move, stop at floors, and update display
- Design should be scalable, clean, and thread-safe

This is NOT a DSA problem.
This is a LOW LEVEL DESIGN + CONCURRENCY problem.

-------------------------------------------------------------------------------

HIGH LEVEL APPROACH
-------------------
We break the problem into responsibilities:

1) ElevatorSystem
   - Acts as the SINGLE entry point for clients
   - Starts/stops elevators
   - Assigns external requests to elevators

2) Elevator
   - Represents a real elevator
   - Runs on its OWN THREAD
   - Maintains its own state and request queues

3) Request
   - Represents a button press (internal or external)

4) Patterns are used to avoid complexity:
   - Strategy → choosing elevator
   - State → elevator movement logic
   - Observer → display updates

-------------------------------------------------------------------------------

WHY THIS IS A CONCURRENCY PROBLEM
--------------------------------
- Multiple elevators move at the same time
- Each elevator has its own life-cycle
- Requests can come while elevators are moving

Solution:
- Each Elevator implements Runnable
- ExecutorService manages threads
- Each elevator thread runs independently

Key Rule:
ExecutorService STARTS threads
Runnable decides WHEN to STOP

-------------------------------------------------------------------------------

THREADING DESIGN (IMPORTANT)
----------------------------
- Elevator implements Runnable
- ElevatorSystem uses ExecutorService (FixedThreadPool)
- submit(elevator) → executor runs elevator.run() on a thread
- run() contains a loop controlled by a volatile boolean

Why volatile?
- Stop signal must be visible across threads

Why AtomicInteger for currentFloor?
- Multiple threads read/write currentFloor safely
- Avoid race conditions

-------------------------------------------------------------------------------

PATTERNS USED (VERY IMPORTANT FOR INTERVIEWS)
---------------------------------------------

1) FACADE PATTERN
-----------------
Class: ElevatorSystem

Why:
- Client should not deal with threads, states, queues
- Client only calls:
  - start()
  - requestElevator()
  - selectFloor()
  - shutdown()

This reduces coupling and hides complexity.

---

2) STRATEGY PATTERN
-------------------
Interface: ElevatorSelectionStrategy
Implementation: NearestElevatorStrategy

Why:
- Elevator selection logic can change
- Today: nearest elevator
- Tomorrow: least loaded, direction-based, AI-based

Client code does not change.

---

3) STATE PATTERN
----------------
Interface: State
Implementations:
- IdleState
- UpState
- DownState

Why:
- Elevator behavior depends on current state
- Avoid large if-else chains
- Each state handles:
  - how to move
  - how to accept requests
  - what direction it represents

State transitions happen inside elevator.

---

4) OBSERVER PATTERN
-------------------
Interface: Observer
Implementation: Display

Why:
- Elevator should not know HOW status is shown
- Display/logging should react to changes
- Decouples business logic from presentation

Whenever elevator changes floor or state → observers notified.

-------------------------------------------------------------------------------

REQUEST HANDLING LOGIC
----------------------

EXTERNAL REQUEST (Hall Button):
- Goes to ElevatorSystem
- Strategy selects ONE elevator
- Request added ONLY to that elevator
- No broadcast → avoids duplicate servicing

INTERNAL REQUEST (Cabin Button):
- Directly added to selected elevator
- Direction is not important (handled by state)

-------------------------------------------------------------------------------

DATA STRUCTURES USED
-------------------
- TreeSet for upRequests (sorted ascending)
- TreeSet for downRequests (sorted descending)

Why?
- Always know next closest floor in direction
- Efficient removal once served

-------------------------------------------------------------------------------

IMPORTANT DESIGN DECISIONS
--------------------------

1) Why not call run() directly?
- run() is just a normal method
- submit() creates thread context
- Calling run() directly breaks concurrency

2) Why ExecutorService?
- Controls number of threads
- Reuses threads
- Clean shutdown

3) Why synchronized addRequest()?
- Multiple threads can add requests
- Prevent race conditions on queues

4) Why Optional in strategy?
- Avoid null handling
- Explicitly represent "no elevator found"

-------------------------------------------------------------------------------

COMMON MISTAKES TO AVOID (YOU FACED THESE)
------------------------------------------
- Forgetting to call system.start()
- Forgetting to attach observers
- Using int instead of AtomicInteger
- Calling run() instead of submit()
- Forgetting volatile for stop flag
- Broadcasting requests to all elevators

-------------------------------------------------------------------------------

HOW TO EXPLAIN THIS IN INTERVIEW (SHORT)
----------------------------------------
"This is a concurrent elevator system using ExecutorService where each elevator
runs independently. External requests are assigned using a strategy pattern,
movement is handled via state pattern, and display updates use observer pattern.
Thread safety is ensured using AtomicInteger, synchronized methods, and volatile
flags."

-------------------------------------------------------------------------------

MENTAL MODEL TO REMEMBER
-----------------------
Client → Facade → Strategy → Elevator → State
                       ↘ Observer

ExecutorService → Threads → Elevator.run()

-------------------------------------------------------------------------------

END OF REVISION NOTES
===============================================================================
*/
