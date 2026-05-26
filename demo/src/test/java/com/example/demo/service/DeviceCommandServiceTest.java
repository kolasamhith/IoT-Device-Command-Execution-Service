package com.example.demo.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.model.CommandLog;
import com.example.demo.model.CommandStatus;
import com.example.demo.model.CommandType;
import com.example.demo.model.DeviceCommand;

class DeviceCommandServiceTest {

    // Subclass to control device simulation in tests
    private static class TestableCommandService extends DeviceCommandService {
        private boolean deviceOnline = true;

        void setDeviceOnline(boolean online) { this.deviceOnline = online; }

        @Override
        protected boolean simulateDeviceOnline(Long id) { return deviceOnline; }
    }

    private TestableCommandService service;

    @BeforeEach
    void setUp() {
        service = new TestableCommandService();
        service.setMaxRetries(2); 
    }

    // ─── Queuing Tests ────────────────────────────────────────────────────────

    @Test
    void queueCommand_validInput_returnsCommandWithPendingStatus() {
        DeviceCommand cmd = service.queueCommand(
                "Turn On Light", CommandType.TURN_ON, "DEVICE-001", "LOBBY", null);

        assertNotNull(cmd);
        assertEquals(CommandStatus.PENDING, cmd.getStatus());
        assertEquals("DEVICE-001", cmd.getDeviceId());
        assertEquals(CommandType.TURN_ON, cmd.getCommandType());
    }

    @Test
    void queueCommand_emptyCommandName_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.queueCommand("", CommandType.TURN_ON, "DEVICE-001", "LOBBY", null)
        );
        assertEquals("Command name cannot be empty", ex.getMessage());
    }

    @Test
    void queueCommand_emptyDeviceId_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.queueCommand("Turn On", CommandType.TURN_ON, "", "LOBBY", null)
        );
    }

    // ─── Retrieval Tests ──────────────────────────────────────────────────────

    @Test
    void getCommand_nonExistentId_throwsTaskNotFoundException() {
        assertThrows(
                TaskNotFoundException.class,
                () -> service.getCommand(999L)
        );
    }

    @Test
    void getAllCommands_afterQueuing_returnsAllCommands() {
        service.queueCommand("Cmd1", CommandType.TURN_ON, "DEV-1", "ZONE_A", null);
        service.queueCommand("Cmd2", CommandType.TURN_OFF, "DEV-2", "ZONE_B", null);

        assertEquals(2, service.getAllCommands().size());
    }

    // ─── Execution Tests ──────────────────────────────────────────────────────

    @Test
    void executeCommand_deviceOnline_statusBecomeDone() {
        service.setDeviceOnline(true);
        DeviceCommand cmd = service.queueCommand(
                "Turn On", CommandType.TURN_ON, "DEV-1", "LOBBY", null);

        service.executeCommand(cmd.getId());

        assertEquals(CommandStatus.DONE, service.getCommand(cmd.getId()).getStatus());
    }

    @Test
    void executeCommand_deviceOffline_firstAttempt_statusRemainsRetrying() {
        service.setDeviceOnline(false);
        DeviceCommand cmd = service.queueCommand(
                "Reboot", CommandType.REBOOT, "DEV-2", "SERVER_ROOM", null);

        service.executeCommand(cmd.getId());

        DeviceCommand updated = service.getCommand(cmd.getId());
        assertEquals(CommandStatus.PENDING, updated.getStatus());
        assertEquals(1, updated.getRetryCount());
    }

    @Test
    void executeCommand_deviceOffline_maxRetriesExhausted_statusFailed() {
        service.setDeviceOnline(false);
        DeviceCommand cmd = service.queueCommand(
                "Reboot", CommandType.REBOOT, "DEV-3", "FLOOR_2", null);

        // Execute 3 times (1 initial + 2 retries = max)
        service.executeCommand(cmd.getId());
        service.executeCommand(cmd.getId());
        service.executeCommand(cmd.getId());

        assertEquals(CommandStatus.FAILED, service.getCommand(cmd.getId()).getStatus());
    }

    // ─── Zone Execution Tests ─────────────────────────────────────────────────

    @Test
    void executeZone_withPendingCommands_executesAll() {
        service.setDeviceOnline(true);
        service.queueCommand("Cmd1", CommandType.TURN_ON, "DEV-1", "LOBBY", null);
        service.queueCommand("Cmd2", CommandType.BRIGHTEN, "DEV-2", "LOBBY", null);

        List<DeviceCommand> executed = service.executeZone("LOBBY");

        assertEquals(2, executed.size());
        executed.forEach(cmd ->
                assertEquals(CommandStatus.DONE, service.getCommand(cmd.getId()).getStatus()));
    }

    @Test
    void executeZone_noMatchingCommands_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.executeZone("NONEXISTENT_ZONE")
        );
    }

    // ─── Audit Log Tests ──────────────────────────────────────────────────────

    @Test
    void auditLogs_afterExecution_recordsStateTransitions() {
        service.setDeviceOnline(true);
        DeviceCommand cmd = service.queueCommand(
                "Turn On", CommandType.TURN_ON, "DEV-1", "LOBBY", null);

        service.executeCommand(cmd.getId());

        List<CommandLog> logs = service.getLogsForCommand(cmd.getId());
        assertTrue(logs.size() >= 2); // At minimum: PENDING + DONE
    }
}