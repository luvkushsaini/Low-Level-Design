import java.util.*;

/* ==============================================================
   ATM MACHINE DESIGN (LLD) — Interview Simplified Version
   ---------------------------------------------------------------
   ✅ Patterns used:
     1. State Pattern → ATM state transitions (Idle → HasCard → Authenticated)
     2. Strategy Pattern → Different transaction types (Withdraw, Balance)
     3. Factory Pattern → Creates transaction objects
     4. Chain of Responsibility → Dispensing cash through denominations
   ---------------------------------------------------------------
   Patterns NOT implemented here (optional in interviews):
     - Adapter (for real bank APIs / hardware abstraction)
     - Command (for logging, undo, or queued transactions)
   ============================================================== */

// ---------- STATE INTERFACE ----------
// 🧩 STATE PATTERN: common interface for all ATM states
interface ATMStates {
    void insertCard();
    void enterPin(int pin);
    void selectTransaction(String s);
    void ejectCard();
}

// ---------- ATM CONTEXT CLASS ----------
// 🧩 CONTEXT class in State Pattern — holds current state and delegates behavior
class ATM {
    private ATMStates currState;
    private Card card;
    private Bankserver bankServer;
    private CashHandler cashDispenser; // 🧩 Chain of Responsibility root

    ATM() {
        this.currState = new IdleState(this);
        this.bankServer = new Bankserver();

        // 🧩 CHAIN OF RESPONSIBILITY setup (Cash dispensers)
        CashHandler twoThousand = new TwoThousandDispenser(10);
        CashHandler fiveHundred = new FiveHundredDispenser(20);
        CashHandler hundred = new HundredDispenser(50);

        twoThousand.setNextDispenser(fiveHundred);
        fiveHundred.setNextDispenser(hundred);

        this.cashDispenser = twoThousand;
    }

    public void setNextState(ATMStates nextAtmState) {
        this.currState = nextAtmState;
    }

    public Bankserver getBankserver() {
        return this.bankServer;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public void insertCard(Card card) {
        setCard(card);
        currState.insertCard();
    }

    public void enterPin(int pin) {
        currState.enterPin(pin);
    }

    public void selectTransaction(String type) {
        currState.selectTransaction(type);
    }

    public void dispenseCash(long amount) {
        if (cashDispenser.getTotal() < amount) {
            System.out.println("ATM doesn't have enough cash.");
        } else {
            cashDispenser.dispense(amount);
        }
    }

    public long getTotalCash() {
        return cashDispenser.getTotal();
    }
}

// ---------- ATM STATES IMPLEMENTATION ----------
// 🧩 Concrete States of State Pattern

class IdleState implements ATMStates {
    private ATM atm;

    IdleState(ATM atm) {
        this.atm = atm;
    }

    public void insertCard() {
        System.out.println("Card Inserted Successfully.");
        atm.setNextState(new HasCardState(atm)); // state transition
    }

    public void enterPin(int pin) {
        System.out.println("Please insert your card first.");
    }

    public void selectTransaction(String s) {
        System.out.println("Please insert your card first.");
    }

    public void ejectCard() {
        System.out.println("No card to eject.");
    }
}

class HasCardState implements ATMStates {
    private ATM atm;

    HasCardState(ATM atm) {
        this.atm = atm;
    }

    public void insertCard() {
        System.out.println("Card already inserted.");
    }

    public void enterPin(int pin) {
        if (atm.getBankserver().verifyPin(atm.getCard().getCardNumber(), pin)) {
            System.out.println("PIN verified. Select transaction.");
            atm.setNextState(new AuthenticatedState(atm)); // transition
        } else {
            System.out.println("Incorrect PIN. Try again.");
        }
    }

    public void selectTransaction(String s) {
        System.out.println("Please enter your PIN first.");
    }

    public void ejectCard() {
        atm.setCard(null);
        atm.setNextState(new IdleState(atm));
        System.out.println("Card ejected successfully.");
    }
}

class AuthenticatedState implements ATMStates {
    private ATM atm;

