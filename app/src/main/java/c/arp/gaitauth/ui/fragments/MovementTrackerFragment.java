package c.arp.gaitauth.ui.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import c.arp.gaitauth.R;

public class MovementTrackerFragment extends Fragment implements SensorEventListener {

    private static boolean gatherSensorData = false;

    private ProgressBar mProgressBar;
    private TextView textProgress;
    private String username;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGyroscope;

    private FileOutputStream mFileOutputStream;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_movement_tracker, container, false);

        mProgressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        textProgress = (TextView) fragmentView.findViewById(R.id.textProgress);

        Button btnStartMovementTracking = (Button) fragmentView.findViewById(R.id.buttonStartTracking);
        btnStartMovementTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //only run if we are not gathering data already
                if(!gatherSensorData) {

                    username = ((TextView)fragmentView.findViewById(R.id.username)).getText().toString();

                    //inform user that he needs to input username before starting tracking
                    if("".equals(username)) {
                        Toast.makeText(getActivity(), "You did not enter the username", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), getContext().getFilesDir().toString(), Toast.LENGTH_SHORT).show();

                        return;
                    }

                    //hide keyboard after pressing the button
                    if (getActivity().getCurrentFocus() != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(fragmentView.getWindowToken(), 0);
                    }

                    //after five seconds start gathering data
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gatherSensorData = true;
                            startCollectionSensorData();
                            setProgressBarAndTimer();
                        }
                    }, 5000);
                }
            }
        });

        //setup sensor manager and accelerometer and gyroscope
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        return fragmentView;
    }

    private void setProgressBarAndTimer() {
        new CountDownTimer(10000, 1000) {

            @Override
            public void onTick(long l) {
                int remainingSeconds = (int) l/1000;
                mProgressBar.setProgress(remainingSeconds);
                textProgress.setText(String.valueOf(remainingSeconds));
            }

            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "Finished gathering data", Toast.LENGTH_SHORT).show();
                mProgressBar.setProgress(0);
                textProgress.setText("0");
                stopCollectionSensorData();
            }
        }.start();
    }

    private void startCollectionSensorData() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //If it isn't mounted - we can't write into it.
            Toast.makeText(getActivity(), "There is no external storage detected", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), username + ".txt");
        try {
            file.createNewFile();
            mFileOutputStream = new FileOutputStream(file, false);
            mFileOutputStream.write("username;timestamp;accX;accY;accZ\n".getBytes());
            mFileOutputStream.flush();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "There was a problem writing to file", Toast.LENGTH_SHORT).show();
            stopCollectionSensorData();
        }
    }

    private void stopCollectionSensorData() {
        gatherSensorData = false;
        mSensorManager.unregisterListener(this);
        try {
            mFileOutputStream.close();
        } catch (IOException e) {}
    }


    @Override
    public void onPause() {
        super.onPause();

        //unregister all the sensor before pausing fragment/activity
        stopCollectionSensorData();
        mSensorManager.unregisterListener(this);

        //try to close the file writer if it is still open
        try {
            mFileOutputStream.close();
        } catch (IOException e) {}
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long timeStamp = sensorEvent.timestamp;
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        String row = String.format("%s;%d;%f;%f;%f\n", username, timeStamp, x, y, z);

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