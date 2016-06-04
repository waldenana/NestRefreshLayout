/**
 * ClassName : MaterialPageListLoader</br>
 * <p/>
 */
package cn.appsdream.nestrefresh.matetialstyle;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.github.anzewei.pagelist.R;
import cn.appsdream.nestrefresh.base.AbsRefreshLayout;


public class MaterialRefreshLayout extends AbsRefreshLayout {

    private DecelerateInterpolator mDecelerateInterpolator;
    private WaveView mWaveView;
    private ProgressAnimationImageView mCircleView;

    private static final int DEFAULT_CIRCLE_BG_LIGHT = 0xff2196F3;
    private static final int DEFAULT_PG_LIGHT = Color.WHITE;

    private enum VERTICAL_DRAG_THRESHOLD {
        FIRST(0.1f), SECOND(0.16f + FIRST.val), THIRD(0.5f + FIRST.val);

        final float val;

        VERTICAL_DRAG_THRESHOLD(float val) {
            this.val = val;
        }
    }

    private static final int SCALE_DOWN_DURATION = 200;
    private static final float MAX_PROGRESS_ROTATION_RATE = 0.8f;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private static final float DRAGGING_WEIGHT = 0.5f;

    public MaterialRefreshLayout(Context context) {
        this(context, null);
    }

