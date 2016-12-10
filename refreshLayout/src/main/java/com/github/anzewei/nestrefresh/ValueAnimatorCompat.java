package com.github.anzewei.nestrefresh;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.animation.Interpolator;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

class ValueAnimatorCompat {
    static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR
            = new ValueAnimatorCompat.Creator() {
        @Override
        public ValueAnimatorCompat createAnimator() {
            return new ValueAnimatorCompat(Build.VERSION.SDK_INT >= 12
                    ? new ValueAnimatorCompatImplHoneycombMr1()
                    : new ValueAnimatorCompatImplGingerbread());
        }
    };

    static ValueAnimatorCompat createAnimator() {
        return DEFAULT_ANIMATOR_CREATOR.createAnimator();
    }

    interface AnimatorUpdateListener {
        /**
         * <p>Notifies the occurrence of another frame of the animation.</p>
         *
         * @param animator The animation which was repeated.
         */
        void onAnimationUpdate(ValueAnimatorCompat animator);
    }

    /**
     * An animation listener receives notifications from an animation.
     * Notifications indicate animation related events, such as the end or the
     * repetition of the animation.
     */
    interface AnimatorListener {
        /**
         * <p>Notifies the start of the animation.</p>
         *
         * @param animator The started animation.
         */
        void onAnimationStart(ValueAnimatorCompat animator);
        /**
         * <p>Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animator The animation which reached its end.
         */
        void onAnimationEnd(ValueAnimatorCompat animator);
        /**
         * <p>Notifies the cancellation of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animator The animation which was canceled.
         */
        void onAnimationCancel(ValueAnimatorCompat animator);
    }

    static class AnimatorListenerAdapter implements AnimatorListener {
        @Override
        public void onAnimationStart(ValueAnimatorCompat animator) {
        }

        @Override
        public void onAnimationEnd(ValueAnimatorCompat animator) {
        }

        @Override
        public void onAnimationCancel(ValueAnimatorCompat animator) {
        }
    }

    interface Creator {
        @NonNull
        ValueAnimatorCompat createAnimator();
    }

    static abstract class Impl {
        interface AnimatorUpdateListenerProxy {
            void onAnimationUpdate();
        }

        interface AnimatorListenerProxy {
            void onAnimationStart();
            void onAnimationEnd();
            void onAnimationCancel();
        }

        abstract void start();
        abstract boolean isRunning();
        abstract void setInterpolator(Interpolator interpolator);
        abstract void addListener(AnimatorListenerProxy listener);
        abstract void addUpdateListener(AnimatorUpdateListenerProxy updateListener);
        abstract void setIntValues(int from, int to);
        abstract int getAnimatedIntValue();
        abstract void setFloatValues(float from, float to);
        abstract float getAnimatedFloatValue();
        abstract void setDuration(long duration);
        abstract void cancel();
        abstract float getAnimatedFraction();
        abstract void end();
        abstract long getDuration();
    }

    private final Impl mImpl;

    ValueAnimatorCompat(Impl impl) {
        mImpl = impl;
    }

    public void start() {
        mImpl.start();
    }

    public boolean isRunning() {
        return mImpl.isRunning();
    }

    public void setInterpolator(Interpolator interpolator) {
        mImpl.setInterpolator(interpolator);
    }

    public void addUpdateListener(final AnimatorUpdateListener updateListener) {
        if (updateListener != null) {
            mImpl.addUpdateListener(new Impl.AnimatorUpdateListenerProxy() {
                @Override
                public void onAnimationUpdate() {
                    updateListener.onAnimationUpdate(ValueAnimatorCompat.this);
                }
            });
        } else {
            mImpl.addUpdateListener(null);
        }
    }

    public void addListener(final AnimatorListener listener) {
        if (listener != null) {
            mImpl.addListener(new Impl.AnimatorListenerProxy() {
                @Override
                public void onAnimationStart() {
                    listener.onAnimationStart(ValueAnimatorCompat.this);
                }

                @Override
                public void onAnimationEnd() {
                    listener.onAnimationEnd(ValueAnimatorCompat.this);
                }

                @Override
                public void onAnimationCancel() {
                    listener.onAnimationCancel(ValueAnimatorCompat.this);
                }
            });
        } else {
            mImpl.addListener(null);
        }
    }

    public void setIntValues(int from, int to) {
        mImpl.setIntValues(from, to);
    }

    public int getAnimatedIntValue() {
        return mImpl.getAnimatedIntValue();
    }

    public void setFloatValues(float from, float to) {
        mImpl.setFloatValues(from, to);
    }

    public float getAnimatedFloatValue() {
        return mImpl.getAnimatedFloatValue();
    }

    public void setDuration(long duration) {
        mImpl.setDuration(duration);
    }

    public void cancel() {
        mImpl.cancel();
    }

    public float getAnimatedFraction() {
        return mImpl.getAnimatedFraction();
    }

    public void end() {
        mImpl.end();
    }

    public long getDuration() {
        return mImpl.getDuration();
    }
}
