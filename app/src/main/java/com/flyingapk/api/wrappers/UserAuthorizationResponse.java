package com.flyingapk.api.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAuthorizationResponse extends BaseResponse {

    private String mAccessToken;

    public UserAuthorizationResponse() {
    }

    public UserAuthorizationResponse(int code) {
        this();
        setCode(code);
    }

    public UserAuthorizationResponse(Parcel in) {
        super(in);
        mAccessToken = in.readString();
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public static final Parcelable.Creator<UserAuthorizationResponse> CREATOR =
            new Parcelable.Creator<UserAuthorizationResponse>() {

                public UserAuthorizationResponse createFromParcel(Parcel in) {
                    return new UserAuthorizationResponse(in);
                }

                public UserAuthorizationResponse[] newArray(int size) {
                    return new UserAuthorizationResponse[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mAccessToken);
    }

}
