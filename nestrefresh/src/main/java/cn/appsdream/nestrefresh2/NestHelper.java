package cn.appsdream.nestrefresh2;

/**
 * Created by 58 on 2016/11/30.
 */

public class NestHelper {
    private int mOffsetY = 0;
    private int mOffsetX = 0;

    public int getOffsetY() {
        return mOffsetY;
    }

    public int getOffsetX() {
        return mOffsetX;
    }

    public void offsetTopAndBottom(int offset) {
        mOffsetY = -offset;
    }

    public void offsetLeftAndRight(int offset) {
        mOffsetX = -offset;
    }
}
