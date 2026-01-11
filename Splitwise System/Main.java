package Questions.Splitwise;

/*
============================================================
BIG PICTURE – HOW THE SPLITWISE SYSTEM FLOWS
============================================================
1. Users are created and each user owns a BalanceSheet.
2. Groups are optional containers to organize users.
3. An Expense is created using ExpenseBuilder.
4. ExpenseBuilder takes:
   - who paid
   - total amount
   - participants
   - split strategy
5. SplitStrategy breaks the expense into per-user Split objects.
6. SplitwiseServices iterates over Splits and updates BalanceSheets.
7. BalanceSheet stores pairwise balances (who owes whom).
8. settleUp() reduces balances when real money is paid.
============================================================
*/

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// ========================= USER =========================
class User {
    private final String id;
    private final String name;
    private final String email;
    private final BalanceSheet balanceSheet;

    User(String name, String email) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.balanceSheet = new BalanceSheet(this);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public BalanceSheet getBalanceSheet() { return balanceSheet; }
}

// ========================= BALANCE SHEET =========================
class BalanceSheet {
    private final User owner;
    private final Map<User, Double> balances = new ConcurrentHashMap<>();

    BalanceSheet(User owner) {
        this.owner = owner;
    }

    /*
    Positive value  -> other user owes owner
    Negative value  -> owner owes other user
    */
    public void adjustBalance(User otherUser, double amount) {
        if (otherUser.equals(owner)) return;
        balances.merge(otherUser, amount, Double::sum);
    }

    public void showBalances() {
        System.out.println("--- Balance Sheet for " + owner.getName() + " ---");
        if (balances.isEmpty()) {
            System.out.println("All settled up!");
            return;
        }
        for (Map.Entry<User, Double> entry : balances.entrySet()) {
            double amount = entry.getValue();
            if (amount > 0) {
                System.out.println(entry.getKey().getName() + " owes " + owner.getName() + " $" + amount);
            } else if (amount < 0) {
                System.out.println(owner.getName() + " owes " + entry.getKey().getName() + " $" + (-amount));
            }
        }
        System.out.println("--------------------------------");
    }
}

// ========================= GROUP =========================
class Group {
    private final String id;
    private final String name;
    private final List<User> members;

    Group(String name, List<User> members) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.members = new ArrayList<>(members);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<User> getMembers() { return new ArrayList<>(members); }
}

// ========================= SPLIT =========================
class Split {
    private final User user;
    private final double amount;

    Split(User user, double amount) {
        this.user = user;
        this.amount = amount;
    }

    public User getUser() { return user; }
    public double getAmount() { return amount; }
}

// ========================= EXPENSE =========================
class Expense {
    private final String id;
    private final String description;
    private final double amount;
    private final User paidBy;
    private final List<Split> splits;
    private final LocalDateTime timestamp;

    private Expense(ExpenseBuilder builder) {
        this.id = UUID.randomUUID().toString();
        this.description = builder.description;
        this.amount = builder.amount;
        this.paidBy = builder.paidBy;
        this.timestamp = LocalDateTime.now();
        this.splits = builder.strategy.calculateSplits(
                builder.amount,
                builder.paidBy,
                builder.participants,
                builder.splitValues
        );
    }

    public User getPaidBy() { return paidBy; }
    public List<Split> getSplits() { return splits; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    // ========================= EXPENSE BUILDER =========================
    static class ExpenseBuilder {
        private SplitStrategy strategy;
        private String description;
        private double amount;
        private User paidBy;
        private List<User> participants;
        private List<Double> splitValues;

        public ExpenseBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public ExpenseBuilder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public ExpenseBuilder setPaidBy(User paidBy) {
            this.paidBy = paidBy;
            return this;
        }

        public ExpenseBuilder setParticipants(List<User> participants) {
            this.participants = participants;
            return this;
        }

        public ExpenseBuilder setSplitValues(List<Double> splitValues) {
            this.splitValues = splitValues;
            return this;
        }

        public ExpenseBuilder setSplitStrategy(SplitStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Expense build() {
            return new Expense(this);
        }
    }
}

// ========================= SPLIT STRATEGY =========================
interface SplitStrategy {
    List<Split> calculateSplits(double totalAmount,
                                User paidBy,
                                List<User> participants,
                                List<Double> splitValues);
}

class EqualSplitStrategy implements SplitStrategy {
    public List<Split> calculateSplits(double totalAmount, User paidBy,
                                       List<User> participants, List<Double> splitValues) {
        List<Split> result = new ArrayList<>();
        double each = totalAmount / participants.size();
        for (User user : participants) {
            result.add(new Split(user, each));
        }
        return result;
    }
}

class ExactSplitStrategy implements SplitStrategy {
    public List<Split> calculateSplits(double totalAmount, User paidBy,
                                       List<User> participants, List<Double> splitValues) {
        List<Split> result = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            result.add(new Split(participants.get(i), splitValues.get(i)));
        }
        return result;
    }
}

class PercentageSplitStrategy implements SplitStrategy {
    public List<Split> calculateSplits(double totalAmount, User paidBy,
                                       List<User> participants, List<Double> splitValues) {
        List<Split> result = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            double amount = (splitValues.get(i) * totalAmount) / 100.0;
            result.add(new Split(participants.get(i), amount));
        }
        return result;
    }
}

