package com.example.hw1.task1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;


// Обрабатывает строку и вычисляет результат
@Service
public class Executor {
    public BigInteger answer(String str) {
        String[] strings = str.split(" ");
        String ops;

        BigInteger ans = BigInteger.ZERO;
        try {
            BigInteger firstInt = new BigInteger(strings[2]);
            BigInteger secondInt = new BigInteger(strings[4]);
            ops = strings[3];

            ans = switch (ops) {
                case "+" -> firstInt.add(secondInt);
                case "-" -> firstInt.subtract(secondInt);
                case "*" -> firstInt.multiply(secondInt);
                case "/" -> firstInt.divide(secondInt);
                case "%" -> firstInt.mod(secondInt);
                default -> {
                    System.out.println("В задании 1 передали неизвестную операцию: " + ops);
                    yield BigInteger.ZERO; // fallback
                }
            };
        } catch (IllegalArgumentException e) {
            System.out.println("В задании 1 передали неверный формат данных. \nОжидалось: Q 1/100: 89 * 22 = ? \nПередали: " + str);
        }

        return ans;

    }
}
