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

public class InfoAboutNewAppFragment extends DialogFragment {

    public static final String TAG = InfoAboutNewAppFragment.class.getSimpleName();

    private static final String VERSION_APP = "version_app";
    private static final String WHATS_NEW = "whats_new";

    private TextView tvVersionApp;
    private TextView tvWhatsNew;
    private OnDialogInfoAboutNewAppListener mListener;
    private String mVersionApp;
    private String mWhatsNew;

    public static InfoAboutNewAppFragment newInstance(String versionApp, String whatsNew) {
        InfoAboutNewAppFragment fragment = new InfoAboutNewAppFragment();

        Bundle data = new Bundle();
        data.putString(VERSION_APP, versionApp);
        data.putString(WHATS_NEW, whatsNew);
        fragment.setArguments(data);

        return fragment;
    }

    public InfoAboutNewAppFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            mVersionApp = getArguments().getString(VERSION_APP);
            mWhatsNew = getArguments().getString(WHATS_NEW);
        }

        View view = Tools.getLayoutInflater(getActivity()).inflate(R.layout.fragment_info_about_new_app, null);
        tvVersionApp = (TextView) view.findViewById(R.id.tv_version_app);
        tvWhatsNew = (TextView) view.findViewById(R.id.tv_whats_new);

        tvVersionApp.setText(String.format("Version: %s", mVersionApp));
        tvWhatsNew.setText(String.format("What's new: %s", mWhatsNew));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.update_now, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            mListener.onUpdateApp();
                        }
                        dismiss();
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
            }
        });

        return dialog;
    }

    public void setListener(OnDialogInfoAboutNewAppListener listener) {
        mListener = listener;
    }

    @Override
    public String toString() {
        return TAG;
    }

    public interface OnDialogInfoAboutNewAppListener {
        public void onUpdateApp();
    }

}