// ========================= SERVICE LAYER =========================
class SplitwiseServices {
    private static SplitwiseServices instance;
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Group> groups = new ConcurrentHashMap<>();

    private SplitwiseServices() {}

    public static synchronized SplitwiseServices getInstance() {
        if (instance == null) {
            instance = new SplitwiseServices();
        }
        return instance;
    }

    public User addUser(String name, String email) {
        User user = new User(name, email);
        users.put(user.getId(), user);
        return user;
    }

    public Group addGroup(String name, List<User> members) {
        Group group = new Group(name, members);
        groups.put(group.getId(), group);
        return group;
    }

    /*
    CORE LOGIC:
    - Build expense
    - Iterate splits
    - Update pairwise balances
    */
    public synchronized void createExpense(Expense.ExpenseBuilder builder) {
        Expense expense = builder.build();
        User paidBy = expense.getPaidBy();

        for (Split split : expense.getSplits()) {
            if (split.getUser().equals(paidBy)) continue;
            paidBy.getBalanceSheet().adjustBalance(split.getUser(), split.getAmount());
            split.getUser().getBalanceSheet().adjustBalance(paidBy, -split.getAmount());
        }
    }

    public void showBalances(String userId) {
        users.get(userId).getBalanceSheet().showBalances();
    }

    public void settleUp(String payerId, String payeeId, double amount) {
        User payer = users.get(payerId);
        User payee = users.get(payeeId);

        payer.getBalanceSheet().adjustBalance(payee, amount);
        payee.getBalanceSheet().adjustBalance(payer, -amount);
    }
}

