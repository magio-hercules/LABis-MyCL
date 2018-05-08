package com.labis.mycl.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
import com.labis.mycl.R;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.model.Register;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RegisterActivity extends Activity {
    private static final String LOG = "[REGISTER]";

    RetroClient retroClient;

    @BindView(R.id.register_image)
    ImageView register_image;
    @BindView(R.id.register_email)
    EditText register_email;
    @BindView(R.id.register_password)
    EditText register_password;
    @BindView(R.id.register_age)
    EditText register_age;
    @BindView(R.id.register_gender)
    EditText register_gender;
    @BindView(R.id.register_nickname)
    EditText register_nickname;
    @BindView(R.id.register_phone)
    EditText register_phone;
    @BindView(R.id.register_registerbtn)
    Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        retroClient = RetroClient.getInstance(this).createBaseApi();
    }


    @OnClick(R.id.register_registerbtn)
    void onClick_register(){
        String str_email = register_email.getText().toString();
        String str_pw = register_password.getText().toString();
        String str_age = register_age.getText().toString();
        String str_gender = register_gender.getText().toString();
        String str_nickname = register_nickname.getText().toString();
        String str_phone = register_phone.getText().toString();

        Log.e(LOG, "email: " + str_email + ", pw: "+str_pw+ ", age: "+str_age
                         + ", gender: "+str_gender+ ", nick: "+str_nickname+ ", phone: "+str_phone);

        retroClient.postRegister(str_email, str_pw, str_age, str_gender, str_nickname, str_phone, new RetroCallback<Register>() {
            @Override
            public void onError(Throwable t) {
                Log.e(LOG, t.toString());
                Toast.makeText(RegisterActivity.this, "Register Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int code, Register data) {
                Log.e(LOG, "SUCCESS");
                Toast.makeText(RegisterActivity.this, data.getReason(), Toast.LENGTH_SHORT).show();

                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.putExtra("id", data.getId());
                startActivity(i);
            }

            @Override
            public void onFailure(int code) {
                Log.e(LOG, "FAIL");
                Toast.makeText(RegisterActivity.this, "Register Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
