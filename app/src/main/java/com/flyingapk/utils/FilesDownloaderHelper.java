package com.flyingapk.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.constants.App;
import com.flyingapk.services.FilesDownloader;

public class FilesDownloaderHelper {

    public static final String TAG = FilesDownloaderHelper.class.getSimpleName();

    private Context mContext;
    private FilesDownloaderCallback mFilesDownloaderListener;
    private IntentFilter mIntentFilter;
    private FilesDownloaderReceiver mFilesDownloaderReceiver;

    public FilesDownloaderHelper(Context context) {
        mContext = context;
    }

    public void onStart() {
        mFilesDownloaderReceiver = new FilesDownloaderReceiver();
        mIntentFilter = new IntentFilter(App.FILE_DOWNLOADER_BROADCAST_ACTION);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mFilesDownloaderReceiver, mIntentFilter);
    }

    public void onStop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mFilesDownloaderReceiver);
        mFilesDownloaderListener = null;
    }

    public void setListener(FilesDownloaderCallback filesDownloaderListener) {
        mFilesDownloaderListener = filesDownloaderListener;
    }

    // API
    public void getFile(String file, String checksum) {
        Intent apiServiceIntent = new Intent(mContext, FilesDownloader.class);

        Bundle data = new Bundle();
        data.putString(MapFilesDownloader.Request.Params.FILE, file);
        data.putString(MapFilesDownloader.Request.Params.CHECKSUM_FILE, checksum);

        apiServiceIntent.putExtras(data);

        mContext.startService(apiServiceIntent);
    }

    public void cancel() {
        Intent intent = new Intent(mContext, FilesDownloader.class);
        intent.putExtra(MapFilesDownloader.REQUEST, MapFilesDownloader.Request.Command.STOP_SERVICE);
        mContext.startService(intent);
    }

    // All classes-wrapper are used for communication between Activity <-> Service
    private class FilesDownloaderReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            int response = intent.getIntExtra(MapFilesDownloader.RESPONSE, 0);

            switch (response) {
                case MapFilesDownloader.Response.Command.START_DOWNLOADING : {
                    if (mFilesDownloaderListener != null) {
                        mFilesDownloaderListener.onStartDownloading();
                    }
                } break;

                case MapFilesDownloader.Response.Command.PROGRESS_DOWNLOADING : {
                    if (mFilesDownloaderListener != null) {
                        mFilesDownloaderListener.onProgressDownloading(intent.getExtras().getInt(MapFilesDownloader.Response.Params.PROGRESS));
                    }
                } break;

                case MapFilesDownloader.Response.Command.STOP_DOWNLOADING : {
                    if (mFilesDownloaderListener != null) {
                        mFilesDownloaderListener.onFinishDownloading(intent.getExtras().getString(MapFilesDownloader.Response.Params.PATH_TO_APP),
                                intent.getExtras().getBoolean(MapFilesDownloader.Response.Params.STATUS_DOWNLOADING));
                    }
                } break;
            }
        }
    }

    public interface FilesDownloaderCallback {
        public void onStartDownloading();
        public void onProgressDownloading(int progress);
        public void onFinishDownloading(String pathToApp, boolean statusDownloading);
    }

}
