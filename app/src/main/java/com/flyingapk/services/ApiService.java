package com.flyingapk.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.api.wrappers.ListAndroidAppsResponse;
import com.flyingapk.api.wrappers.ListBuildsResponse;
import com.flyingapk.api.wrappers.UserAuthorizationResponse;
import com.flyingapk.api.wrappers.UserLogoutResponse;
import com.flyingapk.constants.App;
import com.flyingapk.utils.JsonParser;
import com.flyingapk.utils.RequestBuilder;
import com.flyingapk.utils.Tools;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiService extends Service {

    public static final String TAG = ApiService.class.getSimpleName();

    private static final long TIMEOUT_CONNECTION = 3;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private JsonParser mJsonParser;
    private RequestBuilder mRequestBuilder;
    private String mAccessToken;

    @Override
    public void onCreate() {
        mRequestBuilder = new RequestBuilder();
        mJsonParser = new JsonParser();

        mAccessToken = Tools.getAccessToken(this);

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
                case MapApiFunctions.Request.Command.LOGIN : {
                    Bundle data = msg.getData();

                    if (!data.isEmpty()) {
                        String email = data.getString(MapApiFunctions.Request.Params.EMAIL);
                        String password = data.getString(MapApiFunctions.Request.Params.PASSWORD);
                        String tag = data.getString(MapApiFunctions.Request.Params.TAG_CALLER);

                        OkHttpClient client = new OkHttpClient();
                        client.setConnectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS);

                        Uri uri = new Uri.Builder()
                                .encodedAuthority(App.ENDPOINT_URL)
                                .scheme("http")
                                .path("api/login")
                                .build();

                        Request request = new Request.Builder()
                                .headers(mRequestBuilder.getCommonHeader())
                                .url(uri.toString())
                                .post(mRequestBuilder.getAuthorizationRequest(null, email, password))
                                .build();

                        int code = 0;
                        String responseJson = null;

                        try {
                            Response response = client.newCall(request).execute();
                            code = response.code();
                            responseJson = response.body().string();
                        } catch (IOException e) {
                        }


                        //Debug
                        Tools.logD(TAG, String.valueOf(responseJson));

                        UserAuthorizationResponse result = mJsonParser.getUserAuthorizationResponse(code, responseJson);

                        if (result.getCode() == 200) {
                            Tools.saveAccessToken(ApiService.this, result.getAccessToken());
                        } else if (result.getCode() == 0) {
                            result.getErrors().add("bad connection");
                        }

                        Intent localIntent = new Intent(App.API_BROADCAST_ACTION)
                                .putExtra(MapApiFunctions.RESPONSE, MapApiFunctions.Response.Command.LOGIN);

                        data.clear();
                        data.putParcelable(MapApiFunctions.Response.Params.AUTHORIZATION_RESULT, result);
                        data.putString(MapApiFunctions.Response.Params.TAG_CALLER, tag);

                        localIntent.putExtras(data);

                        LocalBroadcastManager.getInstance(ApiService.this).sendBroadcast(localIntent);
                    }
                } break;

                case MapApiFunctions.Request.Command.REGISTER : {
                    Bundle data = msg.getData();

                    if (!data.isEmpty()) {
                        String name = data.getString(MapApiFunctions.Request.Params.NAME);
                        String email = data.getString(MapApiFunctions.Request.Params.EMAIL);
                        String password = data.getString(MapApiFunctions.Request.Params.PASSWORD);
                        String tag = data.getString(MapApiFunctions.Request.Params.TAG_CALLER);

                        OkHttpClient client = new OkHttpClient();
                        client.setConnectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS);

                        Uri uri = new Uri.Builder()
                                .encodedAuthority(App.ENDPOINT_URL)
                                .scheme("http")
                                .path("api/register")
                                .build();

                        Request request = new Request.Builder()
                                .headers(mRequestBuilder.getCommonHeader())
                                .url(uri.toString())
                                .post(mRequestBuilder.getAuthorizationRequest(name, email, password))
                                .build();

                        int code = 0;
                        String responseJson = null;

                        try {
                            Response response = client.newCall(request).execute();
                            code = response.code();
                            responseJson = response.body().string();
                        } catch (IOException e) {
                        }


                        //Debug
                        Tools.logD(TAG, String.valueOf(responseJson));

                        UserAuthorizationResponse result = mJsonParser.getUserAuthorizationResponse(code, responseJson);

                        if (result.getCode() == 200) {
                            Tools.saveAccessToken(ApiService.this, result.getAccessToken());
                        } else if (result.getCode() == 0) {
                            result.getErrors().add("bad connection");
                        }

                        Intent localIntent = new Intent(App.API_BROADCAST_ACTION)
                                .putExtra(MapApiFunctions.RESPONSE, MapApiFunctions.Response.Command.REGISTER);

                        data.clear();
                        data.putParcelable(MapApiFunctions.Response.Params.AUTHORIZATION_RESULT, result);
                        data.putString(MapApiFunctions.Response.Params.TAG_CALLER, tag);

                        localIntent.putExtras(data);

                        LocalBroadcastManager.getInstance(ApiService.this).sendBroadcast(localIntent);
                    }
                } break;

                case MapApiFunctions.Request.Command.LIST_APPS : {
                    Bundle data = msg.getData();

                    if (!data.isEmpty()) {
                        String tag = data.getString(MapApiFunctions.Request.Params.TAG_CALLER);

                        OkHttpClient client = new OkHttpClient();
                        client.setConnectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS);

                        Uri uri = new Uri.Builder()
                                .encodedAuthority(App.ENDPOINT_URL)
                                .scheme("http")
                                .path("api/android_apps")
                                .build();

                        Request request = new Request.Builder()
                                .headers(mRequestBuilder.getHeaderWithToken(mAccessToken))
                                .url(uri.toString())
                                .get()
                                .build();

                        int code = 0;
                        String responseJson = null;

                        try {
                            Response response = client.newCall(request).execute();
                            code = response.code();
                            responseJson = response.body().string();
                        } catch (IOException e) {
                        }


                        //Debug
                        Tools.logD(TAG, String.valueOf(responseJson));

                        ListAndroidAppsResponse result = mJsonParser.getListAndroidAppsResponse(code, responseJson);

                        if (result.getCode() != 200) {
                            result.getErrors().add("bad connection");
                        }

                        Intent localIntent = new Intent(App.API_BROADCAST_ACTION)
                                .putExtra(MapApiFunctions.RESPONSE, MapApiFunctions.Response.Command.LIST_APPS);

                        data.clear();
                        data.putParcelable(MapApiFunctions.Response.Params.LIST_APPS_RESULT, result);
                        data.putString(MapApiFunctions.Response.Params.TAG_CALLER, tag);

                        localIntent.putExtras(data);

                        LocalBroadcastManager.getInstance(ApiService.this).sendBroadcast(localIntent);
                    }
                } break;

                case MapApiFunctions.Request.Command.LIST_BUILDS : {
                    Bundle data = msg.getData();

                    if (!data.isEmpty()) {
                        int appId = data.getInt(MapApiFunctions.Request.Params.APP_ID);
                        String tag = data.getString(MapApiFunctions.Request.Params.TAG_CALLER);

                        OkHttpClient client = new OkHttpClient();
                        client.setConnectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS);

                        Uri uri = new Uri.Builder()
                                .encodedAuthority(App.ENDPOINT_URL)
                                .scheme("http")
                                .path("api/builds")
                                .appendQueryParameter("app_id", String.valueOf(appId))
                                .build();

                        Request request = new Request.Builder()
                                .headers(mRequestBuilder.getHeaderWithToken(mAccessToken))
                                .url(uri.toString())
                                .get()
                                .build();

                        int code = 0;
                        String responseJson = null;

                        try {
                            Response response = client.newCall(request).execute();
                            code = response.code();
                            responseJson = response.body().string();
                        } catch (IOException e) {
                        }


                        //Debug
                        Tools.logD(TAG, String.valueOf(responseJson));

                        ListBuildsResponse result = mJsonParser.getListBuildsResponse(code, responseJson);

                        if (result.getCode() != 200) {
                            result.getErrors().add("bad connection");
                        }

                        Intent localIntent = new Intent(App.API_BROADCAST_ACTION)
                                .putExtra(MapApiFunctions.RESPONSE, MapApiFunctions.Response.Command.LIST_BUILDS);

                        data.clear();
                        data.putParcelable(MapApiFunctions.Response.Params.LIST_BUILDS_RESULT, result);
                        data.putString(MapApiFunctions.Response.Params.TAG_CALLER, tag);

                        localIntent.putExtras(data);

                        LocalBroadcastManager.getInstance(ApiService.this).sendBroadcast(localIntent);
                    }
                } break;

                case MapApiFunctions.Request.Command.LOGOUT : {
                    Bundle data = msg.getData();

                    if (!data.isEmpty()) {
                        String tag = data.getString(MapApiFunctions.Request.Params.TAG_CALLER);

                        OkHttpClient client = new OkHttpClient();
                        client.setConnectTimeout(TIMEOUT_CONNECTION, TimeUnit.SECONDS);

                        Uri uri = new Uri.Builder()
                                .encodedAuthority(App.ENDPOINT_URL)
                                .scheme("http")
                                .path("api/logout")
                                .build();

                        Request request = new Request.Builder()
                                .headers(mRequestBuilder.getHeaderWithToken(mAccessToken))
                                .url(uri.toString())
                                .method("POST", null)
                                .build();

                        int code = 0;
                        String responseJson = null;

                        try {
                            Response response = client.newCall(request).execute();
                            code = response.code();
                            responseJson = response.body().string();
                        } catch (IOException e) {
                        }


                        //Debug
                        Tools.logD(TAG, String.valueOf(responseJson));

                        UserLogoutResponse result = mJsonParser.getUserLogoutResponse(code, responseJson);

                        if (result.getCode() == 0) {
                            result.getErrors().add("bad connection");
                        }

                        Intent localIntent = new Intent(App.API_BROADCAST_ACTION)
                                .putExtra(MapApiFunctions.RESPONSE, MapApiFunctions.Response.Command.LOGOUT);

                        data.clear();
                        data.putParcelable(MapApiFunctions.Response.Params.LOGOUT_RESULT, result);
                        data.putString(MapApiFunctions.Response.Params.TAG_CALLER, tag);

                        localIntent.putExtras(data);

                        LocalBroadcastManager.getInstance(ApiService.this).sendBroadcast(localIntent);
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
                msg.arg1 = intent.getIntExtra(MapApiFunctions.REQUEST, -1);
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

    public void onDestroy() {
        Tools.logD(TAG, "Stop service");

        super.onDestroy();
    }
}
