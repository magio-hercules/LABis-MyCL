package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.model.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ContentsActivity";

    // -- Global Variable Section -- ////////////////////////////////////////
    public RetroClient retroClient;
    public RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    RecyclerViewAdapter mAdapter;
    Toolbar toolbar;

    public ArrayList<Content> myContents = new ArrayList();
    public ArrayList<Genre> genreList = new ArrayList();
    static String modeStatus = "MY";
    public Content touchContentItem;
    public boolean myContentsRefresh = true;

    public static User userData;
    public static HashMap<String, String> genreMap = new HashMap<String, String>();

    private ProgressDialog progressDoalog = null;
    Menu contentsMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);

        // -- ToolBar -- //
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // -- LoginData ( User , ArrayList<Gente> ) -- //
        LoginData logingData = (LoginData) getIntent().getExtras().getParcelable("LoingData");
        userData = logingData.getUser();
        ArrayList<Genre> genreData = logingData.getGenreList();
        if (genreData != null) {
            for (int i = 0; i < genreData.size(); i++) {
                genreMap.put(genreData.get(i).id, genreData.get(i).name);
            }
        }

        //-- ProgressDialog Setting --//
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // -- RecyclerView -- //
        mRecyclerView = (RecyclerView)findViewById(R.id.myContentsView);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        drawSwipeMenu();


        // -- DrawerLayout View -- //
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // -- RetroClient -- //
        retroClient = RetroClient.getInstance(this).createBaseApi();

        if(modeStatus == "MY") {
            loadMyContents();
        } else if(modeStatus == "TOTAL") {
            loadTotalContent();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_contents, menu);
        contentsMainMenu = menu;

        MenuItem item = menu.findItem(R.id.action_my_contents);
        item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_total_contents) {
            loadTotalContent();
            return true;
        } else if (id == R.id.action_my_contents) {
            myContentsRefresh = true;
            loadMyContents();
            return true;
        } else if (id == R.id.action_custom_contents) {
            Intent i = new Intent(getApplicationContext(), CustomActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.rightin_activity, R.anim.no_move_activity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    // -- User Function Section -- ////////////////////////////////////////


    private void delToMyContents(int position) {
        progressDoalog.show();
        final int pos= position;
        retroClient.postDeleteMyContents(userData.id, mAdapter.mItems.get(position).id, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
            @Override
            public void onSuccess(int code, Object receivedData) {
                Toast.makeText(getApplicationContext(), "DELETE SUCCESS : " + code, Toast.LENGTH_SHORT).show();
                mAdapter.mItems.remove(pos);
                mAdapter.notifyItemRemoved(pos);
                mAdapter.notifyItemRangeChanged(pos, mAdapter.getItemCount());

                myContentsRefresh = true;
                progressDoalog.dismiss();
            }
            @Override
            public void onFailure(int code) {
                Toast.makeText(getApplicationContext(), "DELETE FAIL : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }

    private void addToMyContents(int position) {
        int chapterCnt = 0;
        if (touchContentItem.chapter_end > 0) {
            chapterCnt = 1;
        }
        progressDoalog.show();
        retroClient.postInsertMyContents(touchContentItem.id, userData.id, chapterCnt, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Toast.makeText(getApplicationContext(), "INSERT SUCCESS : " + code, Toast.LENGTH_SHORT).show();
                myContentsRefresh = true;
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                Toast.makeText(getApplicationContext(), "INSERT FAIL : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });

    }

    private void drawSwipeMenu() {
        final SwipeController swipeController = new SwipeController(this, new SwipeControllerActions() {
            // MY콘텐츠 삭제
            @Override
            public void onLeftClicked(int position) {
                delToMyContents(position);
            }

            // MY콘텐츠 추가
            @Override
            public void onRightClicked(int position) {
                addToMyContents(position);
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(mRecyclerView);


        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    private void clearRecyclerView() {
        ArrayList<Content> items = new ArrayList();
        mAdapter = new RecyclerViewAdapter(this, items);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.refreshDrawableState();
    }

    public void updateItemDataView(int updateIndex) {
        mAdapter.notifyItemChanged(updateIndex);
    }

    public void loadTotalContent() {
        Log.d("EVOL","전체 리스트");

        modeStatus = "TOTAL";
        getSupportActionBar().setTitle("모든 콘텐츠");
        getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.actionBar)));
        progressDoalog.show();
        retroClient.postTotalContents(userData.id, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                clearRecyclerView(); //initialize
                contentsMainMenu.findItem(R.id.action_total_contents).setVisible(false);
                contentsMainMenu.findItem(R.id.action_my_contents).setVisible(true);
                List<Content> data = (List<Content>) receivedData;
                if (!data.isEmpty()) {
                    ArrayList<Content> items = new ArrayList();
                    items.addAll(data);
                    mAdapter = new RecyclerViewAdapter(ContentsActivity.this, items);
                    mRecyclerView.setAdapter(mAdapter);
                    drawSwipeMenu();
                } else {
                    Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                }
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }

    private void loadMyContents() {
        Log.d("EVOL","내 리스트 / " + modeStatus);
        modeStatus = "MY";
        getSupportActionBar().setTitle("내 콘텐츠");
        getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.colorPrimary)));
        progressDoalog.show();
        if(myContentsRefresh) {
            retroClient.postMyContents(userData.id, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, t.toString());
                    Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    clearRecyclerView(); // Initialize
                    myContentsRefresh = false;
                    contentsMainMenu.findItem(R.id.action_total_contents).setVisible(true);
                    contentsMainMenu.findItem(R.id.action_my_contents).setVisible(false);

                    List<Content> data = (List<Content>) receivedData;
                    myContents.clear();
                    if (!data.isEmpty()) {
                        myContents.addAll(data);
                        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, myContents);
                        mRecyclerView.setAdapter(mAdapter);
                        drawSwipeMenu();
                    } else {
                        Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                    }
                    progressDoalog.dismiss();
                }

                @Override
                public void onFailure(int code) {
                    Log.e(TAG, "FAIL");
                    Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }
            });
        } else {
            mAdapter = new RecyclerViewAdapter(this, myContents);
            mRecyclerView.setAdapter(mAdapter);
            drawSwipeMenu();
            progressDoalog.dismiss();
        }
    }

    // -- Drawer Section -- ////////////////////////////////////////
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.usadrama) {
            // Handle the camera action
        } else if (id == R.id.koreadrama) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (modeStatus.equals("TOTAL")) {
            myContentsRefresh = true;
            loadMyContents();
        } else {
            super.onBackPressed();
        }
    }
}
