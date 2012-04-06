package com.feixun.smart;

import java.util.Random;

public class Utils {

    private static final Random sRandom = new Random();

    public static double rand() {
        return sRandom.nextFloat();
    }

    public static int randInt(int x, int y) {
        // return (int)(x + (y-x) * rand());
        if (x >= y) {
            return x;
        }
        return (x + sRandom.nextInt(y - x));
    }

    // returns a random float between zero and 1
    public static double randFloat() {
        return rand();
    }

    // returns a random float in the range -1 < n < 1
    public static Double randomClamped() {
        return randFloat() - randFloat();
    }

    // void Clamp(double &arg, double min, double max);

    public static double clamp(double arg, double min, double max) {
        if (arg < min) {
            arg = min;
        }

        if (arg > max) {
            arg = max;
        }
        return arg;
    }

    public static float degrees(float radians) {
        return radians * Params.RAD_TO_DEG;
    }
}
