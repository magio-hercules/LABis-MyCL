package com.labis.mycl.contents;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.labis.mycl.contents.TabContentsList;
import com.labis.mycl.contents.TabMyContents;

/**
 * Created by Junyoung on 2016-06-23.
 */

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                TabMyContents tabFragment1 = new TabMyContents();
                return tabFragment1;
            case 1:
                TabContentsList tabFragment2 = new TabContentsList();
                return tabFragment2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
