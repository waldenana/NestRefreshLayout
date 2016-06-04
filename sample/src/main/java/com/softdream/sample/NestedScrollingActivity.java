package com.softdream.sample;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class NestedScrollingActivity extends AppCompatActivity {

    private FragmentAdapter mTabAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager_course);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }


    private void setupViewPager(ViewPager viewPager) {
        mTabAdapter = new FragmentAdapter(getSupportFragmentManager());
        mTabAdapter.addFragment(ItemFragment.newInstance(1), "list");
        mTabAdapter.addFragment(ItemFragment.newInstance(2), "Grid");
        viewPager.setAdapter(mTabAdapter);
    }

}
