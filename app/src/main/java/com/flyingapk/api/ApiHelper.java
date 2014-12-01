package com.flyingapk.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.flyingapk.api.wrappers.BaseResponse;
import com.flyingapk.constants.App;
import com.flyingapk.services.ApiService;

import java.util.HashSet;
import java.util.Set;

public class ApiHelper {

    public static final String TAG = ApiHelper.class.getSimpleName();

    private Context mContext;
    private Set<ApiCallback> mApiListeners;
    private IntentFilter mStatusIntentFilter;
    private ApiResponseReceiver mApiResponseReceiver;

    public ApiHelper(Context context) {
        mContext = context;
    }

    public void onCreate() {
        mApiListeners = new HashSet<ApiCallback>();

        mApiResponseReceiver = new ApiResponseReceiver();
        mStatusIntentFilter = new IntentFilter(App.API_BROADCAST_ACTION);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mApiResponseReceiver, mStatusIntentFilter);
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mApiResponseReceiver);
        mApiListeners.clear();
    }

    // API
    public void login(String email, String password) {
        callOnStart(MapApiFunctions.Request.Command.LOGIN, TAG);

        Intent apiServiceIntent = new Intent(mContext, ApiService.class);
        apiServiceIntent.putExtra(MapApiFunctions.REQUEST, MapApiFunctions.Request.Command.LOGIN);

        Bundle data = new Bundle();
        data.putString(MapApiFunctions.Request.Params.TAG_CALLER, TAG);
        data.putString(MapApiFunctions.Request.Params.EMAIL, email);
        data.putString(MapApiFunctions.Request.Params.PASSWORD, password);

        apiServiceIntent.putExtras(data);

        mContext.startService(apiServiceIntent);
    }

    public void register(String name, String email, String password) {
        callOnStart(MapApiFunctions.Request.Command.REGISTER, TAG);

        Intent apiServiceIntent = new Intent(mContext, ApiService.class);
        apiServiceIntent.putExtra(MapApiFunctions.REQUEST, MapApiFunctions.Request.Command.REGISTER);

        Bundle data = new Bundle();
        data.putString(MapApiFunctions.Request.Params.TAG_CALLER, TAG);
        data.putString(MapApiFunctions.Request.Params.NAME, name);
        data.putString(MapApiFunctions.Request.Params.EMAIL, email);
        data.putString(MapApiFunctions.Request.Params.PASSWORD, password);

        apiServiceIntent.putExtras(data);

        mContext.startService(apiServiceIntent);
    }

    public void getListApps() {
        callOnStart(MapApiFunctions.Request.Command.LIST_APPS, TAG);

        Intent apiServiceIntent = new Intent(mContext, ApiService.class);
        apiServiceIntent.putExtra(MapApiFunctions.REQUEST, MapApiFunctions.Request.Command.LIST_APPS);

        Bundle data = new Bundle();
        data.putString(MapApiFunctions.Request.Params.TAG_CALLER, TAG);

        apiServiceIntent.putExtras(data);

        mContext.startService(apiServiceIntent);
    }

    public void getListBuilds(int appId) {
        callOnStart(MapApiFunctions.Request.Command.LIST_BUILDS, TAG);

        Intent apiServiceIntent = new Intent(mContext, ApiService.class);
        apiServiceIntent.putExtra(MapApiFunctions.REQUEST, MapApiFunctions.Request.Command.LIST_BUILDS);
        apiServiceIntent.putExtra(MapApiFunctions.Request.Params.APP_ID, appId);

        Bundle data = new Bundle();
        data.putString(MapApiFunctions.Request.Params.TAG_CALLER, TAG);

        apiServiceIntent.putExtras(data);

        mContext.startService(apiServiceIntent);
    }

    // All classes-wrapper are used for communication between Activity <-> Service
    private class ApiResponseReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            int response = intent.getIntExtra(MapApiFunctions.RESPONSE, 0);

            switch (response) {
                case MapApiFunctions.Response.Command.LOGIN : {
                    Bundle data = intent.getExtras();

                    BaseResponse result = (BaseResponse) data.
                            getParcelable(MapApiFunctions.Response.Params.AUTHORIZATION_RESULT);

                    callOnFinish(MapApiFunctions.Response.Command.LOGIN, result, null);
                } break;

                case MapApiFunctions.Response.Command.REGISTER : {
                    Bundle data = intent.getExtras();

                    BaseResponse result = (BaseResponse) data.
                            getParcelable(MapApiFunctions.Response.Params.AUTHORIZATION_RESULT);

                    callOnFinish(MapApiFunctions.Response.Command.REGISTER, result, null);
                } break;

                case MapApiFunctions.Response.Command.LIST_APPS : {
                    Bundle data = intent.getExtras();

                    BaseResponse result = (BaseResponse) data.
                            getParcelable(MapApiFunctions.Response.Params.LIST_APPS_RESULT);

                    callOnFinish(MapApiFunctions.Response.Command.LIST_APPS, result, null);
                } break;

                case MapApiFunctions.Response.Command.LIST_BUILDS : {
                    Bundle data = intent.getExtras();

                    BaseResponse result = (BaseResponse) data.
                            getParcelable(MapApiFunctions.Response.Params.LIST_BUILDS_RESULT);

                    callOnFinish(MapApiFunctions.Response.Command.LIST_BUILDS, result, null);
                } break;
            }
        }
    }

    public void addListener(ApiCallback listener) {
        mApiListeners.add(listener);
    }

    public void removeListener(ApiCallback listener) {
        mApiListeners.remove(listener);
    }

    public void callOnStart(int code, String tag) {
        for (ApiCallback item : mApiListeners) {
            if (item != null) {
                item.onStart(code, tag);
            }
        }
    }

    public void callOnFinish(int code, BaseResponse struct, String tag) {
        for (ApiCallback item : mApiListeners) {
            if (item != null) {
                item.onFinish(code, struct, tag);
            }
        }
    }

    public interface ApiCallback {
        public void onStart(int code, String tag);
        public void onFinish(int code, BaseResponse struct, String tag);
    }

}
