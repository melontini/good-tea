package me.melontini.goodtea.util;

import java.util.Random;

public class JavaRandomUtil {
    public static int nextInt(Random random, int min, int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }
}
