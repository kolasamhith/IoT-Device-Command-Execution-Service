package com.example.demo.model;

import java.time.LocalDateTime;

public class DeviceCommand {

    private Long id;
    private String commandName;
    private CommandType commandType;
    private String deviceId;
    private String zone;
    private CommandStatus status;
    private LocalDateTime scheduledTime;
    private int retryCount;

    public DeviceCommand(Long id, String commandName, CommandType commandType,
                         String deviceId, String zone, LocalDateTime scheduledTime) {
        this.id = id;
        this.commandName = commandName;
        this.commandType = commandType;
        this.deviceId = deviceId;
        this.zone = zone;
        this.status = CommandStatus.PENDING;
        this.scheduledTime = scheduledTime;
        this.retryCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCommandName() { return commandName; }
    public void setCommandName(String commandName) { this.commandName = commandName; }

    public CommandType getCommandType() { return commandType; }
    public void setCommandType(CommandType commandType) { this.commandType = commandType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public CommandStatus getStatus() { return status; }
    public void setStatus(CommandStatus status) { this.status = status; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
}