package com.labis.mycl.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
import com.labis.mycl.R;


public class RegisterActivity extends Activity {
    EditText email,password;
    Button login,register;
    String emailtxt,passwordtxt;
//    List<NameValuePair> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        register = (Button)findViewById(R.id.registerbtn);
        login = (Button)findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regactivity = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(regactivity);
                finish();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                emailtxt = email.getText().toString();
                passwordtxt = password.getText().toString();
//                params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("email", emailtxt));
//                params.add(new BasicNameValuePair("password", passwordtxt));
//                ServerRequest sr = new ServerRequest();
//                JSONObject json = sr.getJSON("http://10.0.2.2:8080/register",params);
//
//                if(json != null){
//                    try{
//                        String jsonstr = json.getString("response");
//
//                        Toast.makeText(getApplication(),jsonstr,Toast.LENGTH_LONG).show();
//
//                        Log.d("Hello", jsonstr);
//                    }catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
    }




}
