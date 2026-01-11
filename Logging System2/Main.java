enum LogLevel {
    INFO, DEBUG, WARNING, ERROR;
}

// Abstract base handler
abstract class Logger {
    protected LogLevel level;
    protected Logger nextLogger;

    public Logger(LogLevel level) {
        this.level = level;
    }

    // set next handler in chain
    public void setNextLogger(Logger nextLogger) {
        this.nextLogger = nextLogger;
    }

    // template method
    public void logMessage(LogLevel level, String message) {
        if (this.level.ordinal() <= level.ordinal()) {
            write(message);
        }
        if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }

    protected abstract void write(String message);
}

// Concrete loggers
class ConsoleLogger extends Logger {
    public ConsoleLogger(LogLevel level) {
        super(level);
    }

    @Override
    protected void write(String message) {
        System.out.println("[Console] " + message);
    }
}

class FileLogger extends Logger {
    public FileLogger(LogLevel level) {
        super(level);
    }

    @Override
    protected void write(String message) {
        System.out.println("[File] Writing to file: " + message);
    }
}

class ErrorLogger extends Logger {
    public ErrorLogger(LogLevel level) {
        super(level);
    }

    @Override
    protected void write(String message) {
        System.out.println("[Error] Sending to error monitoring system: " + message);
    }
}

public class Main {
    private static Logger getChainOfLoggers() {
        Logger consoleLogger = new ConsoleLogger(LogLevel.INFO);
        Logger fileLogger = new FileLogger(LogLevel.WARNING);
        Logger errorLogger = new ErrorLogger(LogLevel.ERROR);

        consoleLogger.setNextLogger(fileLogger);
        fileLogger.setNextLogger(errorLogger);

        return consoleLogger;
    }

    public static void main(String[] args) {
        Logger loggerChain = getChainOfLoggers();

        loggerChain.logMessage(LogLevel.INFO, "Application started successfully.");
        loggerChain.logMessage(LogLevel.WARNING, "Low memory warning.");
        loggerChain.logMessage(LogLevel.ERROR, "NullPointerException encountered!");
    }
}
/*
============================================================
🔥 PROJECT: Logging System 2 — Using Chain of Responsibility
============================================================

🧩 CONTEXT / BACKGROUND:
------------------------------------------------------------
In our previous version, **Logging System 1**, 
we used the **Singleton Design Pattern** and a **Strategy-like approach**.

That system worked like this:
   - There was a single `Logger` instance shared across the system (Singleton pattern).
   - Multiple `Appender` implementations (like ConsoleAppender, FileAppender).
   - Every log message was sent to **all appenders**.
   - Example: log -> goes to both console and file regardless of log level.

✅ Pros:
   - Centralized logging system.
   - Easy to add new appenders.

❌ Cons:
   - No control over which appender should handle which level.
   - For example, even `INFO` logs were being written to file unnecessarily.
   - No filtering logic — all logs went everywhere.

------------------------------------------------------------
🧠 PROBLEM WE WANT TO SOLVE NOW:
------------------------------------------------------------
We want more **granular control** —
   - INFO logs → only to Console
   - WARNING logs → to Console + File
   - ERROR logs → to all (Console + File + Error system)

For this, we’ll now use the **Chain of Responsibility Design Pattern**.

------------------------------------------------------------
🧩 DESIGN PATTERN USED: Chain of Responsibility (Behavioral)
------------------------------------------------------------
In this pattern:
   - Each object in the chain decides whether to handle a request.
   - If it can’t handle it, it passes it to the next handler.

🎯 Real-world Analogy:
   Think of a Customer Support System:
   - Level 1 support handles simple queries.
   - Level 2 handles technical issues.
   - Level 3 handles critical escalations.
   If Level 1 can’t handle it → passes to Level 2 → Level 3.

Here, we’ll make:
   - ConsoleLogger (handles INFO and above)
   - FileLogger (handles WARNING and above)
   - ErrorLogger (handles ERROR only)

------------------------------------------------------------
⚙️ HOW IT WORKS:
------------------------------------------------------------
1️⃣ Each logger has a “log level” (like INFO, WARNING, ERROR).
2️⃣ When a message comes in:
    - The chain starts from the first logger.
    - Each logger checks if its level <= message level.
    - If yes → it handles (prints, writes to file, etc.)
    - Then passes the message to the next logger.
3️⃣ This way, higher-severity messages can be handled by multiple loggers.

------------------------------------------------------------
🧱 CLASS STRUCTURE OVERVIEW:
------------------------------------------------------------
1. `enum LogLevel` → defines levels (INFO, DEBUG, WARNING, ERROR)
2. `abstract class Logger` → base handler (defines `logMessage()` and chain setup)
3. `ConsoleLogger`, `FileLogger`, `ErrorLogger` → concrete handlers
4. `Main` → sets up the chain and sends logs

------------------------------------------------------------
🧩 THINKING PROCESS BEHIND CHOOSING PATTERNS:
------------------------------------------------------------
📘 Logging System 1:
   - We needed **only one** global logger object → ✅ Singleton.
   - We had **multiple output strategies (console, file)** → ✅ Strategy/Composition approach.
   - All outputs were equal and executed together.

📘 Logging System 2:
   - We wanted **conditional logging behavior** → e.g., ERROR logs go further up.
   - We needed **a chain of responsibility flow** where each handler can decide what to do.
   - ✅ Perfect use case for the Chain of Responsibility Pattern.

============================================================
*/