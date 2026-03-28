package com.example.demo.model;

import java.time.LocalDateTime;

public class CommandLog {
    private Long commandId;
    private LocalDateTime timestamp;
    private CommandStatus status;
    private String message;

    public CommandLog(Long commandId, CommandStatus status, String message) {
        this.commandId = commandId;
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
    }

    public Long getCommandId() { return commandId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public CommandStatus getStatus() { return status; }
    public String getMessage() { return message; }
}