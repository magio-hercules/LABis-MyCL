package com.labis.mycl.contents;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.login.RegisterActivity;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.CheckPermission;
import com.labis.mycl.util.ImagePicker;
import com.labis.mycl.util.Utility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomActivity extends AppCompatActivity {

    private static final String TAG = "CustomActivity";

    @BindView(R.id.editTitle)
    EditText editTitle;

    @BindView(R.id.comboGenre)
    Spinner comboGenre;

    @BindView(R.id.saveBtn)
    Button saveBtn;

    @BindView(R.id.imageView)
    ImageView imageView;

    SpinnerAdapter sAdapter;

    private RetroClient retroClient;

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
        setContentView(R.layout.activity_custom);
        ButterKnife.bind(this);

        retroClient = RetroClient.getInstance(this).createBaseApi();
        
        // -- ToolBar -- //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("커스텀 콘텐츠 추가");
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

        sAdapter = ArrayAdapter.createFromResource(this, R.array.genre, R.layout.spinner_item);

        comboGenre.setAdapter(sAdapter);
        comboGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), ""+ sAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView parent) {
            }
        });

        // 이미지 픽커
        imgPicker = new ImagePicker(CustomActivity.this,CAMERA_CODE,GALLERY_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_CODE:
                    imageView.setImageBitmap(imgPicker.getImage());
                    break;
                case GALLERY_CODE:
                    imageView.setImageBitmap(imgPicker.getImage(data.getData()));
                    break;
                default:
                    break;
            }
        }
    }

    // -- Event Function Section -- ////////////////////////////////////////
    @OnClick(R.id.imageView)
    void onClick_imageView(){
        final CharSequence[] items = {"촬영하기", "가져오기", "취소"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 선택");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                CheckPermission permission = new CheckPermission(permissions, MULTIPLE_PERMISSIONS);
                boolean result = permission.checkPermissions(CustomActivity.this);

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

    @OnClick(R.id.saveBtn)
    void onClick_saveBtn() {
        final ProgressDialog progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();
        retroClient.postInserCustomContents("A01", "1", "EVOL TEST", "에볼", "500", "0",
                "1", "asdasdas", "최욱", "0", "http", new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Toast.makeText(getApplicationContext(), "커스텀 추가 SUCCESS : " + code, Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onFailure(int code) {
                    Toast.makeText(getApplicationContext(), "커스텀 추가 FAIL : " + code, Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }
            });
    }









}
