package com.labis.mycl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.login.RegisterActivity;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.Register;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroBaseApiService;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.AuthManager;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    @BindView(R.id.btn_kakao_login)
//    LoginButton button_kakao;

    // Animation
    Animation animFadein;

    // Auth
    private AuthManager authManager;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    private FirebaseAnalytics mFirebaseAnalytics;

    private boolean bAutoLogin;

    private long lastTimeBackPressed;

    // kakao
    Session session;
    KakaoSessionCallback kakaoSessionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Context context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        retroClient = RetroClient.getInstance(this).createBaseApi();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        bAutoLogin = false;

        loadGenreData();

        animFadein = AnimationUtils.loadAnimation(context, R.anim.fade_in);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        if (id != null) {
            edit_email.setText(id);
            edit_password.requestFocus();

//            bAutoLogin = true;
        }

        // for test : 카카오 해시키 획득
        getHashKey(context);

//        Session.initialize(this, INDIVIDUAL);
        kakaoSessionCallback = new KakaoSessionCallback(context);
        session = Session.getCurrentSession();
        session.addCallback(kakaoSessionCallback);
//        session.checkAndImplicitOpen(); // ???
//        if (session.checkAndImplicitOpen()) {
//            // 액세스토큰 유효하거나 리프레시 토큰으로 액세스 토큰 갱신을 시도할 수 있는 경우
//        } else {
//            // 무조건 재로그인을 시켜야 하는 경우
//        }
    }



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if (authManager != null) {
            authManager.removeAuthStateListener();
        }

        session.removeCallback(kakaoSessionCallback);

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    private void loadGenreData() {
        showProgressDialog();
        retroClient.getTotalGenre(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                hideProgressDialog();

                showAlertsDialog("서버 접속에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
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
                        Log.d(TAG, "condition !bAutoLogin : " + !bAutoLogin);
                        Log.d(TAG, "condition mainLayout.getVisibility() : " + mainLayout.getVisibility());

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
                Log.d(TAG, "Failure Code : " + code);
                hideProgressDialog();

                showAlertsDialog("서버 접속에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
            }
        });
    }


    // 프로젝트의 해시키를 반환

    @Nullable
    public static String getHashKey(Context context) {
        final String TAG = "[KAKAO] KeyHash";
        String keyHash = null;

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                Log.d(TAG, keyHash);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }

        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }
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

    @OnClick(R.id.login_kakaobtn)
    void onClick_kakao(){
//        button_kakao.performClick();
//        session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
        session.open(AuthType.KAKAO_TALK, MainActivity.this);
    }

