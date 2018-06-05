package com.labis.mycl.rest;


import android.content.Context;

import com.labis.mycl.model.Content;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.Register;
import com.labis.mycl.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sonchangwoo on 2017. 1. 6..
 */

public class RetroClient {

    private RetroBaseApiService apiService;
    public static String baseUrl = RetroBaseApiService.Base_URL;
    private static Context mContext;
    private static Retrofit retrofit;

    private static class SingletonHolder {
        private static RetroClient INSTANCE = new RetroClient(mContext);
    }

    public static RetroClient getInstance(Context context) {
        if (context != null) {
            mContext = context;
        }
        return SingletonHolder.INSTANCE;
    }

    private RetroClient(Context context) {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build();
    }

    public RetroClient createBaseApi() {
        apiService = create(RetroBaseApiService.class);
        return this;
    }

    /**
     * create you ApiService
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public  <T> T create(final Class<T> service) {
        if (service == null) {
            throw new RuntimeException("Api service is null!");
        }
        return retrofit.create(service);
    }

    public void getUser(String id, final RetroCallback callback) {
        apiService.getUser(id).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    /// GET API ////////////////////////////////////////////////////////////////
    public void getTotalGenre(final RetroCallback callback) {
        apiService.getTotalGenre().enqueue(new Callback<List<Genre>>() {
            @Override
            public void onResponse(Call<List<Genre>> call, Response<List<Genre>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Genre>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    /// POST API ////////////////////////////////////////////////////////////////
    public void postTotalContents(String userid, final RetroCallback callback) {
        apiService.postTotalContents(userid).enqueue(new Callback<List<Content>>() {
            @Override
            public void onResponse(Call<List<Content>> call, Response<List<Content>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Content>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postMyContents(String userid, final RetroCallback callback) {
        apiService.postMyContents(userid).enqueue(new Callback<List<Content>>() {
            @Override
            public void onResponse(Call<List<Content>> call, Response<List<Content>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Content>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postInsertMyContents(String userid, ArrayList<String> id_list, final RetroCallback callback) {
        apiService.postInsertMyContents(userid, id_list).enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postUpdateMyContents(String id, String userid, int chapter, final RetroCallback callback) {
        apiService.postUpdateMyContents(id, userid, chapter).enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postDeleteMyContents(String userid, ArrayList<String> id_list, final RetroCallback callback) {
        apiService.postDeleteMyContents(userid, id_list).enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postInserCustomContents(String gen_id, int season, String name, String name_org,
                                        int theatrical, String summary, String publisher, int auth, String image, final RetroCallback callback) {
        apiService.postInserCustomContents( gen_id,  season,  name,  name_org,
                 theatrical, summary, publisher,  auth,  image).enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postLogin(String id, String pw, String uid, final RetroCallback callback) {
        apiService.postLogin(id, pw, uid).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postCheckIdToken(String id, String uid, String idToken, final RetroCallback callback) {
        apiService.postCheckIdToken(id, uid, idToken).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postRegister(String id, String pw, String age, String gender,
                               String nickname, String phone, String image, String uid, final RetroCallback callback) {
        apiService.postRegister(id, pw, age, gender, nickname, phone, image, uid).enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void updateContentsImage(String id, String url, final RetroCallback callback) {
        apiService.updateContentsImage(id, url).enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<Register> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postFilterMyContents(String user_id, String gen_id, final RetroCallback callback) {
        apiService.postFilterMyContents(user_id, gen_id).enqueue(new Callback<List<Content>>() {
            @Override
            public void onResponse(Call<List<Content>> call, Response<List<Content>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Content>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void postFilterContentsList(String gen_id, final RetroCallback callback) {
        apiService.postFilterContentsList(gen_id).enqueue(new Callback<List<Content>>() {
            @Override
            public void onResponse(Call<List<Content>> call, Response<List<Content>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.code(), response.body());
                } else {
                    callback.onFailure(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Content>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
