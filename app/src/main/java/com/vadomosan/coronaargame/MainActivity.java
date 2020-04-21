package com.vadomosan.coronaargame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final static int min = -8, max = 8;
    private static final double MIN_OPENGL_VERSION = 3.0;
    private int state = 1;
    private Scene scene;
    private SoundPool soundPool;
    private int sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            if (!checkIsSupportedDeviceOrFinish(this)) {
                Toast.makeText(getApplicationContext(), getString(R.string.not_supported), Toast.LENGTH_LONG).show();
            } else {
                FragmentAR fragment = (FragmentAR) getSupportFragmentManager().findFragmentById(R.id.fragment);
                scene = fragment.getArSceneView().getScene();
                preLoade(10);
                loadSound();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void preLoade(int count) {

        for (int i = 0; i < count; i++) {

            Random random = new Random();

            float x = min + random.nextFloat() * (max - min);
            float y = min + random.nextFloat() * (max - min);
            float z = min + random.nextFloat() * (max - min);

            if (state == 1)
                setToTheScene("model1.sfb", x, y, z);

            if (state == 2)
                setToTheScene("model2.sfb", x, y, z);

            if (state == 3)
                setToTheScene("model3.sfb", x, y, z);

            state++;

            if (state == 4)
                state = 1;

        }
    }

    private void loadSound() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attributes).build();
        sound = soundPool.load(this, R.raw.blop_sound, 1);
    }


    private void setToTheScene(String model, float x, float y, float z) {

        ModelRenderable.builder()
                .setSource(this, Uri.parse(model))
                .build()
                .thenAccept(modelRenderable -> {

                    RotatingNode node = new RotatingNode(false, 10f);
                    node.setRenderable(modelRenderable);
                    node.setParent(scene);

                    Vector3 position = new Vector3(x, y, -z - 5f);
                    Vector3 worldPosition = scene.getCamera().getWorldPosition();

                    node.setWorldPosition(position);
                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(1f, 1f, 1f), 100));
                    scene.addChild(node);

                    node.setOnTapListener((hitTestResult, motionEvent) -> {
                        scene.removeChild(node);
                        soundPool.play(sound, 1, 1, 1, 0, 1);
                        preLoade(1);
                    });

                });
    }

    public boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        String openGlVersionString =
                ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Toast.makeText(activity, getString(R.string.not_supported), Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

}
