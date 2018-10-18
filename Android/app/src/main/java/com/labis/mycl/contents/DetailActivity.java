package com.labis.mycl.contents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.SoftKeyboard;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//import com.google.android.gms.ads.AdView;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    public RetroClient retroClient;

    @BindView(R.id.detail_ll)
    RelativeLayout totalLayout;

    @BindView(R.id.detail_appbar)
    AppBarLayout appBar;
    @BindView(R.id.detail_zoom_btn)
    ImageView detailZoom;
    @BindView(R.id.temp_image)
    ImageView tempImgView;
    @BindView(R.id.original_image)
    ImageView orgImgView;

    @BindView(R.id.detail_minus_btn)
    TextView minusBtn;

    @BindView(R.id.detail_plus_btn)
    TextView plusBtn;

    @BindView(R.id.detail_chapter_div)
    LinearLayout detailChapterDiv;
    @BindView(R.id.detail_chapter)
    TextView chapterView;

    //EVOL
    @BindView(R.id.detail_complete_div)
    LinearLayout detailCompleteDiv;
    @BindView(R.id.detail_switch_complete)
    Switch detailComplete;

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

    //즐겨찾기
    @BindView(R.id.detail_fav_div)
    LinearLayout detailFavoriteTotalDiv;
    @BindView(R.id.detail_image_favorite)
    ImageView favoriteImageView;

    @BindView(R.id.detail_scroll_view)
    ScrollView scrollView;

    @BindView(R.id.adview_layout)
    LinearLayout adviewLayout;

    private Handler mHandler = new Handler ();
    private SoftKeyboard softKeyboard;

    private boolean posterImageFlag = true;
    private boolean posterLoadFlag = true;
    private int screenW = 0;
    private int screenH = 0;

    // Google AD
