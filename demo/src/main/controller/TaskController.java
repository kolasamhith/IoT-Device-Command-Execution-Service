package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public Task create(@RequestParam String name) {
        return service.createTask(name);
    }

    @PostMapping("/{id}/run")
    public String run(@PathVariable Long id) {
        try {
            service.runTask(id);
            return "Task started in background.";
        } catch (InterruptedException e) {
            return "Error starting task.";
        }
    }

    @GetMapping
    public List<Task> getAll() {
        return service.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getOne(@PathVariable Long id) {
        return service.getTask(id);
    }
}