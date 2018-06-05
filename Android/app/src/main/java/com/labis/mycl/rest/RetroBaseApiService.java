package com.labis.mycl.rest;

import com.labis.mycl.model.Content;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.Register;
import com.labis.mycl.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by sonchangwoo on 2017. 1. 1..
 */

public interface RetroBaseApiService {

    final String Base_URL = "http://ec2-18-188-106-154.us-east-2.compute.amazonaws.com:9000/MyCL/";
    //http://ec2-18-188-106-154.us-east-2.compute.amazonaws.com:9000/MyCL/user?id=khercules

    // http://ec2-18-188-106-154.us-east-2.compute.amazonaws.com:9000/MyCL/contents?id=0001&gen_id=A01

    /// POST API ////////////////////////////////////////////////////////////////
    @FormUrlEncoded
    @POST("login")
    Call<List<User>> postLogin(@Field("id") String id, @Field("pw") String pw, @Field("uid") String uid);

    @FormUrlEncoded
    @POST("check_id_token")
    Call<List<User>> postCheckIdToken(@Field("id") String id, @Field("uid") String uid, @Field("idToken") String idToken);

    @FormUrlEncoded
    @POST("register")
    Call<Register> postRegister(@Field("id") String id, @Field("pw") String pw,
                                @Field("age") String age, @Field("gender") String gender,
                                @Field("nickname") String nickname, @Field("phone") String phone, @Field("image") String image, @Field("uid") String uid);

    @FormUrlEncoded
    @POST("user")
    Call<List<User>> getUser(@Query("id") String id);

    @FormUrlEncoded
    @POST("my_contents")
    Call<List<Content>> postMyContents(@Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("insert_my_contents")
    Call<Register> postInsertMyContents(@Field("user_id") String user_id, @Field("id_list") ArrayList<String> id_list);

    @FormUrlEncoded
    @POST("delete_my_contents")
    Call<Register> postDeleteMyContents(@Field("user_id") String user_id, @Field("id_list") ArrayList<String> id_list);

    @FormUrlEncoded
    @POST("update_my_contents")
    Call<Register> postUpdateMyContents(@Field("id") String id, @Field("user_id") String user_id, @Field("chapter") int chapter);

    @FormUrlEncoded
    @POST("total_contents")
    Call<List<Content>> postTotalContents(@Field("user_id") String user_id);

    @FormUrlEncoded
    @POST("filter_my_contents")
    Call<List<Content>> postFilterMyContents(@Field("user_id") String user_id, @Field("gen_id") String gen_id);

    @FormUrlEncoded
    @POST("insert_contents_list")
    Call<Register> postInserCustomContents(@Field("gen_id") String gen_id, @Field("season") int season,
                                           @Field("name") String name, @Field("name_org") String name_org,
                                           @Field("theatrical") int theatrical,
                                           @Field("summary") String summary,
                                           @Field("publisher") String publisher, @Field("auth") int auth,
                                           @Field("image") String image);

    @FormUrlEncoded
    @POST("update_contents_image")
    Call<Register> updateContentsImage(@Field("id") String id, @Field("url") String url);

    @FormUrlEncoded
    @POST("filter_contents_list")
    Call<List<Content>> postFilterContentsList(@Field("gen_id") String gen_id);


    /// GET API ////////////////////////////////////////////////////////////////


    @GET("total_genre")
    Call<List<Genre>> getTotalGenre();
}