// ========================= DRIVER =========================
public class Main {
    public static void main(String[] args) {
        SplitwiseServices services = SplitwiseServices.getInstance();

        User luv = services.addUser("luv", "luv@gmail.com");
        User bhavy = services.addUser("bhavy", "bhavy@gmail.com");
        User sandy = services.addUser("sandy", "sandy@gmail.com");
        User taufique = services.addUser("taufique", "taufique@gmail.com");

        services.createExpense(
                new Expense.ExpenseBuilder()
                        .setDescription("Thailand Trip")
                        .setAmount(600)
                        .setPaidBy(luv)
                        .setParticipants(Arrays.asList(luv, bhavy, sandy, taufique))
                        .setSplitStrategy(new EqualSplitStrategy())
        );

        services.showBalances(luv.getId());
        services.showBalances(bhavy.getId());
        services.showBalances(sandy.getId());
        services.showBalances(taufique.getId());
    }
}
/*
=====================================================================
SPLITWISE – INTERVIEW REVISION NOTES (FOR FUTURE ME)
=====================================================================

----------------------------
1. INTERVIEW QUESTION ASKED
----------------------------
"Design a Splitwise-like system where:
- Users can create expenses
- One user pays, others share the cost
- System supports different split types (equal, exact, percentage)
- Users should know who owes whom and how much
- Balances should update correctly after every expense"

Follow-up questions usually include:
- How do you model balances?
- How do you avoid double counting?
- How do you extend for new split types?
- Where would concurrency issues arise?

------------------------------------------------
2. HOW I BROKE DOWN THE PROBLEM (THINKING PROCESS)
------------------------------------------------
I broke the problem into 5 core responsibilities:

1) Who are the actors?
   -> Users

2) What is the core action?
   -> Adding an Expense

3) What varies?
   -> How an expense is split (equal / exact / percentage)

4) What must be persisted logically?
   -> Pairwise balances between users

5) What should be extensible?
   -> Split logic (new strategies later)

This naturally led to:
- Entities for data
- Strategy Pattern for split logic
- Service layer for orchestration

---------------------------------
3. ENTITIES CHOSEN AND WHY
---------------------------------

User
- Represents a real person
- Owns exactly ONE BalanceSheet
- Keeps User simple and focused

BalanceSheet
- Stores pairwise balances (User -> Amount)
- Decouples balance logic from User
- Makes showing balances and settling easier

Group
- Optional container for users
- Helps organize expenses (not tightly coupled)
- Real Splitwise feature

Expense
- Represents ONE transaction
- Immutable once created
- Built using ExpenseBuilder to avoid constructor explosion

Split
- Atomic unit: (User, Amount)
- Output of split strategy
- Makes expense processing clean

SplitwiseServices
- Acts as a Facade / Service layer
- Central place for:
  - user creation
  - group creation
  - expense creation
  - settle up

------------------------------------------------
4. WHY BALANCE SHEET IS PAIRWISE (IMPORTANT)
------------------------------------------------
Instead of:
- storing total owed
I store:
- "User A vs User B" balances

Rule:
Positive value  -> other user owes owner
Negative value  -> owner owes other user

This ensures:
- No ambiguity
- Easy settlement
- Easy printing
- No recomputation required

------------------------------------------------
5. CORE FLOW OF THE CODE (STEP BY STEP)
------------------------------------------------
1) User calls createExpense(...)
2) ExpenseBuilder builds Expense
3) SplitStrategy calculates List<Split>
4) For each Split:
     - If user != paidBy:
         paidBy.balance += amount
         user.balance   -= amount
5) BalanceSheets are updated symmetrically
6) showBalances() simply reads stored values

IMPORTANT:
No balance calculation happens during showBalances()
Everything is precomputed at expense time

----------------------------------------
6. DESIGN PATTERNS USED AND WHY
----------------------------------------

1) Builder Pattern (ExpenseBuilder)
WHY:
- Expense has many optional fields
- Clean readable object creation
- Avoids telescoping constructors

2) Strategy Pattern (SplitStrategy)
WHY:
- Split logic varies
- Open/Closed Principle
- Easy to add:
    - UnequalSplit
    - ShareBasedSplit
    - CustomSplit

3) Singleton (SplitwiseServices)
WHY:
- One central coordinator
- Mimics real backend service
- Prevents inconsistent state

4) Facade (SplitwiseServices)
WHY:
- Hides internal complexity
- Interviewer sees clean APIs:
    addUser()
    createExpense()
    settleUp()

-----------------------------------------
7. CONCURRENCY CONSIDERATIONS
-----------------------------------------
- ConcurrentHashMap used for:
    users
    groups
    balances

- createExpense() is synchronized
WHY:
- Multiple expenses updating same users
- Prevent race conditions in balance updates

In real system:
- Would use DB transactions
- Or optimistic locking

-----------------------------------------
8. EDGE CASES I HANDLED / CAN DISCUSS
-----------------------------------------
- User paying for own expense (ignored in split loop)
- Empty balances -> "All settled up!"
- Extending split types without modifying service
- Group not mandatory for expense

-----------------------------------------
9. EXTENSIONS I CAN TALK ABOUT IN INTERVIEW
-----------------------------------------
- Add Expense validation (sum checks)
- Add settlement optimization (graph simplification)
- Add persistence layer (DB)
- Add REST APIs
- Add notifications
- Add currency support

-----------------------------------------
10. CHALLENGES I FACED (VERY IMPORTANT)
-----------------------------------------
1) Deciding where balance logic should live
   -> Solved by BalanceSheet abstraction

2) Avoiding double updates
   -> Solved by symmetric updates in one place

3) Making split logic extensible
   -> Solved via Strategy Pattern

4) Keeping interview code simple but realistic
   -> Avoided over-engineering

-----------------------------------------
11. ONE-LINE SUMMARY (FOR QUICK RECALL)
-----------------------------------------
"I modeled Splitwise by separating concerns:
entities for data, strategies for split logic,
and a service layer for orchestration,
using Builder + Strategy + Singleton patterns
to keep the design clean, extensible, and interview-ready."

=====================================================================
END OF REVISION NOTES
=====================================================================
*/
