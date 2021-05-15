package ru.baronessdev.personal.redage.redagemain.util;

public class BooleanUtil {

    public static boolean fromInt(int i) {
        return i == 1;
    }

    public static int toInt(boolean b) {
        return b ? 1 : 0;
    }
}
