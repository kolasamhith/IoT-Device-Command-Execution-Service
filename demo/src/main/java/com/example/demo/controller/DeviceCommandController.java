package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.DeviceCommand;
import com.example.demo.service.DeviceCommandService;

@RestController
@RequestMapping("/commands") 
public class DeviceCommandController {

    private final DeviceCommandService service;

    public DeviceCommandController(DeviceCommandService service) {
        this.service = service;
    }

    @PostMapping
    public DeviceCommand createCommand(
            @RequestParam String commandName, 
            @RequestParam String deviceId, 
            @RequestParam(defaultValue = "UNKNOWN_ZONE") String zone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime) {
        
        // If no specific time is provided, execute it right now
        if (scheduledTime == null) {
            scheduledTime = LocalDateTime.now();
        }
        
        return service.queueCommand(commandName, deviceId, zone, scheduledTime);
    }

    @GetMapping
    public List<DeviceCommand> getAllCommands() {
        return service.getAllCommands();
    }

    // You can keep the manual execute endpoint as an "override" to force immediate execution
    @PostMapping("/{id}/execute")
    public DeviceCommand executeCommand(@PathVariable Long id) {
        service.executeCommand(id);
        return service.getCommand(id); 
    }

    @GetMapping("/{id}")
    public DeviceCommand getCommandStatus(@PathVariable Long id) {
        return service.getCommand(id);
    }

// NEW: View system-wide audit logs
    @GetMapping("/logs")
    public List<com.example.demo.model.CommandLog> getSystemLogs() {
        return service.getAllLogs();
    }

    // NEW: View execution history for a single command
    @GetMapping("/{id}/logs")
    public List<com.example.demo.model.CommandLog> getCommandLogs(@PathVariable Long id) {
        // First verify the command exists to trigger a 404 if it doesn't
        service.getCommand(id);
        return service.getLogsForCommand(id);
    }
}