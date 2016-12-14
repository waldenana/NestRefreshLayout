package cn.appsdream.nestrefresh2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.appsdream.nestrefresh2.IHeaderFooter;
import cn.appsdream.nestrefresh2.R;
import cn.appsdream.nestrefresh2.RefreshLayout;
import cn.appsdream.nestrefresh2.ViewOffsetHelper;

/**
 * Created by 58 on 2016/11/30.
 */

public class NestHorizontalHeader extends VerticalContainer implements IHeaderFooter {

    private CircleImageView mCircleView;
    //    private ProgressBar mProgressBar;
    private int mMiniPull = -1;
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
    private TextView mHintTextView;
    private MaterialProgressDrawable mProgress;
    private String mStrNormal;
    private String mStrReady;
    private String mStrFreshing;

    public NestHorizontalHeader(Context context) {
        this(context, null);
    }

    public NestHorizontalHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestHorizontalHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyle) {
        inflate(context, R.layout.pull_horizontal_header, this);
        if (isInEditMode()) {
            return;
        }
        getChildSeekBar().setRotationAngle(VerticalRelativeLayout.ROTATION_ANGLE_CW_270);
        mOffsetHelper = new ViewOffsetHelper(this);
        mCircleView = (CircleImageView) findViewById(R.id.header_arrow);
        mHintTextView = (TextView) findViewById(R.id.header_hint);
//        mProgressBar = (ProgressBar) findViewById(R.id.header_progressbar);
        mStrNormal = getResources().getString(R.string.loader_pull_load);
        mStrReady = getResources().getString(R.string.loader_pull_ready);
        mStrFreshing = getResources().getString(R.string.loader_loading);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mCircleView.setImageDrawable(mProgress);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout,
                defStyle, 0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.RefreshLayout_headerNormal)
                mStrNormal = a.getString(attr);
            else if (attr == R.styleable.RefreshLayout_headerReady)
                mStrReady = a.getString(attr);
            else if (attr == R.styleable.RefreshLayout_headerLoading)
                mStrFreshing = a.getString(attr);
        }
        mHintTextView.setTextAppearance(getContext(), a.getResourceId(R.styleable.RefreshLayout_headerTextAppearance, android.R.style.TextAppearance));
        mProgress.setBackgroundColor(a.getColor(R.styleable.RefreshLayout_progress_background, Color.WHITE));
        setColorSchemeColors(a.getColor(R.styleable.RefreshLayout_progress_color, Color.BLUE));
        a.recycle();
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mMiniPull = (int) (64 * metrics.density) * 2;
    }

    /**
     * Set the color resources used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response
     * to a user swipe gesture.
     *
     * @param colorResIds
     */
    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    /**
     * Set the colors used in the progress animation. The first
     * color will also be the color of the bar that grows in response to a user
     * swipe gesture.
     *
     * @param colors
     */
    public void setColorSchemeColors(@ColorInt int... colors) {
        mProgress.setColorSchemeColors(colors);
    }


    @Override
    public void onNestedScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        if (dx < 0) {
            consumed[0] = dx;
        }
    }

    @Override
    public void onNestedPreScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        int offsetTop = parent.getNestedScrollX();
        if (offsetTop < 0) {
            int consumedY = Math.min(-offsetTop, dx);
            consumed[0] = consumedY;
        }
    }

    @Override
    public void onParentScrolled(RefreshLayout parent, int dx, int dy) {
        mOffsetHelper.setLeftAndRightOffset(parent.getOffsetX() - getMeasuredWidth());
        moveSppiner(parent.getNestedScrollX());
        if (mState == STATE_READY && -parent.getNestedScrollX() < mMiniPull) {
            setState(STATE_NORMAL);
        } else if (mState == STATE_NORMAL && -parent.getNestedScrollX() > mMiniPull) {
            setState(STATE_READY);
        }
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
        float originalDragPercent = overscrollTop / mMiniPull;
        float extraOS = Math.abs(overscrollTop) - mMiniPull;
        float slingshotDist = mMiniPull;
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
                mCircleView.clearAnimation();
                mProgress.stop();
                mHintTextView.setText(mStrNormal);
                break;
            case STATE_READY:
                mHintTextView.setText(mStrReady);
                break;
            case STATE_REFRESHING:
                mProgress.setAlpha(255);
                mProgress.start();
                mHintTextView.setText(mStrFreshing);
                break;
        }
        mState = state;
    }

    @Override
    public View getView(RefreshLayout parent) {
        RefreshLayout.LayoutParams params = (RefreshLayout.LayoutParams) getLayoutParams();
        if (params == null) {
            params = new RefreshLayout.LayoutParams(mMiniPull / 2, ViewGroup.LayoutParams.MATCH_PARENT);
            this.setLayoutParams(params);
        }
        return this;
    }

    @Override
    public int getStartHeight() {
        return -mMiniPull;
    }

    @Override
    public boolean shouldStartLoad() {
        return mState == STATE_READY || mState == STATE_REFRESHING;
    }

    @Override
    public int getHeaderType() {
        return 3;
    }


}
