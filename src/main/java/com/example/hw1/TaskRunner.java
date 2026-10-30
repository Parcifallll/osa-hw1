package com.example.hw1;

import com.example.hw1.task1.Task1;
import com.example.hw1.task2.Task2;
import com.example.hw1.task3.Task3;
import com.example.hw1.task4.Task4;
import com.example.hw1.task5.Task5;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TaskRunner implements CommandLineRunner {

    @Value("${user-id}")
    private String userId;

    @Value("${task-server-address}")
    private String host;

    @Value("${task-ports.task-1}")
    private int port1;

    @Value("${task-ports.task-2}")
    private int port2;

    @Value("${task-ports.task-3}")
    private int port3;

    @Value("${task-ports.task-4}")
    private int port4;

    @Value("${task-ports.task-5}")
    private int port5;

    private final Task1 task1;
    private final Task2 task2;
    private final Task3 task3;
    private final Task4 task4;
    private final Task5 task5;

    public TaskRunner(Task1 task1, Task2 task2, Task3 task3, Task4 task4, Task5 task5) {
        this.task1 = task1;
        this.task2 = task2;
        this.task3 = task3;
        this.task4 = task4;
        this.task5 = task5;
    }

    @Override
    public void run(String... args) {
        task1.run(host, port1, userId);
        task2.run(host, port2, userId);
        task3.run(host, port3, userId);
        task4.run(host, port4, userId);
        task5.run(host, port5, userId);
    }
}