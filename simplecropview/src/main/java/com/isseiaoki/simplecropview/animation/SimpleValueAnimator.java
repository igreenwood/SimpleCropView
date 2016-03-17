package com.isseiaoki.simplecropview.animation;

public interface SimpleValueAnimator {
    public void startAnimation(long duration);
    public void cancelAnimation();
    public boolean isAnimationStarted();
    public void addAnimatorListener(SimpleValueAnimatorListener animatorListener);
}
