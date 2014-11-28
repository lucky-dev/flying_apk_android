package com.flyingapk.api.wrappers;

import android.os.Parcel;

import com.flyingapk.models.Build;

import java.util.ArrayList;
import java.util.List;

public class ListBuildsResponse extends BaseResponse {

    private List<Build> mListBuilds;

    public ListBuildsResponse() {
        mListBuilds = new ArrayList<Build>();
    }

    public ListBuildsResponse(int code) {
        this();
        setCode(code);
    }

    public ListBuildsResponse(Parcel in) {
        super();
        in.readTypedList(mListBuilds, Build.CREATOR);
    }

    public void setListBuilds(List<Build> listBuilds) {
        mListBuilds = listBuilds;
    }

    public List<Build> getListBuilds() {
        return mListBuilds;
    }

    public static final Creator<ListBuildsResponse> CREATOR =
            new Creator<ListBuildsResponse>() {

                public ListBuildsResponse createFromParcel(Parcel in) {
                    return new ListBuildsResponse(in);
                }

                public ListBuildsResponse[] newArray(int size) {
                    return new ListBuildsResponse[size];
                }

            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(mListBuilds);
    }

}
