package com.example.hw1.task3;

import com.example.hw1.Net;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Component
public class Task3 {

    private static final int RTT_PROBES = 20;

    @Value("${task3-attempts}")
    private int attempts;

    public void run(String host, int port, String userId) {
        try {
            double rtt = Double.MAX_VALUE;
            for (int i = 0; i < RTT_PROBES; i++) {
                long start = System.nanoTime();
                try (Socket probe = new Socket(host, port)) {
                    rtt = Math.min(rtt, (System.nanoTime() - start) / 1_000_000.0);
                }
            }

            try (Socket socket = new Socket(host, port)) {
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(15_000);
                InputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream out = socket.getOutputStream();

                Net.sendLine(out, userId);

                double best = Double.MAX_VALUE;
                for (int i = 0; i < attempts; i++) {
                    Net.sendLine(out, "PING " + i);
                    long start = System.nanoTime();

                    String line;
                    do {
                        line = Net.readLine(in);
                        if (line == null) {
                            throw new EOFException("Server closed the connection");
                        }
                    } while (!line.endsWith("PONG " + i));

                    best = Math.min(best, (System.nanoTime() - start) / 1_000_000.0);
                }

                long ms = Math.round(best - rtt);
                System.out.println("task-3: min " + best + " ms, tcp rtt " + rtt + " ms, answer " + ms + " ms");

                Net.sendLine(out, "ANSWER " + ms);

                String line;
                while ((line = Net.readLine(in)) != null) {
                    System.out.println("task-3: " + line);
                    if (line.contains("NQ{")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("task-3: error - " + e);
        }
    }
}