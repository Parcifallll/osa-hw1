package com.example.hw1.task2;

import com.example.hw1.Net;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

@Component
public class Task2 {

    public void run(String host, int port, String userId) {
        try (Socket socket = new Socket(host, port)) {
            InputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();

            socket.setSoTimeout(15_000);
            Net.skipUntil(in, "ID:");
            Net.sendLine(out, userId);
            Net.skipUntil(in, "BEGIN\n");

            socket.setSoTimeout(3_000);
            long count = 0;
            byte[] buf = new byte[8192];
            try {
                int n;
                while ((n = in.read(buf)) != -1) {
                    count += n;
                }
            } catch (SocketTimeoutException e) {
            }
            System.out.println("task-2: received " + count + " bytes");

            Net.sendLine(out, String.valueOf(count));

            ByteArrayOutputStream reply = new ByteArrayOutputStream();
            try {
                int n;
                while ((n = in.read(buf)) != -1) {
                    reply.write(buf, 0, n);
                }
            } catch (SocketTimeoutException e) {
            }
            String text = reply.toString(StandardCharsets.UTF_8).strip();
            System.out.println("task-2: " + text.replace("\n", "\ntask-2: "));
        } catch (IOException e) {
            System.out.println("task-2: error - " + e);
        }
    }
}