// for kakao login
//    @OnClick(R.id.login_kakaologoutbtn)
//    void onClick_kakao_logout(){
//        UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
//            @Override public void onFailure(ErrorResult errorResult) {
//                Log.e(TAG, "onFailure : " + errorResult.toString());
//            }
//            @Override public void onSessionClosed(ErrorResult errorResult) {
//                Log.e(TAG, "onSessionClosed : " + errorResult.toString());
//            }
//            @Override public void onNotSignedUp() {
//                Log.e(TAG, "onNotSignedUp");
//            }
//            @Override public void onSuccess(Long userId) {
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//            }
//        });
//    }

    @OnClick(R.id.login_guest_btn)
    void onClick_guestLogin(){
        if (authManager != null) {
            authManager.removeAuthStateListener();
        }

        User newUser = new User();
        LoginData loginData = new LoginData(newUser, genreData);
        Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
        i.putExtra("LoingData", loginData);
        i.putExtra("LoginMode", "GUEST");
        startActivity(i);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        finish();
    }

    public void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("로딩중...");
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
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                .create();
        alertDialog.show();
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
            valid = false;
            edit_email.setError("필수항목");
        }

        String password = edit_password.getText().toString();
        if (TextUtils.isEmpty(password)) {
            valid = false;
            edit_password.setError("필수항목");
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
                    Toast.makeText(MainActivity.this, "접속 실패", Toast.LENGTH_SHORT).show();
                }

                hideProgressDialog();
            }
        };

        authManager.signIn(this, completeListener, email, password);
    }

    private void signIn(final String token) {
        Log.d(TAG, "signIn() token: " + token);
//        if (!validateForm()) {
//            return;
//        }

        showProgressDialog();

        OnCompleteListener<AuthResult> completeListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signIn() token OnCompleteListener()");

                if (task.isSuccessful()) {
                    Log.d(TAG, "signIn() token:success");
                    Log.d(TAG, "task.getResult().getUser() : " + task.getResult().getUser());
                    Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());
                    authManager.setFirebaseUser(task.getResult().getUser());
                    Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());

                    String email = task.getResult().getUser().getEmail();
                    String uid = task.getResult().getUser().getUid();
                    Log.d(TAG, "email : " + email);
                    Log.d(TAG, "uid : " + uid);

                    doLogin(email, null, uid);
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "접속 실패", Toast.LENGTH_SHORT).show();
                    if (task.getException() != null) {
                        Log.e(TAG, task.getException().toString());
                    }
                }

                hideProgressDialog();
            }
        };

        authManager.signIn(this, completeListener, token);
    }

    private void doLogin(String email, String pw, String uid) {
        Log.i(TAG, "doLogin()");
        Log.i(TAG, "email: " + email +", pw: " + anonymizePassword(pw) + ", uid: " + uid);
        showProgressDialog();

        retroClient.postLogin(email, pw, uid, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.d(TAG, t.toString());
                Toast.makeText(MainActivity.this, "로그인 에러", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onFailure(int code) {
                Log.d(TAG, "FAIL");
                Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.d(TAG, "SUCCESS");
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


    /**
     *
     * @param kakaoAccessToken Access token retrieved after successful Kakao Login
     * @return Task object that will call validation server and retrieve firebase token
     */
    private Task<String> getFirebaseJwt(final String kakaoAccessToken) {

        Log.d(TAG, "[KAKAO] getFirebaseJwt");

        final TaskCompletionSource<String> source = new TaskCompletionSource<>();

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = RetroBaseApiService.Base_URL + "verifyToken";
        HashMap<String, String> validationObject = new HashMap<>();
        validationObject.put("token", kakaoAccessToken);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(validationObject), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "[KAKAO] onResponse");
                try {
                    String firebaseToken = response.getString("firebase_token");
                    source.setResult(firebaseToken);
                } catch (Exception e) {
                    source.setException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "[KAKAO] onErrorResponse");
                Log.e(TAG, error.toString());
                source.setException(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Log.d(TAG, "[KAKAO] getParams");
                Map<String, String> params = new HashMap<>();
                params.put("token", kakaoAccessToken);
                return params;
            }
        };

        queue.add(request);
        return source.getTask();
    }


    public class KakaoSessionCallback implements ISessionCallback {
        Context context = null;

        public KakaoSessionCallback(Context context) {
            this.context = context;
        }

        // 로그인에 성공한 상태
        @Override
        public void onSessionOpened() {
            Log.d(TAG, "[KAKAO] onSessionOpened");

//            requestMe();
            showProgressDialog();

            if (true) {
                Log.d(TAG, "[KAKAO] Successfully logged in to Kakao. Now creating or updating a Firebase User.");
                String accessToken = Session.getCurrentSession().getTokenInfo().getAccessToken();

                retroClient.postVerifyToken(accessToken, new RetroCallback<Register>() {
                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, t.toString());
                        Toast.makeText(MainActivity.this, "postVerifyToken 에러", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }
                    @Override
                    public void onFailure(int code) {
                        Log.d(TAG, "FAIL");
                        Toast.makeText(MainActivity.this, "postVerifyToken 실패", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }
                    @Override
                    public void onSuccess(int code, Register data) {
                        Log.d(TAG, "SUCCESS");

                        String firebaseToken = data.getId();
                        if (firebaseToken == null) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    Log.d(TAG, "[KAKAO] 로그인 실패 후 로그아웃");

                                    UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                                        @Override public void onFailure(ErrorResult errorResult) {
                                            Log.e(TAG, "onFailure : " + errorResult.toString());
                                        }
                                        @Override public void onSessionClosed(ErrorResult errorResult) {
                                            Log.e(TAG, "onSessionClosed : " + errorResult.toString());
                                        }
                                        @Override public void onNotSignedUp() {
                                            Log.e(TAG, "onNotSignedUp");
                                        }
                                        @Override public void onSuccess(Long userId) {
                                            Log.d(TAG, "[KAKAO] firebaseToken is null");
                                            Toast.makeText(MainActivity.this, "카카오계정(이메일) 체크가 필요합니다.", Toast.LENGTH_SHORT).show();
                                            hideProgressDialog();
                                        }
                                    });
                                }
                            }, 100);
                            return;
                        }
                        Log.d(TAG, "[KAKAO] token : " + firebaseToken);

                        FirebaseAuth auth = authManager.getFirebaseAuth();
                        Log.d(TAG, "[KAKAO] auth.signInWithCustomToken");
//                        auth.signInWithCustomToken(firebaseToken);

                        signIn(firebaseToken);
                    }
                });
            } else {
                //            Toast.makeText(getApplicationContext(), "Successfully logged in to Kakao. Now creating or updating a Firebase User.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "[KAKAO] Successfully logged in to Kakao. Now creating or updating a Firebase User.");
                String accessToken = Session.getCurrentSession().getTokenInfo().getAccessToken();
                getFirebaseJwt(accessToken).continueWithTask(new Continuation<String, Task<AuthResult>>() {
                    @Override
                    public Task<AuthResult> then(@NonNull Task<String> task) {
                        Log.d(TAG, "[KAKAO] then");
                        String firebaseToken = task.getResult();
                        FirebaseAuth auth = authManager.getFirebaseAuth();
                        return auth.signInWithCustomToken(firebaseToken);
                    }
                }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "[KAKAO] onComplete");
                        if (task.isSuccessful()) {
//                        Toast.makeText(getApplicationContext(), "continueWithTask addOnCompleteListener", Toast.LENGTH_LONG).show();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Log.d(TAG, "task.getResult().getUser() : " + task.getResult().getUser());
                            Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());
                            authManager.setFirebaseUser(task.getResult().getUser());
                            Log.d(TAG, "authManager.getmFirebaseUser() : " + authManager.getmFirebaseUser());

                            String email = task.getResult().getUser().getEmail();
                            String uid = task.getResult().getUser().getUid();
                            Log.d(TAG, "email : " + email);
                            Log.d(TAG, "uid : " + uid);

                            doLogin(email, null, uid);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to create a Firebase user.", Toast.LENGTH_LONG).show();
                            if (task.getException() != null) {
                                Log.e(TAG, task.getException().toString());
                            }
                        }
                    }
                });
            }
        }

        // 로그인에 실패한 상태
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.d(TAG, "[KAKAO] onSessionOpenFailed");
            Log.e(TAG, "onSessionOpenFailed : " + exception.getMessage());
        }

        // 사용자 정보 요청
        public void requestMe_org() {
            // 사용자정보 요청 결과에 대한 Callback
            UserManagement.getInstance().requestMe(new MeResponseCallback() {
                // 세션 오픈 실패. 세션이 삭제된 경우,
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.e(TAG, "requestMe onSessionClosed : " + errorResult.getErrorMessage());
                }

                // 회원이 아닌 경우,
                @Override
                public void onNotSignedUp() {
                    Log.e(TAG, "requestMe onNotSignedUp");
                }

                // 사용자정보 요청에 성공한 경우,
                @Override
                public void onSuccess(UserProfile userProfile) {
                    Log.e(TAG, "requestMe onSuccess");
                    Log.e(TAG, "requestMe onSuccess");
                    String nickname = userProfile.getNickname();
                    String email = userProfile.getEmail();
                    String profileImagePath = userProfile.getProfileImagePath();
                    String thumnailPath = userProfile.getThumbnailImagePath();
                    String UUID = userProfile.getUUID();
                    long id = userProfile.getId();

                    Log.e(TAG, "nickname : " + nickname + "");
                    Log.e(TAG, "email : " + email + "");
                    Log.e(TAG, "profileImagePath : " + profileImagePath  + "");
                    Log.e(TAG, "thumnailPath : " + thumnailPath + "");
                    Log.e(TAG, "UUID : " + UUID + "");
                    Log.e(TAG, "id : " + id + "");
                }

                // 사용자 정보 요청 실패
                @Override
                public void onFailure(ErrorResult errorResult) {
                    Log.e(TAG, "requestMe onFailure : " + errorResult.getErrorMessage());
                }
            });
        }


        private void handleScopeError(UserAccount account) {
            List<String> neededScopes = new ArrayList<>();
            if (account.needsScopeAccountEmail()) {
                neededScopes.add("account_email");
            }
            if (account.needsScopeGender()) {
                neededScopes.add("gender");
            }
//            Session.getCurrentSession().updateScopes(MainActivity.this, neededScopes, new
//                    AccessTokenCallback() {
//                        @Override
//                        public void onAccessTokenReceived(AccessToken accessToken) {
//                            // 유저에게 성공적으로 동의를 받음. 토큰을 재발급 받게 됨.
//                        }
//
//                        @Override
//                        public void onAccessTokenFailure(ErrorResult errorResult) {
//                            // 동의 얻기 실패
//                        }
//                    });
        }


        private void requestMe() {
            List<String> keys = new ArrayList<>();
            keys.add("properties.nickname");
            keys.add("properties.profile_image");
            keys.add("kakao_account.email");

            UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    String message = "failed to get user info. msg=" + errorResult;
                    Log.d(TAG, message);
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
//                    redirectLoginActivity();
                    Log.d(TAG, "onSessionClosed");
                }

                @Override
                public void onSuccess(MeV2Response response) {
                    Log.d(TAG,"user id : " + response.getId());
                    Log.d(TAG,"email: " + response.getKakaoAccount().getEmail());
//                    Log.d(TAG,"profile image: " + response.getKakaoAccount().getProfileImagePath());
//                    redirectMainActivity();
                }

//                @Override
//                public void onNotSignedUp() {
////                    showSignup();
//                    Log.d(TAG, "onNotSignedUp");
//                }
            });
        }


        public void logout() {
            Log.e(TAG, "Kakao requestLogout");

            UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                @Override
                public void onCompleteLogout() {
                    Log.e(TAG, "requestLogout onCompleteLogout");

                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }
            });
        }


    }
}
