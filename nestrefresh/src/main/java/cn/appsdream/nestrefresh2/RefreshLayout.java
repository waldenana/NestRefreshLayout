package cn.appsdream.nestrefresh2;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;


import cn.appsdream.nestrefresh2.widget.NestHorizontalFooter;
import cn.appsdream.nestrefresh2.widget.NestHorizontalHeader;
import cn.appsdream.nestrefresh2.widget.NestVerticalFooter;
import cn.appsdream.nestrefresh2.widget.NestVerticalHeader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


/**
 * Created by 58 on 2016/11/30.
 */

public class RefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    //region field
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE =
            new Class[]{Context.class, AttributeSet.class, int.class};
    private static final String TAG = "RefreshLayout";
    private NestHelper mNestHelper;

    private View mTagetView;
    private ViewOffsetHelper mViewOffsetHelper;
    private int mLastTouchX;
    private int mLastTouchY;
    private int mTouchSlop;
    private int mScrollPointerId;
    public static final int INVALID_POINTER_ID = -1;
    private NestedScrollingChildHelper mScrollingChildHelper;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private boolean canScrollHorizontally = false;
    private boolean canScrollVertically = true;
    private boolean isLoading = false;
    private final int[] mScrollOffset = new int[2];
    private final int[] mNestScroll = new int[2];
    private final int[] mTmpTransform = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mScrollPreConsumed = new int[2];
    ArrayList<IHeaderFooter> mHeaders = new ArrayList<>();
    ArrayList<IHeaderFooter> mFooters = new ArrayList<>();

    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);
    private int mScrollState;
    private OnPullListener mOnPullListener;
    /**
     * The RecyclerView is not currently scrolling.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * The RecyclerView is currently being dragged by outside input such as user touch input.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * The RecyclerView is currently animating to a final position while not under
     * outside control.
     */
    public static final int SCROLL_STATE_SETTLING = 2;
    private MotionEvent mLastMoveEvent;
    private boolean mNestedScrollInProgress;
    private NestedHeaderHelper mHeaderHelper;
    private boolean mEnableFresh = true;
    private boolean mEnablePullLoad = true;
    private Transform mTransform = new Transform();
    //endregion

    //region RefreshLayout
    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setNestedScrollingEnabled(true);
        mHeaderHelper = new NestedHeaderHelper(this);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        if (attrs != null) {
            int defStyleRes = 0;
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout,
                    defStyleAttr, defStyleRes);
            int orientation = a.getInt(R.styleable.RefreshLayout_android_orientation, 1);
            if (orientation == 0) {
                canScrollHorizontally = true;
                canScrollVertically = false;
            } else {
                canScrollHorizontally = false;
                canScrollVertically = true;
            }
            String headerName = a.getString(R.styleable.RefreshLayout_header);
            String footerName = a.getString(R.styleable.RefreshLayout_footer);
            a.recycle();
            if (headerName == null) {
                headerName = canScrollHorizontally ? NestHorizontalHeader.class.getName() : NestVerticalHeader.class.getName();
            }
            if (footerName == null) {
                footerName = canScrollHorizontally ? NestHorizontalFooter.class.getName() : NestVerticalFooter.class.getName();
            }
            createFooter(context, footerName, attrs, defStyleAttr, defStyleRes);
            createHeader(context, headerName, attrs, defStyleAttr, defStyleRes);
        } else {
            setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }
        mNestHelper = new NestHelper();
    }
    //endregion

    //region super
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();
        int maxWidth = 0;
        int maxHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
                if (measureMatchParentChildren) {
                    if (lp.width == FrameLayout.LayoutParams.MATCH_PARENT ||
                            lp.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }
        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ensureTarget();
        int childCount = getChildCount();
        View child;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int l = 0;
            int t = 0;
            if (params.mViewType == LayoutParams.TYPE_CONTENT) {
                child.layout(mNestHelper.getOffsetX(), mNestHelper.getOffsetY(), mNestHelper.getOffsetX() + width,
                        mNestHelper.getOffsetY() + height);
            } else if (params.mViewType == LayoutParams.TYPE_HEADER) {
                IHeaderFooter headerFooter = params.mHeaderFooter;
                l = canScrollHorizontally ? mNestHelper.getOffsetX() - width : 0;
                t = canScrollVertically ? mNestHelper.getOffsetY() - height : 0;
                headerFooter.layoutView(this, l, t, l + child.getMeasuredWidth(), t + child.getMeasuredHeight());
            } else {
                IHeaderFooter headerFooter = params.mHeaderFooter;
                l = canScrollHorizontally ? mTagetView == null ? 0 : mTagetView.getMeasuredWidth() : 0;
                l += mNestHelper.getOffsetX();
                t = canScrollVertically ? mTagetView == null ? 0 : mTagetView.getMeasuredHeight() : 0;
                t += mNestHelper.getOffsetY();
                headerFooter.layoutView(this, l, t, l + width, t + height);
            }
        }
    }
    //endregion

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        mScrollState = state;
    }

    private void resetTouch() {
        IHeaderFooter headerFooter = getReleaseTop();
        if (headerFooter != null) {
            headerFooter.onStartLoad(this);
            animation2Y(headerFooter.getStartHeight(), headerFooter.getHeaderType());

        } else
            animation2Y(0, -1);
    }

    private IHeaderFooter getReleaseTop() {
        for (IHeaderFooter header : mHeaders) {
            if (header.shouldStartLoad()) {
                return header;
            }
        }
        for (IHeaderFooter footer : mFooters) {
            if (footer.shouldStartLoad())
                return footer;
        }
        return null;
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mScrollPointerId = e.getPointerId(newIndex);
            mLastTouchX = (int) (e.getX(newIndex));
            mLastTouchY = (int) (e.getY(newIndex));
        }
    }

    private void animation2Y(int y, final int headertype) {
        int start = canScrollHorizontally ? getNestedScrollX() : getNestedScrollY();
//        final boolean vertical = mNestHelper.getOffsetX() == 0;
        if (start == 0) return;
        ValueAnimatorCompat animator = ValueAnimatorCompat.createAnimator();
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setIntValues(start, y);
        animator.setDuration(Math.abs(y - start));
        animator.addUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimatorCompat animation) {
                int value = animation.getAnimatedIntValue();
                int dx = canScrollVertically ? 0 : value - getNestedScrollX();
                int dy = canScrollVertically ? value - getNestedScrollY() : 0;
                onNestedScroll(mTagetView, dx, dy, dx, dy);
            }
        });
        animator.addListener(new ValueAnimatorCompat.AnimatorListener() {
            @Override
            public void onAnimationStart(ValueAnimatorCompat animator) {

            }

            @Override
            public void onAnimationEnd(ValueAnimatorCompat animator) {
                if (headertype != -1 && !isLoading && mOnPullListener != null) {
                    mOnPullListener.onLoading(RefreshLayout.this, headertype);
                    isLoading = true;
                }
            }

            @Override
            public void onAnimationCancel(ValueAnimatorCompat animator) {

            }
        });
        animator.start();
    }

    private boolean scrollParent(int x, int y) {
        mScrollConsumed[0] = 0;
        mScrollConsumed[1] = 0;
        onNestedPreScroll(this, x, y, mScrollConsumed);
        x -= mScrollConsumed[0];
        y -= mScrollConsumed[1];
        Log.d(TAG,"scrollParent x=" +x +" y="+y);
        if (x != 0 || y != 0) {
            if(onNestedScrollInternal(this, x, y, x, y))
                return true;
            if (canScrollVertically && !shouldInterceptVertically(y + getNestedScrollY())) {
                sendUpEvent();
                sendDownEvent();
                return false;
            }
            if (canScrollHorizontally && !shouldInterceptHorizontally(x + getNestedScrollX())) {
                sendUpEvent();
                sendDownEvent();
                return false;
            }
        }
        return true;
    }

    @Override
    public void scrollBy(int x, int y) {
        mHeaderHelper.nestedScroll(x, y);
    }

    void onPulled(int dx, int dy) {

        boolean offset = false;
        mNestScroll[0] = dx;
        mNestScroll[1] = dy;
        mTmpTransform[0] = 0;
        mTmpTransform[1] = 0;
        for (IHeaderFooter header : mHeaders) {
            offset = mTransform.transformNestScroll(header.getHeaderType(), mNestScroll, mTmpTransform);
            if (offset)
                break;
        }
        if (!offset) {
            mTmpTransform[0] = 0;
            mTmpTransform[1] = 0;
            for (IHeaderFooter header : mFooters) {
                offset = mTransform.transformNestScroll(header.getHeaderType(), mNestScroll, mTmpTransform);
                if (offset)
                    break;
            }
        }
        dx = mTmpTransform[0];
        dy = mTmpTransform[1];
        if (canScrollHorizontally) {
            mNestHelper.offsetLeftAndRight(dx);
            mViewOffsetHelper.setLeftAndRightOffset(-dx);
        }
        if (canScrollVertically) {
            mNestHelper.offsetTopAndBottom(dy);
            mViewOffsetHelper.setTopAndBottomOffset(-dy);
        }
        for (IHeaderFooter headerFooter : mHeaders) {
            headerFooter.onParentScrolled(this, -dx, -dy);
        }
        for (IHeaderFooter headerFooter : mFooters) {
            headerFooter.onParentScrolled(this, -dx, -dy);
        }
    }

    public void setTransform(Transform transform) {
        mTransform = transform;
    }

    public void setEnableRefresh(boolean enableFresh) {
        mEnableFresh = enableFresh;
        mHeaderHelper.setHeaderEnable(enableFresh);
    }

    public void setEnablePullLoad(boolean enablePullLoad) {
        mEnablePullLoad = enablePullLoad;
        mHeaderHelper.setFooterEnable(enablePullLoad);
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTagetView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                LayoutParams p = (LayoutParams) child.getLayoutParams();
                if (p.mViewType == LayoutParams.TYPE_CONTENT) {
                    mTagetView = child;
                    break;
                }
            }
            if (mTagetView != null)
                mViewOffsetHelper = new ViewOffsetHelper(mTagetView);
        }
    }

    public int getOffsetY() {
        return mNestHelper.getOffsetY();
    }


    public int getOffsetX() {
        return mNestHelper.getOffsetX();
    }

    public int getNestedScrollX() {
        return mHeaderHelper.getNestedScrollX();
    }

    public int getNestedScrollY() {
        return mHeaderHelper.getNestedScrolledY();
    }

    //region header&footer
    public void addHeader(IHeaderFooter header) {
        mHeaders.add(header);
        View view = header.getView(this);
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null)
            params = generateDefaultLayoutParams();
        params.mViewType = LayoutParams.TYPE_HEADER;
        params.mHeaderFooter = header;
        addView(view, params);
    }

    public void addFooter(IHeaderFooter footer) {
        mFooters.add(footer);
        View view = footer.getView(this);
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null)
            params = generateDefaultLayoutParams();
        params.mViewType = LayoutParams.TYPE_FOOTER;
        params.mHeaderFooter = footer;
        addView(view, params);
    }

    public IHeaderFooter getHeader(int index) {
        if (index < 0 || index > mHeaders.size())
            return null;
        return mHeaders.get(index);
    }

    public IHeaderFooter getFooter(int index) {
        if (index < 0 || index > mFooters.size())
            return null;
        return mFooters.get(index);
    }

    /**
     * Instantiate and set a LayoutManager, if specified in the attributes.
     */
    private void createHeader(Context context, String className, AttributeSet attrs,
                              int defStyleAttr, int defStyleRes) {
        if (className != null) {
            className = className.trim();
            if (className.length() != 0) {  // Can't use isEmpty since it was added in API 9.
                className = getFullClassName(context, className);
                try {
                    ClassLoader classLoader;
                    if (isInEditMode()) {
                        // Stupid layoutlib cannot handle simple class loaders.
                        classLoader = this.getClass().getClassLoader();
                    } else {
                        classLoader = context.getClassLoader();
                    }
                    Class<? extends IHeaderFooter> layoutManagerClass =
                            classLoader.loadClass(className).asSubclass(IHeaderFooter.class);
                    Constructor<? extends IHeaderFooter> constructor;
                    Object[] constructorArgs = null;
                    try {
                        constructor = layoutManagerClass
                                .getConstructor(LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE);
                        constructorArgs = new Object[]{context, attrs, defStyleAttr};
                    } catch (NoSuchMethodException e) {
                        try {
                            constructor = layoutManagerClass.getConstructor();
                        } catch (NoSuchMethodException e1) {
                            e1.initCause(e);
                            throw new IllegalStateException(attrs.getPositionDescription() +
                                    ": Error creating IHeaderFooter " + className, e1);
                        }
                    }
                    constructor.setAccessible(true);
                    addHeader(constructor.newInstance(constructorArgs));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Unable to find IHeaderFooter " + className, e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Could not instantiate the IHeaderFooter: " + className, e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Could not instantiate the IHeaderFooter: " + className, e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Cannot access non-public constructor " + className, e);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Class is not a IHeaderFooter " + className, e);
                }
            }
        }
    }

    /**
     * Instantiate and set a LayoutManager, if specified in the attributes.
     */
    private void createFooter(Context context, String className, AttributeSet attrs,
                              int defStyleAttr, int defStyleRes) {
        if (className != null) {
            className = className.trim();
            if (className.length() != 0) {  // Can't use isEmpty since it was added in API 9.
                className = getFullClassName(context, className);
                try {
                    ClassLoader classLoader;
                    if (isInEditMode()) {
                        // Stupid layoutlib cannot handle simple class loaders.
                        classLoader = this.getClass().getClassLoader();
                    } else {
                        classLoader = context.getClassLoader();
                    }
                    Class<? extends IHeaderFooter> layoutManagerClass =
                            classLoader.loadClass(className).asSubclass(IHeaderFooter.class);
                    Constructor<? extends IHeaderFooter> constructor;
                    Object[] constructorArgs = null;
                    try {
                        constructor = layoutManagerClass
                                .getConstructor(LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE);
                        constructorArgs = new Object[]{context, attrs, defStyleAttr};
                    } catch (NoSuchMethodException e) {
                        try {
                            constructor = layoutManagerClass.getConstructor();
                        } catch (NoSuchMethodException e1) {
                            e1.initCause(e);
                            throw new IllegalStateException(attrs.getPositionDescription() +
                                    ": Error creating IFooter " + className, e1);
                        }
                    }
                    constructor.setAccessible(true);
                    addFooter(constructor.newInstance(constructorArgs));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Unable to find IFooter " + className, e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Could not instantiate the IFooter: " + className, e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Could not instantiate the IFooter: " + className, e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Cannot access non-public constructor " + className, e);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(attrs.getPositionDescription()
                            + ": Class is not a IFooter " + className, e);
                }
            }
        }
    }

    private String getFullClassName(Context context, String className) {
        if (className.charAt(0) == '.') {
            return context.getPackageName() + className;
        }
        if (className.contains(".")) {
            return className;
        }
        return NestVerticalHeader.class.getPackage().getName() + '.' + className;
    }
    //endregion

    // region NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
        resetTouch();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mScrollingChildHelper;
    }

    private NestedScrollingParentHelper getScrollingParentHelper() {
        if (mNestedScrollingParentHelper == null) {
            mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        }
        return mNestedScrollingParentHelper;
    }

    //endregion

    //region NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        int oritation = canScrollVertically ? ViewCompat.SCROLL_AXIS_VERTICAL : ViewCompat.SCROLL_AXIS_HORIZONTAL;
        return isEnabled() && (nestedScrollAxes & oritation) != 0;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        getScrollingParentHelper().onNestedScrollAccepted(child, target, ViewCompat.SCROLL_AXIS_VERTICAL | ViewCompat.SCROLL_AXIS_HORIZONTAL);
        // Dispatch up to the nested parent
        int oritation = canScrollVertically ? ViewCompat.SCROLL_AXIS_VERTICAL : ViewCompat.SCROLL_AXIS_HORIZONTAL;
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL | ViewCompat.SCROLL_AXIS_HORIZONTAL);
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.d(TAG, "=============== x=" + dx + " y=" + dy);
        Log.d(TAG, "=============== nx=" + getNestedScrollX() + " ny=" + getNestedScrollY());
        if (!isLoading && mHeaderHelper.dispatchNestedPreScroll(dx, dy, consumed, null, true)) {
            dx -= consumed[0];
            dy -= consumed[1];
        }
        mScrollPreConsumed[0] = 0;
        mScrollPreConsumed[1] = 0;
        Log.d(TAG, "onNestedPreScroll x=" + dx + " y=" + dy);
        if (dispatchNestedPreScroll(dx, dy, mScrollPreConsumed, mScrollOffset)) {
            consumed[0] += mScrollPreConsumed[0];
            consumed[1] += mScrollPreConsumed[1];
            mLastTouchX-=mScrollOffset[0];
            mLastTouchY-=mScrollOffset[1];
        }
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        onNestedScrollInternal(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    private boolean onNestedScrollInternal(final View target, final int dxConsumed, final int dyConsumed,
                                           int dxUnconsumed, int dyUnconsumed){
        Log.d(TAG, "---------------------");
        boolean change =false;
        if (!isLoading && mHeaderHelper.dispatchNestedPreScroll(dxUnconsumed, dyUnconsumed, mScrollConsumed, null, true)) {
            dxUnconsumed -= mScrollConsumed[0];
            dyUnconsumed -= mScrollConsumed[1];
            change=true;
        }
        if (dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mScrollOffset)) {
            dxUnconsumed += mScrollOffset[0];
            dyUnconsumed += mScrollOffset[1];
            change=true;
        }
        if (!isLoading && mHeaderHelper.dispatchNestedScroll(dxUnconsumed, dyUnconsumed, null, true))
            change=true;
        Log.d(TAG, "======end=========");
        return change;
    }
    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        stopNestedScroll();
    }

    //endregion

    /**
     * stop load more, reset footer view.
     */
    public void onLoadFinished() {
        for (IHeaderFooter header : mHeaders) {
            header.onStopLoad(this);
        }
        for (IHeaderFooter header : mFooters) {
            header.onStopLoad(this);
        }
        animation2Y(0, -1);
        isLoading = false;
    }

    public void onLoadAll() {
        for (IHeaderFooter header : mHeaders) {
            header.onLoadAll(this);
        }
        for (IHeaderFooter header : mFooters) {
            header.onLoadAll(this);
        }
        animation2Y(0, -1);
        isLoading = false;
    }


    public void setOnPullListener(OnPullListener onPullListener) {
        mOnPullListener = onPullListener;
    }

    //region intercept

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = e.getPointerId(0);
                mLastTouchX = (int) e.getX();
                mLastTouchY = (int) (e.getY());
                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
