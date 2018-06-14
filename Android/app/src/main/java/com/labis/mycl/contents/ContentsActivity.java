package com.labis.mycl.contents;

import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.labis.mycl.util.AlertDialogHelper;
import com.labis.mycl.util.AuthManager;
import com.labis.mycl.util.CircleTransform;
import com.labis.mycl.util.RecyclerItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.mauker.materialsearchview.db.HistoryContract;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import br.com.mauker.materialsearchview.MaterialSearchView;

public class ContentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ContentsActivity";

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
    public static User userData;
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
    AlertDialogHelper alertDialogHelper;
    public static final int PICK_EDIT_REQUEST = 1;
    public int editPosition = -1;
    private MaterialSearchView searchView;
    private ArrayList<String> contestsTitleSuggestionsArray = new ArrayList<String>();
    private boolean isSearchMode = false;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);
        ButterKnife.bind(this);

        // -- Delete Dialog --//
        alertDialogHelper = new AlertDialogHelper(this);

        // -- ToolBar -- //
        toolbar = (Toolbar) findViewById(R.id.content_toolbar);
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
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mNickName = (TextView) findViewById(R.id.nick_name);
        mLoginEmail = (TextView) findViewById(R.id.login_email);
        if(userData.image != null && userData.image.length() > 10) {
            Picasso.get().load(userData.image).transform(new CircleTransform()).into(mProfileImage);
        }
        mNickName.setText(userData.nickname);
        mLoginEmail.setText(userData.id);

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
        if (mActionMode != null) {
            if(selectedGenreId == null && isSearchMode == false) {  // Normal Mode
                if (editContents.contains(ContentsList.get(position))) {
                    editContents.remove(ContentsList.get(position));
                } else {
                    editContents.add(ContentsList.get(position));
                }
            } else {                                                // Search or Filter Mode
                if (editContents.contains(uiShowContentsList.get(position))) {
                    editContents.remove(uiShowContentsList.get(position));
                } else {
                    editContents.add(uiShowContentsList.get(position));
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
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
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
            addList.add(item.id);
            Log.d(TAG,"EVOL Item ID : " + item.id);
        }

        retroClient.postInsertMyContents(userData.id, addList, new RetroCallback() {
            @Override
            public void onError(Throwable t) {
                Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                progressDoalog.dismiss();
            }

            @Override
            public void onSuccess(int code, Object receivedData) {
                myContentsRefresh = true;
                editContents.clear();

                if (mActionMode != null) {
                    mActionMode.finish();
                }
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
                    for(Content i : ContentsList) {
                        contestsTitleSuggestionsArray.add(i.name);
                        contestsTitleSuggestionsArray.add(i.name_org);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                }

                if(contentsMainMenu != null) {
                    contentsMainMenu.findItem(R.id.action_total_contents).setVisible(false);
                    contentsMainMenu.findItem(R.id.action_my_contents).setVisible(true);
                    contentsMainMenu.findItem(R.id.action_edit_contents).setTitle("콘텐츠 추가");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        searchView.clearSuggestions();
                        searchView.addSuggestions(contestsTitleSuggestionsArray);
                    }
                }).start();

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

    public void loadMyContents() {
        progressDoalog.show();
        if (myContentsRefresh) {
            Log.d(TAG, "EVOL REQUEST #2");
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
                    resetDrawerCheckedItem(); // Init Filter
                    modeStatus = "MY";
                    getSupportActionBar().setTitle("내 콘텐츠");
                    getSupportActionBar().setBackgroundDrawable((getResources().getDrawable(R.color.colorPrimary)));

                    myContentsRefresh = false;
                    editContents.clear();
                    ContentsList.clear();
                    uiShowContentsList.clear();
                    contestsTitleSuggestionsArray.clear();
                    List<Content> data = (List<Content>) receivedData;
                    if (!data.isEmpty()) {
                        ContentsList.addAll(data);
                        mAdapter = new RecyclerViewAdapter(ContentsActivity.this, ContentsList);
                        recyclerView.setAdapter(mAdapter);

                        //Search Suggestion
                        for(Content i : ContentsList) {
                            contestsTitleSuggestionsArray.add(i.name);
                            contestsTitleSuggestionsArray.add(i.name_org);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "DATA EMPTY", Toast.LENGTH_SHORT).show();
                    }

                    if (contentsMainMenu != null) {
                        contentsMainMenu.findItem(R.id.action_total_contents).setVisible(true);
                        contentsMainMenu.findItem(R.id.action_my_contents).setVisible(false);
                        contentsMainMenu.findItem(R.id.action_edit_contents).setTitle("콘텐츠 삭제");
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            searchView.clearSuggestions();
                            searchView.addSuggestions(contestsTitleSuggestionsArray);
                        }
                    }).start();;

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
            contentsMainMenu.findItem(R.id.action_edit_contents).setTitle("콘텐츠 삭제");

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

            progressDoalog.dismiss();
        }

        setDrawerItem(true, R.id.favorite);
        resetDrawerCheckedItem();
    }


    // -- Drawer Section -- ////////////////////////////////////////
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.e(TAG, "onNavigationItemSelected");

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

            Log.e(TAG, "title : " + selectedSubTitle + ", genreId : " + genreId);
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
        Log.e(TAG, "resetDrawerCheckedItem()");

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
    }


    // -- Filter , Search Section -- ////////////////////////////////////////
    private void searchContents(String query) {
        Log.e(TAG, "searchContents");
        Log.e(TAG, "query : " + query);

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
    }

    private void filterJenreMyContents(final String title, String userId, String gen_id) {
        Log.e(TAG, "filterJenreMyContents");
        Log.e(TAG, "doFilter() title : " + title);
        Log.e(TAG, "doFilter() user_id : " + userId);
        Log.e(TAG, "doFilter() gen_id : " + gen_id);

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
        Log.e(TAG, "filterJenreContentsList");
        Log.e(TAG, "doFilter() title : " + title);
        Log.e(TAG, "doFilter() gen_id : " + gen_id);

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
                        alertDialogHelper.setAlertDialogListener(new AlertDialogHelper.AlertDialogListener() {
                            @Override
                            public void onPositiveClick(int from) {
                                delToMyContents();
                            }
                            @Override
                            public void onNegativeClick(int from) { }
                            @Override
                            public void onNeutralClick(int from) { }
                        });
                        alertDialogHelper.showAlertDialog("", "삭제하시겠습니까?", "예", "아니요", 1, false);
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
            Log.d(TAG, "EVOL REQUEST #1-3");
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
                            editContents.add(uiShowContentsList.get(editPosition));
                        } else {
                            editContents.add(ContentsList.get(editPosition));
                        }
                        addToMyContents();
                    }
                }
                editPosition = -1;
            } else if(resultCode == RESULT_OK) {   // Refesh Action
                if (modeStatus.equals("MY")) {
                    myContentsRefresh = true;
                    loadMyContents();
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
            loadMyContents();
            return true;
        } else if (id == R.id.action_custom_contents) {
            Intent i = new Intent(getApplicationContext(), CustomActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.rightin_activity, R.anim.no_move_activity);
            return true;
        } else if (id == R.id.action_search_contents) {
            // 검색 기능 활성화
            searchView.openSearch();
            return true;
        } else if (id == R.id.action_edit_contents) {
            // 추가 삭제
            if(modeStatus == "MY") {
                if (mActionMode == null) {
                    isMultiSelect = true;
                    mActionMode = startActionMode(mActionModeCallback);
                }
            } else if(modeStatus == "TOTAL") {
                if (mActionMode == null) {
                    isMultiSelect = true;
                    mActionMode = startActionMode(mActionModeCallback);
                }
            }
            return true;
        } else if (id == R.id.action_s3_url) {
            Intent i = new Intent(ContentsActivity.this, UrlActivity.class);
            startActivity(i);
        } else if (id == R.id.action_help) {
            Intent i = new Intent(ContentsActivity.this, HelpActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.drawer_btn_logout)
    void onClick_logout() {
        AuthManager authManager = AuthManager.getInstance();
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
        Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
        LoginData loginData = new LoginData(userData, null);
        i.putExtra("LoingData", loginData);
        startActivity(i);
    }

}
