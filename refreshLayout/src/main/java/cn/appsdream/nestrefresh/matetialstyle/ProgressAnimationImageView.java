package cn.appsdream.nestrefresh.matetialstyle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.view.animation.Animation;
import android.widget.ImageView;

import cn.appsdream.nestrefresh.util.DisplayUtil;


/**
 * Custom view has progress drawable.
 * Some features of MaterialProgressDrawable are decorated.
 *=
 */
public class ProgressAnimationImageView extends ImageView {
    private MaterialProgressDrawable mProgress;
    private Animation.AnimationListener mListener;

    /**
     * Constructor
     * {@inheritDoc}
     */
    public ProgressAnimationImageView(Context context) {
        super(context);
        mProgress = new MaterialProgressDrawable(getContext(), this);

        if (DisplayUtil.isOver600dp(getContext())) { // Make the progress be big
            mProgress.updateSizes(MaterialProgressDrawable.LARGE);
        }
        initialize();
    }

    private void initialize() {
        setImageDrawable(null);

        mProgress.setBackgroundColor(Color.TRANSPARENT);

        setImageDrawable(mProgress);
//        setVisibility(View.GONE);
    }

    public void makeProgressTransparent() {
        mProgress.setAlpha(0xff);
    }

    public void showArrow(boolean show) {
        mProgress.showArrow(show);
    }

    public void setArrowScale(float scale) {
        mProgress.setArrowScale(scale);
    }

    public void setProgressAlpha(int alpha) {
        mProgress.setAlpha(alpha);
    }

    public void setProgressStartEndTrim(float startAngle, float endAngle) {
        mProgress.setStartEndTrim(startAngle, endAngle);
    }

    public void setProgressRotation(float rotation) {
        mProgress.setProgressRotation(rotation);
    }

    public void startProgress() {
        mProgress.start();
    }

    public void stopProgress() {
        mProgress.stop();
    }

    public void setProgressColorSchemeColors(int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    public void setProgressColorSchemeColorsFromResource(int... resources) {
        final Resources res = getResources();
        final int[] colorRes = new int[resources.length];

        for (int i = 0; i < resources.length; i++) {
            colorRes[i] = res.getColor(resources[i]);
        }
        setProgressColorSchemeColors(colorRes);
    }

    public void scaleWithKeepingAspectRatio(float scale) {
        ViewCompat.setScaleX(this, scale);
        ViewCompat.setScaleY(this, scale);
    }


    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    public void onAnimationStart() {
        super.onAnimationStart();
        if (mListener != null) {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null) {
            mListener.onAnimationEnd(getAnimation());
        }
    }

}
