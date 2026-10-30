package com.example.hw1.task1;

import com.example.hw1.Net;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Component
public class Task1 {

    @Value("${task1-timeout}")
    private int timeout;

    private final Executor executor;

    public Task1(Executor executor) {
        this.executor = executor;
    }

    public void run(String host, int port, String userId) {
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(timeout);
            InputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();

            Net.skipUntil(in, "ID:");
            Net.sendLine(out, userId);

            String line;
            while ((line = Net.readLine(in)) != null) {
                if (line.startsWith("Q ")) {
                    Net.sendLine(out, String.valueOf(executor.answer(line)));
                } else {
                    System.out.println("task-1: " + line);
                    if (line.contains("NQ{")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("task-1: error - " + e);
        }
    }
}