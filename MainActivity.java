package com.example.wj.shakedemo;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    private ImageView mImgShake;
    private ProgressBar mProgress;

    private SensorManager sensorManager = null;
    private Vibrator vibrator = null;

    private boolean isRequest = false;

    private float lastX;
    private float lastY;
    private float lastZ;
    private long lastUpdateTime;
    private static final int SPEED_SHRESHOLD = 45;// 这个值越大需要越大的力气来摇晃手机
    private static final int UPTATE_INTERVAL_TIME = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    public void initView() {

        mImgShake = (ImageView) findViewById(R.id.shake_img);

        mProgress = (ProgressBar) findViewById(R.id.progress);

        sensorManager = (SensorManager) this
                .getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);



    }

    /**
     * 摇动手机成功后调用
     */
    private void onShake() {
        isRequest = true;
        mProgress.setVisibility(View.VISIBLE);
        Animation anim = shakeAnimation(mImgShake.getLeft());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRequest = false;
                Toast.makeText(MainActivity.this,"摇一摇了",Toast.LENGTH_SHORT).show();
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {


            }
        });

        mImgShake.startAnimation(anim);
    }
    /**
     * 位移 Translate
     */
    public static Animation getTranslateAnimation(float fromXDelta,
                                                  float toXDelta, float fromYDelta, float toYDelta,
                                                  long durationMillis) {
        TranslateAnimation translate = new TranslateAnimation(fromXDelta,
                toXDelta, fromYDelta, toYDelta);
        translate.setDuration(durationMillis);
        translate.setFillAfter(true);
        return translate;
    }

    /**
     * 震动动画
     * @param X
     * @return
     */
    public static Animation shakeAnimation(int X) {
        AnimationSet set = new AnimationSet(true);
        Animation anim1 = getTranslateAnimation(0, -200, 0, 0, 100);
        anim1.setStartOffset(100);
        set.addAnimation(anim1);
        Animation anim2 = getTranslateAnimation(-200, 400, 0, 0, 200);
        anim2.setStartOffset(300);
        set.addAnimation(anim2);
        Animation anim3 = getTranslateAnimation(400, -200, 0, 0, 200);
        anim3.setStartOffset(500);
        set.addAnimation(anim3);
        Animation anim4 = getTranslateAnimation(-200, 0, 0, 0, 100);
        anim4.setStartOffset(600);
        set.addAnimation(anim4);
        set.setFillAfter(true);
        set.setDuration(640);
        return set;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {// 注册监听器
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (sensor != null) {
                sensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_GAME);
                // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 取消监听器
        sensorManager.unregisterListener(this);
    }

    /**
     * 重力感应监听
     */

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // 传感器信息改变时执行该方法
        long currentUpdateTime = System.currentTimeMillis();
        long timeInterval = currentUpdateTime - lastUpdateTime;
        if (timeInterval < UPTATE_INTERVAL_TIME) {
            return;
        }
        lastUpdateTime = currentUpdateTime;

        float x = sensorEvent.values[0];// x轴方向的重力加速度，向右为正
        float y = sensorEvent.values[1];// y轴方向的重力加速度，向前为正
        float z = sensorEvent.values[2];// z轴方向的重力加速度，向上为正
        Log.i("Sensor", "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);

        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;

        lastX = x;
        lastY = y;
        lastZ = z;

        // 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
        // 如果不敏感请自行调低该数值,低于10的话就不行了,因为z轴上的加速度本身就已经达到10了
        double speed = (Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ) / timeInterval) * 100;
        if (speed >= SPEED_SHRESHOLD && !isRequest) {
           // mLayoutBottom.setVisibility(View.GONE);
            vibrator.vibrate(300);
            onShake();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
