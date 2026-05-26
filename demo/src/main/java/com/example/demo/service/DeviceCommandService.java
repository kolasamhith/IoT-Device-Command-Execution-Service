package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.model.CommandLog;
import com.example.demo.model.CommandStatus;
import com.example.demo.model.CommandType;
import com.example.demo.model.DeviceCommand;

@Service
public class DeviceCommandService {

    // ConcurrentHashMap: thread-safe for concurrent REST + @Scheduled access
    private final Map<Long, DeviceCommand> commandStore = new ConcurrentHashMap<>();

    // CopyOnWriteArrayList: thread-safe for read-heavy audit log access
    private final List<CommandLog> auditLogs = new CopyOnWriteArrayList<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Value("${command.max-retries:2}")
    private int maxRetries;

    // ─── Core CRUD ────────────────────────────────────────────────────────────

    public DeviceCommand queueCommand(String commandName, CommandType commandType,
                                      String deviceId, String zone,
                                      LocalDateTime scheduledTime) {
        if (commandName == null || commandName.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be empty");
        }
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Target Device ID must be specified");
        }
        if (commandType == null) {
            throw new IllegalArgumentException("Command type must be specified");
        }

        LocalDateTime effectiveTime = (scheduledTime != null) ? scheduledTime : LocalDateTime.now();

        DeviceCommand command = new DeviceCommand(
                idGenerator.getAndIncrement(),
                commandName,
                commandType,
                deviceId,
                zone,
                effectiveTime
        );

        commandStore.put(command.getId(), command);
        recordLog(command.getId(), CommandStatus.PENDING, "Command queued for device: " + deviceId);
        return command;
    }

    public List<DeviceCommand> getAllCommands() {
        return List.copyOf(commandStore.values());
    }

    public DeviceCommand getCommand(Long id) {
        DeviceCommand command = commandStore.get(id);
        if (command == null) {
            throw new TaskNotFoundException("Command ID " + id + " not found in system");
        }
        return command;
    }

    // ─── Execution Logic ──────────────────────────────────────────────────────

    public void executeCommand(Long id) {
        DeviceCommand command = getCommand(id);
        command.setStatus(CommandStatus.RUNNING);
        recordLog(id, CommandStatus.RUNNING, "Attempting to transmit to smart hub.");

        boolean isDeviceOnline = simulateDeviceOnline(id);

        if (isDeviceOnline) {
            command.setStatus(CommandStatus.DONE);
            recordLog(id, CommandStatus.DONE, "SUCCESS: Device acknowledged command.");
        } else {
            if (command.getRetryCount() < maxRetries) {
                command.incrementRetryCount();
                command.setStatus(CommandStatus.PENDING);
                command.setScheduledTime(LocalDateTime.now().plusSeconds(5));
                recordLog(id, CommandStatus.PENDING,
                        "WARNING: Device offline. Scheduled retry "
                                + command.getRetryCount() + " of " + maxRetries);
            } else {
                command.setStatus(CommandStatus.FAILED);
                recordLog(id, CommandStatus.FAILED,
                        "FATAL: Device permanently offline. Max retries exhausted.");
            }
        }
    }

    /**
     * Executes all PENDING commands in a given zone.
     * Useful for bulk zone-level control (e.g. "turn off all lights in LOBBY").
     */
    public List<DeviceCommand> executeZone(String zone) {
        List<DeviceCommand> zoneCommands = commandStore.values().stream()
                .filter(cmd -> zone.equalsIgnoreCase(cmd.getZone()))
                .filter(cmd -> cmd.getStatus() == CommandStatus.PENDING)
                .collect(Collectors.toList());

        if (zoneCommands.isEmpty()) {
            throw new IllegalArgumentException(
                    "No PENDING commands found for zone: " + zone);
        }

        zoneCommands.forEach(cmd -> executeCommand(cmd.getId()));
        return zoneCommands;
    }

    // ─── Scheduler ────────────────────────────────────────────────────────────

    @Scheduled(fixedRateString = "${command.scheduler.rate:5000}")
    public void processScheduledCommands() {
        LocalDateTime now = LocalDateTime.now();

        commandStore.values().stream()
                .filter(cmd -> cmd.getStatus() == CommandStatus.PENDING)
                .filter(cmd -> cmd.getScheduledTime() != null)
                .filter(cmd -> !cmd.getScheduledTime().isAfter(now))
                .forEach(cmd -> executeCommand(cmd.getId()));
    }

    // ─── Audit Logs ───────────────────────────────────────────────────────────

    public List<CommandLog> getLogsForCommand(Long commandId) {
        return auditLogs.stream()
                .filter(log -> log.getCommandId().equals(commandId))
                .collect(Collectors.toList());
    }

    public List<CommandLog> getAllLogs() {
        return List.copyOf(auditLogs);
    }

    // ─── Internal Helpers ─────────────────────────────────────────────────────

    private void recordLog(Long commandId, CommandStatus status, String message) {
        CommandLog log = new CommandLog(commandId, status, message);
        auditLogs.add(log);
        System.out.println("[" + log.getTimestamp() + "] CMD_" + commandId + ": " + message);
    }

    /**
     * Simulates device online/offline status.
     * In a real system, this would call an MQTT broker or HTTP device registry.
     */
    protected boolean simulateDeviceOnline(Long id) {
        return (id % 2 == 0);
    }
}