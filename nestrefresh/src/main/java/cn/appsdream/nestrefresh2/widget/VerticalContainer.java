package cn.appsdream.nestrefresh2.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class VerticalContainer extends FrameLayout {
    public VerticalContainer(Context context) {
        this(context, null, 0);
    }

    public VerticalContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (useViewRotation()) {
            onSizeChangedUseViewRotation(w, h, oldw, oldh);
        } else {
            onSizeChangedTraditionalRotation(w, h, oldw, oldh);
        }
    }

    private void onSizeChangedTraditionalRotation(int w, int h, int oldw, int oldh) {
        final View seekBar = getChildSeekBar();

        if (seekBar != null) {
            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) seekBar.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = h;
            seekBar.setLayoutParams(lp);

            seekBar.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int seekBarWidth = seekBar.getMeasuredWidth();
            seekBar.measure(
                    MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));

            lp.gravity = Gravity.TOP | Gravity.LEFT;
            lp.leftMargin = (w - seekBarWidth) / 2;
            seekBar.setLayoutParams(lp);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void onSizeChangedUseViewRotation(int w, int h, int oldw, int oldh) {
        final VerticalRelativeLayout seekBar = getChildSeekBar();

        if (seekBar != null) {
            seekBar.measure(
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST));
        }

        applyViewRotation(w, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final VerticalRelativeLayout seekBar = getChildSeekBar();
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        if ((seekBar != null) ) {
            final int seekBarWidth;
            final int seekBarHeight;

            if (useViewRotation()) {
                measureChild(seekBar,heightMeasureSpec,widthMeasureSpec);
                seekBarWidth = seekBar.getMeasuredHeight();
                seekBarHeight = seekBar.getMeasuredWidth();
            } else {
                measureChild(seekBar,widthMeasureSpec,heightMeasureSpec);
                seekBarWidth = seekBar.getMeasuredWidth();
                seekBarHeight = seekBar.getMeasuredHeight();
            }

            final int measuredWidth = ViewCompat.resolveSizeAndState(seekBarWidth, widthMeasureSpec, 0);
            final int measuredHeight = ViewCompat.resolveSizeAndState(seekBarHeight, heightMeasureSpec, 0);

            setMeasuredDimension(measuredWidth, measuredHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    void applyViewRotation() {
        applyViewRotation(getWidth(), getHeight());
    }

    private void applyViewRotation(int w, int h) {
        final VerticalRelativeLayout seekBar = getChildSeekBar();

        if (seekBar != null) {
            final ViewGroup.LayoutParams lp = seekBar.getLayoutParams();
            final int rotationAngle = seekBar.getRotationAngle();
            final int paddingTop = seekBar.getPaddingTop();
            final int paddingBottom = seekBar.getPaddingBottom();
            final int w2 = paddingTop + paddingBottom;

            lp.width = h;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            seekBar.setLayoutParams(lp);


            ViewCompat.setRotation(seekBar, toRotationAngleToDegrees(rotationAngle));
            ViewCompat.setTranslationX(seekBar, -(h - w) * 0.5f);
            ViewCompat.setTranslationY(seekBar, Math.max(0.0f, (h - Math.max(w, w2)) * 0.5f));
        }
    }

    protected VerticalRelativeLayout getChildSeekBar() {
        final View child = (getChildCount() > 0) ? getChildAt(0) : null;
        return (VerticalRelativeLayout) child;
    }

    private boolean useViewRotation() {
        final VerticalRelativeLayout seekBar = getChildSeekBar();
        if (seekBar != null) {
            return seekBar.useViewRotation();
        } else {
            return false;
        }
    }

    private static int toRotationAngleToDegrees(int angle) {
        switch (angle) {
            case VerticalRelativeLayout.ROTATION_ANGLE_CW_90:
                return 90;
            case VerticalRelativeLayout.ROTATION_ANGLE_CW_270:
                return -90;
            default:
                return 0;
        }
    }
}

