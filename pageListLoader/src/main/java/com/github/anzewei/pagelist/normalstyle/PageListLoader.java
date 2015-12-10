/**
 * ClassName : PullRefreshContainer</br>
 * <p/>
 */
package com.github.anzewei.pagelist.normalstyle;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.github.anzewei.pagelist.R;
import com.github.anzewei.pagelist.base.AbsListLoader;


/**
 * 分页加载,样式如下
 * header
 * ContentView
 * FooterView
 */
public class PageListLoader extends AbsListLoader {


    public PageListLoader(Context context) {
        this(context, null);
    }

    public PageListLoader(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.pageListLoaderStyle);
    }

    public PageListLoader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public PageListLoader(View listView) {
        super(listView);
        init(listView.getContext(), null, R.attr.pageListLoaderStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PageListLoader, defStyleAttr, 0);
        final int N = a.getIndexCount();
        int resHeader = R.layout.layout_refresh;
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.PageListLoader_refreshLayout) {
                resHeader = a.getInt(attr, resHeader);
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
            ((PageListLoader.LoaderDecor) mHeaderView).scrollRate(getOffsetY());
            if (getScrollY() < -mHeaderView.getMeasuredHeight()) {
                ((PageListLoader.LoaderDecor) mHeaderView).setState(PullHeader.STATE_READY);
            } else {
                ((PageListLoader.LoaderDecor) mHeaderView).setState(PullHeader.STATE_NORMAL);
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
        ((PageListLoader.LoaderDecor) mHeaderView).setState(PullHeader.STATE_REFRESHING);
        animation2Header();
    }

    private void animation2Header() {
        setRefreshing(true);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                PageListLoader.super.invokeRefresh();
            }
        }, animation2Y(-mHeaderView.getMeasuredHeight()));
    }

}