    AuthenticatedState(ATM atm) {
        this.atm = atm;
    }

    public void insertCard() {
        System.out.println("Card already present.");
    }

    public void enterPin(int pin) {
        System.out.println("PIN already verified.");
    }

    public void selectTransaction(String s) {
        // 🧩 FACTORY PATTERN → Creates transaction object
        Transaction txn = TransactionFactory.create(s);
        txn.execute(atm.getCard(), atm.getBankserver(), atm); // 🧩 STRATEGY PATTERN → Executes specific logic
    }

    public void ejectCard() {
        atm.setCard(null);
        atm.setNextState(new IdleState(atm));
        System.out.println("Card ejected successfully.");
    }
}

// ---------- TRANSACTION FACTORY ----------
// 🧩 FACTORY PATTERN — creates transaction strategy objects
class TransactionFactory {
    public static Transaction create(String type) {
        if (type.equalsIgnoreCase("withdraw")) {
            return new WithdrawTransaction();
        } else if (type.equalsIgnoreCase("balance")) {
            return new BalanceTransaction();
        } else {
            throw new IllegalArgumentException("Invalid transaction type");
        }
    }
}

// ---------- STRATEGY INTERFACE ----------
// 🧩 STRATEGY PATTERN — defines transaction behavior
interface Transaction {
    void execute(Card card, Bankserver bankserver, ATM atm);
}

// ---------- WITHDRAW TRANSACTION ----------
// 🧩 Concrete Strategy
class WithdrawTransaction implements Transaction {
    public void execute(Card card, Bankserver bankserver, ATM atm) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter amount to withdraw: ");
        long amount = sc.nextLong();

        if (amount <= 0) {
            System.out.println("Invalid amount entered.");
            return;
        }

        long balance = bankserver.checkBalance(card.getCardNumber());

        if (balance < amount) {
            System.out.println("Insufficient account balance.");
            return;
        }

        if (atm.getTotalCash() < amount) {
            System.out.println("ATM doesn't have enough cash.");
            return;
        }

        atm.dispenseCash(amount); // 🧩 Chain of Responsibility for cash flow
        bankserver.debit(card.getCardNumber(), amount);
        System.out.println("Please collect your cash.");
        System.out.println("Remaining balance: " + bankserver.checkBalance(card.getCardNumber()));
    }
}

// ---------- BALANCE TRANSACTION ----------
// 🧩 Concrete Strategy
class BalanceTransaction implements Transaction {
    public void execute(Card card, Bankserver bank, ATM atm) {
        long bal = bank.checkBalance(card.getCardNumber());
        System.out.println("Your account balance is: " + bal);
    }
}

// ---------- CARD CLASS ----------
class Card {
    private String cardNumber;

    Card(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}

// ---------- BANK SERVER ----------
class Bankserver {
    private Map<String, Long> accounts = new HashMap<>();
    private Map<String, Integer> pins = new HashMap<>();

    Bankserver() {
        accounts.put("1111-2222-3333-4444", 50000L);
        pins.put("1111-2222-3333-4444", 1234);
    }

    boolean verifyPin(String card, int pin) {
        return pins.containsKey(card) && pins.get(card) == pin;
    }

    public void debit(String card, long amount) {
        long current = accounts.getOrDefault(card, 0L);
        accounts.put(card, current - amount);
    }

    public long checkBalance(String card) {
        return accounts.getOrDefault(card, 0L);
    }
}

// ---------- CASH DISPENSER CHAIN ----------
// 🧩 CHAIN OF RESPONSIBILITY PATTERN
abstract class CashHandler {
    protected int denomination;
    protected int count;
    protected CashHandler nextCashHandler;

    CashHandler(int denomination, int count) {
        this.denomination = denomination;
        this.count = count;
    }

    void setNextDispenser(CashHandler nextCashHandler) {
        this.nextCashHandler = nextCashHandler;
    }

