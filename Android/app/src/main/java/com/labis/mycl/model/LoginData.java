package com.labis.mycl.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginData implements Parcelable {
    public final User user;
    public final ArrayList<Genre> genreList;

    public LoginData(User user, ArrayList<Genre> genre) {
        this.user = user;
        this.genreList = genre;
    }

    public LoginData(Parcel in) {
        this.user = (User) in.readParcelable(User.class.getClassLoader());
        Parcelable[] parcelableArray = in.readParcelableArray(Genre.class.getClassLoader());
        Genre[] gTemp = Arrays.copyOf(parcelableArray, parcelableArray.length, Genre[].class);
        this.genreList =  new ArrayList<>(Arrays.asList(gTemp));
    }

    public User getUser() {
        return user;
    }

    public ArrayList<Genre> getGenreList() {
        return genreList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.user, flags);
        dest.writeParcelableArray (this.genreList.toArray(new Parcelable[genreList.size()]), flags);
    }

    @SuppressWarnings("rawtypes")
    public static final Creator CREATOR = new Creator() {

        @Override
        public LoginData createFromParcel(Parcel in) {
            return new LoginData(in);
        }

        @Override
        public LoginData[] newArray(int size) {
            // TODO Auto-generated method stub
            return new LoginData[size];
        }

    };

}
