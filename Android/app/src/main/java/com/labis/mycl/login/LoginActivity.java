package com.labis.mycl.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
import com.labis.mycl.R;
import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.rest.models.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends Activity {
    private static final String LOG = "[LOGIN]";

    RetroClient retroClient;

    Intent intent;

    @BindView(R.id.login_email)
    EditText edit_email;
    @BindView(R.id.login_password)
    EditText edit_password;
    @BindView(R.id.login_loginbtn)
    Button btn_login;
    @BindView(R.id.login_registerbtn)
    Button btn_register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        retroClient = RetroClient.getInstance(this).createBaseApi();

        intent = getIntent();
        String id = intent.getStringExtra("id");
        if (id != null) {
            edit_email.setText(id);
            edit_password.requestFocus();
        }
    }


    @OnClick(R.id.login_loginbtn)
    void onClick_login(){
        String str_email = edit_email.getText().toString();
        String str_pw = edit_password.getText().toString();

        Log.e(LOG, "mail: " + str_email +", pw: "+str_pw);

        retroClient.postLogin(str_email, str_pw, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(LOG, t.toString());
                Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(LOG, "SUCCESS");
                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(LoginActivity.this, ContentsActivity.class);
                startActivity(i);
            }

            @Override
            public void onFailure(int code) {
                Log.e(LOG, "FAIL");
                Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.login_registerbtn)
    void onClick_register(){
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }

}
