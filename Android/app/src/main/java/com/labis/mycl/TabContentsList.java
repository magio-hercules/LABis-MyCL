package com.labis.mycl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.labis.mycl.rest.RetroCallback;
import com.labis.mycl.rest.RetroClient;
import com.labis.mycl.rest.models.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Junyoung on 2016-06-23.
 */

public class TabContentsList extends Fragment {
    private static final String LOG = "TabContentsList";

    @BindView(R.id.contentlistView)
    TextView bodyResultTextView;

    RetroClient retroClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment_contentslist, container, false);
        ButterKnife.bind(this, view);
        retroClient = RetroClient.getInstance(this.getContext()).createBaseApi();

        return view;
    }

    @OnClick(R.id.button)
    void get1() {
        Toast.makeText(this.getContext(), "GET 1 Clicked", Toast.LENGTH_SHORT).show();
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
