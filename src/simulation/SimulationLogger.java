package simulation;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

//singleton logger that records all simulation events with timestamps.
public class SimulationLogger {

    private static SimulationLogger instance; //one single instance
    private final List<String> logEntries;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //
    private SimulationLogger() {
        logEntries = new ArrayList<>();
    }

    //returns the single instance of the logger, creates it if it doesn't exist
    public static synchronized SimulationLogger getInstance() {
        if (instance == null) {
            instance = new SimulationLogger();
        }
        return instance;
    }

    //logs an event with a timestamp and adds it to the log entries list
    public synchronized void log(String event) {
        String entry = LocalDateTime.now().format(FORMATTER);
        logEntries.add("[" + entry + "] " + event);
        System.out.println(entry);//show in console
    }
    
    //writes all the log entries to a selected file
    public synchronized void writeToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("=== Coffee Shop Simulation Log ===");
            for (String entry : logEntries) {
                pw.println(entry);
            }
            pw.println("=== End of Log ===");
            System.out.println("Log written to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write log to " + filename + ": " + e.getMessage());
        }
    }

    public synchronized List<String> getLogEntries() {
        return new ArrayList<>(logEntries);
    }

}
