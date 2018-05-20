package com.labis.mycl.contents;

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

    @BindView(R.id.detail_appbar)
    AppBarLayout appBar;

    @BindView(R.id.detail_minus_btn)
    TextView minusBtn;

    @BindView(R.id.detail_plus_btn)
    TextView plusBtn;

    @BindView(R.id.detail_chapter)
    TextView chapterView;

    private static int index = 0;

    private boolean breakFlag = false;
    private Handler mHandler = new Handler ();

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

        String imageUrl = item.image;
        imageUrl = imageUrl.replace("/resize/", "/images/");
        loadAppTitleImage(imageUrl);


        minusBtn.setOnTouchListener(mTouchEvent);
        plusBtn.setOnTouchListener(mTouchEvent);

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
                    index++;
                    chapterView.setText(String.valueOf(index) + " 화");
                    mHandler.postDelayed(this, 100);
                }
            };

            Runnable minusAction = new Runnable() {
                @Override
                public void run() {
                    index--;
                    chapterView.setText(String.valueOf(index) + " 화");
                    mHandler.postDelayed(this, 100);
                }
            };
        };

    private  void loadAppTitleImage(String url) {
        Target t = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                appBar.setBackground(d);
            }
            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

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
