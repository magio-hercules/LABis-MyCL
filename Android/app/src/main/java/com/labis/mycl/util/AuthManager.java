package com.labis.mycl.util;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private static final String TAG = "[AuthManager]";

    private static AuthManager instance = null;

//    private Activity mActivity;

    // for Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;


    private AuthManager() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                mFirebaseUser = firebaseAuth.getCurrentUser();
//                if (mFirebaseUser != null) {
//                    // User is signed in
//                    Log.d(TAG, "onAuthStateChanged()");
//                    Log.d(TAG, "onAuthStateChanged:signed_in : UID (" + mFirebaseUser.getUid() + ")");
////                    Log.d(TAG, "onAuthStateChanged:signed_in : IdToken (" + mFirebaseUser.getIdToken(false).getResult().getToken() + ")");
//                } else {
//                    // User is signed out
//                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                }
//            }
//        };
//
//
//        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    public static synchronized  AuthManager getInstance(){
        if (instance == null){
            instance = new AuthManager();
        }

        return instance;
    }


    public FirebaseAuth getmFirebaseAuth() {
        return mFirebaseAuth;
    }

    public FirebaseUser getmFirebaseUser() {
        return mFirebaseUser;
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

    public void createAccount(Activity activity, String email, String password, OnCompleteListener listener) {
        Log.d(TAG, "createAccount() email : " + email + ", password : password");

//        OnCompleteListener completeListener = new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "createUserWithEmail:success");
//                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                }
//
//            }
//        };

        // [START create_user_with_email]
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, listener);
    }

    public void signOut() {
        Log.d(TAG, "signOut()");
        mFirebaseAuth.signOut();
    }
}
