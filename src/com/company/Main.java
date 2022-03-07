package com.company;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Formula formula = new Formula();
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите формулу : ");
        String form = scan.nextLine();
        formula.prepare(form);
        System.out.println("Введите, разделяя пробелами, значения переменных" +
                " в порядке, в котором они встречаются в формуле(повторно вводить не нужно)");

        String values = scan.nextLine();
        double result = formula.execute(values);
        System.out.println("Результат вычислений: " + result);
    }
}
