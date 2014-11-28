package com.flyingapk.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AndroidApp implements Parcelable {

    private int mId;
    private String mName;
    private String mDescription;

    public AndroidApp() {
    }

    public AndroidApp(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mDescription = in.readString();
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    public static final Parcelable.Creator<AndroidApp> CREATOR =
            new Parcelable.Creator<AndroidApp>() {

                public AndroidApp createFromParcel(Parcel in) {
                    return new AndroidApp(in);
                }

                public AndroidApp[] newArray(int size) {
                    return new AndroidApp[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeString(mDescription);
    }

}
