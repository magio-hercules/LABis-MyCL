package com.labis.mycl.contents;

import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.model.Genre;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.model.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG = "ContentsActivity";

    // -- Global Variable Section -- ////////////////////////////////////////
    RetroClient retroClient;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    RecyclerViewAdapter mAdapter;
    Toolbar toolbar;

    ArrayList<Content> myContents = new ArrayList();
    ArrayList<Genre> genreList = new ArrayList();

    String ModeStatus = "MY";
    boolean MyContentsRefresh = true;

    HashMap<String, String> genreMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);


        // -- ToolBar -- //
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("내 콘텐츠");
        setSupportActionBar(toolbar);


        // -- Genre Data Setting -- //
        genreMap.put("A01", "만화");
        genreMap.put("B01", "애니");
        genreMap.put("C01", "영화");


        // -- RecyclerView -- //
        mRecyclerView = (RecyclerView)findViewById(R.id.myContentsView);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new RecyclerViewDecoration(this, RecyclerViewDecoration.VERTICAL_LIST));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // -- RetroClient -- //
        retroClient = RetroClient.getInstance(this).createBaseApi();
        if(ModeStatus == "MY") {
            loadGetContents();
        } else if(ModeStatus == "TOTAL") {
            loadTotalContent();
        }


        // -- FloatingAction Button -- //
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                ModeStatus = "TOTAL";
                loadTotalContent();
                toolbar.setTitle("모든 콘텐츠");
                toolbar.setBackgroundResource(R.color.colorAccent);
            }
        });
    }

    // -- User Function Section -- ////////////////////////////////////////
    private void clearRecyclerView() {
        ArrayList<Content> items = new ArrayList();
        mAdapter = new RecyclerViewAdapter(items, genreMap, ModeStatus);
        mRecyclerView.setAdapter(mAdapter);
    }


    private void loadTotalContent() {
        clearRecyclerView();

        // Set up progress before call
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

        retroClient.getTotalContents(new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(LOG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(LOG, "SUCCESS");
                List<Content> data = (List<Content>) receivedData;
                if (!data.isEmpty()) {
                    ArrayList<Content> items = new ArrayList();
                    items.addAll(data);
                    mAdapter = new RecyclerViewAdapter(items, genreMap, ModeStatus);
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                }
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                Log.e(LOG, "FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }

    private void loadGetContents() {
        clearRecyclerView();

        if(MyContentsRefresh) {
            // Set up progress before call
            final ProgressDialog progressDoalog;
            progressDoalog = new ProgressDialog(this);
            progressDoalog.setMessage("잠시만 기다리세요....");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();

            retroClient.postMyContents("evol", new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.e(LOG, t.toString());
                    Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Log.e(LOG, "SUCCESS");
                    List<Content> data = (List<Content>) receivedData;
                    myContents.clear();
                    MyContentsRefresh = false;

                    if (!data.isEmpty()) {
                        myContents.addAll(data);
                        mAdapter = new RecyclerViewAdapter(myContents, genreMap, ModeStatus);
                        mRecyclerView.setAdapter(mAdapter);
                    } else {
                        Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                    }
                    progressDoalog.dismiss();
                }

                @Override
                public void onFailure(int code) {
                    Log.e(LOG, "FAIL");
                    Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }
            });
        } else {
            mAdapter = new RecyclerViewAdapter(myContents, genreMap, ModeStatus);
            mRecyclerView.setAdapter(mAdapter);
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
        } else if (ModeStatus.equals("TOTAL")) {
            ModeStatus = "MY";
            loadGetContents();
            toolbar.setBackgroundResource(R.color.colorPrimary);
            toolbar.setTitle("내 콘텐츠");
        } else {
            super.onBackPressed();
        }
    }
}
