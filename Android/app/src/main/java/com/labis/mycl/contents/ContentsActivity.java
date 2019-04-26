package com.labis.mycl.contents;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.labis.mycl.MainActivity;
import com.labis.mycl.R;
import com.labis.mycl.help.HelpActivity;
import com.labis.mycl.login.RegisterActivity;
import com.labis.mycl.login.UrlActivity;
import com.labis.mycl.model.Content;
import com.labis.mycl.model.Genre;
import com.labis.mycl.model.LoginData;
import com.labis.mycl.model.User;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.util.AuthManager;
import com.labis.mycl.util.CircleTransform;
import com.labis.mycl.util.RecyclerItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.mauker.materialsearchview.MaterialSearchView;
import br.com.mauker.materialsearchview.db.HistoryContract;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BillingProcessor.IBillingHandler {

    private static final String TAG = "ContentsActivity";

    // Remove ADs
    public static boolean RemoveAD = false;
    private BillingProcessor mBillingProcessor;
    public String InAppProductID= "mycl1500";

    // -- Choi Uk / Global Variable Section -- ////////////////////////////////////////
    public RetroClient retroClient;
    // Content Data Variable
    public ArrayList<Content> ContentsList = new ArrayList();
    public ArrayList<Content> uiShowContentsList = new ArrayList();
    public ArrayList<Content> myContentsBackup = new ArrayList();
    public ArrayList<Content> editContents = new ArrayList();

    public static String modeStatus = "MY";
    public boolean myContentsRefresh = true;
    // Login Data Variable
    public static User userData = null;
    public static HashMap<String, String> genreMap = new HashMap<String, String>();
    // UI Variable
    TextView mNickName;
    ImageView mProfileImage;
    TextView mLoginEmail;
    RecyclerViewAdapter mAdapter;
    Toolbar toolbar;
    private ProgressDialog progressDoalog = null;
    Menu contentsMainMenu;
    Menu contentsEditMenu;
    ActionMode mActionMode;
    public boolean isMultiSelect = false;
    RecyclerView recyclerView;
    public static final int PICK_EDIT_REQUEST = 1;
    public int editPosition = -1;
    private MaterialSearchView searchView;
    private ArrayList<String> contestsTitleSuggestionsArray = new ArrayList<String>();
    private boolean isSearchMode = false;
    @BindView(R.id.empty_my_content_layout)
    LinearLayout emptyBackground;

    // -- Kim Jong Min / Global Variable Section -- ////////////////////////////////////////
    @BindView(R.id.drawer_btn_logout)
    Button btnLogout;
    @BindView(R.id.drawer_btn_edit)
    Button btnEdit;
    @BindView(R.id.profile_image)
    ImageView imageProfile;
    private long lastTimeBackPressed;
    private String selectedGenreId;
    private String selectedSubTitle;

    // for Guest Login
    private boolean bGuestMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);
        ButterKnife.bind(this);

        // - InApp & Remove AD
        mBillingProcessor = new BillingProcessor(this, getResources().getString(R.string.licence_key), this);
        mBillingProcessor.initialize();
        RemoveAD = mBillingProcessor.isPurchased(InAppProductID);

        // -- ToolBar -- //
        toolbar = (Toolbar) findViewById(R.id.content_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("내 콘텐츠");

        // -- LoginData ( User , ArrayList<Gente> ) -- //
        LoginData logingData = (LoginData) getIntent().getExtras().getParcelable("LoingData");
        if (logingData != null) {
            userData = logingData.getUser();
            ArrayList<Genre> genreData = logingData.getGenreList();
            if (genreData != null) {
                for (int i = 0; i < genreData.size(); i++) {
                    genreMap.put(genreData.get(i).id, genreData.get(i).name);
                }
            }
            mProfileImage = (ImageView) findViewById(R.id.profile_image);
            mNickName = (TextView) findViewById(R.id.nick_name);
            mLoginEmail = (TextView) findViewById(R.id.login_email);
            if(userData != null && userData.image != null && userData.image.length() > 10) {
                Picasso.get().load(userData.image).transform(new CircleTransform()).into(mProfileImage);
            }
            mNickName.setText(userData.nickname);
            mLoginEmail.setText(userData.id);
        }

        // for Guest Login
        String loginMode = getIntent().getStringExtra("LoginMode");
        if (loginMode != null && loginMode.equals("GUEST")) {
            modeStatus = "TOTAL";

            bGuestMode = true;
            btnLogout.setText("로그인");
            mNickName.setText("게스트");
        }

        //-- ProgressDialog Setting --//
        progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // -- RecyclerView -- //
        recyclerView = (RecyclerView) findViewById(R.id.myContentsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ContentsActivity.this,this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect) {
                    multiSelectItem(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    isMultiSelect = true;
                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }
                multiSelectItem(position);
            }

        }));


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
        if (modeStatus == "MY") {
            loadMyContents();
        } else if (modeStatus == "TOTAL") {
            loadTotalContent();
        }

        // -- Search UI -- //
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setHint("검색");
        searchView.setVoiceHintPrompt("찾고 싶은 콘텐츠의 제목을 말하세요.");
        searchView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        searchView.setListTextColor(getResources().getColor(R.color.recyclerView));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchContents(query);
                return false;
            };

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String suggestion = searchView.getSuggestionAtPosition(i);
                getContentResolver().delete(
                        HistoryContract.HistoryEntry.CONTENT_URI,
                        HistoryContract.HistoryEntry.TABLE_NAME +
                                "." +
                                HistoryContract.HistoryEntry.COLUMN_QUERY +
                                " = ? AND " +
                                HistoryContract.HistoryEntry.TABLE_NAME +
                                "." +
                                HistoryContract.HistoryEntry.COLUMN_IS_HISTORY +
                                " = ?"
                        ,
                        new String[]{suggestion, String.valueOf(1)}
                );
                searchView.closeSearch();
                return true;
            }
        });

    }


    // -- User Function Section -- ////////////////////////////////////////
    public void multiSelectItem(int position) {
        if (mActionMode != null && position > -1) {

            if(editContents.size() == 0) {
                if (modeStatus.equals("MY")) {
                    mActionMode.setTitle("삭제 항목 선택");
                } else {
                    mActionMode.setTitle("추가 항목 선택");
                }
            }

            if(selectedGenreId == null && isSearchMode == false) {  // Normal Mode
                Content selectItem = ContentsList.get(position);
                // Exist Check
                if(isExistMyContent(selectItem)) {
                    Toast.makeText(getApplicationContext(), "내 콘텐츠 보유 항목", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editContents.contains(selectItem)) {
                    editContents.remove(selectItem);
                } else {
                    editContents.add(selectItem);
                }
            } else {                                                // Search or Filter Mode
                Content selectItem = uiShowContentsList.get(position);
                // Exist Check
                if(isExistMyContent(selectItem)) {
                    Toast.makeText(getApplicationContext(), "내 콘텐츠 보유 항목", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editContents.contains(selectItem)) {
                    editContents.remove(selectItem);
                } else {
                    editContents.add(selectItem);
                }
            }

            if (editContents.size() > 0) {
                mActionMode.setTitle("" + editContents.size() + "개 항목 선택");
            } else {
                if(modeStatus.equals("MY")) {
                    mActionMode.setTitle("삭제 항목 선택");
                } else {
                    mActionMode.setTitle("추가 항목 선택");
                }
            }
            mAdapter.notifyDataSetChanged();

        }
    }

    private boolean isExistMyContent(Content item) {
        if(modeStatus.equals("TOTAL")) {
            for (Content i : myContentsBackup) {
                if (i.id.equals(item.id)) return true;
            }
        }
        return false;
    }

    public void delToMyContents() {
        progressDoalog.show();
        ArrayList<String> deleteList = new ArrayList<String>();
        for (Content item : editContents) {
            deleteList.add(item.id);
            Log.d(TAG, "EVOL Item ID : " + item.id);
        }

        retroClient.postDeleteMyContents(userData.id, deleteList, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                for (Content item : editContents) {
                    ContentsList.remove(item);
                    if (selectedGenreId != null || isSearchMode == true) {
                        uiShowContentsList.remove(item);
                        ContentsList.remove(item);
                    }
                }
                editContents.clear();
                mAdapter.notifyDataSetChanged();

                if(isSearchMode) {
                    getSupportActionBar().setTitle("검색 결과 " + uiShowContentsList.size() + "개");
                }
                if(selectedGenreId != null) {
                    getSupportActionBar().setSubtitle("( " + selectedSubTitle + " " + uiShowContentsList.size() + "개 )");
                }

                if (mActionMode != null) {
                    mActionMode.finish();
                }
                emptyToggle();
                progressDoalog.dismiss();
                Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code) {
                Toast.makeText(getApplicationContext(), "DELETE FAIL : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }

    private void addToMyContents() {
        progressDoalog.show();
        ArrayList<String> addList = new ArrayList<String>();
        for(Content item : editContents) {
            myContentsBackup.add(item);
            addList.add(item.id);
            Log.d(TAG,"EVOL Item ID : " + item.id);
        }

        retroClient.postInsertMyContents(userData.id, addList, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                myContentsRefresh = true;
                editContents.clear();

                if (mActionMode != null) {
                    mActionMode.finish();
                }
                emptyToggle();
                progressDoalog.dismiss();
                Toast.makeText(getApplicationContext(), "추가 완료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code) {
                Toast.makeText(getApplicationContext(), "INSERT FAIL : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
    }

    private void clearRecyclerView() {
        ContentsList.clear();
        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.refreshDrawableState();
    }

    public void updateItemDataView(int updateIndex) {
        mAdapter.notifyItemChanged(updateIndex);
    }

    public void loadTotalContent() {
        // Backup MyContentList
        myContentsBackup.clear();
        myContentsBackup.addAll(ContentsList);

        String userId = "";
        if (userData != null) {
            userId = userData.id;
        }

        progressDoalog.show();
        retroClient.postTotalContents(userId, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Log.e(TAG, t.toString());
                Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                clearRecyclerView(); //initialize
                resetDrawerCheckedItem(); // Init Filter
                modeStatus = "TOTAL";
                getSupportActionBar().setTitle("모든 콘텐츠");
                getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.actionBar)));

                editContents.clear();
                ContentsList.clear();
                contestsTitleSuggestionsArray.clear();
                List<Content> data = (List<Content>) receivedData;

                if (!data.isEmpty()) {
                    ContentsList.addAll(data);
                    //Search Suggestion
                    mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
                    recyclerView.setAdapter(mAdapter);

                    //Search Suggestion
                    for (Content i : ContentsList) {
                        contestsTitleSuggestionsArray.add(i.name);
                        contestsTitleSuggestionsArray.add(i.name_org);

                    }

                } else {
                    Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                }

                if(contentsMainMenu != null) {
                    contentsMainMenu.findItem(R.id.action_total_contents).setVisible(false);
                    contentsMainMenu.findItem(R.id.action_my_contents).setVisible(true);
                    contentsMainMenu.findItem(R.id.action_edit_contents).setTitle("내 콘텐츠에 추가");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        searchView.clearSuggestions();
                        searchView.addSuggestions(contestsTitleSuggestionsArray);
                    }
                }).start();
                emptyToggle();
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(int code) {
                Log.e(TAG, "FAIL");
                Toast.makeText(getApplicationContext(), "Failure Code : " + code, Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }
        });
        setDrawerItem(false, R.id.favorite);
        resetDrawerCheckedItem();
    }

    @OnClick(R.id.empty_my_content_layout)
    void emptyClick() {
        loadTotalContent();
    }

    private void emptyToggle() {

        if (ContentsList.size() > 0 || modeStatus.equals("TOTAL") || isSearchMode || selectedGenreId != null) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyBackground.setVisibility(View.GONE);
        } else {
            emptyBackground.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

    }

    public void loadMyContents() {
        progressDoalog.show();
        if (myContentsRefresh) {
            retroClient.postMyContents(userData.id, new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, t.toString());
                    Toast.makeText(getApplicationContext(), "서버 접속 실패", Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    clearRecyclerView(); // Initialize
                    resetDrawerCheckedItem(); // Init Filter
                    modeStatus = "MY";
                    getSupportActionBar().setTitle("내 콘텐츠");
                    getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.colorPrimary)));

                    // Clear
                    myContentsRefresh = false;
                    editContents.clear();
                    ContentsList.clear();
                    uiShowContentsList.clear();
                    contestsTitleSuggestionsArray.clear();

                    List<Content> data = (List<Content>) receivedData;
                    ContentsList.addAll(data);
                    mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
                    recyclerView.setAdapter(mAdapter);

                    //Search Suggestion
                    for (Content i : ContentsList) {
                        contestsTitleSuggestionsArray.add(i.name);
                        contestsTitleSuggestionsArray.add(i.name_org);
                    }


                    if (contentsMainMenu != null) {
                        contentsMainMenu.findItem(R.id.action_total_contents).setVisible(true);
                        contentsMainMenu.findItem(R.id.action_my_contents).setVisible(false);
                        contentsMainMenu.findItem(R.id.action_edit_contents).setTitle("내 콘텐츠에서 삭제");
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            searchView.clearSuggestions();
                            searchView.addSuggestions(contestsTitleSuggestionsArray);
                        }
                    }).start();;
                    emptyToggle();
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
            modeStatus = "MY";
            ContentsList.clear();
            ContentsList.addAll(myContentsBackup);

            getSupportActionBar().setTitle("내 콘텐츠");
            getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.colorPrimary)));
            mAdapter = new RecyclerViewAdapter(this, ContentsList);
            recyclerView.setAdapter(mAdapter);

            contentsMainMenu.findItem(R.id.action_total_contents).setVisible(true);
            contentsMainMenu.findItem(R.id.action_my_contents).setVisible(false);
            contentsMainMenu.findItem(R.id.action_edit_contents).setTitle("내 콘텐츠에서 삭제");

            //Search Suggestion
            contestsTitleSuggestionsArray.clear();
            for(Content i : ContentsList) {
                contestsTitleSuggestionsArray.add(i.name);
                contestsTitleSuggestionsArray.add(i.name_org);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    searchView.clearSuggestions();
                    searchView.addSuggestions(contestsTitleSuggestionsArray);
                }
            }).start();
            emptyToggle();
            progressDoalog.dismiss();
        }

        setDrawerItem(true, R.id.favorite);
        resetDrawerCheckedItem();
    }


    // -- Drawer Section -- ////////////////////////////////////////
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        selectedSubTitle = item.getTitle().toString();
        if(selectedSubTitle.equals("★")) {
            selectedSubTitle = "즐겨찾기";
        }
        String genreId = null;

        switch (id) {
            case R.id.favorite:
                genreId = "FAV";
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

            if (modeStatus.equals("MY")) {
                filterJenreMyContents(selectedSubTitle, userData.id, genreId);
            } else if (modeStatus.equals("TOTAL")) {
                filterJenreTotalContentsList(selectedSubTitle, genreId);
            }

        } else {
            selectedSubTitle = "";
            selectedGenreId = genreId = null;
            item.setChecked(false);
            uiShowContentsList.clear();
            mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
            recyclerView.setAdapter(mAdapter);
            getSupportActionBar().setSubtitle(null);
        }
        emptyToggle();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setDrawerItem(boolean bShow, int id) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if (bShow) {
            navigationView.getMenu().findItem(id).setVisible(true);
        } else {
            navigationView.getMenu().findItem(id).setVisible(false);
        }
    }

    private void resetDrawerCheckedItem() {
        isSearchMode = false;
        selectedGenreId = null;
        selectedSubTitle = null;
        if(modeStatus == "MY") {
            getSupportActionBar().setTitle("내 콘텐츠");
        } else {
            getSupportActionBar().setTitle("모든 콘텐츠");
        }
        getSupportActionBar().setSubtitle(null);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        int size = navigationView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
        emptyToggle();
    }


    // -- Filter , Search Section -- ////////////////////////////////////////
    private void searchContents(String query) {
        resetDrawerCheckedItem();
        ArrayList<Content> searchContents = new ArrayList();
        for(Content item : ContentsList) {
            if(item.name.contains(query) || item.name_org.contains(query)) {
                searchContents.add(item);
            }
        }

        isSearchMode = true;
        uiShowContentsList.clear();
        uiShowContentsList.addAll(searchContents);
        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, uiShowContentsList);
        recyclerView.setAdapter(mAdapter);
        getSupportActionBar().setTitle("검색 결과 " + uiShowContentsList.size() + "개");
        emptyToggle();
    }

    private void filterJenreMyContents(final String title, String userId, String gen_id) {
        ArrayList<Content> filterContents = new ArrayList();
        if(gen_id.equals("FAV")) {
            for(Content item : ContentsList) {
                if(item.favorite == 1) {
                    filterContents.add(item);
                }
            }
        } else {
            for(Content item : ContentsList) {
                if(item.gen_id.equals(gen_id)) {
                    filterContents.add(item);
                }
            }
        }

        uiShowContentsList.clear();
        uiShowContentsList.addAll(filterContents);
        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, uiShowContentsList);
        recyclerView.setAdapter(mAdapter);
        isSearchMode = false;
        if(modeStatus == "MY") {
            getSupportActionBar().setTitle("내 콘텐츠");
        } else {
            getSupportActionBar().setTitle("모든 콘텐츠");
        }
        if(title.length() > 0) {
            getSupportActionBar().setSubtitle("( " + title + " " + uiShowContentsList.size() + "개 )");
        } else {
            getSupportActionBar().setSubtitle(null);
        }
    }

    private void filterJenreTotalContentsList(final String title, String gen_id) {
        ArrayList<Content> filterContents = new ArrayList();
        for(Content item : ContentsList) {
            if(item.gen_id.equals(gen_id)) {
                filterContents.add(item);
            }
        }
        uiShowContentsList.clear();
        uiShowContentsList.addAll(filterContents);
        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, uiShowContentsList);
        recyclerView.setAdapter(mAdapter);
        isSearchMode = false;
        if(modeStatus == "MY") {
            getSupportActionBar().setTitle("내 콘텐츠");
        } else {
            getSupportActionBar().setTitle("모든 콘텐츠");
        }
        if(title.length() > 0) {
            getSupportActionBar().setSubtitle("( " + title + " " + uiShowContentsList.size() + "개 )");
        } else {
            getSupportActionBar().setSubtitle(null);
        }
    }


    // -- Event Override Section -- ////////////////////////////////////////
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.menu_contents_edit, menu);
            contentsEditMenu = menu;

            if (modeStatus == "MY") {
                MenuItem item = contentsEditMenu.findItem(R.id.action_add_contents);
                item.setVisible(false);
            } else {
                MenuItem item = contentsEditMenu.findItem(R.id.action_delete_contents);
                item.setVisible(false);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete_contents:
                    if (editContents.size() > 0) {
                        AlertDialog alertDialog = new AlertDialog.Builder(ContentsActivity.this)
                                .setMessage("삭제하시겠습니까?")
                                .setPositiveButton("예",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                delToMyContents();
                                            }
                                        })
                                .setNegativeButton("아니요",
                                        new android.content.DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.i(TAG, "Click NO");
                                            }
                                        }).create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(getApplicationContext(), "삭제 항목 선택", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case R.id.action_add_contents:
                    if (editContents.size() > 0) {
                        addToMyContents();
                    } else {
                        Toast.makeText(getApplicationContext(), "추가 항목 선택", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            editContents.clear();
            mAdapter.notifyDataSetChanged();

            if (isSearchMode && modeStatus.equals("MY") && uiShowContentsList.size() == 0) {
                mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
                recyclerView.setAdapter(mAdapter);
                isSearchMode = false;
                if(modeStatus == "MY") {
                    getSupportActionBar().setTitle("내 콘텐츠");
                } else {
                    getSupportActionBar().setTitle("모든 콘텐츠");
                }
            }
        }
    };

    @Override
    public void onBackPressed() {

        if (isSearchMode) {
            isSearchMode = false;
            resetDrawerCheckedItem();
            mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
            recyclerView.setAdapter(mAdapter);
            return;
        }

        if (selectedGenreId != null) {
            resetDrawerCheckedItem();
            mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
            recyclerView.setAdapter(mAdapter);
            return;
        }

        if (searchView.isOpen()) {
            searchView.closeSearch();
            return;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (modeStatus.equals("TOTAL")) {
            loadMyContents();
        } else if (modeStatus.equals("MY")) {
            if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
                finishAffinity();
                return;
            }
            Toast toast = Toast.makeText(this, "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if (v != null) v.setGravity(Gravity.CENTER);
            toast.show();
            lastTimeBackPressed = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_EDIT_REQUEST) {
            if (resultCode == RESULT_FIRST_USER) {  // Add or Delete Action
                if (modeStatus.equals("MY")) {
                    if(editPosition > -1) {
                        if(selectedGenreId != null || isSearchMode) {
                            editContents.add(uiShowContentsList.get(editPosition));
                        } else {
                            editContents.add(ContentsList.get(editPosition));
                        }
                        delToMyContents();
                        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
                        recyclerView.setAdapter(mAdapter);
                        resetDrawerCheckedItem(); // Init Filter
                    }
                } else if(modeStatus.equals("TOTAL")) {
                    if(editPosition > -1) {

                        if(selectedGenreId != null || isSearchMode) {

                            Content selectItem = uiShowContentsList.get(editPosition);
                            // Exist Check
                            if(isExistMyContent(selectItem)) {
                                Toast.makeText(getApplicationContext(), "내 콘텐츠 보유 항목", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            editContents.add(selectItem);

                        } else {

                            Content selectItem = ContentsList.get(editPosition);
                            // Exist Check
                            if(isExistMyContent(selectItem)) {
                                Toast.makeText(getApplicationContext(), "내 콘텐츠 보유 항목", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            editContents.add(selectItem);

                        }

                        addToMyContents();
                    }
                }
                editPosition = -1;
            } else if(resultCode == RESULT_OK) {   // Refesh Action
                if (modeStatus.equals("MY")) {
                    myContentsRefresh = true;
                    loadMyContents();
                } else {
                    myContentsRefresh = true;
                }
            }
            return;
        }

        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }
            return;
        }

        // 해당 부분이 있는 경우에는 그런 현상이 발생되지 않았음.
        if (mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent()");
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_contents, menu);
        contentsMainMenu = menu;

        MenuItem item = contentsMainMenu.findItem(R.id.action_my_contents);
        item.setVisible(false);
        if (userData != null && userData.id.equals("labis@labis.com")) {
            item = contentsMainMenu.findItem(R.id.action_s3_url);
            item.setVisible(true);
        }

        if(RemoveAD) {
            MenuItem aditem = contentsMainMenu.findItem(R.id.action_remove_ad);
            aditem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_remove_ad) {
            purchaseProduct(InAppProductID);
            return true;
        } else if (id == R.id.action_total_contents) {
            loadTotalContent();
            return true;
        } else if (id == R.id.action_my_contents) {
            if (bGuestMode) {
                doLogin();
                return false;
            }

            loadMyContents();
            return true;
        } else if (id == R.id.action_custom_contents) {
            if (bGuestMode) {
                doLogin();
                return false;
            }

            Intent i = new Intent(getApplicationContext(), CustomActivity.class);
            startActivityForResult(i,PICK_EDIT_REQUEST);
            overridePendingTransition(R.anim.rightin_activity, R.anim.no_move_activity);
            return true;
        } else if (id == R.id.action_search_contents) {
            // 검색 기능 활성화
            searchView.openSearch();
            return true;
        } else if (id == R.id.action_transfer_contents) {
            if (bGuestMode) {
                doLogin();
                return false;
            }

            if(modeStatus == "MY") {
                loadTotalContent();
            } else {
                loadMyContents();
            }
            return true;
        } else if (id == R.id.action_edit_contents) {
            if (bGuestMode) {
                doLogin();
                return false;
            }

            // 추가 삭제
            if(modeStatus == "MY") {
                if (mActionMode == null) {
                    isMultiSelect = true;
                    mActionMode = startActionMode(mActionModeCallback);
                    if(modeStatus.equals("MY")) {
                        mActionMode.setTitle("삭제 항목 선택");
                    } else {
                        mActionMode.setTitle("추가 항목 선택");
                    }
                }
            } else if(modeStatus == "TOTAL") {
                if (mActionMode == null) {
                    isMultiSelect = true;
                    mActionMode = startActionMode(mActionModeCallback);
                    if(modeStatus.equals("MY")) {
                        mActionMode.setTitle("삭제 항목 선택");
                    } else {
                        mActionMode.setTitle("추가 항목 선택");
                    }
                }
            }
            return true;
        } else if (id == R.id.action_s3_url) {
            Intent i = new Intent(ContentsActivity.this, UrlActivity.class);
            startActivity(i);
        } else if (id == R.id.action_help) {
            Intent i = new Intent(ContentsActivity.this, HelpActivity.class);
            LoginData loginData = new LoginData(userData, null);
            i.putExtra("LoingData", loginData);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.drawer_btn_logout)
    void onClick_logout() {
        if (bGuestMode) {
            doLogin();
            return;
        }

        AuthManager authManager = AuthManager.getInstance();

        // kakao logout
        if (authManager.getmFirebaseUser().getUid().contains("kakao:")) {
            UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                @Override public void onFailure(ErrorResult errorResult) {
                    Log.e(TAG, "[KAKAO] requestUnlink onFailure : " + errorResult.toString());
                }
                @Override public void onSessionClosed(ErrorResult errorResult) {
                    Log.e(TAG, "[KAKAO] requestUnlink onSessionClosed : " + errorResult.toString());
                }
                @Override public void onNotSignedUp() {
                    Log.e(TAG, "[KAKAO] requestUnlink onNotSignedUp");
                }
                @Override public void onSuccess(Long userId) {
                    Log.e(TAG, "[KAKAO] requestUnlink onSuccess");
                }
            });
        }

        authManager.signOut();
        authManager.setFirebaseUser(null);
        myContentsRefresh = true;
        modeStatus = "MY";

        Intent i = new Intent(ContentsActivity.this, MainActivity.class);
        startActivity(i);
        finishAffinity();
    }

    @OnClick(R.id.drawer_btn_edit)
    void onClick_edit() {
        doRegister();
    }

    @OnClick(R.id.profile_image)
    void onClick_profile() {
        doRegister();
    }

    private void doRegister() {
        if (bGuestMode) {
            doLogin();
            return;
        }
        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        LoginData loginData = new LoginData(userData, null);
        i.putExtra("LoingData", loginData);
        startActivity(i);
    }

    private void doLogin() {
        AlertDialog alertDialog = new AlertDialog.Builder(ContentsActivity.this)
                .setMessage("회원 정보가 필요한 메뉴입니다.\n로그인 하시겠습니까?")
                .setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(ContentsActivity.this, MainActivity.class);
                                startActivity(i);
                                finishAffinity();
                            }
                        })
                .setNegativeButton("아니요",
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "Click NO");
                            }
                        }).create();
        alertDialog.show();
    }

    ///-- InApp Purchase -- //


    /** 구매하기 */
    public void purchaseProduct(final String productId) {

        ///Toast.makeText(this,"인앱결제 테스트", Toast.LENGTH_SHORT).show();

        if(!mBillingProcessor.isPurchased(productId)){
            // 구매하였으면 소비하여 없앤 후 다시 구매하게 하는 로직. 만약 1번 구매 후 계속 이어지게 할 것이면 아래 함수는 주석처리.
            //mBillingProcessor.consumePurchase(productId);
            mBillingProcessor.purchase(this, productId);
        } else {
            RemoveAD = true;
            Toast.makeText(this,"이미 결제가 완료 되었습니다", Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onBillingInitialized() {
        /*
         * - 아이템 이름만 가져오는 경우 SkuDetails.title.replaceAll("\\(.*\\)", "")
         * - SkuDetails.priceText: 아이템 가격에 현지 화폐 단위를 붙인 String을 리턴한다. 예를들면 '1.99$'이런 식이다.
         * - SkuDetails.priceLong: 가격을 long 으로 리턴한다. 1.99 이런식이다.
         * - SkuDetails.productId: 제품ID(sku)를 가지고 온다. 이를 통해서 어떤 아이템을 구매했는지 판별 가능하다.

        SkuDetails mProduct = mBillingProcessor.getPurchaseListingDetails(InAppProductID);
        if(mProduct != null) {
            String temp = mProduct.productId + " / " + mProduct.priceText + " / " + mProduct.priceValue + " / " + mProduct.priceLong;
            Log.d(TAG, temp);
            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "mProduct is null.");
        }

        // 연속 구매 테스트 TEST
        //mBillingProcessor.consumePurchase(InAppProductID);
        //RemoveAD = false;
        */
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        // 구매한 아이템 정보
        String purchaseMessage = getResources().getString(R.string.purchase_app_thankyou);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(purchaseMessage)
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RemoveAD = true;
                        invalidateOptionsMenu();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (errorCode != Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            String errorMessage = "구매 에러 발생 " + " (Code " + errorCode + ")";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }
}
