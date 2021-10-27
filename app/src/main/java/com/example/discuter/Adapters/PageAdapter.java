package com.example.discuter.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.discuter.Fragments.ChatFragment;
import com.example.discuter.Fragments.GroupFragment;
import com.example.discuter.Fragments.StatusFragment;

public class PageAdapter extends FragmentPagerAdapter {

    int tabcount;
    public PageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        tabcount=behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: return new ChatFragment();
            case 1: return new GroupFragment();
            case 2: return new StatusFragment();
            default: return new ChatFragment();
        }
    }

    @Override
    public int getCount() {
        return tabcount;
    }
}
