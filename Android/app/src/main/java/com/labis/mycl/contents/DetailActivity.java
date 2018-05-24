package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.labis.mycl.util.SoftKeyboard;
import com.labis.mycl.util.Utility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    @BindView(R.id.detail_ll)
    RelativeLayout totalLayout;

    @BindView(R.id.detail_appbar)
    AppBarLayout appBar;

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

    @BindView(R.id.temp_image)
    ImageView tempImgView;

    @BindView(R.id.detail_save_btn)
    Button saveBtn;

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
    }

    private void inflateContent(Content Item) {

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
                    chapterIndex++;
                    chapterView.setText(String.valueOf(chapterIndex) + " 화");
                    mHandler.postDelayed(this, 100);
                }
            };

            Runnable minusAction = new Runnable() {
                @Override
                public void run() {
                    chapterIndex--;
                    chapterView.setText(String.valueOf(chapterIndex) + " 화");
                    mHandler.postDelayed(this, 100);
                }
            };
        };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
    }

    @OnClick(R.id.detail_save_btn)
    void click() {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        softKeyboard.unRegisterSoftKeyboardCallback();
    }

    @OnClick(R.id.detail_fav_image_layout)
    void detailFavoriteDivClick() {
        if(favoiteFlag) {
            favoiteFlag = false;
            favoriteImageView.setImageResource(R.mipmap.bookmark_favorite);
        } else {
            favoiteFlag = true;
            favoriteImageView.setImageResource(R.mipmap.bookmark_favorite_on);
        }
    }

}
