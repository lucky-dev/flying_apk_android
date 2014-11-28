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

public class RegisterFragment extends DialogFragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private OnDialogRegisterListener mListener;

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();

        return fragment;
    }

    public RegisterFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = Tools.getLayoutInflater(getActivity()).inflate(R.layout.fragment_register, null);
        etName = (EditText) view.findViewById(R.id.et_name);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etPassword = (EditText) view.findViewById(R.id.et_password);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(R.string.register, null);
        builder.setNegativeButton(R.string.cancel, null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            String name = etName.getText().toString();
                            String email = etEmail.getText().toString();
                            String password = etPassword.getText().toString();

                            if (isValidData(name, email, password)) {
                                mListener.onRegisterUserData(name, email, password);
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

    private boolean isValidData(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            Tools.showToast(getActivity(), getString(R.string.error_empty_name), Toast.LENGTH_SHORT);
            return false;
        } else if (!Tools.isValidEmail(email)) {
            Tools.showToast(getActivity(), getString(R.string.error_invalid_email), Toast.LENGTH_SHORT);
            return false;
        } else if ((TextUtils.isEmpty(password)) || (password.length() < 6)) {
            Tools.showToast(getActivity(), getString(R.string.error_password_too_short), Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    public void setListener(OnDialogRegisterListener listener) {
        mListener = listener;
    }

    @Override
    public String toString() {
        return TAG;
    }

    public interface OnDialogRegisterListener {
        public void onRegisterUserData(String name, String email, String password);
    }

}
