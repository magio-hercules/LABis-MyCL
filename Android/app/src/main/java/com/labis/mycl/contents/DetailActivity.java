package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.labis.mycl.R;
import com.labis.mycl.model.Content;
import com.labis.mycl.util.Utility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

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

    @BindView(R.id.deatal_ad_image)
    ImageView imgView;


    private static int chapterIndex = 0;

    private boolean breakFlag = false;
    private Handler mHandler = new Handler ();

    ImageView backImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // -- Get Intent Data -- //
        Intent intent = getIntent();
        Content item = (Content) intent.getSerializableExtra("CONTENT");

        // -- ToolBar -- //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("상세보기");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

        minusBtn.setOnTouchListener(mTouchEvent);
        plusBtn.setOnTouchListener(mTouchEvent);

       //

        backImg = new ImageView(this);
        String imageUrl = item.image;
        imageUrl = imageUrl.replace("/resize/", "/images/");
        Picasso.get().load(imageUrl).into(imgView);

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
        if(Item.summary != null && Item.summary.length() > 0) {
            detailSummmaryDiv.setVisibility(View.VISIBLE);
            detailSummary.setText(Item.summary);
        }

        //Drawable d = new BitmapDrawable(getResources(), bitmap);

        appBar.setBackground(imgView.getBackground());

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

    private  void loadAppTitleImage(String url, final Content item) {
        final ProgressDialog progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Target t = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                appBar.setBackground(d);
                progressDoalog.dismiss();
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                progressDoalog.dismiss();
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                progressDoalog.show();
            }
        };
        Picasso.get().load(url).into(t);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_move_activity, R.anim.rightout_activity);
    }






}
