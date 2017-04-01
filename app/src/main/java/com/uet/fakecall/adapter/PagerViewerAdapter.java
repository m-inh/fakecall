package com.uet.fakecall.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.uet.fakecall.fragment.FakeCallFragment;
import com.uet.fakecall.fragment.FakeSMSFragment;
import com.uet.fakecall.fragment.ScheduleFagment;


public class PagerViewerAdapter extends FragmentStatePagerAdapter {
    private static final int FAKE_CALL = 0;
    private static final int FAKE_SMS = 1;
    private static final int SCHEDULE = 2;

    private FakeCallFragment fragmentFakeCall;
    private FakeSMSFragment fakeSMSFragment;
    private ScheduleFagment scheduleFagment;

    public PagerViewerAdapter(FragmentManager fm, FakeCallFragment fakeCallFragment, FakeSMSFragment fakeSMSFragment, ScheduleFagment scheduleFagment) {
        super(fm);
        this.fakeSMSFragment = fakeSMSFragment;
        this.fragmentFakeCall = fakeCallFragment;
        this.scheduleFagment = scheduleFagment;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment frag = null;
        switch (position){
            case FAKE_CALL:
                frag = fragmentFakeCall;
                break;
            case FAKE_SMS:
                frag = fakeSMSFragment;
                break;
            case SCHEDULE:
                frag = scheduleFagment;
                break;
        }
        return frag;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position){
            case FAKE_CALL:
                title = "FAKE CALL";
                break;
            case FAKE_SMS:
                title = "FAKE SMS";
                break;
            case SCHEDULE:
                title = "SCHEDULE";
                break;
        }
        return title;
    }
}
