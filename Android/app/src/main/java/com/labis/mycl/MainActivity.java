package com.labis.mycl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.login.RegisterActivity;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.AlertDialogHelper;
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
    private AuthManager authManager;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;

    private boolean bAutoLogin;

    private long lastTimeBackPressed;

    AlertDialogHelper alertDialogHelper = null;
    private boolean isShowAlertDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        retroClient = RetroClient.getInstance(this).createBaseApi();

        bAutoLogin = false;

        loadGenreData();

        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

        // TODO
        // for test
        //edit_email.setText("labis@labis.com");
        //edit_password.setText("123456");

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        if (id != null) {
            edit_email.setText(id);
            edit_password.requestFocus();

            bAutoLogin = true;
        }

        // -- Delete Dialog --//
        alertDialogHelper = new AlertDialogHelper(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if (authManager != null) {
            authManager.removeAuthStateListener();
        }
        super.onDestroy();
    }

    private void loadGenreData() {
        showProgressDialog();
        retroClient.getTotalGenre(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                hideProgressDialog();
               /* if (isShowAlertDialog == false) {
                    isShowAlertDialog = true;
                    showAlertsDialog("서버에 접속 실패하였습니다. 잠시 후 다시 시도해주세요");
                }*/
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.d(TAG, "GENRE LOAD SUCCESS");

                genreData = (ArrayList<Genre>) receivedData;

                initAuth();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        hideProgressDialog();
                        if (!bAutoLogin && mainLayout.getVisibility() == View.GONE) {
                            mainLayout.setVisibility(View.VISIBLE);
                            mainLayout.setAnimation(animFadein);
                        }
                    }
                }, 1000);
            }

            @Override
            public void onFailure(int code) {
                Log.d(TAG, "GENRE LOAD FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });
    }


    private void initAuth() {
        Log.d(TAG, "initAuth()");

        authManager = AuthManager.getInstance();

        currentUser = authManager.getmFirebaseUser();
        if (currentUser != null)
            Log.d(TAG, "initAuth currentUID : (" + currentUser.getUid() + ")");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = authManager.getmFirebaseUser();
                FirebaseUser user2 = firebaseAuth.getCurrentUser();

                if (currentUser != null)
                    Log.d(TAG, "onAuthStateChanged currentUser : (" + currentUser.getUid() + ")");
                if (user != null)
                    Log.d(TAG, "onAuthStateChanged uid 1 : (" + user.getUid() + ")");
                if (user2 != null)
                    Log.d(TAG, "onAuthStateChanged uid 2 : (" +  user2.getUid() + ")");

                // firebaseAuth.getCurrentUser();
                if (user != null && !bAutoLogin) {
                    bAutoLogin = true;

                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged()");
                    Log.d(TAG, "onAuthStateChanged:signed_in : UID (" + user.getUid() + ")");
                    Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());

                    showProgressDialog();
                    doLogin(user.getEmail(), null, user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        authManager.addAuthStateListener(mAuthListener);
    }

    @OnClick(R.id.login_loginbtn)
    void onClick_login(){
        String str_email = edit_email.getText().toString();
        String str_pw = edit_password.getText().toString();
        Log.d(TAG, "mail: " + str_email + ", pw: " + anonymizePassword(str_pw));

        signIn(str_email, str_pw);
    }

    @OnClick(R.id.login_registerbtn)
    void onClick_register(){
        if (authManager != null) {
            authManager.removeAuthStateListener();
        }

        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(i);
    }

    public void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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

    public void showAlertsDialog(String message) {
        alertDialogHelper.setAlertDialogListener(new AlertDialogHelper.AlertDialogListener() {
            @Override
            public void onPositiveClick(int from) {
                //forceExitActivity();
                finish();
            }
            @Override
            public void onNegativeClick(int from){}
            @Override
            public void onNeutralClick(int from){}
        });
        alertDialogHelper.showAlertDialog("", message, "확인", 1, false);
    }

    private void forceExitActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 1000);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = edit_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MainActivity.this, "Email Required.", Toast.LENGTH_SHORT).show();
            valid = false;
            edit_email.setError("Required.");
        }

        String password = edit_password.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Email Required.", Toast.LENGTH_SHORT).show();
            valid = false;
            edit_password.setError("Required.");
        }

        return valid;
    }

    private void signIn(final String email, final String password) {
        Log.d(TAG, "signIn() email: " + email + ", password: " + anonymizePassword(password));
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        OnCompleteListener<AuthResult> completeListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "OnCompleteListener()");

                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    Log.d(TAG, "task.getResult().getUser() : " + task.getResult().getUser());
                    Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());
                    authManager.setFirebaseUser(task.getResult().getUser());
                    Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());

                    doLogin(email, password, task.getResult().getUser().getUid());
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }

                if (!task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        };

        authManager.signIn(this, completeListener, email, password);
    }

    private void doLogin(String email, String pw, String uid) {
        Log.i(TAG, "doLogin()");
        Log.i(TAG, "email: " + email +", pw: " + anonymizePassword(pw) + ", uid: " + uid);
        showProgressDialog();

        retroClient.postLogin(email, pw, uid, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(MainActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onFailure(int code) {
                Log.d(TAG, "FAIL");
                Toast.makeText(MainActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.d(TAG, "SUCCESS");
                Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                userData = ((List<User>)receivedData).get(0);

                hideProgressDialog();

                LoginData loginData = new LoginData(userData, genreData);
                Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                i.putExtra("LoingData", loginData);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }
        });
    }

    private String anonymizePassword(String password) {
        if (password == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
