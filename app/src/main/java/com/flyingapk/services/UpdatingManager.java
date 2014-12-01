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
import android.text.TextUtils;

import com.flyingapk.constants.App;
import com.flyingapk.utils.JsonUtil;
import com.flyingapk.utils.MapUpdatingManager;
import com.flyingapk.utils.Tools;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpdatingManager extends Service {

    public static final String TAG = UpdatingManager.class.getSimpleName();

    private static final int READ_TIMEOUT = 15;

    private static final String VERSION_APP = "version_app";
    private static final String WHATS_NEW = "whats_new";
    private static final String FILE = "file";
    private static final String CHECKSUM_FILE = "checksum_file";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private boolean isStopping;
    private boolean statusUpdating;

    @Override
    public void onCreate() {
        super.onCreate();

        Tools.logD(TAG, "Starting updating...");

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
            switch (msg.arg1) {
                case MapUpdatingManager.Request.Command.CHECK_NEW_VERSION_APP : {
                    Intent startIntent = new Intent(App.UPDATING_MANAGER_BROADCAST_ACTION)
                            .putExtra(MapUpdatingManager.RESPONSE, MapUpdatingManager.Response.Command.START_CHECKING_NEW_VERSION_APP);
                    LocalBroadcastManager.getInstance(UpdatingManager.this).sendBroadcast(startIntent);

                    // Get info about updating of app
                    Uri uriUpdInfo = new Uri.Builder()
                            .authority(App.UPD_URL)
                            .scheme("http")
                            .path("/files/upd_info.json")
                            .build();


                    OkHttpClient client = new OkHttpClient();
                    client.setConnectTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
                    Request request = new Request.Builder()
                            .url(uriUpdInfo.toString())
                            .get()
                            .build();

                    Map<String, String> updInfo = new HashMap<String, String>();
                    int versionApp = 0;
                    try {
                        updInfo = parseUpdInfo(client.newCall(request).execute());

                        if (updInfo.get(VERSION_APP) != null) {
                            versionApp = Integer.valueOf(updInfo.get(VERSION_APP));
                        }
                    } catch (NumberFormatException nfe) {
                    } catch (IOException e) {
                    }

                    Intent endIntent = new Intent(App.UPDATING_MANAGER_BROADCAST_ACTION)
                            .putExtra(MapUpdatingManager.RESPONSE, MapUpdatingManager.Response.Command.STOP_CHECKING_NEW_VERSION_APP);

                    if (versionApp > Tools.getVersionApp(UpdatingManager.this)) {
                        endIntent.putExtra(MapUpdatingManager.Response.Params.IS_NEW_VERSION_APP, true);
                        endIntent.putExtra(MapUpdatingManager.Response.Params.VERSION_APP, updInfo.get(VERSION_APP));
                        endIntent.putExtra(MapUpdatingManager.Response.Params.WHATS_NEW, updInfo.get(WHATS_NEW));
                        endIntent.putExtra(MapUpdatingManager.Response.Params.FILE, updInfo.get(FILE));
                        endIntent.putExtra(MapUpdatingManager.Response.Params.CHECKSUM_FILE, updInfo.get(CHECKSUM_FILE));
                    } else {
                        endIntent.putExtra(MapUpdatingManager.Response.Params.IS_NEW_VERSION_APP, false);
                    }

                    LocalBroadcastManager.getInstance(UpdatingManager.this).sendBroadcast(endIntent);
                } break;

                case MapUpdatingManager.Request.Command.UPDATE_APP : {
                    Bundle data = msg.getData();

                    if (!data.isEmpty()) {
                        String file = data.getString(MapUpdatingManager.Request.Params.FILE);
                        String checksum = data.getString(MapUpdatingManager.Request.Params.CHECKSUM_FILE);

                        String pathToApp = "";

                        Intent startIntent = new Intent(App.UPDATING_MANAGER_BROADCAST_ACTION)
                                .putExtra(MapUpdatingManager.RESPONSE, MapUpdatingManager.Response.Command.START_UPDATING);
                        LocalBroadcastManager.getInstance(UpdatingManager.this).sendBroadcast(startIntent);

                        // Get a new version of app from the server
                        File destinationFile = new File(getUpdateDir(), file);
                        if (destinationFile.exists()) {
                            destinationFile.delete();
                        }

                        Uri uri = new Uri.Builder()
                                .authority(App.UPD_URL)
                                .scheme("http")
                                .path("/files/" + file)
                                .build();

                        boolean status = downloadFile(uri.toString(), getUpdateDir().getAbsolutePath(), file, checksum != null, checksum);

                        statusUpdating = status;

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

                        Intent endIntent = new Intent(App.UPDATING_MANAGER_BROADCAST_ACTION)
                                .putExtra(MapUpdatingManager.RESPONSE, MapUpdatingManager.Response.Command.STOP_UPDATING);
                        endIntent.putExtra(MapUpdatingManager.Response.Params.PATH_TO_APP, pathToApp);
                        endIntent.putExtra(MapUpdatingManager.Response.Params.STATUS_UPDATING, statusUpdating);
                        LocalBroadcastManager.getInstance(UpdatingManager.this).sendBroadcast(endIntent);
                    }
                } break;
            }

            stopSelf(msg.arg2);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            Message msg = mServiceHandler.obtainMessage();

            Bundle data = intent.getExtras();

            if (data != null) {
                int command = intent.getIntExtra(MapUpdatingManager.REQUEST, -1);
                if (command == MapUpdatingManager.Request.Command.STOP_SERVICE) {
                    isStopping = true;
                    stopSelf();
                    return START_NOT_STICKY;
                }

                msg.arg1 = intent.getIntExtra(MapUpdatingManager.REQUEST, -1);
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

    private Map<String, String> parseUpdInfo(Response response) {
        Map<String, String> updInfo = new HashMap<String, String>();

        try {
            JSONObject jRoot = new JSONObject(response.body().string());

            updInfo.put(VERSION_APP, JsonUtil.getStringByKeyJson(jRoot, VERSION_APP));
            updInfo.put(WHATS_NEW, JsonUtil.getStringByKeyJson(jRoot, WHATS_NEW));
            updInfo.put(FILE, JsonUtil.getStringByKeyJson(jRoot, FILE));
            updInfo.put(CHECKSUM_FILE, JsonUtil.getStringByKeyJson(jRoot, CHECKSUM_FILE));
        } catch (JSONException e) {
            updInfo = new HashMap<String, String>();
        } catch (IOException e) {
            updInfo = new HashMap<String, String>();
        }

        return updInfo;
    }

    private void updateProgress(long totalBytes, long contentSize) {
        Intent localIntent = new Intent(App.UPDATING_MANAGER_BROADCAST_ACTION)
                .putExtra(MapUpdatingManager.RESPONSE, MapUpdatingManager.Response.Command.PROGRESS_UPDATING);

        Bundle data = new Bundle();
        data.putInt(MapUpdatingManager.Response.Params.PROGRESS, ((int) ((totalBytes * 100) / contentSize)));
        localIntent.putExtras(data);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Tools.logD(TAG, "Stopping updating...");
    }

}
