package com.labis.mycl.util;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private static final String TAG = "[AuthManager]";

    private static AuthManager instance = null;

    // for Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

//    private Activity mActivity;




    private AuthManager() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
    }

    public static synchronized  AuthManager getInstance(){
        if (instance == null){
            instance = new AuthManager();
        }

        return instance;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }

    public FirebaseUser getmFirebaseUser() {
        return mFirebaseUser;
    }

    public void setFirebaseUser(FirebaseUser user) {
        mFirebaseUser = user;
    }

    public void addAuthStateListener(FirebaseAuth.AuthStateListener listener) {
        if (listener == null) {
            return;
        }

        mAuthListener = listener;
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    public void removeAuthStateListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    public void createAccount(Activity activity, OnCompleteListener listener, String email, String password) {
        Log.d(TAG, "createAccount() email : " + email + ", password : " + password);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                       .addOnCompleteListener(activity, listener);
    }

    public void signIn(Activity activity, OnCompleteListener listener, String email, String password) {
        Log.d(TAG, "signIn() email : " + email + ", password : " + anonymizePassword(password));

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                       .addOnCompleteListener(activity, listener);
    }

    public void signOut() {
        Log.d(TAG, "signOut()");
        mFirebaseAuth.signOut();
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
}