//    private AdView mAdView;
//    private AdView mAdViewPoster;

    // Facebook AD
    private AdView adViewDetail;
    private AdView adViewPoster;

    // Original Item & Flag
    Content orgContentInfo = null;
    private String userID = null;
    private String modeStatus = "";
    private String genID ="";

    // Info Data
    private int chapterIndex;
    private int favoiteFlag;
    private int completeFlag;
    private String feelingStr;

    private String curImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // -- RetroClient -- //
        retroClient = RetroClient.getInstance(this).createBaseApi();

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
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(View.FOCUS_DOWN);
                                detailFeeling.requestFocus();
                            }
                        }, 600);
                    }
                });
            }
        });

        // -- Get Intent Data -- //
        Intent intent = getIntent();
        orgContentInfo = (Content) intent.getSerializableExtra("CONTENT");
        modeStatus = intent.getStringExtra("MODE");
        userID =  intent.getStringExtra("USER");

        chapterIndex = orgContentInfo.chapter;
        favoiteFlag = orgContentInfo.favorite;
        completeFlag = orgContentInfo.score;
        if(orgContentInfo.comment ==  null) orgContentInfo.comment ="";
        feelingStr = orgContentInfo.comment;
        genID = orgContentInfo.gen_id;

        // -- Screen Resolution -- //
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.detail_appbar);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)appBar.getLayoutParams();
        lp.height = screenH / 3;

        // -- Chapter Button Event Add -- //
        minusBtn.setOnTouchListener(mTouchEvent);
        plusBtn.setOnTouchListener(mTouchEvent);

        // -- Inflate Content -- //
        inflateContent(orgContentInfo);

        // for AD
       /* AdRequest adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(adRequest);

        mAdViewPoster = findViewById(R.id.adView_poster);
        mAdViewPoster.loadAd(adRequest);
        */

        // Facebook detail AD
        LinearLayout adConDetail = (LinearLayout) findViewById(R.id.ad_facebook_con_detail);
        adViewDetail = new AdView(this, getString(R.string.facebook_detail), AdSize.BANNER_HEIGHT_50);
        adViewDetail.setAdListener(new AdListener() {
            @Override public void onError(Ad ad, AdError adError) { Log.d(TAG,"Facebook detail AD load error (" + adError.getErrorMessage() + ")"); }
            @Override public void onAdLoaded(Ad ad) { Log.d(TAG,"Facebook detail AD loaded)"); }
            @Override public void onAdClicked(Ad ad) { }
            @Override public void onLoggingImpression(Ad ad) { }
        });
        adConDetail.addView(adViewDetail);
        adViewDetail.loadAd();

        // Facebook poster AD
        LinearLayout adConPoster = (LinearLayout) findViewById(R.id.ad_facebook_con_poster);
        adViewPoster = new AdView(this, getString(R.string.facebook_poster), AdSize.BANNER_HEIGHT_50);
        adViewPoster.setAdListener(new AdListener() {
            @Override public void onError(Ad ad, AdError adError) { Log.d(TAG,"Facebook poster AD load error (" + adError.getErrorMessage() + ")"); }
            @Override public void onAdLoaded(Ad ad) { Log.d(TAG,"Facebook poster AD loaded)"); }
            @Override public void onAdClicked(Ad ad) { }
            @Override public void onLoggingImpression(Ad ad) { }
        });
        adConPoster.addView(adViewPoster);
        adViewPoster.loadAd();


        detailFeeling.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                feelingStr = s.toString();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        detailComplete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    completeFlag = 1;
                } else {
                    completeFlag = 0;
                }
            }
        });
    }

    private void inflateContent(Content Item) {

        // 버튼
        if(modeStatus.equals("MY")) {
            optionBtn.setText("삭제");
        } else {
            optionBtn.setText("추가");
        }

        // 챕터
        if(!isNoChapterGenre(Item.gen_id) && modeStatus.equals("MY")) {
            detailChapterDiv.setVisibility(View.VISIBLE);
            String eof = " 화";
            if(genID.indexOf('A') != -1) eof = " 권";
            chapterView.setText(String.valueOf(chapterIndex) + eof);

            //완결 표시
            detailCompleteDiv.setVisibility(View.VISIBLE);
            if (completeFlag == 1) {
                //completeImageView.setImageResource(R.mipmap.bookmark_complete_on);
                detailComplete.setChecked(true);
            } else {
                //completeImageView.setImageResource(R.mipmap.bookmark_complete);
                detailComplete.setChecked(false);
            }
        }

        // 타이틀
        detailTitle.setText(Item.name);
        detailOrgTitle.setText(Item.name_org);

        // 장르
        detailGenre.setText(ContentsActivity.genreMap.get(Item.gen_id));

        // 즐겨찾기
        if(modeStatus.equals("MY")) {
            detailFavoriteTotalDiv.setVisibility(View.VISIBLE);
            if (favoiteFlag == 1) {
                favoriteImageView.setImageResource(R.mipmap.bookmark_favorite_on);
            } else {
                favoriteImageView.setImageResource(R.mipmap.bookmark_favorite);
            }
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
            if( Item.comment.length() > 0) {
                detailFeeling.setText(Item.comment);
            }
        }

        // 이미지 로딩
        curImageUrl = Item.image;
        if(curImageUrl != null && curImageUrl.length() > 10) {
//            imageUrl = imageUrl.replace("/resize/", "/images/");
            Picasso.get().load(curImageUrl).into(tempImgView);
        }
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
                if (chapterIndex < 999) {
                    chapterIndex++;
                    String eof = " 화";
                    if(genID.equals("A00")) eof = " 권";
                    chapterView.setText(String.valueOf(chapterIndex) + eof);
                    mHandler.postDelayed(this, 50);
                } else {
                    Toast.makeText(getApplicationContext(), "더 이상 안됩니다 -_-;;", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Runnable minusAction = new Runnable() {
            @Override
            public void run() {
                if (chapterIndex > 1) {
                    chapterIndex--;
                    String eof = " 화";
                    if(genID.equals("A00")) eof = " 권";
                    chapterView.setText(String.valueOf(chapterIndex) + eof);
                    mHandler.postDelayed(this, 50);
                } else {
                    Toast.makeText(getApplicationContext(), "더 이상 안됩니다 -_-;;", Toast.LENGTH_SHORT).show();
                }
            }
        };
    };

    private Boolean isNoChapterGenre(String gen_id) {
        // 영화
        if(gen_id.equals("B02")) {
            return true;
        }
        return false;
    }

    private Boolean isChangeInfo() {
        boolean result = false;
        if(orgContentInfo.chapter != chapterIndex ) {
            result = true;         // 챕터 정보
        }
        if(orgContentInfo.favorite != favoiteFlag ) {
            result = true;        // 즐겨찾기 정보
        }
        if(orgContentInfo.score != completeFlag ) {
            result = true;        // 완결 정보
        }
        if(orgContentInfo.comment != feelingStr ) {
            result = true;        // 감상평
        }
        return result;
    }

    private void updateContent() {
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

        Log.d(TAG,"EVOL = " + userID);
        retroClient.postUpdateMyContents(orgContentInfo.id, userID, chapterIndex, favoiteFlag, completeFlag, feelingStr, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                progressDoalog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!posterImageFlag) {
            showPoster(posterImageFlag);
        } else {
            overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
            super.onBackPressed();
        }
    }

    @OnClick(R.id.detail_ok_btn)
    void okClick() {
        if(isChangeInfo()) { // 확인
            updateContent(); // 업데이트 실행
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @OnClick(R.id.detail_option_btn)
    void optionClick() {
        if(modeStatus.equals("MY")) { //삭제
            AlertDialog alertDialog = new AlertDialog.Builder(DetailActivity.this)
                    .setMessage("삭제하시겠습니까?")
                    .setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent returnIntent = new Intent();
                                    setResult(Activity.RESULT_FIRST_USER, returnIntent);
                                    finish();
                                }
                            })
                    .setNegativeButton("아니요",
                            new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { }
                            }).create();
            alertDialog.show();
        } else if(modeStatus.equals("TOTAL")) { //추가
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_FIRST_USER, returnIntent);
            finish();
        }
    }

    @Override
    public void onDestroy()
    {
        if (adViewDetail != null) {
            adViewDetail.destroy();
        }
        if (adViewPoster != null) {
            adViewPoster.destroy();
        }

        super.onDestroy();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @OnClick(R.id.detail_fav_image_layout)
    void detailFavoriteDivClick() {
        if(favoiteFlag == 1) {
            favoiteFlag = 0;
            favoriteImageView.setImageResource(R.mipmap.bookmark_favorite);
        } else {
            favoiteFlag = 1;
            favoriteImageView.setImageResource(R.mipmap.bookmark_favorite_on);
        }
    }


    @OnClick(R.id.temp_image)
    void AppBarClick() {
        showPoster(posterImageFlag);
    }

    @OnClick(R.id.detail_zoom_btn)
    void ZoomIconClick() {
        showPoster(posterImageFlag);
    }

    private void showPoster(boolean bShow) {
        if(bShow) {
            posterImageFlag = false;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)appBar.getLayoutParams();
            lp.height = screenH;
            tempImgView.setAlpha(1.0f);
            tempImgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            detailZoom.setImageResource(R.mipmap.zoom_out);
            //detailZoom.setAlpha(0.5f);

            // 동적으로 margin 변경하기
            int marginRightPixel = convertDptoPixel(2);
            int marginBottomPixel = convertDptoPixel(2);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) detailZoom.getLayoutParams();
            layoutParams.setMargins(0, 0, marginRightPixel, marginBottomPixel);
            detailZoom.setLayoutParams(layoutParams);

            orgImgView.setVisibility(View.VISIBLE);
            adviewLayout.setVisibility(View.VISIBLE);

            if(posterLoadFlag && curImageUrl != null && curImageUrl.length() > 10) {
                String imageUrl = curImageUrl.replace("/resize/", "/images/");
                Picasso.get().load(imageUrl).into(orgImgView);
                posterLoadFlag = false;
            }
        } else {
            posterImageFlag = true;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)appBar.getLayoutParams();
            lp.height = screenH / 3;
            tempImgView.setAlpha(0.8f);
            tempImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            detailZoom.setImageResource(R.mipmap.zoom_in);

            // 동적으로 margin 변경하기
            int marginRightPixel = convertDptoPixel(2);
            int marginBottomPixel = convertDptoPixel(2);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) detailZoom.getLayoutParams();
            layoutParams.setMargins(0, 0, marginRightPixel, marginBottomPixel);
            detailZoom.setLayoutParams(layoutParams);

            orgImgView.setVisibility(View.GONE);
            adviewLayout.setVisibility(View.GONE);
        }
    }

    private int convertDptoPixel(int dp) {
        if (dp == 0) {
            return 0;
        }

        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
