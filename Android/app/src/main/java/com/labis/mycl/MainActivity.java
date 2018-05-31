package com.labis.mycl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.login.LoginActivity;
import com.labis.mycl.login.RegisterActivity;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.AuthManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "[MainActivity]";

    RetroClient retroClient;
    ArrayList<Genre> genreData;
    User userData;

    private ProgressDialog mProgressDialog = null;

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;

    @BindView(R.id.login_email)
    EditText edit_email;

    @BindView(R.id.login_password)
    EditText edit_password;

    // Animation
    Animation animFadein;

    // Auth
    private AuthManager authManger;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private boolean bAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        retroClient = RetroClient.getInstance(this).createBaseApi();

        bAutoLogin = false;

        //-- ProgressDialog Setting --//
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("로딩중 ....");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        loadGenreData();

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        authManger = AuthManager.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = authManger.getmFirebaseUser();
                if (user != null) {
                    bAutoLogin = true;

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged()");
                    Log.d(TAG, "onAuthStateChanged:signed_in : UID (" + user.getUid() + ")");
//                    Log.d(TAG, "onAuthStateChanged:signed_in : IdToken (" + mFirebaseUser.getIdToken(true).getResult().getToken() + ")");

                    showProgressDialog();
                    autoLogin(user.getEmail(), user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        authManger.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        authManger.removeAuthStateListener();
        super.onDestroy();
    }

    private void loadGenreData() {
        showProgressDialog();
        retroClient.getTotalGenre(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "GENRE LOAD SUCCESS");

                genreData = (ArrayList<Genre>) receivedData;

//                mainLayout.setVisibility(View.VISIBLE);
//                mainLayout.setAnimation(animFadein);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        hideProgressDialog();

                        if (!bAutoLogin) {
                            Intent i = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                            finish();
                        }
                    }
                }, 1000);
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "GENRE LOAD FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });
    }


    @OnClick(R.id.login_loginbtn)
    void onClick_login(){
        String str_email = edit_email.getText().toString();
        String str_pw = edit_password.getText().toString();
        Log.e(TAG, "mail: " + str_email +", pw: "+str_pw);

        showProgressDialog();
        retroClient.postLogin(str_email, str_pw, null, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "SUCCESS");
                hideProgressDialog();
                userData = ((List<User>)receivedData).get(0);
                Toast.makeText(getApplicationContext(), userData.id + "님 로그인 성공", Toast.LENGTH_SHORT).show();
                LoginData loginData = new LoginData(userData, genreData);
                Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                i.putExtra("LoingData", loginData);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "FAIL");
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "[" + code + "] 에러 발생 - 로그인 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.login_registerbtn)
    void onClick_register(){
        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(i);
        finish();
    }

    @OnClick(R.id.login_testbtn)
    void onClick_loginTest(){
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog() {
        Log.d(TAG, "hideProgressDialog");

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void autoLogin(String email, String uid) {
        Log.d(TAG, "autoLogin");

        retroClient.postLogin(email, null, uid, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(MainActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onFailure(int code) {
                Log.e(TAG, "FAIL");
                Toast.makeText(MainActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "SUCCESS");
                Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                userData = ((List<User>)receivedData).get(0);

                hideProgressDialog();

                LoginData loginData = new LoginData(userData, genreData);
                Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                i.putExtra("LoingData", loginData);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }});
    }
}