    public long getTotal() {
        long total = denomination * count;
        if (nextCashHandler != null) {
            total += nextCashHandler.getTotal();
        }
        return total;
    }

    public void dispense(long amount) {
        if (amount >= denomination && count > 0) {
            long numNotes = Math.min(amount / denomination, count);
            long remaining = amount - (numNotes * denomination);
            count -= numNotes;
            System.out.println("Dispensing " + numNotes + " x " + denomination + " notes");
            if (remaining > 0 && nextCashHandler != null) {
                nextCashHandler.dispense(remaining);
            } else if (remaining > 0) {
                System.out.println("Cannot dispense remaining " + remaining);
            }
        } else if (nextCashHandler != null) {
            nextCashHandler.dispense(amount);
        } else {
            System.out.println("Cannot dispense " + amount);
        }
    }
}

// 🧩 Concrete Handlers
class TwoThousandDispenser extends CashHandler {
    TwoThousandDispenser(int count) {
        super(2000, count);
    }
}

class FiveHundredDispenser extends CashHandler {
    FiveHundredDispenser(int count) {
        super(500, count);
    }
}

class HundredDispenser extends CashHandler {
    HundredDispenser(int count) {
        super(100, count);
    }
}

// ---------- MAIN ----------
public class Main {
    public static void main(String[] args) {
        ATM atm = new ATM();
        Card card = new Card("1111-2222-3333-4444");

        atm.insertCard(card);       // State: Idle → HasCard
        atm.enterPin(1234);         // State: HasCard → Authenticated
        atm.selectTransaction("withdraw"); // Strategy + Factory + CoR
        atm.selectTransaction("balance");  // Strategy + Factory
    }
}

/*
================================================================================
ATM MACHINE — Long Description / Revision Notes (Complete)
================================================================================

1) QUESTION / REQUIREMENT (Interview-style)
-------------------------------------------
Design an ATM machine LLD. The system should allow:
 - Insert card
 - Enter PIN (authenticate)
 - Select and execute transactions (Withdraw, BalanceCheck, etc.)
 - Dispense cash in available denominations
 - Update bank-side balances and keep local transactional behavior reasonable

Non-functional / assumptions:
 - Simple single-ATM process (no distributed bank server complexity)
 - Focus on OOP and patterns, not production security (HSM/TLS, PCI compliance)
 - System must be extensible and clean (easy to add transactions, denominations)
 - Durability & reconciliation discussed qualitatively (local logging / idempotency)

2) HIGH-LEVEL APPROACH & INTUITION
---------------------------------
Split responsibilities into orthogonal subsystems:

  - **State Machine** for ATM UI flow (Idle, HasCard, Authenticated). This models
    allowed operations per state and avoids sprawling if/else logic.
  - **Transaction Layer** to encapsulate business operations (Withdraw, Balance).
    Each Transaction behaves differently but shares the same interface (Strategy).
  - **Factory** to create transaction instances from user selection (keeps UI code simple).
  - **Cash Dispensing Subsystem** implemented as a Chain of Responsibility where
    each handler represents a denomination cassette that dispenses as much as it can
    then forwards leftover to the next handler.
  - **BankServer** to simulate backend verification and debiting.
  - **ATM** (context) coordinates states, holds references to bank server and dispenser.

Design guideline: each module should own its own data and responsibilities (Single Responsibility Principle),
and the system should be open for extension (add new transactions or denominations without editing existing code).

3) DESIGN PATTERNS USED — full explanation
------------------------------------------

A. State Pattern
 - Where: `ATM` has `currentState` which is an `ATMState` implementation.
 - Concrete states: `IdleState`, `HasCardState`, `AuthenticatedState`.
 - Why: ATM behaviour changes depending on whether a card is inserted or the user is authenticated.
 - Benefit: Replaces complex conditionals with polymorphism; each state enforces which actions are valid.
 - Other uses: vending machines, UI modal states, workflow engines.

B. Strategy Pattern
 - Where: `Transaction` interface and concrete strategies `WithdrawTransaction`, `BalanceTransaction`.
 - Why: Transactions share a common interface but different behaviors. Using Strategy lets us pass a transaction around
   and execute without knowing the concrete type.
 - Benefit: Easier to test, extend (add TransferTransaction, DepositTransaction).
 - Other uses: Sorting algorithms, compression strategies, payment methods.

C. Factory Pattern
 - Where: `TransactionFactory.create(type)` returns a `Transaction` strategy.
 - Why: Centralizes transaction creation; removes switch/if/else from core logic and decouples the UI/state from concrete classes.
 - Complementary to Strategy: Factory creates Strategy instances.
 - Other uses: GUI widget creation, parsers for different input types.

D. Chain of Responsibility (CoR)
 - Where: `CashHandler` chain: e.g., `TwoThousandDispenser -> FiveHundredDispenser -> HundredDispenser`.
 - Why: Each cassette handles its denomination, dispenses what it can, passes remainder on. CoR models responsibility passing.
 - Benefit: Extensible — add a new denomination handler without changing existing handler logic. Matches hardware cassettes naturally.
 - Other uses: Logging pipelines, HTTP middleware, request validation filters.

E. Singleton (conceptual)
 - Where: In larger designs you might have single-instance components (ATMKernel, CashVault).
 - Why: Ensure only one instance per ATM process for shared resources. In our simple file-level design we instantiate ATM once in main.
 - Caution: Be careful using Singletons in tests; prefer instance injection for testability.


4) COMPONENT MAPPING (class responsibilities)
---------------------------------------------
 - ATM (context): currentState, card, bankServer reference, cashDispenser chain. Delegates actions to state.
 - ATMStates (interface): insertCard(), enterPin(pin), selectTransaction(type), ejectCard()
 - IdleState / HasCardState / AuthenticatedState: behavior per stage
 - Card: domain object containing card number (PAN masked in real life)
 - Bankserver: simulates backend accounts & PIN verification; performs debit / balance queries
 - Transaction (interface - Strategy): execute(card, bank, atm)
 - WithdrawTransaction / BalanceTransaction: concrete strategies
 - TransactionFactory: produces Transaction objects based on client input
 - CashHandler (CoR base): denomination, noteCount, nextHandler, getTotal(), dispense(amount)
 - Concrete Cash Handlers: TwoThousandDispenser, FiveHundredDispenser, HundredDispenser

5) DETAILED FLOW OF EXECUTION (sequence)
----------------------------------------
User runs: `atm.insertCard(card)` → `atm.enterPin(pin)` → `atm.selectTransaction("withdraw")`:

Sequence (withdraw):
 1. User inserts card: ATM (IdleState) -> set session/card -> transition to HasCardState.
 2. User enters PIN: HasCardState validates via Bankserver.verifyPin(); on success -> AuthenticatedState.
 3. User selects "withdraw": AuthenticatedState calls `TransactionFactory.create("withdraw")`.
 4. WithdrawTransaction.execute():
    a. Prompt user for amount.
    b. Validate amount > 0.
    c. Ask Bankserver.checkBalance(card) if user has funds.
    d. Ask ATM.getTotalCash() (cashDispenser.getTotal()) whether ATM has enough total cash.
    e. If both checks pass:
       - Call atm.dispenseCash(amount) -> cashDispenser.dispense(amount) (CoR logic).
       - Call bankserver.debit(card, amount) to update account.
    f. Print confirmation, remaining balance.
 5. User ejects card -> state resets to IdleState.

Edge cases & error handling:
 - Insufficient account balance -> stop and inform user.
 - ATM total insufficient -> stop and inform user.
 - CoR cannot form the exact amount from available denominations -> fail and (optionally) roll back account update (in robust systems use a two-phase commit or pre-reserve funds then finalize on dispense success).
 - PIN attempts limit (not implemented) -> card retention on repeated failures.

6) WHY WE MADE THESE CHOICES (INTUITION)
----------------------------------------
 - State pattern keeps UI logic clean and focuses each state on allowable actions.
 - Strategy + Factory separates creation & behavior of transactions; easy to extend and test.
 - Chain of Responsibility maps naturally to hardware cassettes; each cassette maintains its own state (note count), which is realistic.
 - Keep Bankserver responsibilities separate from ATM hardware concerns.
 - Keep ATM as orchestrator (not business logic owner).

7) WHERE THESE PATTERNS ARE USEFUL IN FUTURE SYSTEMS
----------------------------------------------------
 - State Pattern: any object with well-defined lifecycle / modes — game entities, network connections, order states.
 - Strategy Pattern: when you have interchangeable algorithms / behaviors – e.g., payment processors, tax calculators.
 - Factory Pattern: when object creation requires encapsulation — e.g., plugin systems, message parsers.
 - Chain of Responsibility: pipelines, validation chains, interceptors, middleware.

8) TRADE-OFFS & IMPLEMENTATION NOTES
------------------------------------
 - Simplicity vs. realism: Real ATMs use HSMs for PIN, secure hardware modules, and robust reconciliation for failures. Our example focuses on architecture and testability.
 - Concurrency: For single-ATM demo, synchronization is minimal. In real systems, cash cassettes & bank operations require concurrency control and transactions.
 - Idempotency & failure modes: Real systems pre-authorize or reserve funds before dispensing; use unique transaction IDs and persistent logs to reconcile partial failures.
 - Testing: Because logic is modular, you can mock `Bankserver` and `CashHandler` chain to test `WithdrawTransaction` independently.

9) SAMPLE OUTPUT
-----------------
Example withdraw flow console output for withdrawing 2,600:
  Card Inserted Successfully.
  PIN verified. Select transaction.
  Enter amount to withdraw: 2600
  Dispensing 1 x 2000 notes
  Dispensing 1 x 500 notes
  Dispensing 1 x 100 notes
  Please collect your cash.
  Remaining balance: 47400
  Your account balance is: 47400

10) EXTENSIONS (good talking points)
-------------------------------------
 - Add `DepositTransaction` that uses cash acceptor hardware and updates cassettes and bank balance.
 - Add `TransactionLog` local persistent log for intents & completions (for reconciliation).
 - Add multi-currency support using denomination sets per currency (CoR extended).
 - Add PIN attempt limiter and card retention policy.
 - Replace `Bankserver` with a network adapter and HSM for secure PIN verification.
 - Add an administrative interface to refill cassettes and view inventory (ATM.getTotalCash()).

11) TEST CASES (must mention in interview)
------------------------------------------
 - Happy path withdraw: sufficient account & ATM cash.
 - Insufficient account balance.
 - ATM does not have enough total cash.
 - ATM has total cash but cannot make exact amount using denominations (e.g., need ₹150 but only ₹200 notes).
 - Wrong PIN retries leading to block.
 - Concurrent withdraw attempts from two threads (if simulating concurrent users).

12) QUICK REMINDERS FOR INTERVIEWS
----------------------------------
 - Start by clarifying requirements and assumptions (offline mode? ATM hardware modeled? security?).
 - Draw 4–6 boxes on the whiteboard: Hardware adapters / ATM Kernel (State Machine) / Transaction Factory / Cash Vault / Bank Service.
 - Explain patterns succinctly: "State for flow, Strategy + Factory for transactions, CoR for cash dispensing, Adapter for hardware".
 - Mention trade-offs (simplicity vs. robustness) and what you'd change for production (HSM, TLS, persistent intent logs, idempotent txn IDs).

--------------------------------------------------------------------------------
End of long description.
Paste this block at the top of your ATM class file for revision; you can also keep a short 2-line summary below it for quick recall.
--------------------------------------------------------------------------------
*/
