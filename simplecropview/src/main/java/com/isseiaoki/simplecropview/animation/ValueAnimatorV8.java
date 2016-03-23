package com.isseiaoki.simplecropview.animation;

import android.os.SystemClock;
import android.view.animation.Interpolator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ValueAnimatorV8 implements SimpleValueAnimator {
    private static final int FRAME_RATE = 30;
    private static final int UPDATE_SPAN = Math.round((float) 1000 / (float) FRAME_RATE);
    private static final int DEFAULT_ANIMATION_DURATION = 150;

    private Interpolator mInterpolator;
    ScheduledExecutorService service;
    long start;
    boolean isAnimationStarted = false;
    long duration;
    private SimpleValueAnimatorListener animatorListener = new SimpleValueAnimatorListener() {
        @Override
        public void onAnimationStarted() {

        }

        @Override
        public void onAnimationUpdated(float scale) {

        }

        @Override
        public void onAnimationFinished() {

        }

    };

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - start;
            if (elapsed > duration) {
                isAnimationStarted = false;
                animatorListener.onAnimationFinished();
                service.shutdown();
                return;
            }
            float scale = Math.min(mInterpolator.getInterpolation((float) elapsed / duration), 1);
            animatorListener.onAnimationUpdated(scale);
        }
    };

    public ValueAnimatorV8(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    @Override
    public void startAnimation(long duration) {
        if (duration >= 0) {
            this.duration = duration;
        } else {
            this.duration = DEFAULT_ANIMATION_DURATION;
        }
        isAnimationStarted = true;
        animatorListener.onAnimationStarted();
        start = SystemClock.uptimeMillis();
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, UPDATE_SPAN, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancelAnimation() {
        isAnimationStarted = false;
        service.shutdown();
        animatorListener.onAnimationFinished();
    }

    @Override
    public boolean isAnimationStarted() {
        return isAnimationStarted;
    }

    @Override
    public void addAnimatorListener(SimpleValueAnimatorListener animatorListener) {
        if (animatorListener != null) this.animatorListener = animatorListener;
    }
}
