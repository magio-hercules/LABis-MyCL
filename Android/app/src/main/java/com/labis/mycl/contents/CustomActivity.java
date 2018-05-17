package com.labis.mycl.contents;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
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
    private String folder_name = "/MyCL/";
    private String currentPhotoPath;
    private Uri photoUri;
    private final int CAMERA_CODE   = 1111;
    private final int GALLERY_CODE  = 1112;

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
    }


    // -- Image Process Function Section -- ////////////////////////////////////////
    private File createImageFile() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory() + folder_name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String captureImageName = timeStamp + ".png";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folder_name + captureImageName);
        currentPhotoPath = storageDir.getAbsolutePath();
        Log.d(TAG, "[INFO] currentPhotoPath : " + currentPhotoPath);

        return storageDir;
    }

    private void selectPhoto() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Log.d(TAG, "[Exception] createImageFile");
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, CAMERA_CODE);
                }
            }
        }
    }

    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(intent, GALLERY_CODE);
    }

    private void setImage() {
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            Log.d(TAG, "[Exception] new ExifInterface");
            e.printStackTrace();
        }

        int exifOrientation, exifDegree;
        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }
        imageView.setImageBitmap(rotate(bitmap, exifDegree));
    }

    // for GALLERY
    private void setImage(Uri imgUri) {
        String imagePath = getRealPathFromURI(imgUri);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation, exifDegree;
        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(rotate(bitmap, exifDegree));

        currentPhotoPath = imagePath;
        Log.d(TAG, "[INFO] currentPhotoPath : " + currentPhotoPath);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String uri = null;
        int column_index=0;
        String[] prj = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(contentUri, prj, null, null, null);
        if (cursor == null) {
            Log.e(TAG, " CURSOR is null");
            return "";
        }

        try {
            if(cursor.moveToFirst()){
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            }
            uri = cursor.getString(column_index);
        } finally {
            cursor.close();
        }

        return uri;
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
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
                boolean result = Utility.checkPermission(getApplicationContext());

                if (items[item].equals("촬영하기")) {
                    if (result) {
                        selectPhoto();
                    }
                } else if (items[item].equals("가져오기")) {
                    if (result) {
                        selectGallery();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_CODE:
                    setImage();
                    break;
                case GALLERY_CODE:
                    setImage(data.getData());
                    break;
                default:
                    break;
            }
        }
    }








}
