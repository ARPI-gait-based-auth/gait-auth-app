package c.arp.gaitauth.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;
import c.arp.gaitauth.Api;
import c.arp.gaitauth.R;


public class UnlockFragment extends Fragment implements SensorEventListener {
    View root;
    Button unlockWithPinButton, unlockWithGaitButton;

    private static boolean gatherSensorData = false;
    private int index = 0;

    private String username;
    private StringBuilder csvData;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private FileOutputStream mFileOutputStream;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_identify_user, container, false);

        unlockWithPinButton = (Button) root.findViewById(R.id.unlockWithPin);
        unlockWithPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPinDialog();
            }
        });

        unlockWithGaitButton = (Button) root.findViewById(R.id.identifyUser);
        unlockWithGaitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = 0;
                runGaitIdentification();
            }
        });

        //setup sensor manager and accelerometer and gyroscope
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.getBoolean(getString(R.string.first_run), true)) {
            openWarningDialog();
        }
    }

    private void openPinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.modal_window_pin, null);

        builder.setView(v);
        builder.setPositiveButton(R.string.submit_action, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    if(((EditText)((Dialog) dialog).findViewById(R.id.PIN)).getText().toString().equals("111111")) {
                        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.navigation_secret_page);
                    } else {
                        openErrorDialog();
                        dialog.dismiss();
                    }
                }
            })
            .setNegativeButton(R.string.cancle_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        builder.create();
        builder.show();
    }

    private void openErrorGaitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.modal_gait_error, null);

        builder.setView(v);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    private void openErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.modal_error, null);

        builder.setView(v);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    private void openWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.modal_warning, null);

        builder.setView(v);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.navigation_tracker);
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    private void runGaitIdentification() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        username = sharedPref.getString(getString(R.string.username), "");

        startCollectionSensorData();
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
            stopCollectionSensorData(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPause() {
        super.onPause();

        //unregister all the sensor before pausing fragment/activity
        mSensorManager.unregisterListener(this);

        if (gatherSensorData) {
            stopCollectionSensorData(true);
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

    private void stopCollectionSensorData(boolean collecting) {
        gatherSensorData = false;

        try {
            mSensorManager.unregisterListener(this);
            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
        } catch (Exception e) {
            mFileOutputStream = null;
        }

        if(collecting) {
            if(!username.isEmpty()) {
                Api.authenticateRecord(username, csvData.toString(), getActivity());
            } else {
                openErrorGaitDialog();
            }
        }
        csvData = new StringBuilder();
    }

    private void startCollectionSensorData() {
        final UnlockFragment uf = this;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                File file = new File(path, username + ".raw.csv");

                csvData = new StringBuilder();
                csvData.append(",timestamp,accX,accY,accZ,username\n");
                try {
                    file.createNewFile();
                    mFileOutputStream = new FileOutputStream(file, false);
                    mFileOutputStream.write(",timestamp,accX,accY,accZ,username\n".getBytes());
                    mFileOutputStream.flush();
                    setTimer();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "There was a problem writing to file", Toast.LENGTH_SHORT).show();
                    stopCollectionSensorData(false);
                }

                mSensorManager.registerListener(uf, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }, 5000);
    }

    private void setTimer() {
        int recordTime = 10;

        new CountDownTimer(recordTime * 1000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "Finished gathering gait auth data", Toast.LENGTH_SHORT).show();
                stopCollectionSensorData(true);
            }
        }.start();
    }
}