package com.example.demo.service;

import com.example.demo.model.Task;
import com.example.demo.model.TaskStatus;
import com.example.demo.repository.TaskRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final Random random = new Random();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task createTask(String name) {
        return repository.save(new Task(name));
    }

    public List<Task> getAllTasks() {
        return repository.findAll();
    }

    public Task getTask(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Async
    public void runTask(Long id) throws InterruptedException {
        Task task = repository.findById(id).orElseThrow();
        
        task.setStatus(TaskStatus.RUNNING);
        task.setLogs(task.getLogs() + "\nRunning...");
        repository.save(task);

        Thread.sleep(3000); // Simulate processing time

        if (random.nextBoolean()) {
            task.setStatus(TaskStatus.DONE);
            task.setLogs(task.getLogs() + "\nSuccess.");
        } else {
            task.setStatus(TaskStatus.FAILED);
            task.setLogs(task.getLogs() + "\nFailed.");
        }
        repository.save(task);
    }
}