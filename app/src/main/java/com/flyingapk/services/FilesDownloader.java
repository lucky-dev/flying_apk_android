package com.flyingapk.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.flyingapk.BuildConfig;
import com.flyingapk.constants.App;
import com.flyingapk.utils.MapFilesDownloader;
import com.flyingapk.utils.Tools;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class FilesDownloader extends Service {

    public static final String TAG = FilesDownloader.class.getSimpleName();

    private static final int READ_TIMEOUT = 15;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private boolean isStopping;
    private boolean statusDownloading;

    @Override
    public void onCreate() {
        super.onCreate();

        Tools.logD(TAG, "Starting downloading...");

        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String pathToApp = "";

            Intent startIntent = new Intent(App.FILE_DOWNLOADER_BROADCAST_ACTION)
                    .putExtra(MapFilesDownloader.RESPONSE, MapFilesDownloader.Response.Command.START_DOWNLOADING);
            LocalBroadcastManager.getInstance(FilesDownloader.this).sendBroadcast(startIntent);

            Bundle data = msg.getData();

            if ((data != null) && (!data.isEmpty())) {
                String file = data.getString(MapFilesDownloader.Request.Params.FILE);
                String checksum = data.getString(MapFilesDownloader.Request.Params.CHECKSUM_FILE);

                File destinationFile = new File(getUpdateDir(), file);
                if (destinationFile.exists()) {
                    destinationFile.delete();
                }

                Uri uri = new Uri.Builder()
                        .encodedAuthority(BuildConfig.ENDPOINT_URL)
                        .scheme("http")
                        .path("files/" + file)
                        .build();

                boolean status = downloadFile(uri.toString(), getUpdateDir().getAbsolutePath(), file, checksum != null, checksum);

                statusDownloading = status;

                if (status) {
                    pathToApp = new File(getUpdateDir(), file).getAbsolutePath();

                    Tools.logD(TAG, String.format("The file %s is downloaded", pathToApp));
                } else {
                    Tools.logD(TAG, String.format("The file (%s/%s) isn't downloaded", getUpdateDir().getAbsolutePath(), file));

                    File resultFile = new File(getUpdateDir(), file);
                    if (resultFile.exists()) {
                        resultFile.delete();
                    }
                }
            }

            Intent endIntent = new Intent(App.FILE_DOWNLOADER_BROADCAST_ACTION)
                    .putExtra(MapFilesDownloader.RESPONSE, MapFilesDownloader.Response.Command.STOP_DOWNLOADING);
            endIntent.putExtra(MapFilesDownloader.Response.Params.PATH_TO_APP, pathToApp);
            endIntent.putExtra(MapFilesDownloader.Response.Params.STATUS_DOWNLOADING, statusDownloading);
            LocalBroadcastManager.getInstance(FilesDownloader.this).sendBroadcast(endIntent);

            stopSelf(msg.arg2);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            Message msg = mServiceHandler.obtainMessage();

            Bundle data = intent.getExtras();

            if (data != null) {
                int command = intent.getIntExtra(MapFilesDownloader.REQUEST, -1);
                if (command == MapFilesDownloader.Request.Command.STOP_SERVICE) {
                    isStopping = true;
                    stopSelf();
                    return START_NOT_STICKY;
                }

                msg.arg2 = startId;
                msg.setData(data);
                mServiceHandler.sendMessage(msg);
            }

        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private File getUpdateDir() {
        File uploadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        if ((uploadDir != null) || (!uploadDir.exists())) {
            uploadDir.mkdir();
        }

        return uploadDir;
    }

    private boolean downloadFile(String fileUrl, String path, String fileName, boolean checkFile, String checkSum) {
        if ((fileUrl == null) || (path == null) || (fileName == null)) {
            return false;
        }

        FileOutputStream fileOutput = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(fileUrl)
                    .get()
                    .build();

            File file = new File(path, fileName);

            fileOutput = new FileOutputStream(file);

            Response response = client.newCall(request).execute();
            long contentSize = response.body().contentLength();

            if (response.code() != 200) {
                return false;
            }

            InputStream inputStream = response.body().byteStream();

            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            long totalBytes = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                if (isStopping) {
                    break;
                }

                fileOutput.write(buffer, 0, bufferLength);

                if (checkFile) {
                    md.update(buffer, 0, bufferLength);
                }

                totalBytes += bufferLength;

                updateProgress(totalBytes, contentSize);
            }

            // Check our file
            if (checkFile) {
                String _checkSum = new BigInteger(1, md.digest()).toString(16);
                if ((_checkSum != null) && (_checkSum.length() < 32)) {
                    _checkSum = "0" + _checkSum;
                }

                if ((checkSum == null) || (_checkSum == null) || (!checkSum.equals(_checkSum))) {
                    Tools.logE(TAG, String.format("The file (%s/%s) is invalid", path, fileName));
                    return false;
                }

                Tools.logD(TAG, String.format("Checksum of %s is OK", fileName));
            }
        } catch (NoSuchAlgorithmException e) {
            Tools.logE(TAG, "Algorithm exception", e);
            return false;
        } catch (IOException e) {
            Tools.logE(TAG, "Exception retrieving update info", e);
            return false;
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close();
                } catch (IOException e) {
                    Tools.logE(TAG, "Exception closing HUC reader", e);
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    private void updateProgress(long totalBytes, long contentSize) {
        Intent localIntent = new Intent(App.FILE_DOWNLOADER_BROADCAST_ACTION)
                .putExtra(MapFilesDownloader.RESPONSE, MapFilesDownloader.Response.Command.PROGRESS_DOWNLOADING);

        Bundle data = new Bundle();
        data.putInt(MapFilesDownloader.Response.Params.PROGRESS, ((int) ((totalBytes * 100) / contentSize)));
        localIntent.putExtras(data);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Tools.logD(TAG, "Stopping downloading...");
    }

}
