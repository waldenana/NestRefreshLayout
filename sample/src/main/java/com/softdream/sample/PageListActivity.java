package com.softdream.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.anzewei.pagelist.base.AbsListLoader;
import com.github.anzewei.pagelist.base.IPageListener;
import com.github.anzewei.pagelist.normalstyle.PageListLoader;

/**
 * Created by zewei on 2015-12-09.
 */
public class PageListActivity extends AppCompatActivity implements IPageListener {
    AbsListLoader mLoader;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        ListView listView = (ListView) findViewById(R.id.lsv);
        mLoader = (AbsListLoader) findViewById(R.id.page);
        mLoader.setOnLoadingListener(this);
        mLoader.setPullLoadEnable(true);
        mLoader.setPullRefreshEnable(true);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        getData(true);
    }

    @Override
    public void onRefresh(AbsListLoader listLoader) {
        getData(true);
    }

    @Override
    public void onLoading(AbsListLoader listLoader) {
        getData(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

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
