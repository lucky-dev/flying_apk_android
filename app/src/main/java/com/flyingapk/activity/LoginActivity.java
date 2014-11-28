package com.flyingapk.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.flyingapk.R;
import com.flyingapk.api.ApiHelper;
import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.api.wrappers.BaseResponse;
import com.flyingapk.api.wrappers.UserAuthorizationResponse;
import com.flyingapk.fragments.LoginFragment;
import com.flyingapk.fragments.RegisterFragment;
import com.flyingapk.utils.Tools;

import java.util.List;

import static com.flyingapk.fragments.LoginFragment.OnDialogLoginListener;

public class LoginActivity extends ActionBarActivity
        implements ApiHelper.ApiCallback, OnDialogLoginListener, RegisterFragment.OnDialogRegisterListener {

    private Button btnLogin;
    private Button btnRegister;
    private ApiHelper mApiHelper;
    private ProgressDialog mProgressDialog;
    private LoginFragment mLoginDialog;
    private RegisterFragment mRegisterDialog;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Tools.getAccessToken(this) != null) {
            Tools.navigateUpTo(this, new Intent(this, ListAppsActivity.class));
            return;
        }

        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        mApiHelper = new ApiHelper(getApplicationContext());
        mApiHelper.onCreate();

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(mClickOnLogin);

        btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(mClickOnRegister);
    }

    @Override
    public void onStart() {
        super.onStart();

        mApiHelper.addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mApiHelper.removeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mApiHelper != null) {
            mApiHelper.onDestroy();
        }
    }

    @Override
    public void onLoginUserData(String email, String password) {
        mEmail = email;
        mApiHelper.login(email, password);
    }

    @Override
    public void onRegisterUserData(String name, String email, String password) {
        mEmail = email;
        mApiHelper.register(name, email, password);
    }

    private View.OnClickListener mClickOnLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLoginDialog = LoginFragment.newInstance();
            mLoginDialog.setListener(LoginActivity.this);
            mLoginDialog.show(getSupportFragmentManager(), LoginFragment.TAG);
        }
    };

    private View.OnClickListener mClickOnRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mRegisterDialog = RegisterFragment.newInstance();
            mRegisterDialog.setListener(LoginActivity.this);
            mRegisterDialog.show(getSupportFragmentManager(), RegisterFragment.TAG);
        }
    };

    @Override
    public void onStart(int code, String tag) {

        if ((code == MapApiFunctions.Request.Command.LOGIN) ||
            (code == MapApiFunctions.Request.Command.REGISTER)) {
            if (mProgressDialog == null) {
                mProgressDialog = Tools.getIndeterminateProgressDialog(this);
            }
        }

    }

    @Override
    public void onFinish(int code, BaseResponse struct, String tag) {

        if ((code == MapApiFunctions.Response.Command.LOGIN) ||
            (code == MapApiFunctions.Response.Command.REGISTER)) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }

            UserAuthorizationResponse result = (UserAuthorizationResponse) struct;

            if (result.getCode() == 200) {
                if (mLoginDialog != null) {
                    mLoginDialog.dismiss();
                    mLoginDialog = null;
                }

                if (mRegisterDialog != null) {
                    mRegisterDialog.dismiss();
                    mRegisterDialog = null;
                }

                Tools.saveEmail(LoginActivity.this, mEmail);

                Tools.navigateUpTo(this, new Intent(this, ListAppsActivity.class));
            } else {
                List<String> listErrors = result.getErrors();
                StringBuffer errors = new StringBuffer();
                for (int i = 0; i < listErrors.size(); i++) {
                    errors.append(listErrors.get(i));

                    if ((i < listErrors.size() - 1) || (listErrors.size() > 1)) {
                        errors.append("\n");
                    }
                }

                Tools.showToast(this, errors.toString(), Toast.LENGTH_LONG);
            }
        }

    }

}