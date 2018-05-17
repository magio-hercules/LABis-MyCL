package com.labis.mycl.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.labis.mycl.rest.RetroClient;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UrlActivity extends Activity {
    private static final String TAG = "[TEST] [S3] ";

    RetroClient retroClient;

    Bitmap mBitmap;

    @BindView(R.id.btn_down)
    Button mDown;
    @BindView(R.id.imgTranslate)
    ImageView mImgTrans;
    @BindView(R.id.edit_url)
    EditText mUrl;
    @BindView(R.id.edit_url_s3)
    EditText mUrlS3;


    // Image 선택
    private String folder_name = "/MyCL/";
    private String currentPhotoPath;
    private Uri photoUri;

    // for S3
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);

        ButterKnife.bind(this);



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


    @OnClick(R.id.btn_down)
    void onClick_image_down(){
        String url = mUrl.getText().toString();
        Log.d("[TEST] ", "URL : " + url);
        new LoadImage().execute(url);

//        storeImageToBucket();
    }

    @OnClick(R.id.btn_change)
    void onClick_change_s3(){
//        storeImageToBucket();
    }



    private class LoadImage extends AsyncTask<String, String, Bitmap> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UrlActivity.this);
            pDialog.setMessage("이미지 로딩중입니다...");
            pDialog.show();
        }

        protected Bitmap doInBackground(String... args) {
            try {
                mBitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

                Log.d(TAG, "Width : " + mBitmap.getWidth() + ", Height : " + mBitmap.getHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mBitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if (image != null) {
                mImgTrans.setImageBitmap(image);
                pDialog.dismiss();

            } else {
                pDialog.dismiss();
                Toast.makeText(UrlActivity.this, "이미지가 존재하지 않습니다.",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }



    private void storeImageToBucket() {
        final File currentPhotoFile = new File(currentPhotoPath);
//        mBitmap;
        
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

//                        registUser(resourceUrl);
                        mUrlS3.setText(resourceUrl);
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

//    private void selectImage() {
//        Log.d(TAG, "select Image");
//        final CharSequence[] items = {"촬영하기", "가져오기", "취소"};
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("사진 선택");
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//                int permissionGalleryCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.READ_EXTERNAL_STORAGE);
//
//                int permissionCameraCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.CAMERA);
//
//                int permissionWriteCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//
//                if (items[item].equals("촬영하기")) {
//                    if (permissionCameraCheck == PackageManager.PERMISSION_GRANTED) {
//                        if(permissionWriteCheck == PackageManager.PERMISSION_GRANTED)
//                        {
//                            selectPhoto();
//                        } else {
//                            ActivityCompat.requestPermissions(RegisterActivity.this,
//                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                    WRITE_CODE);
//                        }
//                    } else {
//                        ActivityCompat.requestPermissions(RegisterActivity.this,
//                                new String[]{Manifest.permission.CAMERA},
//                                CAMERA_CODE);
//                    }
//                } else if (items[item].equals("가져오기")) {
//                    if (permissionGalleryCheck == PackageManager.PERMISSION_GRANTED) {
//                        selectGallery();
//                    } else {
//                        ActivityCompat.requestPermissions(RegisterActivity.this,
//                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                                GALLERY_CODE);
//                    }
//                } else if (items[item].equals("취소")) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case GALLERY_CODE: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    selectGallery();
//                }
//                return;
//            }
//            case CAMERA_CODE : {
//                int permissionWriteCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if (permissionWriteCheck == PackageManager.PERMISSION_GRANTED) {
//                    selectPhoto();
//                } else {
//                    ActivityCompat.requestPermissions(RegisterActivity.this,
//                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            WRITE_CODE);
//                }
//                return;
//            }
//            case WRITE_CODE : {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    selectPhoto();
//                }
//                return;
//            }
//        }
//    }
//
//    private void selectPhoto() {
//        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                File photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                } catch (IOException e) {
//                    Log.d(TAG, "[Exception] createImageFile");
//                    e.printStackTrace();
//                }
//                if (photoFile != null) {
//                    photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                    startActivityForResult(intent, CAMERA_CODE);
//                }
//            }
//        }
//    }
//
//
//    private File createImageFile() throws IOException {
//        File dir = new File(Environment.getExternalStorageDirectory() + folder_name);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String captureImageName = timeStamp + ".png";
//
//        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folder_name + captureImageName);
//        currentPhotoPath = storageDir.getAbsolutePath();
//        Log.d(TAG, "[INFO] currentPhotoPath : " + currentPhotoPath);
//
//        return storageDir;
//    }
//
//
//    private void selectGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//
//        startActivityForResult(intent, GALLERY_CODE);
//    }
//
//
//    // for CAMERA
//    private void setImage() {
//        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
//        ExifInterface exif = null;
//        try {
//            exif = new ExifInterface(currentPhotoPath);
//        } catch (IOException e) {
//            Log.d(TAG, "[Exception] new ExifInterface");
//            e.printStackTrace();
//        }
//
//        int exifOrientation, exifDegree;
//        if (exif != null) {
//            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            exifDegree = exifOrientationToDegrees(exifOrientation);
//        } else {
//            exifDegree = 0;
//        }
//        register_image.setImageBitmap(rotate(bitmap, exifDegree));
//    }


//    // for GALLERY
//    private void setImage(Uri imgUri) {
//        String imagePath = getRealPathFromURI(imgUri);
//        ExifInterface exif = null;
//        try {
//            exif = new ExifInterface(imagePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        int exifOrientation, exifDegree;
//        if (exif != null) {
//            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            exifDegree = exifOrientationToDegrees(exifOrientation);
//        } else {
//            exifDegree = 0;
//        }
//        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//        register_image.setImageBitmap(rotate(bitmap, exifDegree));
//
//        currentPhotoPath = imagePath;
//        Log.d(TAG, "[INFO] currentPhotoPath : " + currentPhotoPath);
//    }


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
