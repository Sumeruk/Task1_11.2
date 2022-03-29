package com.company;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formula {
    private List<String> variables;
    private String formula;

    public void prepare(String originalFormula) {
        String formula = originalFormula.replaceAll(" ","");

        List<String> variables = new ArrayList<>();
        Pattern allsymbols = Pattern.compile("[^a-z0-9+*/().-]");
        Matcher firstmatcher = allsymbols.matcher(formula);
        if (firstmatcher.find()){
            throw new NumberFormatException("Формула содержит некорректные данные");
        }

        Pattern alphabet = Pattern.compile("[a-z]");
        Matcher secondmatcher = alphabet.matcher(formula);
        while (secondmatcher.find()) {
            if (variableNotWas(secondmatcher.group(), variables))
                variables.add(secondmatcher.group());
        }

        if(variables.size() == 0){
            throw new IndexOutOfBoundsException("Формула не содержит переменных");
        }

        Pattern firstBracket = Pattern.compile("[(]");
        Matcher fb = firstBracket.matcher(formula);
        int numfirstbracket = 0;
        while (fb.find()) {
            numfirstbracket++;
        }
        Pattern secBracket = Pattern.compile("[)]");
        Matcher sb = firstBracket.matcher(formula);
        int numsecbracket = 0;
        while (sb.find()){
            numsecbracket++;
        }
        if (numsecbracket != numfirstbracket) {
            throw new NumberFormatException("Разное количество открывающих и закрывающих скобок");
        }

        this.variables = variables;
        this.formula = formula;

    }
    private boolean variableNotWas(String x, List<String> variables) {
        for (int i = 0; i< variables.size();i++) {
            if (variables.get(i).equals(x)) {
                return false;
            }
        }
        return true;
    }

    public double execute(String numbers){
        List<String> numb = findNumbers(numbers);
        if (variables.size() != numb.size()){
            throw new ArrayIndexOutOfBoundsException("Количество введенных чисел не соответствует количеству " +
                    "введенных переменных");
        } else {
            for (int i = 0;i< numb.size();i++){
                if (Double.parseDouble(numb.get(i))<0){
                    numb.set(i,"(" + numb.get(i) + ")");
                }
                formula = formula.replaceAll(variables.get(i), numb.get(i));
            }

            String resultformula = repleaseBracketAndCalc();
            if (resultformula.contains("-")) {
                resultformula = resultformula.substring(1, resultformula.length() - 1);
            }

            return Double.parseDouble(resultformula);
        }
    }

    private List<String> findNumbers(String lineWithNumbers) {
        List<String> numbers = new ArrayList<>();

        Pattern pattern = Pattern.compile("[-]?[0-9]+[.]?[0-9]*");
        Matcher matcher = pattern.matcher(lineWithNumbers);

        while (matcher.find()) {
            if (!matcher.group().endsWith(".")) {
                numbers.add(matcher.group());
            } else {
                throw new NumberFormatException("Данные введены неверно: в конце числа не может стоять <<.>>");
            }
        }

        return numbers;
    }

    private String repleaseBracketAndCalc(){
        String result;
        while (formula.contains("(") && formula.contains(")")){
            int indFirstBracket = formula.lastIndexOf("(");
            int indSecondBracket = formula.indexOf(")", indFirstBracket);
            if (indSecondBracket != -1 && indFirstBracket != -1){
                String expressionInBrackets = formula.substring(indFirstBracket + 1, indSecondBracket);
                StringBuilder builder = new StringBuilder(formula);

                if (expressionInBrackets.lastIndexOf("-") == 0 && !expressionInBrackets.contains("+") &&
                        !expressionInBrackets.contains("*") && !expressionInBrackets.contains("/")) {
                    builder.setCharAt(indFirstBracket, '{');
                    builder.setCharAt(indSecondBracket, '}');
                } else {
                    String number = calcExpressionInBrackets(expressionInBrackets);
                    builder.replace(indFirstBracket, indSecondBracket + 1, number);
                }
                formula = String.valueOf(builder);
        }

    }
        result = calcExpressionInBrackets(formula);
        return result;
}

    private String calcExpressionInBrackets(String expression){
        if (expression.charAt(0) == '-') {
            expression = "0" + expression;
        }

        List<Character> allSymbols = Arrays.asList('*', '/', '+', '-');

        while (!((findIndexLastRequiredSymbol(allSymbols, expression) == -1) || (expression.lastIndexOf("{") == 0 &&
                findIndexLastRequiredSymbol(allSymbols, expression) == 1))) {

            int indexFirstSymbol, indexMiddleSymbol, indexLastSymbol;

            List<Character> firstOrLastSymbols, middleSymbols;
            List<Character> multiplicationAndDivision = Arrays.asList('*', '/');

            if (findIndexLastRequiredSymbol(multiplicationAndDivision, expression) != -1) {
                firstOrLastSymbols = allSymbols;
                middleSymbols = multiplicationAndDivision;
            } else {
                firstOrLastSymbols = middleSymbols = Arrays.asList('+', '-');
            }

            indexMiddleSymbol = findIndexFirstRequiredSymbol(middleSymbols, expression);
            if (expression.charAt(indexMiddleSymbol - 1) == '{') {
                String partExpression = expression.substring(indexMiddleSymbol + 1);
                indexMiddleSymbol = findIndexFirstRequiredSymbol(middleSymbols, partExpression) + indexMiddleSymbol + 1;
            }

            String firstPart = expression.substring(0, indexMiddleSymbol);
            String secondPart = expression.substring(indexMiddleSymbol + 1);

            double firstNumber, secondNumber;

            indexFirstSymbol = findIndexLastRequiredSymbol(firstOrLastSymbols, firstPart);
            if (expression.charAt(indexMiddleSymbol - 1) == '}') {
                indexFirstSymbol = firstPart.lastIndexOf('{') - 1;
                firstNumber = Double.parseDouble(firstPart.substring(indexFirstSymbol + 2, firstPart.length() - 1));
            } else {
                firstNumber = Double.parseDouble(expression.substring(indexFirstSymbol + 1, indexMiddleSymbol));
            }

            indexLastSymbol = findIndexFirstRequiredSymbol(firstOrLastSymbols, secondPart);
            if (expression.charAt(indexMiddleSymbol + 1) == '{') {
                secondNumber = Double.parseDouble(secondPart.substring(indexLastSymbol, secondPart.indexOf('}')));
                indexLastSymbol = indexMiddleSymbol + secondPart.indexOf('}') + 2;
            } else {

                if (indexLastSymbol == -1) {
                    indexLastSymbol = expression.length();
                } else {
                    indexLastSymbol = indexMiddleSymbol + indexLastSymbol + 1;
                }
                secondNumber = Double.parseDouble(expression.substring(indexMiddleSymbol + 1, indexLastSymbol));
            }

            String symbol = Character.toString(expression.charAt(indexMiddleSymbol));
            String result = findResult(symbol, firstNumber, secondNumber);
            StringBuilder builder = new StringBuilder(expression);


            expression = String.valueOf(builder.replace(indexFirstSymbol + 1, indexLastSymbol, result));
        }
        return expression;
    }
    private int findIndexFirstRequiredSymbol(List<Character> symbols, String expression) {
        int index = expression.length();

        for (Character symbol : symbols) {
            if (expression.contains(Character.toString(symbol)) && expression.indexOf(symbol) < index) {
                index = expression.indexOf(symbol);
            }
        }

        if (index == expression.length()) {
            index = -1;
        }

        return index;
    }

    private int findIndexLastRequiredSymbol(List<Character> symbols, String expression) {
        int index = -1;

        for (Character symbol : symbols) {
            if (expression.contains(Character.toString(symbol)) && expression.lastIndexOf(symbol) > index) {
                index = expression.lastIndexOf(symbol);
            }
        }

        return index;
    }

    private String findResult(String symbol, double firstNumber, double secondNumber) {
        double result = 0;

        if (symbol.equals("/") && secondNumber == 0) {
            throw new ArithmeticException("Деление на 0");
        }

        if (symbol.equals("*")) {
            result = firstNumber * secondNumber;
        }

        if (symbol.equals("+")) {
            result = firstNumber + secondNumber;
        }

        if (symbol.equals("-")) {
            result = firstNumber - secondNumber;
        }

        if (symbol.equals("/")) {
            result = firstNumber / secondNumber;
        }


        if (result < 0) {
            return "{" + result + "}";
        }

        return Double.toString(result);
    }
}













