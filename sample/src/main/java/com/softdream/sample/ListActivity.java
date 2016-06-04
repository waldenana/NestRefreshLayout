package com.softdream.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cn.appsdream.nestrefresh.base.AbsRefreshLayout;
import cn.appsdream.nestrefresh.base.OnPullListener;
import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;

/**
 * Created by zewei on 2015-12-09.
 */
public class ListActivity extends AppCompatActivity implements OnPullListener {
    AbsRefreshLayout mLoader;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        mLoader = new NestRefreshLayout(listView);
        mLoader.setOnLoadingListener(this);
        mLoader.setPullLoadEnable(true);
        mLoader.setPullRefreshEnable(true);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        setContentView(mLoader);
        getData(true);
    }

    @Override
    public void onRefresh(AbsRefreshLayout listLoader) {
        getData(true);
    }

    @Override
    public void onLoading(AbsRefreshLayout listLoader) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

        getData(false);
            }
        });
    }

    private void getData(boolean refresh) {
        if (refresh)
            mAdapter.clear();
        int count=mAdapter.getCount();
        for (int i = 0; i < 20; i++) {
            mAdapter.add("Item " +( i+count));
        }
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getCount() == 100) {
            mLoader.onLoadAll();
        } else
            mLoader.onLoadFinished();
        Log.d("List","load finish");
    }
}
