package com.labis.mycl.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.labis.mycl.R;
import com.labis.mycl.contents.ContentsActivity;
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

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;


public class LoginActivity extends Activity {
    private static final String TAG = "[LOGIN]";

    RetroClient retroClient;

    Intent intent;

    @BindView(R.id.login_email)
    EditText edit_email;
    @BindView(R.id.login_password)
    EditText edit_password;
    @BindView(R.id.login_detail)
    EditText edit_detail;
    @BindView(R.id.login_loginbtn)
    Button btn_login;
    @BindView(R.id.login_registerbtn)
    Button btn_register;

    // for S3 url
    @BindView(R.id.login_s3)
    Button btn_login_s3;

    ArrayList<Genre> genreData = null;
    User userData = null;

    // for Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @VisibleForTesting
    public ProgressDialog mProgressDialog;


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

//        genreData = getIntent().getParcelableArrayListExtra("genre");

        loadGenreData();

        // [START initialize_auth]
        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged()");
                    Log.d(TAG, "onAuthStateChanged:signed_in : UID (" + mFirebaseUser.getUid() + ")");
//                    Log.d(TAG, "onAuthStateChanged:signed_in : IdToken (" + mFirebaseUser.getIdToken(true).getResult().getToken() + ")");

                    showProgressDialog();

//                    autoLogin();
                    autoLogin2();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };


        // [END initialize_auth]
    }

    @Override
    protected void onStart() {
        super.onStart();

        mFirebaseAuth.addAuthStateListener(mAuthListener);

        // 활동을 초기화할 때 사용자가 현재 로그인되어 있는지 확인
        // Check if user is signed in (non-null) and update UI accordingly.
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            boolean emailVerified = mFirebaseUser.isEmailVerified();
            Log.i(TAG, "로그인 된 사용자 (" + mFirebaseUser.getUid() + ")");
            Log.i(TAG, "emailVerified (" + emailVerified + ")");
            updateUI(mFirebaseUser);
//            doLogin();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @OnClick(R.id.login_loginbtn)
    void onClick_login(){
        String email = edit_email.getText().toString();
        String password = edit_password.getText().toString();

        Log.d(TAG, "email : " + email + ", password : " + password);

        signIn(email, password);
    }

    private void loadGenreData() {
//        progressDoalog.show();
        retroClient.getTotalGenre(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
//                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "GENRE LOAD SUCCESS");
//                progressDoalog.dismiss();
                genreData = (ArrayList<Genre>) receivedData;
//                mainLayout.setVisibility(View.VISIBLE);
//                mainLayout.setAnimation(animFadein);
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "GENRE LOAD FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
//                progressDoalog.dismiss();
            }
        });
    }

    @OnClick(R.id.login_registerbtn)
    void onClick_register(){
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }


    // for S3 URL 변경기능
    @OnClick(R.id.login_s3)
    void onClick_login_s3(){
        Intent i = new Intent(LoginActivity.this, UrlActivity.class);
        startActivity(i);
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

    private boolean validateForm() {
        boolean valid = true;

        String email = edit_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Email Required.", Toast.LENGTH_SHORT).show();
            valid = false;
            edit_email.setError("Required.");
        } else {
//            mEmailField.setError(null);
        }

        String password = edit_password.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Email Required.", Toast.LENGTH_SHORT).show();
            valid = false;
            edit_password.setError("Required.");
        } else {
//            mPasswordField.setError(null);
        }

        return valid;
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(User user) {
        Log.d(TAG, "signIn() Token 정보 : " + user.token);

        mFirebaseAuth.signInWithCustomToken(user.token)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success");
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            updateUI(mFirebaseUser);

                            Toast.makeText(getApplicationContext(), mFirebaseUser.getEmail() + "님 로그인 성공", Toast.LENGTH_SHORT).show();

                            LoginData loginData = new LoginData(userData, genreData);
                            Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                            i.putExtra("LoingData", loginData);
                            startActivity(i);
                            finish();
                            hideProgressDialog();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            hideProgressDialog();
                        }
                    }
                });

    }


    private void signIn(String token) {
        Log.d(TAG, "signIn() Token 정보 : " + token);

        mFirebaseAuth.signInWithCustomToken(token)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success");
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
//                            updateUI(mFirebaseUser);
//
//                            Toast.makeText(getApplicationContext(), mFirebaseUser.getEmail() + "님 로그인 성공", Toast.LENGTH_SHORT).show();
//
//                            LoginData loginData = new LoginData(userData, genreData);
//                            Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
//                            i.putExtra("LoingData", loginData);
//                            startActivity(i);
//                            finish();
                            doLogin(mFirebaseUser.getEmail(), null, mFirebaseUser.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }


    private void signIn(final String email, final String password) {
        Log.d(TAG, "signIn() email: " + email + ", password: " + password);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
//                            updateUI(mFirebaseUser);
                            doLogin(email, password, null);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        if (!task.isSuccessful()) {
//                            mStatusTextView.setText(R.string.auth_failed);
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signOut() {
        Log.d(TAG, "signOut");
        mFirebaseAuth.signOut();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        Log.d(TAG, "updateUI");

//        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                    user.getEmail(), user.isEmailVerified()));
            Log.d(TAG, "user != null");
            edit_email.setText(user.getEmail().toString());
            edit_detail.setText(user.getUid() + ", "  + user.getIdToken(true));
//            edit_detail.setText(user.getUid() + ", "  + user.getIdToken(true).getResult().getToken());
//            edit_detail.setText(user.getUid());
        } else {
            Log.d(TAG, "user == null");
            edit_email.setText("");
            edit_password.setText("");
            edit_detail.setText("");
        }
    }

    private void autoLogin() {
        Log.d(TAG, "autoLogin");

        final String str_email = mFirebaseUser.getEmail();
        final String str_uid   = mFirebaseUser.getUid();

        mFirebaseUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        Log.d(TAG, "autoLogin onComplete");
                        if (task.isSuccessful()) {
                            final String str_idToken = task.getResult().getToken();
                            Log.d(TAG, "id : " + str_email);
                            Log.d(TAG, "uid : " + str_uid);
                            Log.d(TAG, "idToken : " + str_idToken);
                            // Send token to your backend via HTTPS
                            // TODO
                            // uid와 idToken을 보내 백엔드에서 유저 확인

                            retroClient.postCheckIdToken(str_email, str_uid, str_idToken, new RetroCallback() {
                                @Override
                                public void onError(Throwable t) {
                                    Log.e(TAG, t.toString());
                                    Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onFailure(int code) {
                                    Log.e(TAG, "FAIL");
                                    Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onSuccess(int code, Object receivedData) {
                                    Log.e(TAG, "postCheckIdToken SUCCESS");
                                    Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                                    userData = ((List<User>)receivedData).get(0);
//                                    doLogin(str_email, "", str_uid);
                                    signIn(userData.token);
                                }
                            });
                        } else {
                            // Handle error -> task.getException();
                            Log.d(TAG, "getIdToken:failure", task.getException());
                        }
                    }
                });
    }

    private void autoLogin2() {
        Log.d(TAG, "autoLogin2");

        final String str_email = mFirebaseUser.getEmail();
        final String str_uid   = mFirebaseUser.getUid();

        retroClient.postLogin(str_email, null, str_uid, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onFailure(int code) {
                Log.e(TAG, "FAIL");
                Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "SUCCESS");
                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                userData = ((List<User>)receivedData).get(0);

                LoginData loginData = new LoginData(userData, genreData);
                Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                i.putExtra("LoingData", loginData);
                startActivity(i);
                finish();
                hideProgressDialog();
            }
        });
    }

    private void doLogin(String email, String pw, String uid) {
        Log.i(TAG, "doLogin()");
        Log.i(TAG, "email: " + email +", pw: " + pw + ", uid: " + uid);
        showProgressDialog();

        retroClient.postLogin(email, pw, uid, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onFailure(int code) {
                Log.e(TAG, "FAIL");
                Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(TAG, "SUCCESS");
                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();

                userData = ((List<User>)receivedData).get(0);

                LoginData loginData = new LoginData(userData, genreData);
                Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                i.putExtra("LoingData", loginData);
                startActivity(i);
                finish();
                hideProgressDialog();
            }
        });
    }
}
