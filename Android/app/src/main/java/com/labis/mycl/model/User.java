package com.labis.mycl.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public final String id;
    public final String pw;
    public final String age;
    public final String gender;
    public final String nickname;
    public final String phone;
    public final String image;

    public User(String id, String pw, String age, String gender, String nickname,String phone, String image) {
        this.id = id;
        this.pw = pw;
        this.age = age;
        this.gender = gender;
        this.nickname = nickname;
        this.phone = phone;
        this.image = image;
    }

    public User(Parcel in) {
        this.id = in.readString();
        this.pw = in.readString();
        this.age = in.readString();
        this.gender = in.readString();
        this.nickname = in.readString();
        this.phone = in.readString();
        this.image = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.pw);
        dest.writeString(this.age);
        dest.writeString(this.gender);
        dest.writeString(this.nickname);
        dest.writeString(this.phone);
        dest.writeString(this.image);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

    @Override
    public User createFromParcel(Parcel in) {
        return new User(in);
    }

    @Override
    public User[] newArray(int size) {
        // TODO Auto-generated method stub
        return new User[size];
    }

};
}
