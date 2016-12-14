package com.github.anzewei.refreshtest;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.anzewei.refreshtest.dummy.DummyContent;

import java.util.ArrayList;

import cn.appsdream.nestrefresh2.OnPullListener;
import cn.appsdream.nestrefresh2.RefreshLayout;

public class HorizontalRecycleViewActivity extends BaseControlActivity implements OnPullListener {

    private int mCount = 0;
    private RecyclerView mView;
    private MyItemRecyclerViewAdapter.OnListFragmentInteractionListener mListener = new MyItemRecyclerViewAdapter.OnListFragmentInteractionListener() {
        @Override
        public void onListFragmentInteraction(DummyContent.DummyItem item) {
            Toast.makeText(HorizontalRecycleViewActivity.this, item.content, Toast.LENGTH_SHORT).show();
        }
    };

    private ArrayList<DummyContent.DummyItem> mDummyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horizontal_recycleview_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mRefreshLayout = ((RefreshLayout) findViewById(R.id.refresh_layout));
        mRefreshLayout.setOnPullListener(this);
        mView = (RecyclerView) findViewById(R.id.text);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mView.setLayoutManager(manager);
        mDummyItems.addAll(DummyContent.ITEMS);
        mView.setAdapter(new MyItemRecyclerViewAdapter(mDummyItems, mListener));

    }


    @Override
    public void onLoading(final RefreshLayout listLoader, final int headerType) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (headerType == 4)
                    mCount++;
                else mCount = 0;
                if (mCount == 0) {
                    mDummyItems.clear();
                }
                for (int i = 0; i < 20; i++) {
                    mDummyItems.add(DummyContent.createDummyItem(i + 1 + mCount * 20));
                }
                mView.getAdapter().notifyDataSetChanged();
                if (headerType == 2)
                    mView.smoothScrollBy(0, 300);
                if (mCount == 10)
                    listLoader.onLoadAll();
                else
                    listLoader.onLoadFinished();
            }
        }, 2000);
    }

}
