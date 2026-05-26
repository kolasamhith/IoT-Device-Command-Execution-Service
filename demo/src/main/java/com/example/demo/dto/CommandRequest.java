package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.demo.model.CommandType;
import java.time.LocalDateTime;

public class CommandRequest {

    @NotBlank(message = "Command name cannot be empty")
    @Size(max = 50, message = "Command name must be 50 characters or fewer")
    private String commandName;

    @NotBlank(message = "Device ID must be specified")
    private String deviceId;

    private String zone = "UNKNOWN_ZONE";

    @NotNull(message = "Command type must be specified")
    private CommandType commandType;

    private LocalDateTime scheduledTime;

    // Getters and Setters
    public String getCommandName() { return commandName; }
    public void setCommandName(String commandName) { this.commandName = commandName; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public CommandType getCommandType() { return commandType; }
    public void setCommandType(CommandType commandType) { this.commandType = commandType; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
}