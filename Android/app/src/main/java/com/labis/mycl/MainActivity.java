package com.labis.mycl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.contents.RecyclerViewAdapter;
import com.labis.mycl.login.LoginActivity;
import com.labis.mycl.model.Content;
import com.labis.mycl.model.Genre;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG = "MainActivity";

    RetroClient retroClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        retroClient = RetroClient.getInstance(this).createBaseApi();

        findViewById(R.id.loginbutton).setOnClickListener(loginButtonClickListener);
        findViewById(R.id.linkbutton).setOnClickListener(linkButtonClickListener);
    }

    Button.OnClickListener loginButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), LoginActivity.class);
            startActivity(i);
        }
    };

    Button.OnClickListener linkButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Set up progress before call
            final ProgressDialog progressDoalog;
            progressDoalog = new ProgressDialog(v.getContext());
            progressDoalog.setMessage("잠시만 기다리세요....");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();

            retroClient.getTotalGenre(new RetroCallback() {
                @Override
                public void onError(Throwable t) {
                    Log.e(LOG, t.toString());
                    Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                    progressDoalog.dismiss();
                }

                @Override
                public void onSuccess(int code, Object receivedData) {
                    Log.e(LOG, "SUCCESS");
                    ArrayList<Genre> genreData = (ArrayList<Genre>) receivedData;
                    if (!genreData.isEmpty()) {
                        Intent i = new Intent(getApplicationContext(), ContentsActivity.class);
                        i.putParcelableArrayListExtra("genre", genreData);
                        startActivity(i);
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
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
