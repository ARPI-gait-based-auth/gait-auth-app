package c.arp.gaitauth.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;
import c.arp.gaitauth.R;


public class UnlockFragment extends Fragment {
    View root;
    Button unlockWithPinButton, unlockWithGaitButton;

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
                runGaitIdentification();
            }
        });

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
        //TODO
    }
}