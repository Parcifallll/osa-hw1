package com.example.hw1.task5;

import com.example.hw1.Net;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class Task5 {

    private static final int MAGIC = 0x4E51;
    private static final int QUESTION = 1;
    private static final int ANSWER = 2;
    private static final int NOISE = 3;
    private static final int RESULT = 4;
    private static final int ERROR = 5;

    public void run(String host, int port, String userId) {
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(15_000);
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();

            Net.skipUntil(in, "ID:");
            Net.sendLine(out, userId);
            Net.skipUntil(in, "BINARY");
            int c;
            while ((c = in.read()) != -1 && c != '\n') {
            }

            while (true) {
                int magic = in.readUnsignedShort();
                if (magic != MAGIC) {
                    throw new IOException("Bad magic: 0x" + Integer.toHexString(magic));
                }
                int type = in.readUnsignedByte();
                int length = in.readInt();
                if (length < 0) {
                    throw new IOException("Bad length: " + length);
                }
                byte[] payload = new byte[length];
                in.readFully(payload);

                switch (type) {
                    case QUESTION -> sendAnswer(out, solve(payload));
                    case NOISE -> {
                    }
                    case RESULT -> {
                        System.out.println("task-5: " + new String(payload, StandardCharsets.UTF_8));
                        return;
                    }
                    case ERROR -> {
                        System.out.println("task-5: server error - " + new String(payload, StandardCharsets.UTF_8));
                        return;
                    }
                    default -> throw new IOException("Unknown frame type: " + type);
                }
            }
        } catch (IOException e) {
            System.out.println("task-5: error - " + e);
        }
    }

    private long solve(byte[] payload) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int op = bb.get() & 0xFF;
        int n = bb.getShort() & 0xFFFF;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = bb.getInt();
        }

        return switch (op) {
            case 1 -> {
                long sum = 0;
                for (int x : nums) {
                    sum += x;
                }
                yield sum;
            }
            case 2 -> {
                int max = Integer.MIN_VALUE;
                for (int x : nums) {
                    max = Math.max(max, x);
                }
                yield max;
            }
            case 3 -> {
                int min = Integer.MAX_VALUE;
                for (int x : nums) {
                    min = Math.min(min, x);
                }
                yield min;
            }
            case 4 -> {
                int xor = 0;
                for (int x : nums) {
                    xor ^= x;
                }
                yield xor;
            }
            default -> throw new IOException("Unknown operation: " + op);
        };
    }

    private void sendAnswer(OutputStream out, long result) throws IOException {
        ByteBuffer frame = ByteBuffer.allocate(2 + 1 + 4 + 8);
        frame.putShort((short) MAGIC).put((byte) ANSWER).putInt(8).putLong(result);
        out.write(frame.array());
        out.flush();
    }
}