package com.labis.mycl.contents;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.labis.mycl.R;
import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CustomActivity extends AppCompatActivity {

    @BindView(R.id.editTitle)
    EditText editTitle;

    @BindView(R.id.comboGenre)
    Spinner comboGenre;

    @BindView(R.id.saveBtn)
    Button saveBtn;

    SpinnerAdapter sAdapter;

    private RetroClient retroClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);
        ButterKnife.bind(this);

        retroClient = RetroClient.getInstance(this).createBaseApi();
        
        // -- ToolBar -- //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("커스텀 콘텐츠 추가");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

        sAdapter = ArrayAdapter.createFromResource(this, R.array.genre, R.layout.spinner_item);

        comboGenre.setAdapter(sAdapter);
        comboGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), ""+ sAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView parent) {
            }
        });
    }


    @OnClick(R.id.saveBtn)
    void onClick_saveBtn(){
        final ProgressDialog progressDoalog = new ProgressDialog(this);
        progressDoalog.setMessage("잠시만 기다리세요....");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();
        retroClient.postInserCustomContents("A01","1","EVOL TEST","에볼","500","0",
                "1","asdasdas","최욱","0","http",new RetroCallback() {
                    @Override
                    public void onError(Throwable t) {
                        Toast.makeText(getApplicationContext(), "서버 접속에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
                    }
                    @Override
                    public void onSuccess(int code, Object receivedData) {
                        Toast.makeText(getApplicationContext(), "커스텀 추가 SUCCESS : " + code, Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
                    }
                    @Override
                    public void onFailure(int code) {
                        Toast.makeText(getApplicationContext(), "커스텀 추가 FAIL : " + code, Toast.LENGTH_SHORT).show();
                        progressDoalog.dismiss();
                    }
                });
    }








}
