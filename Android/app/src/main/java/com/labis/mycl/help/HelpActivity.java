package com.labis.mycl.help;

import android.app.ProgressDialog;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.labis.mycl.R;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.SoftKeyboard;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;


public class HelpActivity extends AppCompatActivity {
    private static final String TAG = "[HELP]";

    @BindView(R.id.help_combo)
    Spinner comboHelp;
    @BindView(R.id.help_scroll_view)
    ScrollView scrollView;
    @BindView(R.id.help_request_title)
    TextView helpRequestTitle;
    @BindView(R.id.help_description)
    EditText helpDescription;
    @BindView(R.id.detail_ok_btn)
    Button sendButton;
    @BindView(R.id.app_info)
    TextView appInfo;

    private Toolbar toolbar;
    private AdView mAdView;

    public static User userData;

    private static ViewPager mPager;
    private static int currentPage = 0;
    private static final Integer[] HELP = {R.mipmap.help_1, R.mipmap.help_2, R.mipmap.help_3, R.mipmap.help_4, R.mipmap.help_5};
    private ArrayList<Integer> HELPArray = new ArrayList<Integer>();
    private SoftKeyboard softKeyboard;

    public RetroClient retroClient;

    private ProgressDialog mProgressDialog = null;

    private String curComboIndex = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        LoginData loginData = (LoginData) getIntent().getExtras().getParcelable("LoingData");
        if (loginData != null) {
            userData = loginData.getUser();
        }

        toolbar = (Toolbar) findViewById(R.id.help_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("문의 및 도움말");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final SpinnerAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.helplist, R.layout.spinner_item);
        comboHelp.setAdapter(sAdapter);

        // for ad
        mAdView = findViewById(R.id.help_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                LinearLayout layout = (LinearLayout)findViewById(R.id.layout_scroll_ad);
                layout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }
        });

        // Request Combo
        comboHelp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                String item = sAdapter.getItem(position).toString();
                if(position > 0) {
                    scrollToRequest();
                }

                curComboIndex = Integer.toString(position);

                switch(item) {
                    case "콘텐츠 추가":
                        helpDescription.setHint("어떤 장르의 어떤 콘텐츠를 추가할까요?");
                        break;
                    case "장르 추가":
                        helpDescription.setHint("어떤 장르를 추가할까요?");
                        break;
                    case "버그 신고":
                        helpDescription.setHint("발생한 오류에 대해 제보해 주세요.");
                        break;
                    case "사용자 개선 건의":
                        helpDescription.setHint("사용중 불편함이 있으면 건의해 주세요.");
                        break;
                    case "탈퇴 요청":
                        helpDescription.setHint("탈퇴 이유를 간단히 적어주세요.");
                        break;
                    case "개발자에게":
                        helpDescription.setHint("개발자들에게 하고 싶은 말을 해주세요.");
                        break;
                    case "기타":
                        helpDescription.setHint("");
                        break;
                    default:
                        break;
                }
            }
            public void onNothingSelected(AdapterView parent) { }
        });

        // Keypad Control
        InputMethodManager controlManager = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(scrollView, controlManager);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() { }});
            }

            @Override
            public void onSoftKeyboardShow() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollToRequest();
                    }
                }, 600);
            }
        });

        // Help Pager View
        initHelpImage();
        retroClient = RetroClient.getInstance(this).createBaseApi();

        String txt = "- 개발자 : Labis Corp.(쫑미니, 쿨영후니, 에볼\n- 버전 : 1.0.0\n- 업데이트 날짜 : ‘18.06.22\n- 다운로드 크기 : 5.5MB\n- 개인정보처리방침 : http://evolhim.net";
        appInfo.setText(txt);
        Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
            @Override
            public String transformUrl(Matcher match, String url) {
                return "";
            }
        };
        Pattern pattern1 = Pattern.compile("http://evolhim.net");
        Linkify.addLinks(appInfo, pattern1, "http://evolhim.net", null, mTransform);

    }

    private void scrollToRequest() {
        int x = helpRequestTitle.getLeft();
        int y = helpRequestTitle.getTop();
        scrollView.scrollTo(x, y);
    }

    private void initHelpImage() {
        for (int i = 0; i < HELP.length; i++) {
            HELPArray.add(HELP[i]);
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyAdapter(HelpActivity.this, HELPArray));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        /*
        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == HELP.length) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 4000, 4000);
        */
    }

    public void showProgressDialog() {
        Log.d(TAG, "showProgressDialog");
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog() {
        Log.d(TAG, "hideProgressDialog");

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @OnClick(R.id.detail_ok_btn)
    void onClick_send() {
        String reqType = curComboIndex;
        String comment = helpDescription.getText().toString();

        if (reqType.equals("0")) {
            Toast.makeText(getApplicationContext(), "항목을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.equals("")) {
            Toast.makeText(getApplicationContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "reqType : " + reqType + ", comment : " + comment);

        showProgressDialog();
        retroClient.postInsertRequest(userData.id, reqType, comment, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "전송 오류", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {

                comboHelp.setSelection(0);
                helpDescription.setText(null);
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code) {
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
