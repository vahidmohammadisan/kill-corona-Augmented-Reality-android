package com.vadomosan.coronaargame;

import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;

public class RotatingNode extends Node {

    @Nullable
    private ObjectAnimator orbitAnimation = null;
    private float degreesPerSecond = 90.0f;

    private final boolean clockwise;
    private final float axisTiltDeg;
    private float lastSpeedMultiplier = 1.0f;
    private float speed = 1.0f;

    public RotatingNode(boolean clockwise, float axisTiltDeg) {
        this.clockwise = clockwise;
        this.axisTiltDeg = axisTiltDeg;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        if (orbitAnimation == null) {
            return;
        }

        float speedMultiplier = 1.0f;

        if (lastSpeedMultiplier == speedMultiplier) {
            return;
        }

        if (speedMultiplier == 0.0f) {
            orbitAnimation.pause();
        } else {
            orbitAnimation.resume();

            float animatedFraction = orbitAnimation.getAnimatedFraction();
            orbitAnimation.setDuration(getAnimationDuration());
            orbitAnimation.setCurrentFraction(animatedFraction);
        }
        lastSpeedMultiplier = speedMultiplier;
    }

    public void setDegreesPerSecond(float degreesPerSecond) {
        this.degreesPerSecond = degreesPerSecond;
    }

    @Override
    public void onActivate() {
        startAnimation();
    }

    @Override
    public void onDeactivate() {
        stopAnimation();
    }

    private long getAnimationDuration() {
        return (long) (1000 * 360 / (degreesPerSecond * speed));
    }

    private void startAnimation() {
        if (orbitAnimation != null) {
            return;
        }

        orbitAnimation = createAnimator(clockwise, axisTiltDeg);
        orbitAnimation.setTarget(this);
        orbitAnimation.setDuration(getAnimationDuration());
        orbitAnimation.start();
    }

    private void stopAnimation() {
        if (orbitAnimation == null) {
            return;
        }
        orbitAnimation.cancel();
        orbitAnimation = null;
    }

    private static ObjectAnimator createAnimator(boolean clockwise, float axisTiltDeg) {

        Quaternion[] orientations = new Quaternion[4];

        Quaternion baseOrientation = Quaternion.axisAngle(new Vector3(1.0f, 0f, 0.0f), axisTiltDeg);
        for (int i = 0; i < orientations.length; i++) {
            float angle = i * 360 / (orientations.length - 1);
            if (clockwise) {
                angle = 360 - angle;
            }
            Quaternion orientation = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), angle);
            orientations[i] = Quaternion.multiply(baseOrientation, orientation);
        }

        ObjectAnimator orbitAnimation = new ObjectAnimator();

        orbitAnimation.setObjectValues((Object[]) orientations);

        orbitAnimation.setPropertyName("localRotation");

        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
    }
}
