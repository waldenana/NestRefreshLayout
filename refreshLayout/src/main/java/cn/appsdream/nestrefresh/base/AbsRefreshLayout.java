/**
 * ClassName : AbsOverScroll</br>
 * <p/>
 */
package cn.appsdream.nestrefresh.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Scroller;

import com.github.anzewei.pagelist.R;
import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;
import cn.appsdream.nestrefresh.normalstyle.NestHeader;


/**
 * 分页加载基类
 *
 * @author zewei
 */
public abstract class AbsRefreshLayout extends ViewGroup implements NestedScrollingChild, NestedScrollingParent {


    private final int[] mParentOffsetInWindow = new int[2];
    private final int[] mParentScrollConsumed = new int[2];
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private boolean mNestedScrollInProgress;

    public interface LoaderDecor {

        /**
         * 下拉可以刷新
         */
        int STATE_NORMAL = 0;
        /**
         * 显示松开可以刷新
         */
        int STATE_READY = 1;
        /**
         * 刷新中
         */
        int STATE_REFRESHING = 2;

        /**
         * 加载全部
         */
        int STATE_ALL = 4;
        /**
         * 刷新成功
         */
        int STATE_SUCCESS = 5;

        /**
         * 滑动时调用
         *
         * @param y 下拉的距离
         */
        void scrollRate(int y);

        /**
         * 设置状态
         *
         * @param state {@link #STATE_NORMAL},{@link #STATE_READY},{@link #STATE_REFRESHING}
         */
        void setState(int state);
    }

    //region field
    protected static final String TAG = AbsRefreshLayout.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;

    protected static final int DROP_CIRCLE_ANIMATOR_DURATION = 200;
    private boolean mbLayout;
    private Scroller mScroller;
    protected View mEmptyView;
    protected View mTargetView;
    // -- header view
    protected View mHeaderView;
    private boolean mbRefreshing;
    private boolean mbRefreshingInvoked = false;
    /**
     * 下拉刷新
     */
    private boolean mEnableFresh = true;

    // Footer
    protected View mFooterView;
    private boolean mEnablePullLoad = true;
    private boolean mbLoadAll = false;
    private boolean mbLoading;
    private boolean mbLoadingInvoked = false;

    private OnPullListener mLoadListener;

    private int mActivePointerId;
    /**
     * 第一次点击的位置
     */
    protected float mFirstTouchDownPointY;
    /**
     * 总偏移值
     */
    private int mTotalOffset;
    /**
     * 最后touch事件偏移值
     */
    private int mLastEventOffset;

    private MotionEvent mLastMoveEvent;
    //endregion

    //region Constructors

