package com.example.demo.model;

import java.time.LocalDateTime;

public class DeviceCommand {
    private Long id;
    private String commandName; 
    private String deviceId;    
    private String zone;        
    private CommandStatus status;
    private LocalDateTime scheduledTime;
    private int retryCount; // NEW FIELD

    public DeviceCommand(Long id, String commandName, String deviceId, String zone, LocalDateTime scheduledTime) {
        this.id = id;
        this.commandName = commandName;
        this.deviceId = deviceId;
        this.zone = zone;
        this.status = CommandStatus.PENDING;
        this.scheduledTime = scheduledTime;
        this.retryCount = 0; // Initialize at 0
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCommandName() { return commandName; }
    public void setCommandName(String commandName) { this.commandName = commandName; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public CommandStatus getStatus() { return status; }
    public void setStatus(CommandStatus status) { this.status = status; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    // NEW GETTER AND SETTER
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
}