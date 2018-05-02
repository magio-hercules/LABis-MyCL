package com.labis.mycl.contents;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.labis.mycl.R;

/**
 * Created by Junyoung on 2016-06-23.
 */

public class TabMyContents extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_fragment_mycontents, container, false);
    }
}
