package cn.appsdream.nestrefresh2;

import android.view.View;

/**
 * Created by 58 on 2016/11/30.
 */

public interface IHeaderFooter {
    void onNestedScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser);
    void onNestedPreScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser);
    void onParentScrolled(RefreshLayout parent, int dx, int dy);

    void layoutView(RefreshLayout parent,int left,int top,int right,int bottom);
    void onStopLoad(RefreshLayout parent);

    void onLoadAll(RefreshLayout parent);

    void onStartLoad(RefreshLayout parent);

    View getView(RefreshLayout parent);

    int getStartHeight();

    boolean shouldStartLoad();

    int getHeaderType();
}
