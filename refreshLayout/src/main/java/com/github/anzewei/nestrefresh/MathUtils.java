package com.github.anzewei.nestrefresh;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

class MathUtils {

    static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

}