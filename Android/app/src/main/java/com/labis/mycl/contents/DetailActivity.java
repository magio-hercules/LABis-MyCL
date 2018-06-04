package com.labis.mycl.contents;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.labis.mycl.util.SoftKeyboard;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    @BindView(R.id.detail_ll)
    RelativeLayout totalLayout;

    @BindView(R.id.detail_appbar)
    AppBarLayout appBar;
    @BindView(R.id.detail_zoom_btn)
    ImageView detailZoom;
    @BindView(R.id.temp_image)
    ImageView tempImgView;

    @BindView(R.id.detail_minus_btn)
    TextView minusBtn;

    @BindView(R.id.detail_plus_btn)
    TextView plusBtn;

    @BindView(R.id.detail_chapter_div)
    LinearLayout detailChapterDiv;
    @BindView(R.id.detail_chapter)
    TextView chapterView;

    @BindView(R.id.detail_title)
    TextView detailTitle;

    @BindView(R.id.detail_org_title)
    TextView detailOrgTitle;

    @BindView(R.id.detail_genre)
    TextView detailGenre;

    @BindView(R.id.detail_season_div)
    LinearLayout detailSeasonDiv;
    @BindView(R.id.detail_season)
    TextView detailSeason;

    @BindView(R.id.detail_theater_div)
    LinearLayout detailTheaterDiv;
    @BindView(R.id.detail_switch_theater)
    Switch detailTheater;

    @BindView(R.id.detail_summmary_div)
    LinearLayout detailSummmaryDiv;
    @BindView(R.id.detail_summary)
    TextView detailSummary;

    @BindView(R.id.detail_feeling_div)
    LinearLayout detailFeelingDiv;
    @BindView(R.id.detail_feeling)
    EditText detailFeeling;

    @BindView(R.id.detail_ok_btn)
    Button okBtn;
    @BindView(R.id.detail_option_btn)
    Button optionBtn;

    @BindView(R.id.detail_fav_div)
    LinearLayout detailFavoriteTotalDiv;
    @BindView(R.id.detail_fav_image_layout)
    LinearLayout detailFavoriteDiv;
    @BindView(R.id.detail_image_favorite)
    ImageView favoriteImageView;


    private static int chapterIndex = 0;
    private String modeStatus = "";

    private Handler mHandler = new Handler ();
    private SoftKeyboard softKeyboard;

    private boolean favoiteFlag = false;
    private boolean posterImageFlag = true;

    private int screenW = 0;
    private int screenH = 0;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // -- KeyPad Event Control -- //
        InputMethodManager controlManager = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(totalLayout, controlManager);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        appBar.setVisibility(View.VISIBLE);
                    }
                });
            }
            @Override
            public void onSoftKeyboardShow() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        appBar.setVisibility(View.GONE);
                    }
                });
            }
        });

        // -- Get Intent Data -- //
        Intent intent = getIntent();
        Content item = (Content) intent.getSerializableExtra("CONTENT");
        modeStatus = intent.getStringExtra("MODE");

        // -- Screen Resolution -- //
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.detail_appbar);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)appBar.getLayoutParams();
        lp.height = screenH / 3;

        // -- ToolBar -- //
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        // -- Chapter Button Event Add -- //
        minusBtn.setOnTouchListener(mTouchEvent);
        plusBtn.setOnTouchListener(mTouchEvent);

        // -- Inflate Content -- //
        inflateContent(item);

        // for AD
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void inflateContent(Content Item) {

        // 버튼
        if(modeStatus.equals("MY")) {
            optionBtn.setText("삭제");
        } else {
            optionBtn.setText("추가");
        }

        // 챕터
        if(Item.chapter > 0) {
            detailChapterDiv.setVisibility(View.VISIBLE);
            chapterIndex = Item.chapter;
            chapterView.setText(String.valueOf(chapterIndex) + " 화");
        }

        // 타이틀
        detailTitle.setText(Item.name);
        detailOrgTitle.setText(Item.name_org);

        // 장르
        detailGenre.setText(ContentsActivity.genreMap.get(Item.gen_id));

        // 즐겨찾기
        if(modeStatus.equals("MY")) {
            detailFavoriteTotalDiv.setVisibility(View.VISIBLE);
        }

        // 시즌
        if(Item.season > 0) {
            detailSeasonDiv.setVisibility(View.VISIBLE);
            detailSeason.setText(String.valueOf(Item.season));
        }

        // 극장판
        if(Item.theatrical > 0) {
            detailTheaterDiv.setVisibility(View.VISIBLE);
            detailTheater.setChecked(true);
        }

        // 줄거리
        if(modeStatus.equals("TOTAL")) {
            detailSummmaryDiv.setVisibility(View.VISIBLE);
            if(Item.summary != null && Item.summary.length() > 0) {
                detailSummary.setText(Item.summary);
            }
        }

        // 감상평
        if(modeStatus.equals("MY")) {
            detailFeelingDiv.setVisibility(View.VISIBLE);
            if(Item.comment != null && Item.comment.length() > 0) {
                detailFeeling.setText(Item.comment);
            }
        }

        // 이미지 로딩
        String imageUrl = Item.image;
        imageUrl = imageUrl.replace("/resize/", "/images/");
        Picasso.get().load(imageUrl).into(tempImgView);
    }


    private View.OnTouchListener mTouchEvent = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                int id = v.getId();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (id == R.id.detail_minus_btn) {
                            mHandler.postDelayed(minusAction, 10);
                        } else if (id == R.id.detail_plus_btn) {
                            mHandler.postDelayed(plusAction, 10);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mHandler.removeCallbacks(plusAction);
                        mHandler.removeCallbacks(minusAction);
                        break;
                }
                return true;
            }

            Runnable plusAction = new Runnable() {
                @Override
                public void run() {

                    if(chapterIndex < 99) {
                        chapterIndex++;
                        chapterView.setText(String.valueOf(chapterIndex) + " 화");
                        mHandler.postDelayed(this, 100);
                    } else {
                        Toast.makeText(getApplicationContext(), "더 이상 안됩니다 -_-;;", Toast.LENGTH_SHORT).show();
                    }

                }
            };

            Runnable minusAction = new Runnable() {
                @Override
                public void run() {
                    if(chapterIndex > 1) {
                        chapterIndex--;
                        chapterView.setText(String.valueOf(chapterIndex) + " 화");
                        mHandler.postDelayed(this, 100);
                    } else {
                        Toast.makeText(getApplicationContext(), "더 이상 안됩니다 -_-;;", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
    }

    @OnClick(R.id.detail_ok_btn)
    void okClick() {
        finish();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @OnClick(R.id.detail_option_btn)
    void optionClick() {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @OnClick(R.id.detail_fav_image_layout)
    void detailFavoriteDivClick() {
        if(favoiteFlag) {
            favoiteFlag = false;
            favoriteImageView.setImageResource(R.mipmap.bookmark_favorite);
            Toast.makeText(getApplicationContext(), "즐겨찾기 해제", Toast.LENGTH_SHORT).show();
        } else {
            favoiteFlag = true;
            favoriteImageView.setImageResource(R.mipmap.bookmark_favorite_on);
            Toast.makeText(getApplicationContext(), "즐겨찾기 추가", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.temp_image)
    void AppBarClick() {
        if(posterImageFlag) {
            posterImageFlag = false;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)appBar.getLayoutParams();
            lp.height = screenH;
            tempImgView.setAlpha(1.0f);
            tempImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            detailZoom.setImageResource(R.mipmap.zoom_out);
            detailZoom.setAlpha(0.5f);
        } else {
            posterImageFlag = true;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)appBar.getLayoutParams();
            lp.height = screenH / 3;
            tempImgView.setAlpha(0.8f);
            tempImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            detailZoom.setImageResource(R.mipmap.zoom_in);
            detailZoom.setAlpha(0.8f);
        }
    }

}
