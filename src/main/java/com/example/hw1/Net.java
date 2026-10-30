package com.example.hw1;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class Net {

    private Net() {
    }

    public static void sendLine(OutputStream out, String text) throws IOException {
        out.write((text + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            buf.write(b);
        }
        if (b == -1 && buf.size() == 0) {
            return null;
        }
        return buf.toString(StandardCharsets.UTF_8).strip();
    }

    public static void skipUntil(InputStream in, String marker) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            sb.append((char) b);
            int from = sb.length() - marker.length();
            if (from >= 0 && sb.substring(from).equals(marker)) {
                return;
            }
        }
        throw new EOFException("Маркер не найден: " + marker);
    }
}