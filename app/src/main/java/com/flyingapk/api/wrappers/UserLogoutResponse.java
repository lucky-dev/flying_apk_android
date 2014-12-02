package com.flyingapk.api.wrappers;

import android.os.Parcel;

public class UserLogoutResponse extends BaseResponse {

    public UserLogoutResponse() {
    }

    public UserLogoutResponse(int code) {
        this();
        setCode(code);
    }

    public UserLogoutResponse(Parcel in) {
        super(in);
    }

    public static final Creator<UserLogoutResponse> CREATOR =
            new Creator<UserLogoutResponse>() {

                public UserLogoutResponse createFromParcel(Parcel in) {
                    return new UserLogoutResponse(in);
                }

                public UserLogoutResponse[] newArray(int size) {
                    return new UserLogoutResponse[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

}
