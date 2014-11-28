package com.flyingapk.api.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class BaseResponse implements Parcelable {

    private int mCode;
    private int mApiVersion;
    private List<String> mErrors;

    public BaseResponse() {
        mErrors = new ArrayList<String>();
    }

    public BaseResponse(Parcel in) {
        mCode = in.readInt();
        mApiVersion = in.readInt();
        mErrors = new ArrayList<String>();
        in.readStringList(mErrors);
    }

    public void setCode(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    public void setApiVersion(int apiVersion) {
        mApiVersion = apiVersion;
    }

    public int getApiVersion() {
        return mApiVersion;
    }

    public void setErrors(List<String> errors) {
        mErrors = errors;
    }

    public List<String> getErrors() {
        return mErrors;
    }

    public static final Creator<BaseResponse> CREATOR =
            new Creator<BaseResponse>() {

                public BaseResponse createFromParcel(Parcel in) {
                    return new BaseResponse(in);
                }

                public BaseResponse[] newArray(int size) {
                    return new BaseResponse[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCode);
        dest.writeInt(mApiVersion);
        dest.writeStringList(mErrors);
    }

}
