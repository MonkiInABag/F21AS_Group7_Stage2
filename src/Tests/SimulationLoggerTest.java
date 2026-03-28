package Tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import simulation.SimulationLogger;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationLoggerTest {

    @Test
    @DisplayName("SimulationLogger writes logs to file")
    void loggerWritesToFile() throws Exception {

        SimulationLogger logger = SimulationLogger.getInstance();

        String testMessage = "Test event";
        String filePath = "test_log.txt";

        logger.log(testMessage);
        logger.writeToFile(filePath);

        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        assertTrue(content.contains(testMessage));
    }
}