package ru.optimus.discord.channelstream.utils;

import java.util.regex.*;

public class NumberExtractor {

    /**
     * Проверяет, содержит ли строка ровно одно число (с возможными разделителями),
     * и возвращает это число как строку.
     * Если чисел нет или их больше одного - возвращает null.
     */
    public static String extractSingleNumber(String text) {
        if(text == null)
            return null;
        Pattern pattern = Pattern.compile("-?\\d+(?:\\.\\d+)?");
        Matcher matcher = pattern.matcher(text);

        String foundNumber = null;
        int count = 0;

        while (matcher.find()) {
            count++;
            if (count > 1) {
                return null; // Найдено больше одного числа
            }
            foundNumber = matcher.group();
        }

        return foundNumber;
    }

    /**
     * Улучшенная проверка, является ли строка числом
     * с учетом чисел в середине текста (если число только одно)
     */
    public static boolean isNumeric(String text) {
        String number = extractSingleNumber(text);
        return number != null && number.equals(text);
    }
}