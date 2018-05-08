package com.labis.mycl.model;

public class User {
    public final String id;
    public final String pw;
    public final String age;
    public final String gender;
    public final String nickname;
    public final String phone;
    public final String fav_genre;
    public final String fav_id;

    public User(String id, String pw, String age, String gender, String nickname,String phone, String fav_genre, String fav_id) {
        this.id = id;
        this.pw = pw;
        this.age = age;
        this.gender = gender;
        this.nickname = nickname;
        this.phone = phone;
        this.fav_genre = fav_genre;
        this.fav_id = fav_id;
    }
}
