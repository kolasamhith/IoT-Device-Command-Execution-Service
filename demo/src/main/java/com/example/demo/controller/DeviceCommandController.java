package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CommandRequest;
import com.example.demo.model.CommandLog;
import com.example.demo.model.DeviceCommand;
import com.example.demo.service.DeviceCommandService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/commands")
public class DeviceCommandController {

    private final DeviceCommandService service;

    public DeviceCommandController(DeviceCommandService service) {
        this.service = service;
    }

    // POST /commands — queue a new command
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceCommand createCommand(@Valid @RequestBody CommandRequest request) {
        return service.queueCommand(
                request.getCommandName(),
                request.getCommandType(),
                request.getDeviceId(),
                request.getZone(),
                request.getScheduledTime()
        );
    }

    // GET /commands — list all commands
    @GetMapping
    public List<DeviceCommand> getAllCommands() {
        return service.getAllCommands();
    }

    // GET /commands/{id} — get a single command
    @GetMapping("/{id}")
    public DeviceCommand getCommandStatus(@PathVariable Long id) {
        return service.getCommand(id);
    }

    // POST /commands/{id}/execute — manually force immediate execution
    @PostMapping("/{id}/execute")
    public DeviceCommand executeCommand(@PathVariable Long id) {
        service.executeCommand(id);
        return service.getCommand(id);
    }

    // POST /commands/zone/{zone}/execute — execute all PENDING commands in a zone
    @PostMapping("/zone/{zone}/execute")
    public List<DeviceCommand> executeZone(@PathVariable String zone) {
        return service.executeZone(zone);
    }

    // GET /commands/logs — system-wide audit trail
    @GetMapping("/logs")
    public List<CommandLog> getSystemLogs() {
        return service.getAllLogs();
    }

    // GET /commands/{id}/logs — execution history for a single command
    @GetMapping("/{id}/logs")
    public List<CommandLog> getCommandLogs(@PathVariable Long id) {
        service.getCommand(id); // triggers 404 if not found
        return service.getLogsForCommand(id);
    }
}