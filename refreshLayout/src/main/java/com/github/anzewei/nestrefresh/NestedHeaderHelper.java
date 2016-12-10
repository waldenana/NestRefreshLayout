package com.github.anzewei.nestrefresh;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewParentCompat;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class NestedHeaderHelper {
    private static final String TAG = "NestedHeaderHelper";
    private final RefreshLayout mView;
    private boolean mHeaderEnable = true;
    private boolean mFooterEnable = true;
    private final ArrayList<IHeaderFooter> mHeaders;
    private final ArrayList<IHeaderFooter> mFooters;
    private int[] mTempNestedScrollConsumed = new int[2];
    private int[] mNestedScrollConsumed = new int[2];

    public NestedHeaderHelper(RefreshLayout view) {
        mView = view;
        mHeaders = view.mHeaders;
        mFooters = view.mFooters;
    }

    public boolean isHeaderEnable() {
        return mHeaderEnable;
    }

    public void setHeaderEnable(boolean headerEnable) {
        mHeaderEnable = headerEnable;
    }

    public boolean isFooterEnable() {
        return mFooterEnable;
    }

    public void setFooterEnable(boolean footerEnable) {
        mFooterEnable = footerEnable;
    }

    public boolean dispatchNestedScroll(final int dxUnconsumed, final int dyUnconsumed, int[] offsetInWindow, boolean byUser) {
        if (mFooterEnable || mHeaderEnable) {
            if (dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    startX = mView.getOffsetX();
                    startY = mView.getOffsetY();
                }
                int dx= dxUnconsumed;
                int dy=dyUnconsumed;
                final int[] consumed=mTempNestedScrollConsumed;
                if (mFooterEnable)
                    for (IHeaderFooter headerFooter : mFooters) {
                        clear(consumed);
                        headerFooter.onNestedScroll(mView, dx, dy, consumed, byUser);
                        dx -= consumed[0];
                        dy -= consumed[1];
                        if (dx == 0 && dy == 0)
                            break;
                    }
                if (mHeaderEnable)
                    for (IHeaderFooter headerFooter : mHeaders) {
                        clear(consumed);
                        headerFooter.onNestedScroll(mView, dx, dy, consumed, byUser);
                        dx -= consumed[0];
                        dy -= consumed[1];
                        if (dx == 0 && dy == 0)
                            break;
                    }
                consumed[0]=dxUnconsumed-dx;
                consumed[1]=dyUnconsumed-dy;
                nestedScroll(consumed);
                Log.d(TAG, "dispatchNestedScroll ="+Arrays.toString(consumed));
                if (offsetInWindow != null) {
                    offsetInWindow[0] = mView.getOffsetX() - startX;
                    offsetInWindow[1] = mView.getOffsetY() - startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                // No motion, no dispatch. Keep offsetInWindow up to date.
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    private void clear(int[] array) {
        if (array != null)
            Arrays.fill(array, 0);
    }

    public boolean dispatchNestedPreScroll(final int dxUnconsumed, final int dyUnconsumed, @NonNull int[] consumed, int[] offsetInWindow, boolean byUser) {
        if (mFooterEnable || mHeaderEnable) {
            if (dxUnconsumed != 0 || dyUnconsumed != 0) {
                Log.d(TAG, "dispatchNestedPreScroll enter");
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    startX = mView.getOffsetX();
                    startY = mView.getOffsetY();
                }
                int dx= dxUnconsumed;
                int dy=dyUnconsumed;
                if (mFooterEnable)
                    for (IHeaderFooter headerFooter : mFooters) {
                        clear(consumed);
                        headerFooter.onNestedPreScroll(mView, dx, dy, consumed, byUser);
                        dx -= consumed[0];
                        dy -= consumed[1];
                        if (dx == 0 && dy == 0)
                            break;
                    }
                if (mHeaderEnable)
                    for (IHeaderFooter headerFooter : mHeaders) {
                        clear(consumed);
                        headerFooter.onNestedPreScroll(mView, dx, dy, consumed, byUser);
                        dx -= consumed[0];
                        dy -= consumed[1];
                        if (dx == 0 && dy == 0)
                            break;
                    }
                consumed[0]=dxUnconsumed-dx;
                consumed[1]=dyUnconsumed-dy;
                nestedScroll(consumed);
                Log.d(TAG, "dispatchNestedPreScroll ="+Arrays.toString(consumed));
                if (offsetInWindow != null) {
                    offsetInWindow[0] = mView.getOffsetX() - startX;
                    offsetInWindow[1] = mView.getOffsetY() - startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                // No motion, no dispatch. Keep offsetInWindow up to date.
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public void nestedScroll(int... consumed) {
        if (consumed[0] == 0 && consumed[1] == 0)
            return;
        mNestedScrollConsumed[0] += consumed[0];
        mNestedScrollConsumed[1] += consumed[1];
        int offset = 0;
        for (IHeaderFooter header : mHeaders) {
            offset = header.getDisplayOffset(mView, mNestedScrollConsumed[0], mNestedScrollConsumed[1]);
            if (offset != 0)
                break;
        }
        if (offset == 0)
            for (IHeaderFooter header : mFooters) {
                offset = header.getDisplayOffset(mView, mNestedScrollConsumed[0], mNestedScrollConsumed[1]);
                if (offset != 0)
                    break;
            }
        mView.onPulled(offset, offset);
    }

    public int getNestedScrollX() {
        return mNestedScrollConsumed[0];
    }

    public int getNestedScrolledY() {
        return mNestedScrollConsumed[1];
    }
}
