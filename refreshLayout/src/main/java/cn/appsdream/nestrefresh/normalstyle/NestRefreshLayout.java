/**
 * ClassName : PullRefreshContainer</br>
 * <p>
 */
package cn.appsdream.nestrefresh.normalstyle;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.anzewei.pagelist.R;
import cn.appsdream.nestrefresh.base.AbsRefreshLayout;


/**
 * 分页加载,样式如下
 * header
 * ContentView
 * FooterView
 */
public class NestRefreshLayout extends AbsRefreshLayout {

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            NestRefreshLayout.super.invokeRefresh();
        }
    };

    public NestRefreshLayout(Context context) {
        this(context, null);
    }

    public NestRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.nestRefreshLayoutStyle);
    }

    public NestRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public NestRefreshLayout(View listView) {
        super(listView);
        init(listView.getContext(), null, R.attr.nestRefreshLayoutStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.NestRefreshLayout, defStyleAttr, 0);
        final int N = a.getIndexCount();
        int resHeader = R.layout.layout_refresh;
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.NestRefreshLayout_headerNestLayout) {
                resHeader = a.getResourceId(attr, resHeader);
            }
        }
        a.recycle();
        /**
         * Convert values in dp to values in px;
         */
        setHeaderView(inflate(context, resHeader, null));
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t < 0 && !isRefreshing() && isRefreshEnable()) { // 未处于刷新状态，更新箭头
            ((NestRefreshLayout.LoaderDecor) mHeaderView).scrollRate(getOffsetY());
            if (getScrollY() < -mHeaderView.getMeasuredHeight()) {
                ((NestRefreshLayout.LoaderDecor) mHeaderView).setState(NestHeader.STATE_READY);
            } else {
                ((NestRefreshLayout.LoaderDecor) mHeaderView).setState(NestHeader.STATE_NORMAL);
            }
        }
    }

    @Override
    protected void touchUp(MotionEvent event) {
        if (getScrollY() < -mHeaderView.getMeasuredHeight()) {// 下拉
            invokeRefresh();
        } else
            super.touchUp(event);
    }

    @Override
    protected void invokeRefresh() {
        ((NestRefreshLayout.LoaderDecor) mHeaderView).setState(NestHeader.STATE_REFRESHING);
        animation2Header();
    }

    private void animation2Header() {
        setRefreshing(true);
        removeCallbacks(runnable);
        postDelayed(runnable, animation2Y(-mHeaderView.getMeasuredHeight()));
    }

}
