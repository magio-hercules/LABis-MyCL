package com.labis.mycl.help;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.labis.mycl.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HelpActivity extends AppCompatActivity {


    @BindView(R.id.help_combo)
    Spinner comboHelp;

    private Toolbar toolbar;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.help_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("도움말");

        SpinnerAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.helplist, R.layout.spinner_item);
        comboHelp.setAdapter(sAdapter);

        // for ad
        mAdView = findViewById(R.id.help_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}
