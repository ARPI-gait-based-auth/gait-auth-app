package c.arp.gaitauth.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import c.arp.gaitauth.R;

public class SecretPageFragment extends Fragment {
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_secret_page, container, false);

        BottomNavigationView bottomNavigationView = getActivity().getWindow().getDecorView().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.VISIBLE);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.getBoolean(getString(R.string.first_run), true)) {
            openWarningDialog();
        }

        return null;
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
}