//                if (canScrollHorizontally) {
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
//                }
//                if (canScrollVertically) {
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
//                }
                startNestedScroll(nestedScrollAxis);
            }
            break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                mScrollPointerId = e.getPointerId(0);
                mLastTouchX = (int) (e.getX());
                mLastTouchY = (int) (e.getY());
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " +
                            mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                releaseEvent();
                mLastMoveEvent = MotionEvent.obtain(e);
                final int x = (int) (e.getX(index));
                final int y = (int) (e.getY(index));
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;
                mScrollOffset[0] = 0;
                mScrollOffset[1] = 0;
//                if (mScrollState != SCROLL_STATE_DRAGGING) {
//                    boolean startScroll = false;
//                    if (canScrollHorizontally && Math.abs(dx) > mTouchSlop) {
//                        if (dx > 0) {
//                            dx -= mTouchSlop;
//                        } else {
//                            dx += mTouchSlop;
//                        }
//                        startScroll = true;
//                    }
//                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
//                        if (dy > 0) {
//                            dy -= mTouchSlop;
//                        } else {
//                            dy += mTouchSlop;
//                        }
//                        startScroll = true;
//                    }
//                    if (startScroll) {
//                        setScrollState(SCROLL_STATE_DRAGGING);
//                    }
//                }
                mLastTouchX = x ;
                mLastTouchY = y;
                return scrollParent(dx, dy);
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopNestedScroll();
                break;
        }
        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTagetView instanceof AbsListView)
                || (mTagetView != null && !ViewCompat.isNestedScrollingEnabled(mTagetView))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * @see ViewGroup#onInterceptTouchEvent(MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        int pointerIndex = -1;
        if (!isEnabled() || mNestedScrollInProgress) {
            return false;
        }
        if (!mEnableFresh && !mEnablePullLoad) {
            return false;
        }
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = event.getPointerId(0);
                mLastTouchX = (int) (event.getX());
                mLastTouchY = (int) (event.getY());
                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
