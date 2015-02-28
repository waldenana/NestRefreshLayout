package com.softdream.pagelistloader;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.Scroller;
/**
 * ClassName : PageListLoader <br>
 * 功能描述： <br>
 * History <br>
 * Create User: An Zewei <br>
 * Create Date: 2013-12-31 下午1:29:19 <br>
 * Update User: <br>
 * Update Date:
 */
public class PageListLoader extends ViewGroup {

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PageListLoader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    /**
     * @param context
     * @param attrs
     */
    public PageListLoader(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.pageListLoaderStyle);
    }

    /**
     * @param context
     */
    public PageListLoader(Context context) {
        this(context, null);
    }

    private Scroller mScroller;
    private View mTargetView;

    /**
     * Constant value for touch state TOUCH_STATE_REST : no touch
     * TOUCH_STATE_SCROLLING : scrolling
     */
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    private int mTouchState = TOUCH_STATE_REST;

    /**
     * Distance in pixels a touch can wander before we think the user is
     * scrolling
     */
    private int mTouchSlop;

    /**
     * Values for saving axis of the last touch event.
     */
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastInMotionY = Integer.MAX_VALUE;
    ;

    // -- header view
    private View mHeaderView;
    private int mHeaderViewHeight;
    private boolean mPullRefreshing;
    /**
     * 下拉刷新
     */
    private boolean mEnableFresh = false;

    // Footer
    private View mFooter;
    private int mFooterHeight = 0;
    private boolean mEnablePullLoad = true;
    private boolean mPullLoading;
    private boolean mPullLoadingByTouch;

    /**
     * 下拉刷新的临界
     */
    private int mnRefreshHeight = 60;
    /**
     * 标示方向，-1向下，1向上
     */
    private int mScrollDir = 0;//
    private int mnPageSize = 10;
    private IPageListener mLoadListener;
    private int mDurationReset = 5000;

    /**
     * @param context
     */
    @SuppressWarnings("deprecation")
    public PageListLoader(View listView) {
        this(listView.getContext());
        mTargetView = listView;

        ViewGroup group = (ViewGroup) mTargetView.getParent();
        if (group != null) {
            LayoutParams params = mTargetView.getLayoutParams();
            int index = group.indexOfChild(listView);
            group.removeView(listView);
            group.addView(this, index, params);
        }
        setBackgroundDrawable(mTargetView.getBackground());
        mTargetView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        super.addView(mTargetView, 1);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, 1, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mTargetView = child;
    }

    public void setAdapterView(View adapterView) {
        mTargetView = adapterView;
        addView(adapterView, 1);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(getContext());
        context.obtainStyledAttributes(R.styleable.Theme);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PageListLoader, defStyleAttr, 0);
        final int N = a.getIndexCount();
        int resHeader = R.layout.layout_refresh;
        int resFooter = R.layout.layout_loadmore;
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.PageListLoader_refreshLayout) {
                resHeader = a.getInt(attr, resHeader);
            } else if (attr == R.styleable.PageListLoader_loadMoreLayout) {
                resFooter = a.getInt(attr, resFooter);
            }
        }
        /**
         * Convert values in dp to values in px;
         */
        mHeaderView = inflate(context, resHeader, null);
        mFooter = inflate(context, resFooter, null);
        super.addView(mHeaderView, 0);
        super.addView(mFooter);
    }

    /**
     * @see android.view.View#onSaveInstanceState()
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    /**
     * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    /**
     * 下拉刷新和加载更多的回调
     *
     * @param listener
     */
    public void setOnLoadingListener(IPageListener listener) {
        mLoadListener = listener;
    }

    /**
     * @see android.widget.LinearLayout#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int sugesswidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY);
        int sugessheightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);
        mTargetView.measure(sugesswidthMeasureSpec, sugessheightMeasureSpec);
        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
        measureChild(mFooter, widthMeasureSpec, heightMeasureSpec);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        mFooterHeight = mFooter.getMeasuredHeight();
        mnRefreshHeight = mHeaderViewHeight;
        setMeasuredDimension(mTargetView.getMeasuredWidth(),
                mTargetView.getMeasuredHeight());
        if (getScrollY() == 0) {
            resetHeader();
        }
    }

    /**
     * @see android.widget.LinearLayout#onLayout(boolean, int, int, int, int)
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mHeaderView.layout(0, 0, r - l, mHeaderViewHeight);
        mTargetView.layout(0, mHeaderView.getBottom(), getWidth(),
                mTargetView.getMeasuredHeight() + mHeaderView.getBottom());
        mFooter.layout(0, mTargetView.getBottom(), r - l,
                mTargetView.getBottom() + mFooter.getMeasuredHeight());
    }

    /**
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPullLoadingByTouch) {
            return true;
        }
        final int action = event.getAction();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastInMotionY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mLastInMotionY == Integer.MAX_VALUE) {
                    mLastInMotionY = y;
                }
                int deltaY = (int) (mLastInMotionY - y);
                if (deltaY * mScrollDir < 0) {
                    return false;
                }
                deltaY /= 2;
                scrollTo(0, mHeaderViewHeight + deltaY);
                if (mScrollDir < 0) {
                    ((LoaderDecor) mHeaderView).scrollRate(deltaY);
                    if (!mPullRefreshing && mEnableFresh) { // 未处于刷新状态，更新箭头
                        if (getScrollY() < -mnRefreshHeight) {
                            ((LoaderDecor) mHeaderView).setState(LoaderDecor.STATE_READY);
                        } else {
                            ((LoaderDecor) mHeaderView).setState(LoaderDecor.STATE_NORMAL);
                        }
                    }
                } else {
                    if (!mPullLoading && mEnablePullLoad) {
                        if (getScrollY() > mFooterHeight + mHeaderViewHeight) {
                            ((LoaderDecor) mFooter).setState(LoaderDecor.STATE_READY);
                        } else {
                            ((LoaderDecor) mFooter).setState(LoaderDecor.STATE_NORMAL);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLastInMotionY = Integer.MAX_VALUE;
                if (mScrollDir < 0) {// 下拉
                    if (getScrollY() < -mnRefreshHeight && !mPullRefreshing) {
                        startRefresh();
                    } else {
                        resetHeader();
                    }
                } else {
                    if (getScrollY() > mFooterHeight + mHeaderViewHeight
                            && !mPullLoading) {
                        startLoadMore();
                    } else {
                        resetHeader();
                    }
                }
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return true;
    }

    private void startRefresh() {
        mPullRefreshing = true;
        ((LoaderDecor) mHeaderView).setState(LoaderDecor.STATE_REFRESHING);
        if (mLoadListener != null) {
            mLoadListener.onRefresh(this);
            int dy = -getScrollY();
            scrollByWithAnim(dy);
        } else {
            resetHeader();
        }

    }

    private void startLoadMore() {
        mPullLoadingByTouch = true;
        mPullLoading = true;
        ((LoaderDecor) mFooter).setState(LoaderDecor.STATE_REFRESHING);
        if (mLoadListener != null) {
            int dy = mHeaderViewHeight + mFooterHeight - getScrollY();
            scrollByWithAnim(dy);
            mLoadListener.onLoading(this);
        } else {
            resetHeader();
        }
    }

    /**
     * enable or disable pull down refresh feature.
     *
     * @param enable
     */
    public void setRefreshEnable(boolean enable) {
        mEnableFresh = enable;
        if (!mEnableFresh) {
            resetHeader();
        } else {
            mPullRefreshing = false;
        }
    }

    /**
     * enable or disable pull up load more feature.
     *
     * @param enable
     */
    public void setLoadMoreEnable(boolean enable) {
        mEnablePullLoad = enable;
        if (!mEnablePullLoad) {
            resetHeader();
        } else {
            mPullLoading = false;
        }
    }

    /**
     * stop load more, reset footer view.
     */
    public void stopLoading() {
        setLoadMoreEnable(true);
        if (mPullLoading == true) {
            mPullLoading = false;
            ((LoaderDecor) mFooter).setState(LoaderDecor.STATE_NORMAL);
        }
        ((LoaderDecor) mHeaderView).setState(LoaderDecor.STATE_NORMAL);
        mPullRefreshing = false;
        scrollAdapter();
        mPullLoadingByTouch = false;
    }

    private void scrollAdapter() {
        if (mTargetView != null && mPullLoadingByTouch) {
            if (mTargetView instanceof AbsListView) {
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        ((AbsListView) mTargetView).smoothScrollBy(mFooterHeight, 0);
                        scrollTo(0, mHeaderViewHeight);
                    }
                }, 100);
            } else if (mTargetView instanceof ScrollView) {
                ((ScrollView) mTargetView).smoothScrollBy(0, mFooterHeight);
                resetHeader();
            }
        } else {
            resetHeader();
        }
    }

    public void onLoadAll() {
        setLoadMoreEnable(false);
        mPullLoadingByTouch = false;
        mPullRefreshing = false;
        ((LoaderDecor) mHeaderView).setState(LoaderDecor.STATE_NORMAL);
        resetHeader();
    }

    /**
     * @param direction >0 手指上划，< 0 手指下滑
     * @return
     */
    public boolean canScroll(int direction) {

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AdapterView) {
                return canListScroll(direction);
            } else {
                return direction < 0 ? mTargetView.getScrollY() > 0
                        : mTargetView.getScrollY() < mTargetView
                        .getMeasuredHeight();
            }
        } else {
            return ViewCompat.canScrollVertically(mTargetView, direction);
        }
    }

    private boolean canListScroll(int direction) {
        AdapterView<?> absListView = (AdapterView<?>) mTargetView;
        final int itemCount = absListView.getCount();
        final int childCount = absListView.getChildCount();
        final int firstPosition = absListView.getFirstVisiblePosition();
        final int lastPosition = firstPosition + childCount;

        if (itemCount == 0) {
            return false;
        }
        if (direction > 0) {
            // Are we already showing the entire last item?
            if (lastPosition >= itemCount) {
                final View lastView = absListView.getChildAt(childCount - 1);
                if (lastView != null
                        && lastView.getBottom() >= mTargetView.getHeight()) {
                    return false;
                }
            }
        } else if (direction < 0) {
            // Are we already showing the entire first item?
            if (firstPosition <= 0) {
                final View firstView = absListView.getChildAt(0);
                if (firstView != null && firstView.getTop() >= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * stop refresh, reset header view.
     */
    public void stopRefresh() {
        if (mPullRefreshing == true) {
            mPullRefreshing = false;
            ((LoaderDecor) mHeaderView).setState(LoaderDecor.STATE_NORMAL);
            scrollByWithAnim(mHeaderViewHeight - getScrollY(),
                    2 * mDurationReset);
        }
    }

    /**
     * With the horizontal scroll of the animation
     *
     * @param nDx x-axis offset
     */
    void scrollByWithAnim(int nDx) {
        scrollByWithAnim(nDx, Math.abs(nDx));
    }

    /**
     * With the horizontal scroll of the animation
     *
     * @param nDx x-axis offset
     */
    void scrollByWithAnim(int nDx, int duration) {
        if (nDx == 0) {
            return;
        }

        mScroller.startScroll(0, getScrollY(), 0, nDx, duration);
        invalidate();
    }

    public void resetHeader() {
        int dy = mHeaderViewHeight - getScrollY();
        scrollByWithAnim(dy);
    }

    /**
     * @see android.view.ViewGroup#generateDefaultLayoutParams()
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(false);
    }

    public void requestDisallowIntercept(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    /**
     * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                mLastMotionX = x;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_MOVE:
                int dis = (int) (mLastMotionY - y);
                final int xDiff = Math.abs(dis);
                if (xDiff > mTouchSlop && Math.abs(mLastMotionX - x) / Math.abs(mLastMotionY - y) < 1
                        && shouldIntercept(dis))
                    mTouchState = TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return mTouchState != TOUCH_STATE_REST;
    }

    private boolean shouldIntercept(int dis) {
        if (dis == 0) {
            return false;
        }
        if (!mEnableFresh && !mEnablePullLoad) {
            return false;
        }

        if (mPullLoadingByTouch) {
            return true;
        }
        mScrollDir = dis;
        if (!canScroll(dis)) {
            return dis < 0 ? mEnableFresh : mEnablePullLoad;
        }
        return false;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    /**
     * @param pagesize
     */
    public void setPageSize(int pagesize) {
        mnPageSize = pagesize;
    }

    public interface LoaderDecor {

        /**
         * 下拉可以刷新
         */
        public final static int STATE_NORMAL = 0;
        /**
         * 显示松开可以刷新
         */
        public final static int STATE_READY = 1;
        /**
         * 刷新中
         */
        public final static int STATE_REFRESHING = 2;

        /**
         * 滑动时调用
         *
         * @param y 下拉的距离
         */
        public void scrollRate(int y);

        /**
         * 设置状态
         *
         * @param state {@link #STATE_NORMAL},{@link #STATE_READY},{@link #STATE_REFRESHING}
         */
        public void setState(int state);
    }

}
