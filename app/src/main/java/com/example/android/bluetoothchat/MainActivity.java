/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.bluetoothchat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p/>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase
        implements BluetoothChatFragment.ActivityToFragment, SensorEventListener {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    private SeekBar seekBar;
    private TextView textView;
    BluetoothChatFragment fragment;
    private SensorManager sm;
    private Sensor sensorAcc;

    private Button btnBreak;
    private Button btnAccelerate;

    Timer timer;
    TimerTask timerTask;
    double slider;
    double speed;
    double angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speed = 0;

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        textView = (TextView) findViewById(R.id.tv);
        seekBar = (SeekBar) findViewById(R.id.slider);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //textView.setText(Integer.toString(progress));
                //sendToFragment((double) progress, 0.0, 0.0, 0.0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAcc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        btnBreak = (Button) findViewById(R.id.btnBreak);
        btnAccelerate = (Button) findViewById(R.id.btnAccelerate);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sm.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_UI);
        startTimer();
    }

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 0, 1000);
    }

    public void stopTimerTask(View v) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (btnBreak.isPressed()) {
                            if (speed > 0) {
                                speed -= 1;
                            }
                        }
                        if (btnAccelerate.isPressed()){
                            speed += 1;
                        }
                        textView.setText(Double.toString(speed));
                        sendToFragment(speed,angle);
                    }
                });
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        sm.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void sendToFragment(double speed, double angle) {
        if (fragment == null) {
            fragment = new BluetoothChatFragment();
        }
        fragment.getMessage(speed,angle);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(sensorAcc)) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            angle = Math.toDegrees(Math.atan((y)/(-x)));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
