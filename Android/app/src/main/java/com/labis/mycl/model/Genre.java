package com.labis.mycl.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Genre implements Parcelable {
    public final String id;
    public final String name;

    public Genre(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Genre(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Genre[size];
        }

    };

}
