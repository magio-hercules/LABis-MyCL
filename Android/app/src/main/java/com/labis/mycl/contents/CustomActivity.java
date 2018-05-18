package com.labis.mycl.contents;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Switch;
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
import com.labis.mycl.model.Content;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.CheckPermission;
import com.labis.mycl.util.ImagePicker;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomActivity extends AppCompatActivity {

    private static final String TAG = "CustomActivity";

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.comboGenre)
    Spinner comboGenre;

    @BindView(R.id.editTitle)
    EditText editTitle;

    @BindView(R.id.editOriginal)
    EditText editOriginal;

    @BindView(R.id.editSeason)
    EditText editSeason;

    @BindView(R.id.switchTheater)
    Switch switchTheater;

    @BindView(R.id.editSummary)
    EditText editSummary;

    @BindView(R.id.saveBtn)
    Button saveBtn;

    SpinnerAdapter sAdapter;

    private RetroClient retroClient;

    // Image 선택
    private final int CAMERA_CODE   = 1111;
    private final int GALLERY_CODE  = 1112;
    private ImagePicker imgPicker;
    private  boolean isTitleImage = false;

    // 퍼미션 획득
    private final int  MULTIPLE_PERMISSIONS = 101;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    // for S3
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;

    private ProgressDialog progressDoalog;

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

        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    // -- User Function Section -- ////////////////////////////////////////

    private String getGenreID(String genreName) {
        if(ContentsActivity.genreMap != null) {
            return ContentsActivity.genreMap.get(genreName);
        }
        return null;
    }

    private void regContentsWithS3(Content Item) {
        final Content mItem = Item;
        final File currentPhotoFile = new File(imgPicker.getCurrentPhotoPath());
        final TransferObserver observer = transferUtility.upload(
                "mycl.userimage",
                "images/" + currentPhotoFile.getName(),
                currentPhotoFile
        );
        progressDoalog.show();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                String resourceUrl = ((AmazonS3Client) s3).getResourceUrl(observer.getBucket(), "resize/" + currentPhotoFile.getName());  // get resourceUrl
                Log.d(TAG, "onStateChanged:" + state + " / " + "resourceUrl:" + resourceUrl);

                switch (state) {
                    case COMPLETED: {
                        Log.d(TAG, "IMAGE_UPLOAD_COMPLETED");
                        transferUtility.deleteTransferRecord(id);
                        mItem.setImage(resourceUrl);
                        regContents(mItem);
                        break;
                    }
                    case CANCELED: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "IMAGE_UPLOAD_CANCELED", Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
                        break;
                    }
                    case FAILED: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "IMAGE_UPLOAD_FAILED", Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
                        break;
                    }
                    case PAUSED: {
                        Toast.makeText(getApplicationContext(), "IMAGE_UPLOAD_PAUSED", Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
                        break;
                    }
                    case WAITING_FOR_NETWORK: {
                        transferUtility.deleteTransferRecord(id);
                        Toast.makeText(getApplicationContext(), "WAITING_FOR_NETWORK", Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
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
            }
        });
    }

    private void regContents(Content data) {
        if(!progressDoalog.isShowing()) {
            progressDoalog.show();
        }
        final Content Item = data;
        retroClient.postInserCustomContents(Item.gen_id, Item.season, Item.name, Item.name_org, Item.theatrical,
                 Item.summary, Item.publisher, Item.auth, Item.image, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Toast.makeText(getApplicationContext(), "[" + Item.name + "] 추가 성공", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
                finish();
                overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
            }

            @Override
            public void onFailure(int code) {
                Toast.makeText(getApplicationContext(), "[" + code + "] 에러 발생", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }

    // -- Event Function Section -- ////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_CODE:
                    imageView.setImageBitmap(imgPicker.getImage());
                    isTitleImage = true;
                    break;
                case GALLERY_CODE:
                    imageView.setImageBitmap(imgPicker.getImage(data.getData()));
                    isTitleImage = true;
                    break;
                default:
                    break;
            }
        }
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
        // Make Content
        String gen_name = comboGenre.getSelectedItem().toString();
        String gen_id = getGenreID(gen_name);
        String name = editTitle.getText().toString();
        String name_org = editOriginal.getText().toString();
        int season = Integer.parseInt(editSeason.getText().toString());
        int theatrical = switchTheater.isChecked() ? 1 : 0;
        String summary = editSummary.getText().toString();
        String publisher = ContentsActivity.userData.id;
        int auth = 0;
        String image = null;

        // 타이틀 검사
        if(name.length() < 1) {
            Toast.makeText(getApplicationContext(), "타이틀을 입력해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // 장르 검사
        if(gen_name.equals("선택")) {
            Toast.makeText(getApplicationContext(), "장르를 선택해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Content Item = new Content(gen_id, season, name, name_org, theatrical, summary, publisher, auth, image);
        if(isTitleImage) {
            regContentsWithS3(Item);       // 업로드 이미지 ON
        } else {
            regContents(Item);            // 업로드 이미지 OFF
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
    }

}
