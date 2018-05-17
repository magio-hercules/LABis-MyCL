package com.labis.mycl.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.labis.mycl.model.Register;
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

    // Image 선택
    private String folder_name = "/MyCL/";
    private String currentPhotoPath;
    private Uri photoUri;

    private final int CAMERA_CODE   = 1111;
    private final int GALLERY_CODE  = 1112;

    // for S3
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;




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
    }


    @OnClick(R.id.register_registerbtn)
    void onClick_register(){
        storeImageToBucket();
    }


    @OnClick(R.id.register_image)
    void onClick_image(){
        selectImage();
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


    private void registUser(String url) {
        String str_email = register_email.getText().toString();
        String str_pw = register_password.getText().toString();
        String str_age = register_age.getText().toString();
        String str_gender = register_gender.getText().toString();
        String str_nickname = register_nickname.getText().toString();
        String str_phone = register_phone.getText().toString();

        Log.e(TAG, "email: " + str_email + ", pw: "+str_pw+ ", age: "+str_age
                + ", gender: "+str_gender+ ", nick: "+str_nickname+ ", phone: "+str_phone + ", image: " + url);

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
        final File currentPhotoFile = new File(currentPhotoPath);

        final TransferObserver observer = transferUtility.upload(
                "mycl.userimage",
                "images/" + currentPhotoFile.getName(),
                currentPhotoFile
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                String resourceUrl = ((AmazonS3Client)s3).getResourceUrl(observer.getBucket(), "resize/" + currentPhotoFile.getName());  // get resourceUrl
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


    private void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(intent, GALLERY_CODE);
    }


    // for CAMERA
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
        register_image.setImageBitmap(rotate(bitmap, exifDegree));
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
        register_image.setImageBitmap(rotate(bitmap, exifDegree));

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
}
