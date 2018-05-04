package com.labis.mycl.contents;

import android.os.Bundle;
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
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.rest.models.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG = "ContentsActivity";

    // REST
    @BindView(R.id.contentlistView)
    TextView bodyResultTextView;
    RetroClient retroClient;

    // RECYCLER
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);

        //Adding toolbar to the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // RecyclerView
        mRecyclerView = (RecyclerView)findViewById(R.id.myContentsView);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // ArrayList 에 Item 객체(데이터) 넣기
        ArrayList<Item> items = new ArrayList();
        items.add(new Item("1", "하나","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("2", "둘","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("3", "셋","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("4", "넷","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("5", "다섯","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("1", "하나","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("2", "둘","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("3", "셋","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("4", "넷","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("5", "다섯","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("1", "하나","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("2", "둘","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("3", "셋","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("4", "넷","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        items.add(new Item("5", "다섯","https://www.google.co.kr/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"));
        // LinearLayout으로 설정
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Animation Defualt 설정
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Decoration 설정
        mRecyclerView.addItemDecoration(new RecyclerViewDecoration(this, RecyclerViewDecoration.VERTICAL_LIST));

        // Adapter 생성
        mAdapter = new RecyclerViewAdapter(items);
        mRecyclerView.setAdapter(mAdapter);

        ButterKnife.bind(this);
        retroClient = RetroClient.getInstance(this).createBaseApi();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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

    @OnClick(R.id.button)
    void get1() {
        Toast.makeText(this, "GET 1 Clicked", Toast.LENGTH_SHORT).show();
        retroClient.getUser("khercules", new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(LOG, t.toString());
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                Log.e(LOG, "SUCCESS");
                List<User> data = (List<User>) receivedData;
                if (!data.isEmpty()) {
                    bodyResultTextView.setText(data.get(0).nickname);
                } else {
                    bodyResultTextView.setText("Empty");
                }
            }

            @Override
            public void onFailure(int code) {
                Log.e(LOG, "FAIL");
                bodyResultTextView.setText("Failure");
            }
        });
    }
}