//                if (canScrollHorizontally) {
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
//                }
//                if (canScrollVertically) {
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
//                }
                startNestedScroll(nestedScrollAxis);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mScrollPointerId == INVALID_POINTER_ID) {
                    return false;
                }

                pointerIndex = event.findPointerIndex(mScrollPointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final int currentY = (int) event.getY(pointerIndex);
                final int currentX = (int) event.getX(pointerIndex);

                if (currentY == -1) {
                    return false;
                }

                if (mLastTouchY == -1) {
                    mLastTouchY = currentY;
                }
                if (mLastTouchX == -1) {
                    mLastTouchX = currentX;
                }

                final int yDiff = mLastTouchY - currentY;
                final int xDiff = mLastTouchX - currentX;

                mLastTouchX = currentX ;
                mLastTouchY = currentY;
                if (dispatchNestedPreScroll(xDiff, yDiff, mScrollPreConsumed, mScrollOffset)) {
                    mLastTouchX-=mScrollOffset[0];
                    mLastTouchY-=mScrollOffset[1];
                    return true;
                }
                // State is changed to drag if over slop
                if (Math.abs(yDiff) > Math.abs(xDiff))
                    return shouldInterceptVertically(yDiff);
                if (Math.abs(yDiff) < Math.abs(xDiff))
                    return shouldInterceptHorizontally(xDiff);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mScrollPointerId = INVALID_POINTER_ID;
                break;
        }

        return false;
    }

    private boolean shouldInterceptHorizontally(int xdis) {
        Log.d(TAG, "shouldInterceptHorizontally x=" + xdis);
        return canScrollHorizontally
                && (mEnableFresh || mEnablePullLoad)
                && (mEnableFresh && xdis < 0 || mEnablePullLoad && xdis > 0) && !canScrollHorizontally(xdis);

    }

    private boolean shouldInterceptVertically(int ydis) {
        return canScrollVertically
                && (mEnableFresh || mEnablePullLoad)
                && (mEnableFresh && ydis < 0 || mEnablePullLoad && ydis > 0)
                && !canScrollVertically(ydis);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTagetView instanceof AdapterView) {
                return canListScroll(direction);
            } else {
                return direction < 0 ? mTagetView.getScrollY() > 0
                        : mTagetView.getScrollY() < mTagetView
                        .getMeasuredHeight();
            }
        } else {
            return ViewCompat.canScrollVertically(mTagetView, direction);
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return direction < 0 ? mTagetView.getScrollX() > 0
                    : (mTagetView.getScrollX() < mTagetView
                    .getMeasuredWidth() || isChildOver(mTagetView));
        } else {
            return ViewCompat.canScrollHorizontally(mTagetView, direction);
        }
    }

    private boolean isChildOver(View parent) {
        if (parent instanceof ViewGroup) {
            int count = ((ViewGroup) parent).getChildCount();
            if (count > 0) {
                return ((ViewGroup) parent).getChildAt(count - 1).getRight() < parent.getMeasuredWidth();
            }
        }
        return false;
    }

    private boolean canListScroll(int direction) {
        AdapterView<?> absListView = (AdapterView<?>) mTagetView;
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
                        && lastView.getBottom() >= mTagetView.getHeight()) {
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

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEvent(e);
    }

    private void sendUpEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEvent(e);
    }

    private void releaseEvent() {
        if (mLastMoveEvent != null) {
            mLastMoveEvent.recycle();
            mLastMoveEvent = null;
        }
    }

    //endregion

    //region LayoutParams
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new LayoutParams(lp);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return RefreshLayout.class.getName();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }


    public static class LayoutParams extends ViewGroup.LayoutParams {

        public final static int TYPE_CONTENT = 0;
        public final static int TYPE_HEADER = 1;
        public final static int TYPE_FOOTER = 2;
        private int mViewType = TYPE_CONTENT;
        private IHeaderFooter mHeaderFooter;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
    //endregion

}
