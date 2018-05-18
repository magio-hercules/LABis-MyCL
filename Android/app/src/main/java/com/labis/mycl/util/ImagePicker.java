package com.labis.mycl.util;

import android.app.Activity;
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
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImagePicker {

    private static final String TAG = "ImagePicker";
    private Activity mActivity;

    // Image 선택
    private String folder_name = "/MyCL/";
    private String currentPhotoPath;
    private Uri photoUri;

    private final int CAMERA_CODE;
    private final int GALLERY_CODE;

    public ImagePicker(Activity mActivity, int CAMERA_CODE, int GALLERY_CODE) {
        this.mActivity = mActivity;
        this.CAMERA_CODE = CAMERA_CODE;
        this.GALLERY_CODE = GALLERY_CODE;
    }


    public void selectGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        mActivity.startActivityForResult(intent, GALLERY_CODE);
    }

    public void selectPhoto() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    Log.d(TAG, "[Exception] createImageFile");
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(mActivity, mActivity.getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    mActivity.startActivityForResult(intent, CAMERA_CODE);
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

    // for CAMERA
    public Bitmap getImage() {
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

        return rotate(bitmap, exifDegree);
    }


    // for GALLERY
    public Bitmap getImage(Uri imgUri) {
        String imagePath = getRealPathFromURI(imgUri);
        currentPhotoPath = imagePath;
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
        return rotate(bitmap, exifDegree);
    }


    private String getRealPathFromURI(Uri contentUri) {
        String uri = null;
        int column_index=0;
        String[] prj = {MediaStore.Images.Media.DATA};

        Cursor cursor = mActivity.getContentResolver().query(contentUri, prj, null, null, null);
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

