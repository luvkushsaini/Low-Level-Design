import java.util.ArrayList;

enum LogLevel {
    INFO, DEBUG, WARNING, ERROR;
}

interface Appender {
    void append(LogLevel level, String message);
}

class FileAppender implements Appender {
    private String filename;

    FileAppender(String filename) {
        this.filename = filename;
    }

    public void append(LogLevel level, String message) {
        System.out.println("Writing to file (" + filename + "): [" + level + "] " + message);
    }
}

class ConsoleAppender implements Appender {
    public void append(LogLevel level, String message) {
        System.out.println("[" + level + "] " + message);
    }
}

class Logger {
    private static Logger logger;
    private ArrayList<Appender> appenders = new ArrayList<>();

    private Logger() {}

    public static Logger getInstance() {
        if (logger == null) {
            synchronized (Logger.class) {
                if (logger == null) logger = new Logger();
            }
        }
        return logger;
    }

    public void addAppender(Appender appender) {
        appenders.add(appender);
    }

    public void log(LogLevel level, String message) {
        for (Appender appender : appenders) {
            appender.append(level, message);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.addAppender(new ConsoleAppender());
        logger.addAppender(new FileAppender("app.log"));
        logger.log(LogLevel.INFO, "Application started");
        logger.log(LogLevel.ERROR, "Error occurred in transaction module");
    }
}
/*
--------------------------------------------
🧠 PROJECT: Logging System (LLD Practice)
--------------------------------------------

📌 Problem Statement:
We needed to design a flexible and scalable **Logging System** where log messages 
can be written to multiple destinations (like Console or File) and at different log levels 
(INFO, DEBUG, WARNING, ERROR).

The goal was to create a system that:
- Supports multiple logging outputs (appenders)
- Is easily extendable for new destinations (e.g., DatabaseAppender, NetworkAppender)
- Uses a single shared instance (global logger)
- Keeps code loosely coupled and maintainable

--------------------------------------------
🧩 Design Pattern Used: **Singleton + Strategy (via Composition)**
--------------------------------------------

1️⃣ **Singleton Pattern**
   - Ensures there is only **one Logger instance** throughout the application.
   - Centralized logging is important — we don’t want multiple loggers writing inconsistently.
   - Implemented using a private constructor and a static `getInstance()` method.

   
   -->when we have multiple options to select from during run time dynamically we always use strategy design pattern 


2️⃣ **Strategy Pattern (Behavioral)**
   - Different logging destinations (Console, File, etc.) are treated as interchangeable “strategies.”
   - Each destination implements a common interface `Appender` with the method `append()`.
   - The `Logger` doesn’t care *how* the message is written — it just calls `appender.append()`.

--------------------------------------------
⚙️ Implementation Flow:
--------------------------------------------
1. Define `LogLevel` enum → represents log severity.
2. Create `Appender` interface → defines `append(LogLevel, String)` method.
3. Implement concrete appenders:
     - `ConsoleAppender` → prints to console
     - `FileAppender` → simulates writing to file
4. Create `Logger` (Singleton):
     - Holds a list of appenders.
     - Exposes `addAppender()` and `log()` methods.
     - On `log()`, it iterates through all appenders and delegates the message.
5. In `Main`, configure logger once and use it globally.

--------------------------------------------
🚀 Why this design is good:
--------------------------------------------
✅ Extensible → Add new appenders easily (no change to existing code).
✅ Reusable → Logger can be reused across modules.
✅ Thread-safe → Uses double-checked locking for Singleton.
✅ Decoupled → Logger doesn't depend on how or where logs are written.

--------------------------------------------
🧾 Example Output:
--------------------------------------------
[INFO] Application started
Writing to file (app.log): [INFO] Application started
[ERROR] Error occurred in transaction module
Writing to file (app.log): [ERROR] Error occurred in transaction module

--------------------------------------------
📘 Summary:
We designed a scalable, extensible logging framework using
- Singleton pattern for a single global instance
- Strategy pattern for flexible log destination behaviors.
This structure mirrors how real-world logging frameworks like Log4j or SLF4J work internally.
--------------------------------------------
*/
