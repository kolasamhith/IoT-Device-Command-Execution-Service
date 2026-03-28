package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.model.CommandLog;
import com.example.demo.model.CommandStatus;
import com.example.demo.model.DeviceCommand;

@Service
public class DeviceCommandService {
    
    private final List<DeviceCommand> commandQueue = new ArrayList<>();
    private final List<CommandLog> auditLogs = new ArrayList<>(); // NEW: Log storage
    private final AtomicLong idGenerator = new AtomicLong(1);
    private static final int MAX_RETRIES = 2; 

    public DeviceCommand queueCommand(String commandName, String deviceId, String zone, LocalDateTime scheduledTime) {
        if (commandName == null || commandName.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be empty");
        }
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Target Device ID must be specified");
        }

        DeviceCommand command = new DeviceCommand(idGenerator.getAndIncrement(), commandName, deviceId, zone, scheduledTime);
        commandQueue.add(command);
        
        // Log the creation
        recordLog(command.getId(), CommandStatus.PENDING, "Command queued for device: " + deviceId);
        
        return command;
    }

    public List<DeviceCommand> getAllCommands() {
        return commandQueue;
    }

    public DeviceCommand getCommand(Long id) {
        return commandQueue.stream()
                .filter(cmd -> cmd.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException("Command ID not found in system")); 
    }

    public void executeCommand(Long id) {
        DeviceCommand command = getCommand(id);
        command.setStatus(CommandStatus.RUNNING);
        recordLog(id, CommandStatus.RUNNING, "Attempting to transmit to smart hub.");

        boolean isDeviceOnline = (id % 2 == 0); 

        if (isDeviceOnline) {
            command.setStatus(CommandStatus.DONE);
            recordLog(id, CommandStatus.DONE, "SUCCESS: Device acknowledged command.");
        } else {
            if (command.getRetryCount() < MAX_RETRIES) {
                command.incrementRetryCount();
                command.setStatus(CommandStatus.PENDING); 
                command.setScheduledTime(LocalDateTime.now().plusSeconds(5)); 
                
                recordLog(id, CommandStatus.PENDING, "WARNING: Device offline. Scheduled retry " + command.getRetryCount() + " of " + MAX_RETRIES);
            } else {
                command.setStatus(CommandStatus.FAILED);
                recordLog(id, CommandStatus.FAILED, "FATAL: Device permanently offline. Max retries exhausted.");
            }
        }
    }

    @Scheduled(fixedRate = 5000)
    public void processScheduledCommands() {
        LocalDateTime now = LocalDateTime.now();
        
        for (DeviceCommand cmd : commandQueue) {
            if (cmd.getStatus() == CommandStatus.PENDING && 
                cmd.getScheduledTime() != null && 
                !cmd.getScheduledTime().isAfter(now)) {
                
                executeCommand(cmd.getId());
            }
        }
    }

    // NEW: Helper method to save logs
    private void recordLog(Long commandId, CommandStatus status, String message) {
        CommandLog log = new CommandLog(commandId, status, message);
        auditLogs.add(log);
        System.out.println("[" + log.getTimestamp() + "] CMD_" + commandId + ": " + message);
    }

    // NEW: Retrieve logs for a specific command
    public List<CommandLog> getLogsForCommand(Long commandId) {
        return auditLogs.stream()
                .filter(log -> log.getCommandId().equals(commandId))
                .collect(Collectors.toList());
    }

    // NEW: Retrieve all system logs
    public List<CommandLog> getAllLogs() {
        return auditLogs;
    }
}