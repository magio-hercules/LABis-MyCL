package com.labis.mycl.login;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.labis.mycl.R;
import com.labis.mycl.contents.CustomActivity;
import com.labis.mycl.model.Register;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.CheckPermission;
import com.labis.mycl.util.ImagePicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;


public class RegisterActivity extends Activity {
    private static final String TAG = "[REGISTER]";

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

    // for S3
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;

    // Image 선택
    private final int CAMERA_CODE   = 1111;
    private final int GALLERY_CODE  = 1112;
    private ImagePicker imgPicker;

    // 퍼미션 획득
    private final int  MULTIPLE_PERMISSIONS = 101;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        retroClient = RetroClient.getInstance(this).createBaseApi();

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

        // 이미지 픽커
        imgPicker = new ImagePicker(RegisterActivity.this,CAMERA_CODE,GALLERY_CODE);
    }


    @OnClick(R.id.register_registerbtn)
    void onClick_register() {
        storeImageToBucket();
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
        String str_age = register_age.getText().toString();
        String str_gender = register_gender.getText().toString();
        String str_nickname = register_nickname.getText().toString();
        String str_phone = register_phone.getText().toString();

        Log.e(TAG, "email: " + str_email + ", pw: " + str_pw + ", age: " + str_age
                + ", gender: " + str_gender + ", nick: " + str_nickname + ", phone: " + str_phone + ", image: " + url);

        retroClient.postRegister(str_email, str_pw, str_age, str_gender, str_nickname, str_phone, url, new RetroCallback<Register>() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(RegisterActivity.this, "postRegister Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int code, Register data) {
                Log.e(TAG, "postRegister SUCCESS");
                Toast.makeText(RegisterActivity.this, data.getReason(), Toast.LENGTH_SHORT).show();

                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.putExtra("id", data.getId());
                startActivity(i);
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "postRegister FAIL");
                Toast.makeText(RegisterActivity.this, "Register Fail", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void storeImageToBucket() {
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
                        Toast.makeText(getApplicationContext(), "Image Upload 성공", Toast.LENGTH_SHORT).show();

                        registUser(resourceUrl);
                        break;
                    }
                    case CANCELED: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "Image Upload CANCELED", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case FAILED: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "Image Upload FAILED", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case PAUSED: {
                        Toast.makeText(getApplicationContext(), "Image Upload PAUSED", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case WAITING_FOR_NETWORK: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "WAITING_FOR_NETWORK", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Image upload 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void selectImage() {
        Log.d(TAG, "select Image");
        final CharSequence[] items = {"촬영하기", "가져오기", "취소"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 선택");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                CheckPermission permission = new CheckPermission(permissions, MULTIPLE_PERMISSIONS);
                boolean result = permission.checkPermissions(RegisterActivity.this);

                if (items[item].equals("촬영하기")) {
                    if (result) {
                        imgPicker.selectPhoto();
                    }
                } else if (items[item].equals("가져오기")) {
                    if (result) {
                        imgPicker.selectGallery();
                    }
                } else if (items[item].equals("취소")) {
                    dialog.dismiss();
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
        }
    }

}


