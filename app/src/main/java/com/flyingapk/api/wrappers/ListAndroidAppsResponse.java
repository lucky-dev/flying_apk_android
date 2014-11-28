package com.flyingapk.api.wrappers;

import android.os.Parcel;

import com.flyingapk.models.AndroidApp;

import java.util.ArrayList;
import java.util.List;

public class ListAndroidAppsResponse extends BaseResponse {

    private List<AndroidApp> mListAndroidApps;

    public ListAndroidAppsResponse() {
        mListAndroidApps = new ArrayList<AndroidApp>();
    }

    public ListAndroidAppsResponse(int code) {
        this();
        setCode(code);
    }

    public ListAndroidAppsResponse(Parcel in) {
        super();
        in.readTypedList(mListAndroidApps, AndroidApp.CREATOR);
    }

    public void setListAndroidApps(List<AndroidApp> listAndroidApps) {
        mListAndroidApps = listAndroidApps;
    }

    public List<AndroidApp> getListAndroidApps() {
        return mListAndroidApps;
    }

    public static final Creator<ListAndroidAppsResponse> CREATOR =
            new Creator<ListAndroidAppsResponse>() {

                public ListAndroidAppsResponse createFromParcel(Parcel in) {
                    return new ListAndroidAppsResponse(in);
                }

                public ListAndroidAppsResponse[] newArray(int size) {
                    return new ListAndroidAppsResponse[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(mListAndroidApps);
    }

}
