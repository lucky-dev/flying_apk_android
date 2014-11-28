package com.flyingapk.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.flyingapk.R;
import com.flyingapk.utils.Tools;

public class LoginFragment extends DialogFragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private EditText etEmail;
    private EditText etPassword;
    private OnDialogLoginListener mListener;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();

        return fragment;
    }

    public LoginFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = Tools.getLayoutInflater(getActivity()).inflate(R.layout.fragment_login, null);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etPassword = (EditText) view.findViewById(R.id.et_password);

        etEmail.setText(Tools.getEmail(getActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.login, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            String email = etEmail.getText().toString();
                            String password = etPassword.getText().toString();

                            if (isValidData(email, password)) {
                                mListener.onLoginUserData(email, password);
                            }
                        }
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

    private boolean isValidData(String email, String password) {
        if (!Tools.isValidEmail(email)) {
            Tools.showToast(getActivity(), getString(R.string.error_invalid_email), Toast.LENGTH_SHORT);
            return false;
        } else if ((TextUtils.isEmpty(password)) || (password.length() < 6)) {
            Tools.showToast(getActivity(), getString(R.string.error_password_too_short), Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    public void setListener(OnDialogLoginListener listener) {
        mListener = listener;
    }

    @Override
    public String toString() {
        return TAG;
    }

    public interface OnDialogLoginListener {
        public void onLoginUserData(String email, String password);
    }

}
