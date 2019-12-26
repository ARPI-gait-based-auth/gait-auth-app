package c.arp.gaitauth.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.SeekBar;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import c.arp.gaitauth.R;
import c.arp.gaitauth.StaticStore;

public class MovementTrackerFragment extends Fragment implements SensorEventListener {
    public static final String API_KEY = "";

    private static boolean gatherSensorData = false;
    private int index = 0;
    int recordTime = 30;

    private ProgressBar mProgressBar;
    private TextView textProgress;
    private String username;
    private StringBuilder csvData;
    private Switch saveToDownloads;
    private Button btnStartMovementTracking;
    private SeekBar sbRecordTime;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private FileOutputStream mFileOutputStream;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_movement_tracker, container, false);

        sbRecordTime = (SeekBar) fragmentView.findViewById(R.id.sb_record_time);
        mProgressBar = (ProgressBar) fragmentView.findViewById(R.id.progressBar);
        textProgress = (TextView) fragmentView.findViewById(R.id.textProgress);
        saveToDownloads = (Switch) fragmentView.findViewById(R.id.switch_save_to_downloads);

        btnStartMovementTracking = (Button) fragmentView.findViewById(R.id.buttonStartTracking);

        sbRecordTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                recordTime = progress + 1;
                textProgress.setText(String.valueOf(recordTime));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnStartMovementTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStartMovementTracking.setEnabled(false);
                btnStartMovementTracking.setAlpha(0.3f);
                //only run if we are not gathering data already
                if (!gatherSensorData) {
                    index = 0;
                    gatherSensorData = true;
                    username = ((TextView) fragmentView.findViewById(R.id.username)).getText().toString();

                    //inform user that he needs to input username before starting tracking
                    if ("".equals(username)) {
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
                            if(startCollectionSensorData()) {
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


        BottomNavigationView bottomNavigationView = getActivity().getWindow().getDecorView().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.VISIBLE);

        return fragmentView;
    }

    private void setProgressBarAndTimer() {
        new CountDownTimer(recordTime * 1000, 1000) {

            @Override
            public void onTick(long l) {
                int remainingSeconds = (int) l / 1000;
                mProgressBar.setProgress(remainingSeconds);
                textProgress.setText(String.valueOf(remainingSeconds));
            }

            @Override
            public void onFinish() {
                btnStartMovementTracking.setEnabled(true);
                btnStartMovementTracking.setAlpha(1f);
                Toast.makeText(getActivity(), "Finished gathering data", Toast.LENGTH_SHORT).show();
                mProgressBar.setProgress(0);
                textProgress.setText("0");
                firstRunCompleted();
                stopCollectionSensorData();
            }
        }.start();
    }

    private void firstRunCompleted() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        if(!sharedPref.getBoolean(getString(R.string.first_run), true)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.first_run), false);
            editor.apply();
        }
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
        StaticStore.selectedFile = file.getAbsolutePath();
        csvData = new StringBuilder();
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

        try {
            mSensorManager.unregisterListener(this);
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
        } catch (Exception e) {
            mFileOutputStream = null;
        }

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            this.sendRecord(username, timeStamp, csvData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        csvData = new StringBuilder();
    }


    @Override
    public void onPause() {
        super.onPause();

        //unregister all the sensor before pausing fragment/activity
        mSensorManager.unregisterListener(this);

        if(gatherSensorData) {
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

    public void sendRecord(final String name, final String key, final String data) throws JSONException {
        String url = "https://gait.modri.si/" + API_KEY + "/record/" + name + "/" + key;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("csv", data);
        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response != null){
                            Toast.makeText(getActivity(), "Response OK" , Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error != null){
                            Toast.makeText(getActivity(), "POST FAIL", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ){};
        StaticStore.requstQueue.add(jsonobj);
    }
}