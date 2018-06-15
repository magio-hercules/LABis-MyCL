package com.labis.mycl.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestList implements Parcelable {
    public final String id;
    public final String user_id;
    public final String req_type_id;
    public final String comment;

    public RequestList(String id, String user_id, String req_type_id, String comment) {
        this.id = id;
        this.user_id = user_id;
        this.req_type_id = req_type_id;
        this.comment = comment;
    }

    public RequestList(Parcel in) {
        this.id = in.readString();
        this.user_id = in.readString();
        this.req_type_id = in.readString();
        this.comment = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.user_id);
        dest.writeString(this.req_type_id);
        dest.writeString(this.comment);
    }

    @SuppressWarnings("rawtypes")
    public static final Creator CREATOR = new Creator() {

        @Override
        public RequestList createFromParcel(Parcel in) {
            return new RequestList(in);
        }

        @Override
        public RequestList[] newArray(int size) {
            // TODO Auto-generated method stub
            return new RequestList[size];
        }

    };

}
