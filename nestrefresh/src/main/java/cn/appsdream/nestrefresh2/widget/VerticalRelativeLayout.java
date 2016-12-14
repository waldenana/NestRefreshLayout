package cn.appsdream.nestrefresh2.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class VerticalRelativeLayout extends RelativeLayout {
    public static final int ROTATION_ANGLE_CW_90 = 90;
    public static final int ROTATION_ANGLE_CW_270 = 270;
    private int mRotationAngle = ROTATION_ANGLE_CW_90;
    public VerticalRelativeLayout(Context context) {
        this(context, null, 0);
    }

    public VerticalRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (useViewRotation()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec);

            final ViewGroup.LayoutParams lp = getLayoutParams();

            if (isInEditMode() && (lp != null) && (lp.height >= 0)) {
                setMeasuredDimension(super.getMeasuredHeight(), lp.height);
            } else {
                setMeasuredDimension(super.getMeasuredHeight(), super.getMeasuredWidth());
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (useViewRotation()) {
            super.onSizeChanged(w, h, oldw, oldh);
        } else {
            super.onSizeChanged(h, w, oldh, oldw);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (!useViewRotation()) {
            switch (mRotationAngle) {
                case ROTATION_ANGLE_CW_90:
                    canvas.rotate(90);
                    canvas.translate(0, -super.getWidth());
                    break;
                case ROTATION_ANGLE_CW_270:
                    canvas.rotate(-90);
                    canvas.translate(-super.getHeight(), 0);
                    break;
            }
        }

        super.onDraw(canvas);
    }
    boolean useViewRotation() {
        final boolean isSupportedApiLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        final boolean inEditMode = isInEditMode();
        return isSupportedApiLevel && !inEditMode;
    }

    public void setRotationAngle(int angle) {
        if (!isValidRotationAngle(angle)) {
            throw new IllegalArgumentException("Invalid angle specified :" + angle);
        }

        if (mRotationAngle == angle) {
            return;
        }

        mRotationAngle = angle;

        if (useViewRotation()) {
            VerticalContainer wrapper = getWrapper();
            if (wrapper != null) {
                wrapper.applyViewRotation();
            }
        } else {
            requestLayout();
        }
    }

    private VerticalContainer getWrapper() {
        final ViewParent parent = getParent();

        if (parent instanceof VerticalContainer) {
            return (VerticalContainer) parent;
        } else {
            return null;
        }
    }
    public int getRotationAngle() {
        return mRotationAngle;
    }

    private static boolean isValidRotationAngle(int angle) {
        return (angle == ROTATION_ANGLE_CW_90 || angle == ROTATION_ANGLE_CW_270);
    }

}

