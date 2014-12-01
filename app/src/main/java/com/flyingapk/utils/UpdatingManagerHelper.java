package com.flyingapk.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.flyingapk.constants.App;
import com.flyingapk.services.UpdatingManager;

public class UpdatingManagerHelper {

    public static final String TAG = UpdatingManagerHelper.class.getSimpleName();

    private Context mContext;
    private UpdatingManagerCallback mUpdatingManagerListener;
    private IntentFilter mIntentFilter;
    private UpdatingManagerReceiver mUpdatingManagerReceiver;

    public UpdatingManagerHelper(Context context) {
        mContext = context;
    }

    public void onStart() {
        mUpdatingManagerReceiver = new UpdatingManagerReceiver();
        mIntentFilter = new IntentFilter(App.UPDATING_MANAGER_BROADCAST_ACTION);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mUpdatingManagerReceiver, mIntentFilter);
    }

    public void onStop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUpdatingManagerReceiver);
        mUpdatingManagerListener = null;
    }

    public void setListener(UpdatingManagerCallback updatingManagerListener) {
        mUpdatingManagerListener = updatingManagerListener;
    }

    // API
    public void checkNewVersion() {
        Intent intent = new Intent(mContext, UpdatingManager.class);
        intent.putExtra(MapUpdatingManager.REQUEST, MapUpdatingManager.Request.Command.CHECK_NEW_VERSION_APP);
        mContext.startService(intent);
    }

    public void update(String file, String checksumFile) {
        Intent intent = new Intent(mContext, UpdatingManager.class);
        intent.putExtra(MapUpdatingManager.REQUEST, MapUpdatingManager.Request.Command.UPDATE_APP);
        intent.putExtra(MapUpdatingManager.Request.Params.FILE, file);
        intent.putExtra(MapUpdatingManager.Request.Params.CHECKSUM_FILE, checksumFile);
        mContext.startService(intent);
    }

    public void cancel() {
        Intent intent = new Intent(mContext, UpdatingManager.class);
        intent.putExtra(MapUpdatingManager.REQUEST, MapUpdatingManager.Request.Command.STOP_SERVICE);
        mContext.startService(intent);
    }

    // All classes-wrapper are used for communication between Activity <-> Service
    private class UpdatingManagerReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            int response = intent.getIntExtra(MapUpdatingManager.RESPONSE, 0);

            switch (response) {
                case MapUpdatingManager.Response.Command.START_CHECKING_NEW_VERSION_APP : {
                    if (mUpdatingManagerListener != null) {
                        mUpdatingManagerListener.onStartCheckingNewApp();
                    }
                } break;

                case MapUpdatingManager.Response.Command.STOP_CHECKING_NEW_VERSION_APP : {
                    if (mUpdatingManagerListener != null) {
                        boolean isNewVersionApp = intent.getExtras().getBoolean(MapUpdatingManager.Response.Params.IS_NEW_VERSION_APP);
                        String versionApp = intent.getExtras().getString(MapUpdatingManager.Response.Params.VERSION_APP);
                        String whatsNew = intent.getExtras().getString(MapUpdatingManager.Response.Params.WHATS_NEW);
                        String file = intent.getExtras().getString(MapUpdatingManager.Response.Params.FILE);
                        String checksumFile = intent.getExtras().getString(MapUpdatingManager.Response.Params.CHECKSUM_FILE);
                        mUpdatingManagerListener.onStopCheckingNewApp(isNewVersionApp, versionApp, whatsNew, file, checksumFile);
                    }
                } break;

                case MapUpdatingManager.Response.Command.START_UPDATING : {
                    if (mUpdatingManagerListener != null) {
                        mUpdatingManagerListener.onStartUpdating();
                    }
                } break;

                case MapUpdatingManager.Response.Command.PROGRESS_UPDATING : {
                    if (mUpdatingManagerListener != null) {
                        mUpdatingManagerListener.onProgressUpdating(intent.getExtras().getInt(MapUpdatingManager.Response.Params.PROGRESS));
                    }
                } break;

                case MapUpdatingManager.Response.Command.STOP_UPDATING : {
                    if (mUpdatingManagerListener != null) {
                        mUpdatingManagerListener.onFinishUpdating(intent.getExtras().getString(MapUpdatingManager.Response.Params.PATH_TO_APP),
                                intent.getExtras().getBoolean(MapUpdatingManager.Response.Params.STATUS_UPDATING));
                    }
                } break;
            }
        }
    }

    public interface UpdatingManagerCallback {
        public void onStartCheckingNewApp();
        public void onStopCheckingNewApp(boolean isNewVersionApp, String versionApp, String whatsNew, String file, String checksumFile);
        public void onStartUpdating();
        public void onProgressUpdating(int progress);
        public void onFinishUpdating(String pathToApp, boolean statusDownloading);
    }

}
