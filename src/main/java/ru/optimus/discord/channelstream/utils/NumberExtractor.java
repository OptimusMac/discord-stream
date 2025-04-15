package ru.optimus.discord.channelstream.utils;

import java.util.StringTokenizer;
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
                return null;
            }
            foundNumber = matcher.group();
        }

        return foundNumber;
    }



    public static String extractSplit(String text){
        StringTokenizer stringTokenizer = new StringTokenizer(text, " ");

        while (stringTokenizer.hasMoreTokens()){
            String token = stringTokenizer.nextToken();
            String extract = extractSingleNumber(token);

            if(extract != null){
                return extract;
            }
        }

        return null;
    }

}