package com.github.anzewei.nestrefresh.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.anzewei.nestrefresh.IHeaderFooter;
import com.github.anzewei.nestrefresh.R;
import com.github.anzewei.nestrefresh.RefreshLayout;
import com.github.anzewei.nestrefresh.ViewOffsetHelper;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class CommonFooter extends RelativeLayout implements IHeaderFooter {

    private View mProgressBar;
    private TextView mHintView;

    private ViewOffsetHelper mOffsetHelper;
    /**
     * 下拉可以刷新
     */
    final int STATE_NORMAL = 0;
    /**
     * 显示松开可以刷新
     */
    final int STATE_READY = 1;
    /**
     * 刷新中
     */
    final int STATE_REFRESHING = 2;

    /**
     * 加载全部
     */
    final int STATE_ALL = 4;
    private int mStatus;

    public CommonFooter(Context context) {
        super(context);
        initView(context);
    }

    public CommonFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CommonFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void initView(Context context) {
        inflate(getContext(), R.layout.pull_footer, this);
        mProgressBar = findViewById(R.id.footer_progressbar);
        mHintView = (TextView) findViewById(R.id.footer_hint_textview);
        mOffsetHelper = new ViewOffsetHelper(this);
    }

    private void setState(int state) {
        if (state == mStatus)
            return;
        mStatus = state;
        if (state == STATE_READY) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_load_ready);
        } else if (state == STATE_REFRESHING) {
            mHintView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_loading);
        } else if (state == STATE_NORMAL) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_load_more);
        } else if (state == STATE_ALL) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_no_more);
        } else {
            mHintView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNestedScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        if (dy > 0) {
            consumed[1] = dy;
        }
    }

    @Override
    public void onNestedPreScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        int offsetTop = parent.getNestedScrollY();
        if (offsetTop > 0) {
            int consumedY = Math.max(-offsetTop, dy);
            consumed[1] = consumedY;
        }
    }

    @Override
    public void onParentScrolled(RefreshLayout parent, int dx, int dy) {
        mOffsetHelper.setTopAndBottomOffset(parent.getOffsetY() + parent.getMeasuredHeight());
        if (mStatus == STATE_NORMAL && -parent.getOffsetY() >= getMeasuredHeight()) {
            setState(STATE_READY);
        }else if (mStatus == STATE_READY && -parent.getOffsetY() < getMeasuredHeight()){
            setState(STATE_NORMAL);
        }
    }

    @Override
    public int getDisplayOffset(RefreshLayout parent, int dx, int dy) {
        if (dy > 0)
            return dy / 2;
        return 0;
    }

    @Override
    public void layoutView(RefreshLayout parent, int left, int top, int right, int bottom) {
        layout(left, top, right, bottom);
    }

    @Override
    public void onStopLoad(RefreshLayout parent) {
        setState(STATE_NORMAL);
    }

    @Override
    public void onLoadAll(RefreshLayout parent) {
        setState(STATE_ALL);
    }

    @Override
    public void onStartLoad(RefreshLayout parent) {
        setState(STATE_REFRESHING);
    }

    @Override
    public View getView(RefreshLayout parent) {
        RefreshLayout.LayoutParams params = (RefreshLayout.LayoutParams) getLayoutParams();
        if (params == null) {
            params = new RefreshLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
            this.setLayoutParams(params);
        }
        return this;
    }


    @Override
    public int getStartHeight() {
        return 600;
    }

    @Override
    public boolean shouldStartLoad() {
        return mStatus == STATE_READY || mStatus == STATE_REFRESHING;
    }

    @Override
    public int getHeaderType() {
        return 2;
    }

}
