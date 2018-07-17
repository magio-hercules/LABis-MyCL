package com.labis.mycl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;




public class GlobalApplication extends Application {
    private static volatile GlobalApplication instance = null;
    private static volatile Activity currentActivity = null;


    public static GlobalApplication getGlobalApplicationContext() {
        if (instance == null) {
            throw new IllegalStateException("This Application does not inherit com.kakao.GlobalApplication");
        }

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Kakao Sdk 초기화
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    public static void setCurrentActivity(Activity currentActivity){
        GlobalApplication.currentActivity = currentActivity;
    }

    public static Activity getCurrentActivity(){
        return currentActivity;
    }


    private static class KakaoSDKAdapter extends KakaoAdapter {
        /**
         * Session Config에 대해서는 default값들이 존재한다.
         * 필요한 상황에서만 override해서 사용하면 됨.
         * @return Session의 설정값.
         */
        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    // KAKAO_TALK  : 카카오톡 로그인 타입
                    // KAKAO_STORY : 카카오스토리 로그인 타입
                    // KAKAO_ACCOUNT : 웹뷰 다이얼로그를 통한 계정연결 타입
                    // KAKAO_TALK_EXCLUDE_NATIVE_LOGIN : 카카오톡 로그인 타입과 함께 계정생성을 위한 버튼을 함께 제공
                    // KAKAO_LOGIN_ALL : 모든 로그인 방식을 제공
//                    return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
                    return new AuthType[] {AuthType.KAKAO_TALK};
                }

                // 로그인 웹뷰에서 pause와 resume시에 타이머를 설정하여, CPU의 소모를 절약 할 지의 여부를 지정합니다.
                // true로 지정할 경우, 로그인 웹뷰의 onPuase()와 onResume()에 타이머를 설정해야 합니다.
                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                // 로그인 시 토큰을 저장할 때의 암호화 여부를 지정합니다.
                @Override
                public boolean isSecureMode() {
                    return false;
                }

                // 일반 사용자가 아닌 Kakao와 제휴 된 앱에서 사용되는 값입니다.
                // 값을 지정하지 않을 경우, ApprovalType.INDIVIDUAL 값으로 사용됩니다.
                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                // 로그인 웹뷰에서 email 입력 폼의 데이터를 저장할 지 여부를 지정합니다.
                @Override
                public boolean isSaveFormData() {
//                    로그인 한 정보를 안드로이드에 쿠키(?)를 남겨두고 싶다면 isSaveFromData를 return true;로 바꿔주기
                    return true;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return GlobalApplication.getGlobalApplicationContext();
                }
            };
        }
    }
}
