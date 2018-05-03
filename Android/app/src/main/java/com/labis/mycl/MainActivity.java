package com.labis.mycl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.labis.mycl.contents.ContentsActivity;
import com.labis.mycl.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            Intent i = new Intent(v.getContext(), ContentsActivity.class);
            startActivity(i);
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