    public MaterialRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.materiaProgressBarStyle);
    }

    public MaterialRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public MaterialRefreshLayout(View listView) {
        super(listView);
        init(listView.getContext(), null, R.attr.materiaProgressBarStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        createWaveView();
        createProgressView();
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.MateriaProgressBar, defStyleAttr, 0);
        setWaveColor(a.getColor(
                R.styleable.MateriaProgressBar_mlpb_background_color, DEFAULT_CIRCLE_BG_LIGHT));

        setColorSchemeColors(a.getColor(
                R.styleable.MateriaProgressBar_mlpb_progress_color, DEFAULT_PG_LIGHT));
        a.recycle();
    }

    private void createProgressView() {
        mCircleView = new ProgressAnimationImageView(getContext());
        mCircleView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setHeaderView(mCircleView);
    }

    private void createWaveView() {
        mWaveView = new WaveView(getContext());
        mWaveView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mWaveView);
        mWaveView.setmDropListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isRefreshing()) {
                    invokeRefresh();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        if (mWaveView == mTargetView)
            mTargetView = null;
        if (child == mTargetView) {
            removeViewInLayout(mTargetView);
            super.addView(mTargetView, 0, params);
        }
    }

    @Override
    protected boolean offsetLayout(float diffY) {
        if (getOffsetY() > 0 && getScrollY() == 0) {
            offsetWave(getOffsetY());
            return true;
        } else {
            return super.offsetLayout(diffY);
        }
    }

    private void offsetWave(float diffY) {
        final float overScrollTop = diffY * DRAGGING_WEIGHT;

        final DisplayMetrics metrics = getResources().getDisplayMetrics();

        float originalDragPercent = overScrollTop / (DEFAULT_CIRCLE_TARGET * metrics.density);
        float dragPercent = Math.min(1f, originalDragPercent);
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;

        // 0f...2f
        float tensionSlingshotPercent =
                (originalDragPercent > 3f) ? 2f : (originalDragPercent > 1f) ? originalDragPercent - 1f : 0;
        float tensionPercent = (4f - tensionSlingshotPercent) * tensionSlingshotPercent / 8f;

        mCircleView.showArrow(true);
        reInitCircleView();

        if (originalDragPercent < 1f) {
            float strokeStart = adjustedPercent * .8f;
            mCircleView.setProgressStartEndTrim(0f, Math.min(MAX_PROGRESS_ROTATION_RATE, strokeStart));
            mCircleView.setArrowScale(Math.min(1f, adjustedPercent));
        }

        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
        mCircleView.setProgressRotation(rotation);
        mCircleView.setTranslationY(mWaveView.getCurrentCircleCenterY());

        float seed = diffY / Math.min(getMeasuredWidth(), getMeasuredHeight());
        float firstBounds = seed * (5f - 2 * seed) / 3.5f;
        float secondBounds = firstBounds - VERTICAL_DRAG_THRESHOLD.FIRST.val;
        float finalBounds = (firstBounds - VERTICAL_DRAG_THRESHOLD.SECOND.val) / 5;

        if (firstBounds < VERTICAL_DRAG_THRESHOLD.FIRST.val) {
            // draw a wave and not draw a circle
            onBeginPhase(firstBounds);
        } else if (firstBounds < VERTICAL_DRAG_THRESHOLD.SECOND.val) {
            // draw a circle with a wave
            onAppearPhase(firstBounds, secondBounds);
        } else if (firstBounds < VERTICAL_DRAG_THRESHOLD.THIRD.val) {
            // draw a circle with expanding a wave
            onExpandPhase(firstBounds, secondBounds, finalBounds);
        } else {
            // stop to draw a wave and drop a circle
            onDropPhase();
        }

    }

    private void onBeginPhase(float move1) {
        mWaveView.beginPhase(move1);
    }

    /**
     * 出现水滴
     *
     * @param move1
     * @param move2
     */
    private void onAppearPhase(float move1, float move2) {
        mWaveView.appearPhase(move1, move2);
    }

    private void onExpandPhase(float move1, float move2, float move3) {
        mWaveView.expandPhase(move1, move2, move3);
    }

    private void onDropPhase() {
        mWaveView.animationDropCircle();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 0);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCircleView.setTranslationY(
                        mWaveView.getCurrentCircleCenterY() + mCircleView.getHeight() / 2.f);
            }
        });
        animator.start();
        setRefreshing(true);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int top = 0;
        if (mTargetView != null) {
            mTargetView.layout(0, top, mTargetView.getMeasuredWidth(), mTargetView.getMeasuredHeight() + top);
        }
        if (mWaveView != null) {
            mWaveView.layout(0, 0, mWaveView.getMeasuredWidth(), mWaveView.getMeasuredHeight());
        }
        if (mFooterView != null)
            mFooterView.layout(0, getMeasuredHeight() + top, mFooterView.getMeasuredWidth(), getMeasuredHeight() + mFooterView.getMeasuredHeight() + top);

        final int circleWidth = mCircleView.getMeasuredWidth();
        final int circleHeight = mCircleView.getMeasuredHeight();
        mCircleView.layout((getMeasuredWidth() - circleWidth) / 2, -circleHeight, (getMeasuredWidth() + circleWidth) / 2,
                0);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isRefreshing() || isLoadingMore())
            return false;
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isRefreshing() || isLoadingMore())
            return true;
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void setRefreshing(boolean refresh) {
        if (isRefreshing() != refresh) {
            super.setRefreshing(refresh);
            if (isRefreshing()) {
                animateOffsetToCorrectPosition();
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }


    public void showLoading() {
        mWaveView.manualRefresh();
        reInitCircleView();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 0);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCircleView.setTranslationY(
                        mWaveView.getCurrentCircleCenterY() + mCircleView.getHeight() / 2.f);
            }
        });
        animator.start();
        setRefreshing(true);
    }

    /**
     * Make circle view be visible and even scale.
     */
    private void reInitCircleView() {
        if (mCircleView.getVisibility() != View.VISIBLE) {
            mCircleView.setVisibility(View.VISIBLE);
        }

        mCircleView.scaleWithKeepingAspectRatio(1f);
        mCircleView.makeProgressTransparent();
    }

    @Override
    protected void touchUp(MotionEvent event) {
        if (getScrollY() != 0) {
            super.touchUp(event);
            mWaveView.startWaveAnimation(0);
        } else if (isRefreshEnable())
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    final float diffY = event.getY() - mFirstTouchDownPointY;
                    final float waveHeightThreshold =
                            diffY * (3f - 2 * diffY / Math.min(getMeasuredWidth(), getMeasuredHeight())) / 1000f;
                    mWaveView.startWaveAnimation(waveHeightThreshold);
                case MotionEvent.ACTION_CANCEL:
                    if (!isRefreshing()) {
                        mCircleView.setProgressStartEndTrim(0f, 0f);
                        mCircleView.showArrow(false);
                        mCircleView.setVisibility(GONE);
                    }
                    break;
            }
    }

    private void animateOffsetToCorrectPosition() {
        animateOffsetToCorrectPosition(mRefreshListener);
    }

    private void animateOffsetToCorrectPosition(Animation.AnimationListener listener) {
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null)
            mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mAnimateToCorrectPosition);
    }

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
        }
    };

    public void setMaxDropHeight(int dropHeight) {
        mWaveView.setMaxDropHeight(dropHeight);
    }

    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isRefreshing()) {
                mCircleView.makeProgressTransparent();
                mCircleView.startProgress();
            } else {
                mCircleView.stopProgress();
                mCircleView.setVisibility(View.GONE);
                mCircleView.makeProgressTransparent();
                mWaveView.startDisappearCircleAnimation();
            }
        }
    };


    /**
     * @param listener {@link Animation.AnimationListener}
     */
    private void startScaleDownAnimation(Animation.AnimationListener listener) {
        Animation scaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                mCircleView.scaleWithKeepingAspectRatio(1 - interpolatedTime);
            }
        };

        scaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(scaleDownAnimation);
    }

    /**
     * @param colorResIds ColorのId達
     */
    public void setColorSchemeResources(@IdRes int... colorResIds) {
        final Resources res = getResources();
        final int[] colorRes = new int[colorResIds.length];

        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = res.getColor(colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    /**
     * @param colors セットするColor達
     */
    public void setColorSchemeColors(int... colors) {
        mCircleView.setProgressColorSchemeColors(colors);
    }

    public void setShadowRadius(int radius) {
        radius = Math.max(0, radius); // set zero if negative
        mWaveView.setShadowRadius(radius);
    }

    public void setWaveColor(int color) {
        mWaveView.setWaveColor(color);
    }

    @Override
    protected boolean IsBeingDropped() {
        return isRefreshing();
    }
}
