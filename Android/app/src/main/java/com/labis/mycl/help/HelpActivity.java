package com.labis.mycl.help;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.labis.mycl.R;
import com.labis.mycl.util.SoftKeyboard;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;


public class HelpActivity extends AppCompatActivity {
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

    private Toolbar toolbar;
    private AdView mAdView;

    private static ViewPager mPager;
    private static int currentPage = 0;
    private static final Integer[] HELP = {R.mipmap.intoro_img_01, R.mipmap.help_1, R.mipmap.help_2, R.mipmap.help_3};
    private ArrayList<Integer> HELPArray = new ArrayList<Integer>();
    private SoftKeyboard softKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

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

        // Request Combo
        comboHelp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                String item = sAdapter.getItem(position).toString();
                if(position > 0) {
                    scrollToRequest();
                }

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
    }

}
