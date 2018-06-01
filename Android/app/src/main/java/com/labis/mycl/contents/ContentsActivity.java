package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.login.LoginActivity;
import com.labis.mycl.login.UrlActivity;
import com.labis.mycl.model.Content;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.AuthManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ContentsActivity";

    // -- Global Variable Section -- ////////////////////////////////////////
    public RetroClient retroClient;

    RecyclerViewAdapter mAdapter;
    Toolbar toolbar;

    public ArrayList<Content> myContents = new ArrayList();
    public ArrayList<Genre> genreList = new ArrayList();
    public static String modeStatus = "MY";
    public Content touchContentItem;
    public boolean myContentsRefresh = true;

    public static User userData;
    public static HashMap<String, String> genreMap = new HashMap<String, String>();

    private ProgressDialog progressDoalog = null;
    Menu contentsMainMenu;

    SwipeController swipeController = null;

    private long lastTimeBackPressed;

    private String selectedGenreId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);

        // -- ToolBar -- //
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("내 콘텐츠");

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

        MenuItem item = contentsMainMenu.findItem(R.id.action_my_contents);
        item.setVisible(false);

        if (userData.id.equals("labis@labis.com")) {
            item = contentsMainMenu.findItem(R.id.action_s3_url);
            item.setVisible(true);
        }

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
        }  else if (id == R.id.action_search_contents) {
            // 검색 기능 활성화
            return true;
        } else if (id == R.id.action_sign_out) {
            AuthManager authManager = AuthManager.getInstance();
            authManager.signOut();

            Intent i = new Intent(ContentsActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.action_s3_url) {
            Intent i = new Intent(ContentsActivity.this, UrlActivity.class);
            startActivity(i);
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
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.myContentsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        swipeController = new SwipeController(this, new SwipeControllerActions() {
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
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    private void clearRecyclerView() {
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.myContentsView);
        ArrayList<Content> items = new ArrayList();
        mAdapter = new RecyclerViewAdapter(this, items);
        recyclerView.setAdapter(mAdapter);
        recyclerView.refreshDrawableState();
    }

    public void updateItemDataView(int updateIndex) {
        mAdapter.notifyItemChanged(updateIndex);
    }

    public void loadTotalContent() {
        Log.d("EVOL","전체 리스트");
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
                modeStatus = "TOTAL";
                getSupportActionBar().setTitle("모든 콘텐츠");
                getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.actionBar)));

                contentsMainMenu.findItem(R.id.action_total_contents).setVisible(false);
                contentsMainMenu.findItem(R.id.action_my_contents).setVisible(true);
                List<Content> data = (List<Content>) receivedData;
                if (!data.isEmpty()) {
                    ArrayList<Content> items = new ArrayList();
                    items.addAll(data);
                    mAdapter = new RecyclerViewAdapter(ContentsActivity.this, items);
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

        resetDrawerCheckedItem();
    }

    private void loadMyContents() {
        Log.d("EVOL","내 리스트 / " + modeStatus);
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
                    modeStatus = "MY";
                    getSupportActionBar().setTitle("내 콘텐츠");
                    getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.colorPrimary)));

                    myContentsRefresh = false;
                    contentsMainMenu.findItem(R.id.action_total_contents).setVisible(true);
                    contentsMainMenu.findItem(R.id.action_my_contents).setVisible(false);

                    List<Content> data = (List<Content>) receivedData;
                    myContents.clear();
                    if (!data.isEmpty()) {
                        myContents.addAll(data);
                        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, myContents);
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
            drawSwipeMenu();
            progressDoalog.dismiss();
        }

        resetDrawerCheckedItem();
    }

    // -- Drawer Section -- ////////////////////////////////////////
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.e(TAG, "onNavigationItemSelected");

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title = item.getTitle().toString();
        String genreId = null;

        switch (id) {
            case R.id.favorite:
                genreId = "";
                // TODO
                // 선호장르 조회하기
                break;
            case R.id.usadrama:
                genreId = "B06";
                break;
            case R.id.koreadrama:
                genreId = "B05";
                break;
            case R.id.japandrama:
                genreId = "B07";
                break;
            case R.id.movie:
                genreId = "B02";
                break;
            case R.id.cartoon:
                genreId = "A01";
                break;
            case R.id.animation:
                genreId = "B01";
                break;
            case R.id.tv:
                genreId = "B08";
                break;
            case R.id.book:
                genreId = "A00";
                break;
            case R.id.etc:
                genreId = "Z00";
                break;
            default:
                break;
        }

        if (selectedGenreId != genreId) {
            selectedGenreId = genreId;
            item.setChecked(true);
        } else {
            title = "";
            selectedGenreId = genreId = null;
            item.setChecked(false);
        }

        Log.e(TAG, "title : " + title + ", genreId : " + genreId);
        if (modeStatus.equals("MY")) {
            filterJenreMyContents(title, userData.id, genreId);
        } else if (modeStatus.equals("TOTAL")) {
            filterJenreContentsList(title, genreId);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void resetDrawerCheckedItem() {
        Log.e(TAG, "resetDrawerCheckedItem()");

        selectedGenreId = null;

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (modeStatus.equals("TOTAL")) {
            myContentsRefresh = true;
            loadMyContents();
        } else if (modeStatus.equals("MY")) {
            if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
//            finish();
                finishAffinity();
                return;
            }

            Toast toast = Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            toast.show();
            lastTimeBackPressed = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    private void filterJenreMyContents(final String title, String userId, String gen_id) {
        Log.e(TAG, "filterJenreMyContents");
        Log.e(TAG, "doFilter() title : " + title);
        Log.e(TAG, "doFilter() user_id : " + userId);
        Log.e(TAG, "doFilter() gen_id : " + gen_id);

        progressDoalog.show();
        retroClient.postFilterMyContents(userId, gen_id, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                clearRecyclerView(); //initialize
//                modeStatus = "TOTAL";

                getSupportActionBar().setTitle("내 콘텐츠" + (title.equals("") ? "" : " (" + title + ")"));
//                getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.actionBar)));

                myContentsRefresh = false;

                List<Content> data = (List<Content>) receivedData;
                myContents.clear();
                if (!data.isEmpty()) {
                    myContents.addAll(data);
                    mAdapter = new RecyclerViewAdapter(ContentsActivity.this, myContents);
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

    private void filterJenreContentsList(final String title, String gen_id) {
        Log.e(TAG, "filterJenreContentsList");
        Log.e(TAG, "doFilter() title : " + title);
        Log.e(TAG, "doFilter() gen_id : " + gen_id);

        progressDoalog.show();
        retroClient.postFilterContentsList(gen_id, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                clearRecyclerView(); //initialize
//                modeStatus = "TOTAL";

                getSupportActionBar().setTitle("모든 콘텐츠" + (title.equals("") ? "" : " (" + title + ")"));
//                getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.actionBar)));

                List<Content> data = (List<Content>) receivedData;
                if (!data.isEmpty()) {
                    ArrayList<Content> items = new ArrayList();
                    items.addAll(data);
                    mAdapter = new RecyclerViewAdapter(ContentsActivity.this, items);
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
}
