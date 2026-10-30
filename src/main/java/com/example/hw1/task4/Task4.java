package com.example.hw1.task4;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class Task4 {

    private static final int TIMEOUT_MS = 1_000;
    private static final int MAX_ROUNDS = 60;
    private static final int RESEND_BATCH = 50;

    private final Map<Integer, byte[]> chunks = new TreeMap<>();
    private int total = -1;

    public void run(String host, int port, String userId) {
        chunks.clear();
        total = -1;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetSocketAddress server = new InetSocketAddress(host, port);

            send(socket, server, "GET " + userId);

            for (int round = 0; round < MAX_ROUNDS; round++) {
                receiveUntilQuiet(socket);

                if (total != -1 && chunks.size() == total) {
                    break;
                }

                if (total == -1) {
                    send(socket, server, "GET " + userId);
                } else {
                    requestMissing(socket, server, userId);
                }
            }

            if (total == -1 || chunks.size() != total) {
                System.out.println("task-4: could not collect all chunks: " + chunks.size() + "/" + total);
                return;
            }

            ByteArrayOutputStream message = new ByteArrayOutputStream();
            for (byte[] data : chunks.values()) {
                message.write(data);
            }
            System.out.println("task-4: " + message.toString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("task-4: error - " + e);
        }
    }

    private void receiveUntilQuiet(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[65_535];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                store(packet);
            }
        } catch (SocketTimeoutException e) {
        }
    }

    private void requestMissing(DatagramSocket socket, InetSocketAddress server, String userId) throws IOException {
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            if (!chunks.containsKey(i)) {
                missing.add(String.valueOf(i));
            }
        }
        for (int i = 0; i < missing.size(); i += RESEND_BATCH) {
            List<String> part = missing.subList(i, Math.min(i + RESEND_BATCH, missing.size()));
            send(socket, server, "RESEND " + userId + " " + String.join(",", part));
        }
    }

    private void store(DatagramPacket packet) {
        byte[] raw = packet.getData();
        int start = packet.getOffset();
        int end = start + packet.getLength();

        int bar = start;
        while (bar < end && raw[bar] != '|') {
            bar++;
        }
        if (bar == end) {
            return;
        }

        try {
            String[] header = new String(raw, start, bar - start, StandardCharsets.US_ASCII).split("/");
            int seq = Integer.parseInt(header[0].trim());
            int count = Integer.parseInt(header[1].trim());
            if (seq >= 0 && seq < count) {
                chunks.putIfAbsent(seq, Arrays.copyOfRange(raw, bar + 1, end));
                total = count;
            }
        } catch (RuntimeException e) {
        }
    }

    private void send(DatagramSocket socket, InetSocketAddress server, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(data, data.length, server));
    }
}