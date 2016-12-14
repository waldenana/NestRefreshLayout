package cn.appsdream.nestrefresh2;

import android.view.View;

/**
 * Created by 58 on 2016/11/30.
 */

public interface IFooter {
    void onPreScroll(RefreshLayout parent, int[] consumed);

    void onStopLoading(RefreshLayout parent);

    void onStartLoading(RefreshLayout parent);

    View getView(RefreshLayout parent);
}
