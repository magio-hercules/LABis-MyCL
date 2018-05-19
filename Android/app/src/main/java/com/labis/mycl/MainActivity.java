package com.labis.mycl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.contents.CustomActivity;
import com.labis.mycl.contents.RecyclerViewAdapter;
import com.labis.mycl.login.LoginActivity;
import com.labis.mycl.login.RegisterActivity;
import com.labis.mycl.model.Content;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    RetroClient retroClient;
    ArrayList<Genre> genreData;
    User userData;

    private ProgressDialog progressDoalog = null;

    @BindView(R.id.main_layout)
    LinearLayout mainLayout;

    @BindView(R.id.login_email)
    EditText edit_email;

    @BindView(R.id.login_password)
    EditText edit_password;

    // Animation
    Animation animFadein;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        retroClient = RetroClient.getInstance(this).createBaseApi();

        //-- ProgressDialog Setting --//
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        loadGenreData();

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
    }

    private void loadGenreData() {
        progressDoalog.show();
        retroClient.getTotalGenre(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "GENRE LOAD SUCCESS");
                progressDoalog.dismiss();
                genreData = (ArrayList<Genre>) receivedData;
                mainLayout.setVisibility(View.VISIBLE);
                mainLayout.setAnimation(animFadein);
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "GENRE LOAD FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }


    @OnClick(R.id.login_loginbtn)
    void onClick_login(){
        String str_email = edit_email.getText().toString();
        String str_pw = edit_password.getText().toString();
        Log.e(TAG, "mail: " + str_email +", pw: "+str_pw);

        progressDoalog.show();
        retroClient.postLogin(str_email, str_pw, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                progressDoalog.dismiss();
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "SUCCESS");
                progressDoalog.dismiss();
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
                progressDoalog.dismiss();
                Toast.makeText(getApplicationContext(), "[" + code + "]에러 발생 - 로그인 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.login_registerbtn)
    void onClick_register(){
        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(i);
    }
}
