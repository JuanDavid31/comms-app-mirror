package com.rallytac.engageandroid.legba.util;

public class StringUtils {

    public static String getFirstLetterCapsFrom(String name) {
        String[] names;
        if (name.contains(" ")) {
            names = name.split(" ");
        } else {
            return name.isEmpty() ? "" : String.valueOf(name.toUpperCase().charAt(0));
        }

        String result = "";
        for (int i = 0; (i < names.length) && (i < 2); i++) {
            result += Character.toUpperCase(names[i].charAt(0));
        }
        return result;
    }
}