    /**
     * @param context  context
     * @param attrs    attrs
     * @param defStyle defStyle
     */
    public AbsRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, R.attr.absRefreshLayoutStyle);
    }

    /**
     * @param context
     * @param attrs
     */
    public AbsRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     */
    public AbsRefreshLayout(Context context) {
        this(context, null);
    }

    /**
     */
    @SuppressWarnings("deprecation")
    public AbsRefreshLayout(View listView) {
        this(listView.getContext());
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mTargetView = listView;
        ViewGroup group = (ViewGroup) mTargetView.getParent();
        if (group != null) {
            LayoutParams params = mTargetView.getLayoutParams();
            int index = group.indexOfChild(listView);
            group.removeView(listView);
            group.addView(this, index, params);
        }
        mTargetView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        super.addView(mTargetView);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.AbsRefreshLayout, defStyleAttr, 0);
        final int N = a.getIndexCount();
        int resFooter = R.layout.layout_loadmore;
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.AbsRefreshLayout_footerNestLayout) {
                resFooter = a.getResourceId(attr, resFooter);
                break;
            }
        }
        a.recycle();
        /**
         * Convert values in dp to values in px;
         */
        setFooterView(inflate(context, resFooter, null));
    }
    //endregion

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTargetView instanceof AbsListView)
                || (mTargetView != null && !ViewCompat.isNestedScrollingEnabled(mTargetView))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollInProgress = true;
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int oldScrollY = mTotalOffset;
        if (dy * oldScrollY > 0) {
            final int moveY = Math.abs(dy) > Math.abs(oldScrollY)? oldScrollY:dy;
            consumed[1] = moveY;
            if (offsetLayout(-moveY)) {
                mTotalOffset -= moveY;
            }
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        touchUp(null);
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final View target, int dxConsumed, int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {

        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        int dy = dyUnconsumed + mParentOffsetInWindow[1];
        dy/=2;
        if (dy < 0 && mEnableFresh) {
            if (offsetLayout(-dy)) {
                mTotalOffset -= dy;
            }
        } else if (dy > 0 && mEnablePullLoad) {
            if (offsetLayout(-dy)) {
                mTotalOffset -= dy;
            }
        }
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        resetView();
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    //region 设置view

    /**
     * @param headerView 下拉刷新的veiw
     */
    protected void setHeaderView(View headerView) {
        mHeaderView = headerView;
        addView(mHeaderView);
    }

    /**
     * 设置加载更多的view
     *
     * @param footerView 加载更多
     */
    protected void setFooterView(View footerView) {
        mFooterView = footerView;
        addView(mFooterView);
    }

    /**
     * Set view show when list is empty
     *
     * @param view Empty View
     */
    public void setEmptyView(View view) {
        if (mEmptyView == view)
            return;
        if (mEmptyView != null)
            removeViewInLayout(mEmptyView);
        mEmptyView = view;
        if (mEmptyView.getParent() != null) {
            ((ViewGroup) mEmptyView.getParent()).removeView(mEmptyView);
        }
        mEmptyView.setVisibility(GONE);
        if (mTargetView != null)
            addView(mEmptyView, 1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        else
            addView(mEmptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * @return The Empty view
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * @param view The content view
     */
    public void setContentView(View view) {
        mTargetView = view;
        super.addView(view);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        if (child != mHeaderView && child != mFooterView && mTargetView == null)
            mTargetView = child;
    }
    //endregion

    //region 布局

    /**
     * @see android.widget.LinearLayout#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight());
                childState = ViewCompat.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
            }
        }
        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(ViewCompat.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                ViewCompat.resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << ViewCompat.MEASURED_HEIGHT_STATE_SHIFT));

        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth());
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight());
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mEmptyView != null && mEmptyView.getVisibility() == VISIBLE) {
            mEmptyView.layout(0, 0, mEmptyView.getMeasuredWidth(), mEmptyView.getMeasuredHeight());
        }

        if (mTargetView != null) {
            mTargetView.layout(0, 0, mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight());
        }
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0 - mHeaderView.getMeasuredHeight(), mHeaderView.getMeasuredWidth(), 0);
        }
        if (mFooterView != null)
            mFooterView.layout(0, getMeasuredHeight(), mFooterView.getMeasuredWidth(), getMeasuredHeight() + mFooterView.getMeasuredHeight());

    }

    @Override
    public void requestLayout() {
        if (mbLayout)
            return;
        mbLayout = true;
        super.requestLayout();
        mbLayout = false;
    }
    //endregion

    //region 标志值设置

    /**
     * enable or disable pull down refresh feature.
     *
     * @param enable
     */
    public void setPullRefreshEnable(boolean enable) {
        mEnableFresh = enable;
        if (!mEnableFresh) {
            resetView();
        } else {
            mbRefreshing = false;
        }
    }

    /**
     * enable or disable pull up load more feature.
     *
     * @param enable
     */
    public void setPullLoadEnable(boolean enable) {
        if (mEnablePullLoad == enable)
            return;
        mEnablePullLoad = enable;
        if (!mEnablePullLoad) {
            resetView();
        } else {
            mbLoading = false;
        }
    }

    public boolean isRefreshEnable() {
        return mEnableFresh;
    }

    public boolean isLoadMoreEnable() {
        return mEnablePullLoad;
    }

    public boolean isRefreshing() {
        return mbRefreshing;
    }

    protected void setRefreshing(boolean refresh) {
        mbRefreshing = refresh;
    }

    protected void setLoading(boolean refresh) {
        mbLoading = refresh;
    }

    public boolean isLoadingMore() {
        return mbLoading;
    }

    public boolean isLoadAll() {
        return mbLoadAll;
    }

    public void showLoading() {

    }

    /**
     * 下拉刷新和加载更多的回调
     *
     * @param listener
     */
    public void setOnLoadingListener(OnPullListener listener) {
        mLoadListener = listener;
    }

    //endregion

    //region 加载更多判断
    protected boolean offsetLayout(float delY) {
        scrollBy(0, (int) -delY);
        return true;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            int oldY = getScrollY();
            scrollTo(0, y);
            mTotalOffset-=y-oldY;
            ViewCompat.postInvalidateOnAnimation(this);
        } else super.computeScroll();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t > 0 && !isLoadingMore() && isLoadMoreEnable() && !isLoadAll()) {
            if (t > mFooterView.getMeasuredHeight()) {
                ((NestRefreshLayout.LoaderDecor) mFooterView).setState(NestFooter.STATE_READY);
            } else {
                ((NestRefreshLayout.LoaderDecor) mFooterView).setState(NestFooter.STATE_NORMAL);
            }
        }
    }

    protected void touchUp(MotionEvent event) {
        if (!shouldLoadMore())
            resetView();
    }

    protected boolean shouldLoadMore() {
        if (mFooterView.getMeasuredHeight() <= getScrollY() && !isLoadAll()) {
            ((NestRefreshLayout.LoaderDecor) mFooterView).setState(NestHeader.STATE_REFRESHING);
            animation2Footer();
            return true;
        }
        return false;
    }

    protected void invokeRefresh() {
        if (mLoadListener != null && !mbRefreshingInvoked) {
            mbRefreshingInvoked = true;
            mLoadListener.onRefresh(this);
        } else if (!mbRefreshingInvoked)
            mbRefreshingInvoked = true;
    }

    protected void invokeLoadMore() {
        if (mLoadListener != null && !mbLoadingInvoked) {
            mbLoadingInvoked = true;
            mLoadListener.onLoading(this);
        } else mbLoadingInvoked = true;
    }

    protected int getOffsetY() {
        return mTotalOffset;
    }

    protected void resetView() {
        animation2Y(0);
    }

    protected int animation2Y(int y) {
        mScroller.abortAnimation();
        int duration = Math.abs(y - getScrollY());
        duration = Math.max(DROP_CIRCLE_ANIMATOR_DURATION, duration);
        mScroller.startScroll(0, getScrollY(), 0, y - getScrollY(), duration);
        ViewCompat.postInvalidateOnAnimation(this);
        return duration;
    }

    protected void animation2Footer() {
        setLoading(true);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                invokeLoadMore();
            }
        }, animation2Y(mFooterView.getMeasuredHeight()));
    }
    //endregion

    //region change pull status

    /**
     * stop load more, reset footer view.
     */
    public void onLoadFinished() {
        setRefreshing(false);
        setLoading(false);
        mbRefreshingInvoked = false;
        mbLoadingInvoked = false;
        mbLoadAll = false;
        if (mFooterView != null)
            ((NestRefreshLayout.LoaderDecor) mFooterView).setState(NestHeader.STATE_NORMAL);
        resetView();
    }

    public void onLoadAll() {
        mbRefreshingInvoked = false;
        mbLoadingInvoked = false;
        setRefreshing(false);
        setLoading(false);
        mbLoadAll = true;
        if (mFooterView != null)
            ((NestRefreshLayout.LoaderDecor) mFooterView).setState(NestHeader.STATE_ALL);
        resetView();
    }


    //endregion

    //region intercept

    /**
     * @see ViewGroup#onInterceptTouchEvent(MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        if (!isEnabled() || mNestedScrollInProgress) {
            return false;
        }
        if (!mEnableFresh && !mEnablePullLoad) {
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mFirstTouchDownPointY = getMotionEventY(event, mActivePointerId);
                mLastEventOffset = (int) mFirstTouchDownPointY;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    return false;
                }

                final float currentY = getMotionEventY(event, mActivePointerId);

                if (currentY == -1) {
                    return false;
                }

                if (mFirstTouchDownPointY == -1) {
                    mFirstTouchDownPointY = currentY;
                }
                if (mLastEventOffset == -1)
                    mLastEventOffset = (int) currentY;

                final float yDiff = currentY - mFirstTouchDownPointY;

                // State is changed to drag if over slop
                if (Math.abs(yDiff) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    return shouldIntercept((int) yDiff);
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;
        }

        return false;
    }

    private boolean shouldIntercept(int dis) {
        if (dis == 0) {
            return false;
        }
        if (!mEnableFresh && !mEnablePullLoad) {
            return false;
        }
        dis = -dis;
        return !canScrollVertically(dis) && (dis < 0 ? mEnableFresh : mEnablePullLoad);
    }

    @Override
    public boolean canScrollVertically(int direction) {
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

    private float getMotionEventY(@NonNull MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (!isEnabled() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(event);
        final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                releaseEvent();
                mLastMoveEvent = MotionEvent.obtain(event);
                return pointerIndex >= 0 && onMoveTouchEvent(event, pointerIndex);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    return false;
                }
                touchUp(event);
                mLastEventOffset = -1;
                mFirstTouchDownPointY = -1;
                mActivePointerId = INVALID_POINTER_ID;
                return false;
        }
        return true;
    }

    private void releaseEvent() {
        if (mLastMoveEvent != null) {
            mLastMoveEvent.recycle();
            mLastMoveEvent = null;
        }
    }

    private boolean onMoveTouchEvent(@NonNull MotionEvent event, int pointerIndex) {
        if (IsBeingDropped()) {
            return false;
        }
        final float y = MotionEventCompat.getY(event, pointerIndex);
        float diffY = y - mLastEventOffset;
        diffY /= 2;
        if (diffY >= 0 && !isRefreshEnable() || (diffY < 0 && !isLoadMoreEnable()))
            return false;
        mLastEventOffset = (int) y;
        if (!shouldIntercept((int) (mTotalOffset + diffY))) {
            sendUpEvent();
            sendDownEvent();
            return false;
        }
        if (offsetLayout(diffY)) {
            mTotalOffset += diffY;
            return true;
        }
        return false;
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEvent(e);
    }

    private void sendUpEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_UP, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEvent(e);
    }

    protected boolean IsBeingDropped() {
        return false;
    }

    //endregion

    /**
     * @see ViewGroup#generateDefaultLayoutParams()
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }


}
