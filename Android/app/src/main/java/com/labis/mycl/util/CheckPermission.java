package com.labis.mycl.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.Manifest;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CheckPermission {

    private String[] permissions;
    private static int MULTIPLE_PERMISSIONS;

    public CheckPermission(String[] permissions, int Code) {
        this.permissions = permissions;
        this.MULTIPLE_PERMISSIONS = Code;
    }

    public boolean checkPermissions(Activity activity) {
        int result;
        List<String> noPermissionList = new ArrayList<>();

        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(activity.getApplicationContext(), pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                noPermissionList.add(pm);
            }
        }

        if (!noPermissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity, noPermissionList.toArray(new String[noPermissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }

        return true;
    }



}
