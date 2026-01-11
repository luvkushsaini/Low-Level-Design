import java.util.HashMap;
import java.util.Map;

/*
========================
1️⃣ ITEM ENTITY
========================
Represents a product in the vending machine
*/
class Item {
    String name;
    int price;

    Item(String name, int price) {
        this.name = name;
        this.price = price;
    }
}

/*
========================
2️⃣ INVENTORY ENTITY
========================
Responsible for:
- Storing items
- Tracking stock
*/
class Inventory {
    private Map<String, Item> items = new HashMap<>();
    private Map<String, Integer> stock = new HashMap<>();

    // Add item with quantity
    public void addItem(Item item, int quantity) {
        items.put(item.name, item);
        stock.put(item.name, quantity);
    }

    // Check availability
    public boolean isAvailable(String itemName) {
        return stock.getOrDefault(itemName, 0) > 0;
    }

    // Reduce stock after dispensing
    public void reduceStock(String itemName) {
        stock.put(itemName, stock.get(itemName) - 1);
    }

    // Get item details
    public Item getItem(String itemName) {
        return items.get(itemName);
    }
}

/*
========================
3️⃣ STATE INTERFACE
========================
Defines actions allowed in each state
*/
interface State {
    void selectItem(String itemName);
    void insertMoney(int amount);
    void dispenseItem();
    void refund();
}

/*
========================
4️⃣ IDLE STATE
========================
Machine is waiting for item selection
*/
class IdleState implements State {
    VendingMachine machine;

    IdleState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String itemName) {
        if (!machine.inventory.isAvailable(itemName)) {
            System.out.println("❌ Item out of stock");
            return;
        }
        machine.selectedItem = itemName;
        machine.setState(machine.hasMoneyState);
        System.out.println("✅ Item selected: " + itemName);
    }

    public void insertMoney(int amount) {
        System.out.println("⚠️ Select item first");
    }

    public void dispenseItem() {
        System.out.println("⚠️ Select item first");
    }

    public void refund() {
        System.out.println("⚠️ No money to refund");
    }
}

/*
========================
5️⃣ HAS MONEY STATE
========================
User inserts money after selecting item
*/
class HasMoneyState implements State {
    VendingMachine machine;

    HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String itemName) {
        System.out.println("⚠️ Item already selected");
    }

    public void insertMoney(int amount) {
        machine.balance += amount;
        System.out.println("💰 Money inserted: " + amount);

        Item item = machine.inventory.getItem(machine.selectedItem);
        if (machine.balance >= item.price) {
            machine.setState(machine.dispenseState);
            machine.dispenseItem();
        }
    }

    public void dispenseItem() {
        System.out.println("⚠️ Insert sufficient money");
    }

    public void refund() {
        System.out.println("↩️ Refunding: " + machine.balance);
        machine.balance = 0;
        machine.setState(machine.idleState);
    }
}

/*
========================
6️⃣ DISPENSE STATE
========================
Item is dispensed and change returned
*/
class DispenseState implements State {
    VendingMachine machine;

    DispenseState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String itemName) {}
    public void insertMoney(int amount) {}

    public void dispenseItem() {
        Item item = machine.inventory.getItem(machine.selectedItem);
        machine.inventory.reduceStock(machine.selectedItem);

        System.out.println("🥤 Dispensing: " + item.name);

        int change = machine.balance - item.price;
        if (change > 0) {
            System.out.println("💵 Returning change: " + change);
        }

        machine.balance = 0;
        machine.selectedItem = null;
        machine.setState(machine.idleState);
    }

    public void refund() {}
}

/*
========================
7️⃣ VENDING MACHINE (CONTEXT)
========================
Controls state transitions
*/
class VendingMachine {
    State idleState;
    State hasMoneyState;
    State dispenseState;

    State currentState;
    Inventory inventory;
    int balance = 0;
    String selectedItem;

    VendingMachine() {
        inventory = new Inventory();
        idleState = new IdleState(this);
        hasMoneyState = new HasMoneyState(this);
        dispenseState = new DispenseState(this);
        currentState = idleState;
    }

    void setState(State state) {
        currentState = state;
    }

    void selectItem(String itemName) {
        currentState.selectItem(itemName);
    }

    void insertMoney(int amount) {
        currentState.insertMoney(amount);
    }

    void dispenseItem() {
        currentState.dispenseItem();
    }

    void refund() {
        currentState.refund();
    }
}

/*
========================
8️⃣ MAIN CLASS
========================
Simulates real vending machine workflow
*/
public class Main {
    public static void main(String[] args) {

        VendingMachine machine = new VendingMachine();

        // Setup inventory
        machine.inventory.addItem(new Item("Coke", 50), 5);
        machine.inventory.addItem(new Item("Pepsi", 40), 3);

        /*
        ===== WORKFLOW =====
        1. User selects item
        2. User inserts money
        3. Machine dispenses item
        */

        machine.selectItem("Coke");
        machine.insertMoney(30);
        machine.insertMoney(20);
    }
}


/*
====================================================
📌 VENDING MACHINE LLD – REVISION NOTES
====================================================

🔹 PROBLEM STATEMENT
Design a Vending Machine system that allows a user to:
- Select an item
- Insert money
- Dispense the item
- Return change
- Handle invalid actions (out of stock, insufficient money)

----------------------------------------------------

🔹 HOW TO APPROACH THIS PROBLEM (LLD THINKING)
1. Read the problem and extract real-world nouns.
2. Convert important nouns into entities (classes).
3. Identify how the machine’s behavior changes over time.
4. Whenever behavior changes → introduce a State.
5. Avoid large if-else blocks by using a design pattern.

----------------------------------------------------

🔹 IDENTIFIED ENTITIES
- Item       → represents a product (name, price)
- Inventory  → manages items and stock
- VendingMachine → main controller (context)
- State      → defines behavior based on machine state

----------------------------------------------------

🔹 DESIGN PATTERN USED
✅ STATE DESIGN PATTERN

WHY STATE PATTERN?
- Vending machine behaves differently in different situations.
- Allowed actions depend on current state.
- Prevents large if-else or switch statements.
- Makes the system easy to extend and maintain.

----------------------------------------------------

🔹 STATES USED
1. IdleState
   - Waiting for item selection
   - No money inserted

2. HasMoneyState
   - Item selected
   - Accepts money

3. DispenseState
   - Dispenses item
   - Returns change
   - Moves back to Idle

Each state controls:
- What actions are allowed
- What actions are invalid

----------------------------------------------------

🔹 STATE TRANSITION FLOW

IdleState
   ↓ selectItem
HasMoneyState
   ↓ insertMoney (sufficient)
DispenseState
   ↓ dispense
IdleState

----------------------------------------------------

🔹 KEY DESIGN PRINCIPLES FOLLOWED
- Single Responsibility Principle
- Open for extension, closed for modification
- Loose coupling via interfaces
- Clear separation of concerns

----------------------------------------------------

🔹 WHY THIS DESIGN IS INTERVIEW-FRIENDLY
- Real-world mapping is clear
- Uses a well-known design pattern
- Easy to explain verbally
- Easy to extend (UPI, Card, Coin support)

----------------------------------------------------

🔹 POSSIBLE EXTENSIONS (FOLLOW-UPS)
- Add Coin / Note denominations
- Add UPI / Card payment
- Add OutOfStockState
- Add Maintenance/Admin state
- Make system thread-safe

====================================================
END OF REVISION NOTES
====================================================
*/
