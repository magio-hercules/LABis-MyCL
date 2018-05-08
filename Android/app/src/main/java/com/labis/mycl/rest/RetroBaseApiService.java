package com.labis.mycl.rest;

import java.util.List;

import com.labis.mycl.model.Content;
import com.labis.mycl.model.Register;
import com.labis.mycl.model.User;
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

    @FormUrlEncoded
    @POST("login")
    Call<List<User>> postLogin(@Field("id") String id, @Field("pw") String pw);

    @FormUrlEncoded
    @POST("register")
    Call<Register> postRegister(@Field("id") String id, @Field("pw") String pw,
                                @Field("age") String age, @Field("gender") String gender,
                                @Field("nickname") String nickname, @Field("phone") String phone);

    @GET("user")
    Call<List<User>> getUser(@Query("id") String id);

    @GET("contents")
    Call<List<Content>> getContents(@Query("id") String id, @Query("gen_id") String gen_id);

}
