package com.example.snake3d;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.sdsmdg.harjot.longshadows.LongShadowsImageView;

public class MainActivity extends Activity /*implements SensorEventListener*/ {

    private ActivityGL activitygl;

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        context = this;
        View OpenGL = findViewById(R.id.visualizer);
        OpenGL.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                activitygl = new ActivityGL(context, view.getWidth(), view.getHeight());
            }
        });

//        Intent intent = new Intent(".ControlActivity");/////////////////////////УБРАТЬ
//        startActivity(intent);////////////////////////////////////////////////////////УБРАТЬ
    }

    public void Go (View v)
    {
        Button but = findViewById(R.id.start);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_rotate);

        but.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(".GameActivity");
                startActivity(intent);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void Exit (View v)
    {
        Button but = findViewById(R.id.exit);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_rotate);

        but.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                System.exit(0);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void shop (View v)
    {
        Button but = findViewById(R.id.shop);

        //but.setBackground(getResources().getDrawable(R.drawable.button_start));//Yea, it's all right!

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_rotate);

        but.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(".ShopActivity");
                startActivity(intent);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    public void settings(View v)
    {
        Button but = findViewById(R.id.settings);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_rotate);

        but.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(".SettingsActivity");
                startActivity(intent);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

}
