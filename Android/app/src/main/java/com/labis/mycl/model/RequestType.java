package com.labis.mycl.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestType implements Parcelable {
    public final String id;
    public final String name;
    public final String description;

    public RequestType(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public RequestType(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
    }

    @SuppressWarnings("rawtypes")
    public static final Creator CREATOR = new Creator() {

        @Override
        public RequestType createFromParcel(Parcel in) {
            return new RequestType(in);
        }

        @Override
        public RequestType[] newArray(int size) {
            // TODO Auto-generated method stub
            return new RequestType[size];
        }

    };

}
