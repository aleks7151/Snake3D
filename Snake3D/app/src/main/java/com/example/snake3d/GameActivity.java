package com.example.snake3d;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    private OpenGLRenderer glSurfaceView;

    private SensorManager sensorManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        glSurfaceView = new OpenGLRenderer(getApplicationContext());//this
        setContentView(glSurfaceView);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            switch (accuracy) {
                case 0:
                    Log.d("sensor", "Unreliable");
                    break;
                case 1:
                    Log.d("sensor", "Low Accuracy");
                    break;
                case 2:
                    Log.d("sensor", "Medium Accuracy");
                    break;
                case 3:
                    Log.d("sensor", "High Accuracy");
                    break;
            }
        }
        public void onSensorChanged(SensorEvent event) {
            glSurfaceView.changeCam(event.values[0], event.values[1], event.values[2]);
//            Log.d("sensor", String.format("%f ", 180 / Math.PI * event.values[0]) + " " + String.format("%f ", 180 / Math.PI * event.values[1]) + " " + String.format("%f ", 180 / Math.PI * event.values[2]));
//            Log.d("sensor", String.format("%.3f ", event.values[0]) + " " + String.format("%.3f ", event.values[1]) + " " + String.format("%.3f ", event.values[2]));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor./*TYPE_GYROSCOPE*/TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        glSurfaceView.onResume();
    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }
}
