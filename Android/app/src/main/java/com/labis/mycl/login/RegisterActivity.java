package com.labis.mycl.login;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.labis.mycl.MainActivity;
import com.labis.mycl.R;
import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.Register;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.AuthManager;
import com.labis.mycl.util.CheckPermission;
import com.labis.mycl.util.CircleTransform;
import com.labis.mycl.util.ImagePicker;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "[REGISTER]";

    RetroClient retroClient;

    @BindView(R.id.register_image)
    ImageView register_image;
    @BindView(R.id.register_email)
    EditText register_email;
    @BindView(R.id.register_password)
    EditText register_password;
    @BindView(R.id.register_password_verify)
    EditText register_password_verify;
    @BindView(R.id.register_age)
    Spinner register_age;
    @BindView(R.id.register_nickname)
    EditText register_nickname;
    @BindView(R.id.register_phone)
    EditText register_phone;
    @BindView(R.id.register_registerbtn)
    Button btn_register;
    @BindView(R.id.register_cancel)
    Button btn_cancle;
    @BindView(R.id.register_changePw)
    Button btn_change_pw;

    @BindView(R.id.register_gender_male)
    RadioButton register_gender_male;
    @BindView(R.id.register_gender_female)
    RadioButton register_gender_female;

    // for S3
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;

    // Image 선택
    private final int CAMERA_CODE   = 1111;
    private final int GALLERY_CODE  = 1112;
    private ImagePicker imgPicker;

    // Mode (사용자 등록, 프로필 수정) (CREATE, UPDATE)
    private String currentMode = "CREATE";
    boolean bEditProfile;

    // 비밀번호 변경 기능
    private boolean bChangePw;

   // 퍼미션 획득
    private final int  MULTIPLE_PERMISSIONS = 101;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private String selectImgPick;

    private AuthManager authManager;
    private FirebaseUser currentUser;

    private User userData;
    private ArrayList<Genre> genreData;

    private ProgressDialog mProgressDialog = null;

    String str_age = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        bEditProfile = checkProfile(getIntent());
        if (!bEditProfile) {
            toolbar.setTitle("회원가입");
            currentMode = "CREATE";
            btn_change_pw.setVisibility(View.GONE);
        } else {
            toolbar.setTitle("프로필 수정");
            currentMode = "UPDATE";
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
        
        // S3 자격 증명 (MyCL)
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:2f0e9262-9746-43df-a7c3-4cb6585f11cb", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");
        transferUtility = new TransferUtility(s3, getApplicationContext());

        // retrofit
        retroClient = RetroClient.getInstance(this).createBaseApi();

        // 이미지 픽커
        imgPicker = new ImagePicker(RegisterActivity.this,CAMERA_CODE,GALLERY_CODE);

        authManager = AuthManager.getInstance();

        // 연령대 확인
        final SpinnerAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.agelist, R.layout.spinner_item);
        register_age.setAdapter(sAdapter);
        register_age.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                String item = sAdapter.getItem(position).toString();

                switch(item) {
                    case "10대":
                        str_age = "10대";
                        break;
                    case "20대":
                        str_age = "20대";
                        break;
                    case "30대":
                        str_age = "30대";
                        break;
                    case "40대":
                        str_age = "40대";
                        break;
                    case "50대":
                        str_age = "50대";
                        break;
                    default:
                        str_age = "";
                        break;
                }
            }
            public void onNothingSelected(AdapterView parent) { }
        });


        bChangePw = false;
        setProfile(bEditProfile);
        initAuth();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if (authManager != null) {
            authManager.removeAuthStateListener();
        }
        super.onDestroy();
    }

    @OnClick(R.id.register_cancel)
    void onClick_cancel() {
        finish();
    }

    @OnClick(R.id.register_changePw)
    void onClick_change_pw() {
        String str_email = register_email.getText().toString();
        String str_pw = register_password.getText().toString();

        if (str_pw.equals("")) {
            register_password.setError("필수항목");
            bChangePw = false;
            return;
        }

        showProgressDialog();
        AuthCredential credential = EmailAuthProvider.getCredential(str_email, str_pw);

        FirebaseUser user = authManager.getmFirebaseUser();
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "User re-authenticated.");

                        if (task.isSuccessful()) {
                            Log.d(TAG, "reauthenticate:success");
                            Toast.makeText(RegisterActivity.this, "기존 비밀번호 확인 완료", Toast.LENGTH_SHORT).show();

                            bChangePw = true;
                            register_password.setText("");
                            btn_change_pw.setVisibility(View.GONE);

                            TextView textView = findViewById(R.id.textView_pw);
                            textView.setText("신규 비밀번호");
                        } else {
                            Log.d(TAG, "reauthenticate:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "기존 비밀번호 확인 필요", Toast.LENGTH_SHORT).show();

                            Log.d(TAG, task.getException().getMessage());
                            // TODO 문구 변경
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    @OnClick(R.id.register_registerbtn)
    void onClick_register() {
        Log.d(TAG, "onClick_register");

        if (currentMode.equals("CREATE")) {
            createAccount();
        } else if (currentMode.equals("UPDATE")) {
            updateProfile();
        }
    }


    @OnClick(R.id.register_image)
    void onClick_image() {
        selectImage();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_CODE:
                    register_image.setImageBitmap(imgPicker.getImage());
                    break;
                case GALLERY_CODE:
                    register_image.setImageBitmap(imgPicker.getImage(data.getData()));
                    break;
                default:
                    break;
            }
        }
    }


    private void registUser(String url) {
        String str_email = register_email.getText().toString();
        String str_pw = register_password.getText().toString();
        String str_nickname = register_nickname.getText().toString();
        String str_phone = register_phone.getText().toString();

        String str_gender = "";
        if(register_gender_male.isChecked()) {
            str_gender = "남";
        }
        if(register_gender_female.isChecked()) {
            str_gender = "여";
        }

        String str_uid = null;
        // TODO, 확인용
        if (authManager.getmFirebaseUser() != null) {
            Log.d(TAG, "authManager.getmFirebaseUser().getUid() : " + authManager.getmFirebaseUser().getUid());
            str_uid = authManager.getmFirebaseUser().getUid();
        }
        if (currentUser != null) {
            str_uid = currentUser.getUid();
            Log.d(TAG, "currentUser.getUid() : " + currentUser.getUid());
        }

        Log.d(TAG, "email: " + str_email + ", pw: " + anonymizePassword(str_pw) + ", age: " + str_age
                + ", gender: " + str_gender + ", nick: " + str_nickname + ", phone: " + str_phone + ", image: " + url + ", str_uid: " + str_uid);

        retroClient.postRegister(str_email, str_pw, str_age, str_gender, str_nickname, str_phone, url, str_uid, new RetroCallback<Register>() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(RegisterActivity.this, "사용자 등록 오류", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }

            @Override
            public void onSuccess(int code, Register data) {
                Log.d(TAG, "postRegister SUCCESS");
                // TODO
                Toast.makeText(RegisterActivity.this, "사용자 등록 성공", Toast.LENGTH_SHORT).show();
                hideProgressDialog();

                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                i.putExtra("id", data.getId());
                startActivity(i);
                finishAffinity();
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "postRegister FAIL");
                Toast.makeText(RegisterActivity.this, "사용자 등록 오류", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });
    }


    private void updateUser(String url) {
        final String str_email = register_email.getText().toString();
        final String str_pw = register_password.getText().toString();
        final String str_nickname = register_nickname.getText().toString();
        final String str_phone = register_phone.getText().toString();

        String t_gender = "";
        if(register_gender_male.isChecked()) {
            t_gender = "남";
        }
        if(register_gender_female.isChecked()) {
            t_gender = "여";
        }
        final String str_gender = t_gender;
        final String str_url = url;

        final String str_uid = authManager.getmFirebaseUser().getUid();

        Log.d(TAG, "email: " + str_email + ", pw: " + anonymizePassword(str_pw) + ", age: " + str_age
                + ", gender: " + str_gender + ", nick: " + str_nickname + ", phone: " + str_phone + ", image: " + url + ", str_uid: " + str_uid);

        retroClient.postUpdate(str_email, str_age, str_gender, str_nickname, str_phone, url, str_uid, new RetroCallback<Register>() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(RegisterActivity.this, "프로필 수정 오류", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }

            @Override
            public void onSuccess(int code, Register data) {
                Log.d(TAG, "postUpdate SUCCESS");

                Toast.makeText(RegisterActivity.this, "프로필 수정 완료", Toast.LENGTH_SHORT).show();
                hideProgressDialog();

                User newUser = new User(str_email, str_pw, str_age, str_gender, str_nickname, str_phone, str_url, str_uid);
                LoginData loginData = new LoginData(newUser, genreData);
                Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                i.putExtra("LoingData", loginData);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "postUpdate FAIL");
                Toast.makeText(RegisterActivity.this, "프로필 수정 오류", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });
    }


    private void storeImageToBucket() {
        Log.d(TAG, "storeImageToBucket");

        if (imgPicker.getCurrentPhotoPath() == null) {
            if (currentMode.equals("CREATE")) {
                registUser(null);
            } else if (currentMode.equals("UPDATE")) {
                String url = userData.image;
                updateUser(url);
            }
            return ;
        }

        final File currentPhotoFile = new File(imgPicker.getCurrentPhotoPath());

        final TransferObserver observer = transferUtility.upload(
                "mycl.userimage",
                "images/" + currentPhotoFile.getName(),
                currentPhotoFile
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                String resourceUrl = ((AmazonS3Client) s3).getResourceUrl(observer.getBucket(), "resize/" + currentPhotoFile.getName());  // get resourceUrl
                Log.d(TAG, "onStateChanged: " + state);
                Log.d(TAG, "resourceUrl: " + resourceUrl);

                switch (state) {
                    case COMPLETED: {
                        transferUtility.deleteTransferRecord(id);
                        if (currentMode.equals("CREATE")) {
                            registUser(resourceUrl);
                        } else if (currentMode.equals("UPDATE")) {
                            updateUser(resourceUrl);
                        }
                        break;
                    }
                    case CANCELED: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "이미지 업로드 취소", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        break;
                    }
                    case FAILED: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        break;
                    }
                    case PAUSED: {
                        Toast.makeText(getApplicationContext(), "이미지 업로드 일시중지", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        break;
                    }
                    case WAITING_FOR_NETWORK: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "네트워크 대기중", Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                        break;
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d(TAG, "onProgressChanged: bytesCurrent(" + bytesCurrent + "), bytesTotal(" + bytesTotal + ")");
            }

            @Override
            public void onError(int id, Exception ex) {
                Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });
    }


    private void selectImage() {
        Log.d(TAG, "select Image");
        final CharSequence[] items = {"촬영하기", "가져오기"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 선택");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                CheckPermission permission = new CheckPermission(permissions, MULTIPLE_PERMISSIONS);
                boolean result = permission.checkPermissions(RegisterActivity.this);

                if (items[item].equals("촬영하기")) {
                    selectImgPick = "photo";
                    if (result) {
                        imgPicker.selectPhoto();
                    }
                } else if (items[item].equals("가져오기")) {
                    selectImgPick = "gallery";
                    if (result) {
                        imgPicker.selectGallery();
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        boolean deny = false;

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                deny = true;
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                deny = true;

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                deny = true;
                            }
                        }
                    }
                } else {
                    deny = true;
                }
            }
        }

        if(deny) {
            Toast.makeText(getApplicationContext(), "퍼미션 인증 실패", Toast.LENGTH_SHORT).show();
        } else {
            if(selectImgPick.equals("photo")) {
                imgPicker.selectPhoto();
            } else if (selectImgPick.equals("gallery")) {
                imgPicker.selectGallery();
            }
        }
    }

    private boolean checkProfile(Intent intent) {
        Log.d(TAG, "editProfile()");
        if (intent.getExtras() == null) {
            return false;
        }
        LoginData loginData = (LoginData) intent.getExtras().getParcelable("LoingData");
        if (loginData == null) {
            return false;
        }

        userData = loginData.getUser();
        genreData = loginData.getGenreList();

        return true;
    }

    private void setProfile(boolean bEdit) {
        if (!bEdit) {
            return;
        }

        register_email.setText(userData.id);
        str_age = userData.age;
        switch (userData.age) {
            case "10대":
                register_age.setSelection(1);
                break;
            case "20대":
                register_age.setSelection(2);
                break;
            case "30대":
                register_age.setSelection(3);
                break;
            case "40대":
                register_age.setSelection(4);
                break;
            case "50대":
                register_age.setSelection(5);
                break;
            default:
                register_age.setSelection(0);
                break;
        }

        if (userData.gender.equals("남")) {
            register_gender_male.setChecked(true);
        } else if (userData.gender.equals("여")) {
            register_gender_female.setChecked(true);
        }
        register_nickname.setText(userData.nickname);
        register_phone.setText(userData.phone);

        // 이미지 표시
        ImageView profileImage = (ImageView) findViewById(R.id.register_image);
        if(userData.image != null && userData.image.length() > 10) {
            Picasso.get().load(userData.image).transform(new CircleTransform()).into(profileImage);
        }

        register_email.setEnabled(false);

        TextView textView = findViewById(R.id.textView_pw);
        textView.setText("기존 비밀번호");
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

    private boolean checkProfile() {
        String str_email = register_email.getText().toString();
        String str_pw = register_password.getText().toString();
        String str_pw_verify = register_password_verify.getText().toString();
        String str_nickname = register_nickname.getText().toString();

        if (str_email.equals("")) {
            register_email.setError("필수항목");
        }

        if (str_nickname.equals("")) {
            register_nickname.setError("필수항목");
        }

        if (!bEditProfile || bChangePw) {
            if (str_pw.equals("")) {
                register_password.setError("필수항목");
            }
            if (str_pw_verify.equals("")) {
                register_password_verify.setError("필수항목");
            }
        }

        if (str_email.equals("") || str_nickname.equals("")
            || (bChangePw && (str_pw.equals("") || str_pw_verify.equals("")))) {
            Toast.makeText(this, "필수항목 입력 필요", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ((!bEditProfile || bChangePw) && !str_pw.equals(str_pw_verify)) {
            Toast.makeText(this, "비밀번호 재확인 필요", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createAccount() {
        if (!checkProfile()) {
            return;
        }
        String str_email = register_email.getText().toString();
        String str_pw = register_password.getText().toString();

        showProgressDialog();
        OnCompleteListener completeListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createAccount OnCompleteListener onComplete");

                if (task.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");

                    storeImageToBucket();
                } else {
                    Log.d(TAG, "createUserWithEmail:failure", task.getException());
                    Log.d(TAG, task.getException().getMessage());

                    // TODO
                    String msg = task.getException().getMessage();
                    if(msg.contains("least 6")){
                        msg = "비밀번호 최소 6자리 이상";
                    } else if(msg.contains("already")) {
                        msg = "이미 등록된 이메일";
                    }
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            }
        };
        authManager.createAccount(this, completeListener, str_email, str_pw);
    }

    private void updateProfile() {
        if (!checkProfile()) {
            return;
        }

        if (!bChangePw) {
            storeImageToBucket();
        } else {
            String newPassword = register_password.getText().toString();

            OnCompleteListener completeListener = new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "updatePassword OnCompleteListener onComplete");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "User password updated.");

                        storeImageToBucket();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "updatePassword:failure", task.getException());
                        Log.d(TAG, task.getException().getMessage());
                        // TODO
                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            };
            authManager.updatePassword(completeListener, newPassword);
        }
    }

    private void initAuth() {
        Log.d(TAG, "initAuth()");

        currentUser = authManager.getmFirebaseUser();
        if (currentUser != null)
            Log.d(TAG, "onAuthStateChanged currentUser : (" + currentUser.getUid() + ")");

        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = authManager.getmFirebaseUser();
                FirebaseUser user2 = firebaseAuth.getCurrentUser();

                if (user != null)
                    Log.d(TAG, "onAuthStateChanged authManager.getmFirebaseUser : (" + user.getUid() + ")");
                if (user2 != null)
                    Log.d(TAG, "onAuthStateChanged firebaseAuth.getCurrentUser : (" +  user2.getUid() + ")");

                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    Log.d(TAG, "onAuthStateChanged()");
                    Log.d(TAG, "onAuthStateChanged:signed_in : UID (" + currentUser.getUid() + ")");
//                    Log.d(TAG, "onAuthStateChanged:signed_in : IdToken (" + mFirebaseUser.getIdToken(true).getResult().getToken() + ")");
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        authManager.addAuthStateListener(authListener);
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
}


