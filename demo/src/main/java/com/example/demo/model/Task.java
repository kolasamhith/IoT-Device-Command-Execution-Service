package com.example.demo.model;

import jakarta.persistence.*;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String logs;

    public Task() {}

    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.PENDING;
        this.logs = "Task created.";
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public TaskStatus getStatus() { return status; }
    public String getLogs() { return logs; }
    
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setLogs(String logs) { this.logs = logs; }
}