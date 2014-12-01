package com.flyingapk.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.flyingapk.BuildConfig;
import com.flyingapk.R;
import com.flyingapk.constants.App;

public class Tools {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String EMAIL = "email";

    public static boolean isValidEmail(String email) {
        if ((email != null) && (Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            return true;
        }

        return false;
    }

    public static void showToast(Context context, String text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setText(text);
        toast.show();
    }

    public static ProgressDialog getIndeterminateProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);

        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.large_indeterminate_progress);

        return progressDialog;
    }

    public static void logD(String tag, String value) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, value);
        }
    }

    public static void logE(String tag, String value, Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, value, e);
        }
    }

    public static void logE(String tag, String value) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, value);
        }
    }


    public static void saveAccessToken(Context context, String token) {
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.commit();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME, 0);

        return settings.getString(ACCESS_TOKEN, null);
    }

    public static void clearAccessToken(Context context) {
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(ACCESS_TOKEN);
        editor.commit();
    }

    public static void saveEmail(Context context, String email) {
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(EMAIL, email);
        editor.commit();
    }

    public static String getEmail(Context context) {
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME, 0);

        return settings.getString(EMAIL, null);
    }

    public static void navigateUpTo(Activity activity, Intent upIntent) {
        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(upIntent);
        activity.finish();
    }

    public static LayoutInflater getLayoutInflater(Context context) {
        return (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static AlertDialog createOkDialog(Context context, String title, String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public static int getVersionApp(Context context) {
        PackageManager packageManager = context.getPackageManager();

        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        return 0;
    }

    public static String getVersionNameApp(Context context) {
        PackageManager packageManager = context.getPackageManager();

        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }

        return "0.0";
    }

    private Tools() {
    }

}
