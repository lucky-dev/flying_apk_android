package com.flyingapk.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.flyingapk.R;
import com.flyingapk.utils.Tools;

public class BuildDetailsFragment extends DialogFragment {

    public static final String TAG = BuildDetailsFragment.class.getSimpleName();

    private static final String BUNDLE_ARG_VERSION_APP = "version_app";
    private static final String BUNDLE_ARG_WHATS_NEW = "whats_new";

    private TextView tvVersionApp;
    private TextView tvWhatsNew;

    public static BuildDetailsFragment newInstance(String versionApp, String whatsNew) {
        BuildDetailsFragment fragment = new BuildDetailsFragment();

        Bundle data = new Bundle();
        data.putString(BUNDLE_ARG_VERSION_APP, versionApp);
        data.putString(BUNDLE_ARG_WHATS_NEW, whatsNew);

        fragment.setArguments(data);

        return fragment;
    }

    public BuildDetailsFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String versionApp = null;
        String whatsNew = null;
        if (getArguments() != null) {
            versionApp = getArguments().getString(BUNDLE_ARG_VERSION_APP);
            whatsNew = getArguments().getString(BUNDLE_ARG_WHATS_NEW);
        }

        View view = Tools.getLayoutInflater(getActivity()).inflate(R.layout.fragment_build_details, null);
        tvVersionApp = (TextView) view.findViewById(R.id.tv_version_app);
        tvWhatsNew = (TextView) view.findViewById(R.id.tv_whats_new);

        tvVersionApp.setText(String.format("Version: %s", versionApp));
        tvWhatsNew.setText(String.format("What's new: %s", whatsNew));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public String toString() {
        return TAG;
    }

}
