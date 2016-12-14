package com.github.anzewei.refreshtest;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anzewei.refreshtest.dummy.DummyContent;

import java.util.ArrayList;

import cn.appsdream.nestrefresh2.OnPullListener;
import cn.appsdream.nestrefresh2.RefreshLayout;

public class ListViewActivity extends BaseControlActivity implements OnPullListener {

    private int mCount = 0;
    private ListView mView;
    private ArrayList<DummyContent.DummyItem> mDummyItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mRefreshLayout = ((RefreshLayout) findViewById(R.id.refresh_layout));
        mRefreshLayout.setOnPullListener(this);
        mView = (ListView) findViewById(R.id.text);
        mDummyItems.addAll(DummyContent.ITEMS);
        mView.setAdapter(mBaseAdapter);
    }


    @Override
    public void onLoading(final RefreshLayout listLoader, final int headerType) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (headerType == 2)
                    mCount++;
                else mCount = 0;
                if (mCount == 0) {
                    mDummyItems.clear();
                }
                for (int i = 0; i < 20; i++) {
                    mDummyItems.add(DummyContent.createDummyItem(i + 1 + mCount * 20));
                }
                mBaseAdapter.notifyDataSetChanged();
                if (headerType == 2)
                    mView.smoothScrollBy(0, 300);
                if (mCount == 10)
                    listLoader.onLoadAll();
                else
                    listLoader.onLoadFinished();
            }
        }, 2000);
    }

    private BaseAdapter mBaseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mDummyItems.size();
        }

        @Override
        public DummyContent.DummyItem getItem(int position) {
            return mDummyItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(ListViewActivity.this).inflate(R.layout.fragment_item,parent,false);
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                params.width= ViewGroup.LayoutParams.MATCH_PARENT;
            }
            ((TextView)convertView).setText(getItem(position).content);
            return convertView;
        }
    };

}
