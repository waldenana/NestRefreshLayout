package cn.appsdream.nestrefresh2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class NestHorizontalFooter extends VerticalContainer implements IHeaderFooter {

    private TextView mHintTextView;

    private ViewOffsetHelper mOffsetHelper;
    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;
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
    private int mMiniPull = -1;
    private String mStrNormal;
    private String mStrReady;
    private String mStrFreshing;
    private String mStrAll;

    public NestHorizontalFooter(Context context) {
        this(context, null);
    }

    public NestHorizontalFooter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestHorizontalFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }


    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(getContext(), R.layout.pull_horizontal_footer, this);
        mHintTextView = (TextView) findViewById(R.id.header_hint);
        mOffsetHelper = new ViewOffsetHelper(this);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mMiniPull = (int) (64 * metrics.density) * 2;

        mStrNormal = getResources().getString(R.string.loader_load_more);
        mStrReady = getResources().getString(R.string.loader_load_ready);
        mStrFreshing = getResources().getString(R.string.loader_loading);
        mStrAll = getResources().getString(R.string.loader_no_more);
        mCircleView = (CircleImageView) findViewById(R.id.header_arrow);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mProgress.setBackgroundColor(Color.TRANSPARENT);
        mProgress.setAlpha(255);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout,
                defStyleAttr, 0);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.RefreshLayout_footerNormal)
                mStrNormal = a.getString(attr);
            else if (attr == R.styleable.RefreshLayout_footerReady)
                mStrReady = a.getString(attr);
            else if (attr == R.styleable.RefreshLayout_footerLoading)
                mStrFreshing = a.getString(attr);
            else if (attr == R.styleable.RefreshLayout_footerLoadAll)
                mStrAll = a.getString(attr);
        }
        setColorSchemeColors(a.getColor(R.styleable.RefreshLayout_progress_color, Color.BLUE));
        a.recycle();
        mHintTextView.setText(mStrNormal);
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

    private void setState(int state) {
        if (state == mStatus)
            return;
        mStatus = state;
        if (state == STATE_READY) {
            mCircleView.setVisibility(View.INVISIBLE);
            mHintTextView.setVisibility(View.VISIBLE);
            mHintTextView.setText(mStrReady);
        } else if (state == STATE_REFRESHING) {
            mHintTextView.setVisibility(View.VISIBLE);
            mCircleView.setVisibility(View.VISIBLE);
            mProgress.start();
            mHintTextView.setText(mStrFreshing);
        } else if (state == STATE_NORMAL) {
            mProgress.stop();
            mCircleView.setVisibility(View.INVISIBLE);
            mHintTextView.setVisibility(View.VISIBLE);
            mHintTextView.setText(mStrNormal);
        } else if (state == STATE_ALL) {
            mProgress.stop();
            mCircleView.setVisibility(View.INVISIBLE);
            mHintTextView.setVisibility(View.VISIBLE);
            mHintTextView.setText(mStrAll);
        } else {
            mHintTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNestedScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        if (dx > 0) {
            consumed[0] = dx;
        }
    }

    @Override
    public void onNestedPreScroll(RefreshLayout parent, int dx, int dy, int[] consumed, boolean byUser) {
        int offsetTop = parent.getNestedScrollX();
        if (offsetTop > 0) {
            int consumedY = Math.max(-offsetTop, dx);
            consumed[0] = consumedY;
        }
    }

    @Override
    public void onParentScrolled(RefreshLayout parent, int dx, int dy) {
        mOffsetHelper.setLeftAndRightOffset(parent.getOffsetX() + parent.getMeasuredWidth());
        if (mStatus == STATE_NORMAL && parent.getNestedScrollX() >= mMiniPull) {
            setState(STATE_READY);
        } else if (mStatus == STATE_READY && parent.getNestedScrollX() < mMiniPull) {
            setState(STATE_NORMAL);
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
            params = new RefreshLayout.LayoutParams( mMiniPull / 2,ViewGroup.LayoutParams.MATCH_PARENT);
            this.setLayoutParams(params);
        }
        return this;
    }


    @Override
    public int getStartHeight() {
        return mMiniPull;
    }

    @Override
    public boolean shouldStartLoad() {
        return mStatus == STATE_READY || mStatus == STATE_REFRESHING;
    }

    @Override
    public int getHeaderType() {
        return 4;
    }

}
