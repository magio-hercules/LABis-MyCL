package com.labis.mycl.rest;

import java.util.List;

import com.labis.mycl.rest.models.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by sonchangwoo on 2017. 1. 1..
 */

public interface RetroBaseApiService {

    final String Base_URL = "http://ec2-18-188-106-154.us-east-2.compute.amazonaws.com:9000/MyCL/";
    //http://ec2-18-188-106-154.us-east-2.compute.amazonaws.com:9000/MyCL/user?id=khercules

    @GET("user")
    Call<List<User>> getUser(@Query("id") String id);

}
