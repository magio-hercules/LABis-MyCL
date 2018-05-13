package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.model.Genre;
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

    public String modeStatus = "MY";
    public Content touchContentItem;
    public boolean myContentsRefresh = true;

    public User userData = null;
    public HashMap<String, String> genreMap = new HashMap<String, String>();

    SwipeController swipeController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);

        // -- ToolBar -- //
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("내 콘텐츠");
        setSupportActionBar(toolbar);

        ArrayList<User> userList = getIntent().getParcelableArrayListExtra("user");
        if (userList != null) {
            userData = userList.get(0);
        } else {
            // for test
            userData = new User("evol", null, null, null, null, null, null);
        }

        // -- Genre Data Setting -- //
        ArrayList<Genre> genreData = getIntent().getParcelableArrayListExtra("genre");
        if (genreData != null) {
            for (int i = 0; i < genreData.size(); i++) {
                genreMap.put(genreData.get(i).id, genreData.get(i).name);
            }
        }

        // -- RecyclerView -- //
        mRecyclerView = (RecyclerView)findViewById(R.id.myContentsView);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        swipeController = new SwipeController(this, new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {
                mAdapter.mItems.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
            }

            @Override
            public void onRightClicked(int position) {
                addToMyContents(position);
            }
        });


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
            loadGetContents();
        } else if(modeStatus == "TOTAL") {
            loadTotalContent();
        }

        // -- FloatingAction Button -- //
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                modeStatus = "TOTAL";
                loadTotalContent();
                toolbar.setTitle("모든 콘텐츠");
            }
        });
    }

    // -- User Function Section -- ////////////////////////////////////////
    private void addToMyContents(int position) {
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

        int chapterCnt = 0;
        if (touchContentItem.chapter_end > 0) {
            chapterCnt = 1;
        }
        retroClient.postInsertMyContents(touchContentItem.id, userData.id, chapterCnt, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Toast.makeText(getApplicationContext(), "SUCCESS : " + code, Toast.LENGTH_SHORT).show();
                myContentsRefresh = true;
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                Toast.makeText(getApplicationContext(), "FAIL : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });

    }

    private void drawSwipeMenu() {
        swipeController.buttonShowedState = ButtonsState.GONE;

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
    }

    public void refrshList(ArrayList<Content> items) {
        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, items);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void loadTotalContent() {
        clearRecyclerView();

        // Set up progress before call
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
                Log.e(TAG, "SUCCESS");
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

    private void loadGetContents() {
        clearRecyclerView();

        if(myContentsRefresh) {
            // Set up progress before call
            final ProgressDialog progressDoalog;
            progressDoalog = new ProgressDialog(this);
            progressDoalog.setMessage("잠시만 기다리세요....");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();

            retroClient.postMyContents(userData.id, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, t.toString());
                    Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Log.e(TAG, "SUCCESS");
                    List<Content> data = (List<Content>) receivedData;
                    myContents.clear();
                    myContentsRefresh = false;

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
            modeStatus = "MY";
            loadGetContents();
            toolbar.setTitle("내 콘텐츠");
        } else {
            super.onBackPressed();
        }
    }
}
