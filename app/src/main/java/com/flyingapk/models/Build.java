package com.flyingapk.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Build implements Parcelable {

    private int mId;
    private String mVersion;
    private String mFixes;
    private Date mCreatedTime;
    private String mFileName;
    private String mFileChecksum;

    public Build() {
    }

    public Build(Parcel in) {
        mId = in.readInt();
        mVersion = in.readString();
        mFixes = in.readString();
        mCreatedTime = new Date(in.readLong());
        mFileName = in.readString();
        mFileChecksum = in.readString();
    }

    public void setId(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public String getVersion() {
        return mVersion;
    }

    public void setFixes(String fixes) {
        mFixes = fixes;
    }

    public String getFixes() {
        return mFixes;
    }

    public void setCreatedTime(Date createdTime) {
        mCreatedTime = createdTime;
    }

    public Date getCreatedTime() {
        return mCreatedTime;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileChecksum(String fileChecksum) {
        mFileChecksum = fileChecksum;
    }

    public String getFileChecksum() {
        return mFileChecksum;
    }

    public static final Parcelable.Creator<Build> CREATOR =
            new Parcelable.Creator<Build>() {

                public Build createFromParcel(Parcel in) {
                    return new Build(in);
                }

                public Build[] newArray(int size) {
                    return new Build[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mVersion);
        dest.writeString(mFixes);
        dest.writeLong(mCreatedTime.getTime());
        dest.writeString(mFileName);
        dest.writeString(mFileChecksum);
    }

}
