package com.github.anzewei.nestrefresh.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.anzewei.nestrefresh.IHeaderFooter;
import com.github.anzewei.nestrefresh.R;
import com.github.anzewei.nestrefresh.RefreshLayout;
import com.github.anzewei.nestrefresh.ViewOffsetHelper;

/**
 * Created by 58 on 2016/11/30.
 */

public class NestHeader extends RelativeLayout implements IHeaderFooter {

    private CircleImageView mCircleView;
//    private ProgressBar mProgressBar;

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
    int STATE_ALL = 4;
    private int mState;
    private static final float MAX_PROGRESS_ANGLE = .8f;
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private TextView mHintTextView;
    private MaterialProgressDrawable mProgress;
    private int mTotalDragDistance;

    public NestHeader(Context context) {
        this(context, null);
    }

    public NestHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.pull_header, this);
        if (isInEditMode()) {
            return;
        }
        mOffsetHelper = new ViewOffsetHelper(this);
        mCircleView = (CircleImageView) findViewById(R.id.header_arrow);
        mHintTextView = (TextView) findViewById(R.id.header_hint);
//        mProgressBar = (ProgressBar) findViewById(R.id.header_progressbar);

        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(Color.LTGRAY);
        mCircleView.setImageDrawable(mProgress);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mTotalDragDistance = (int) (DEFAULT_CIRCLE_TARGET * metrics.density);
    }

    @Override
    public void onNestedScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        int offsetTop = parent.getOffsetY();
        if (dy < 0) {
//            int divider = 400 + parent.getOffsetY();
//            int realydy = byUser ? (dy * 200 / divider) : dy;
//            consumed[1] = dy - realydy;
            consumed[1] = dy;
        }
//        else if (offsetTop > 0) {//has pull down
//            consumed[1] = Math.max(-offsetTop, dy);
//            if (byUser && mState == STATE_READY && offsetTop < getMeasuredHeight()) {
//                setState(STATE_NORMAL);
//            }
//        } else
//            consumed[1] = 0;
    }

    @Override
    public void onNestedPreScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        int offsetTop = parent.getNestedScrollY();
        if (offsetTop< 0) {
            int consumedY = Math.min(-offsetTop, dy);
            consumed[1] = consumedY;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTotalDragDistance = getMeasuredHeight();
    }

    @Override
    public void onParentScrolled(RefreshLayout parent, int dx, int dy) {
        mOffsetHelper.setTopAndBottomOffset(parent.getOffsetY() - getMeasuredHeight());
//        ViewCompat.setRotation(mCircleView,getBottom()*180/getMeasuredHeight());
        moveSppiner(parent.getOffsetY());
        if (mState == STATE_READY && parent.getOffsetY() < getMeasuredHeight()) {
            setState(STATE_NORMAL);
        }else if (mState == STATE_NORMAL && parent.getOffsetY() > getMeasuredHeight()){
            setState(STATE_READY);
        }
    }

    @Override
    public int getDisplayOffset(RefreshLayout parent, int dx, int dy) {
        if (dy < 0)
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
        setState(STATE_NORMAL);
    }

    @Override
    public void onStartLoad(RefreshLayout parent) {
        setState(STATE_REFRESHING);
    }

    private void moveSppiner(float overscrollTop) {
        if (mState == STATE_REFRESHING)
            return;
        mProgress.setAlpha(255);
        mProgress.showArrow(true);
        float originalDragPercent = overscrollTop / mTotalDragDistance;
        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
        float slingshotDist = mTotalDragDistance;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2)
                / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        float strokeStart = adjustedPercent * .8f;
        mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
        mProgress.setArrowScale(Math.min(1f, adjustedPercent));

        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
        mProgress.setProgressRotation(rotation);
    }

    private void setState(int state) {
        if (state == mState)
            return;
        switch (state) {
            case STATE_NORMAL:
//                mCircleView.setVisibility(View.VISIBLE);
//                mProgressBar.setVisibility(View.GONE);
                mCircleView.clearAnimation();
                mProgress.stop();
                mHintTextView.setText(R.string.loader_pull_load);
                break;
            case STATE_READY:
                mHintTextView.setText(R.string.loader_pull_ready);
                break;
            case STATE_REFRESHING:
                mProgress.setAlpha(255);
                mProgress.start();
                mHintTextView.setText(R.string.loader_loading);
                break;
        }
        mState = state;
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
        return -600;
    }

    @Override
    public boolean shouldStartLoad() {
        return mState == STATE_READY || mState==STATE_REFRESHING;
    }

    @Override
    public int getHeaderType() {
        return 1;
    }


}
