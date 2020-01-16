package c.arp.gaitauth.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import c.arp.gaitauth.Api;
import c.arp.gaitauth.R;

public class GaitInformationFragment extends Fragment implements SensorEventListener {


    private static boolean gatherSensorData = false;
    private int index = 0;
    private int recordTime = 30;
    private double vibrationTimer = 2.0 / 3.0;

    private ProgressBar mProgressBar;
    private TextView textProgress;
    private String username;
    private StringBuilder csvData;
    private Switch saveToDownloads;
    private Button btnStartMovementTracking;
    private EditText recordTimeEditText;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private FileOutputStream mFileOutputStream;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_movement_tracker, container, false);

        recordTimeEditText = (EditText) fragmentView.findViewById(R.id.recordTimeEditText);
        mProgressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        textProgress = (TextView) fragmentView.findViewById(R.id.textProgress);
        saveToDownloads = (Switch) fragmentView.findViewById(R.id.switch_save_to_downloads);

        btnStartMovementTracking = (Button) fragmentView.findViewById(R.id.buttonStartTracking);

        btnStartMovementTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //only run if we are not gathering data already
                if (!gatherSensorData) {
                    index = 0;
                    username = ((TextView) fragmentView.findViewById(R.id.username)).getText().toString();

                    //inform user that he needs to input username before starting tracking
                    if ("".equals(username)) {
                        Toast.makeText(getActivity(), "You did not enter the username", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), getContext().getFilesDir().toString(), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    gatherSensorData = true;
                    btnStartMovementTracking.setEnabled(false);
                    btnStartMovementTracking.setAlpha(0.3f);

                    //hide keyboard after pressing the button
                    if (getActivity().getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(fragmentView.getWindowToken(), 0);
                    }

                    if(recordTimeEditText.getText().toString().equals("0")) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("username", username);
                        editor.apply();
                        return;
                    }

                    //after five seconds start gathering data
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (startCollectionSensorData()) {
                                setProgressBarAndTimer();
                            }
                        }
                    }, 5000);
                }
            }
        });

        //setup sensor manager and accelerometer and gyroscope
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //setup the edit text and progress text
        recordTimeEditText.setVisibility(View.VISIBLE);
        textProgress.setVisibility(View.GONE);


        BottomNavigationView bottomNavigationView = getActivity().getWindow().getDecorView().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.VISIBLE);

        return fragmentView;
    }

    private void setProgressBarAndTimer() {
        recordTime = Integer.parseInt(recordTimeEditText.getText().toString());
        textProgress.setVisibility(View.VISIBLE);
        recordTimeEditText.setVisibility(View.GONE);
        vibrationTimer = 2.0 / 3.0;

        new CountDownTimer(recordTime * 1000, 1000) {

            @Override
            public void onTick(long l) {
                int remainingSeconds = (int) l / 1000;
                mProgressBar.setProgress(remainingSeconds);
                textProgress.setText(String.valueOf(remainingSeconds));
                //vibrateOnThirds(remainingSeconds);
            }

            @Override
            public void onFinish() {
                btnStartMovementTracking.setEnabled(true);
                btnStartMovementTracking.setAlpha(1f);
                Toast.makeText(getActivity(), "Finished gathering data", Toast.LENGTH_SHORT).show();
                mProgressBar.setProgress(0);
                textProgress.setText("0");
                //vibrate(5000);
                firstRunCompleted();
                stopCollectionSensorData();
            }
        }.start();
    }

    private void vibrateOnThirds(int remainingSeconds) {
        if (remainingSeconds < vibrationTimer * recordTime) {
            vibrate(500);
            vibrationTimer -= 1.0 / 3.0;
        }
    }

    private void vibrate(int timeToVibrate) {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(timeToVibrate, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(timeToVibrate);
        }
    }

    private void firstRunCompleted() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (sharedPref.getBoolean(getString(R.string.first_run), true)) {
            editor.putBoolean(getString(R.string.first_run), false);
        }
        editor.putString("username", username);
        editor.apply();
    }

    private boolean startCollectionSensorData() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //If it isn't mounted - we can't write into it.
            Toast.makeText(getActivity(), "There is no external storage detected", Toast.LENGTH_SHORT).show();
            return false;
        }

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);

        File path;
        if (saveToDownloads.isChecked()) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        }

        File file = new File(path, username + ".raw.csv");

        csvData = new StringBuilder();
        csvData.append(",timestamp,accX,accY,accZ,username\n");
        try {
            file.createNewFile();
            mFileOutputStream = new FileOutputStream(file, false);
            mFileOutputStream.write(",timestamp,accX,accY,accZ,username\n".getBytes());
            mFileOutputStream.flush();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "There was a problem writing to file", Toast.LENGTH_SHORT).show();
            stopCollectionSensorData();
            return false;
        }
        return true;
    }

    private void stopCollectionSensorData() {
        gatherSensorData = false;
        recordTimeEditText.setVisibility(View.VISIBLE);
        textProgress.setVisibility(View.GONE);

        try {
            mSensorManager.unregisterListener(this);
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
        } catch (Exception e) {
            mFileOutputStream = null;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Api.sendRecord(username, timeStamp, csvData.toString(), getActivity());

        csvData = new StringBuilder();
    }


    @Override
    public void onPause() {
        super.onPause();

        //unregister all the sensor before pausing fragment/activity
        mSensorManager.unregisterListener(this);

        if (gatherSensorData) {
            stopCollectionSensorData();
        }
        mSensorManager.unregisterListener(this);

        //try to close the file writer if it is still open
        try {
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
        } catch (IOException e) {
            mFileOutputStream = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long timeStamp = (new Date()).getTime() + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        String row = String.format(Locale.US, "%d,%d,%f,%f,%f,%s\n", index, timeStamp, x, y, z, username);
        csvData.append(row);
        index++;
        try {
            mFileOutputStream.write(row.getBytes());
        } catch (Exception e) {
            Toast.makeText(getActivity(), "There was a problem writing the sensor data to the file", Toast.LENGTH_SHORT).show();
            stopCollectionSensorData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing in this case
    }
}