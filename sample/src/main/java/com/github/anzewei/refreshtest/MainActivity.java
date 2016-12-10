package com.github.anzewei.refreshtest;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.github.anzewei.nestrefresh.OnPullListener;
import com.github.anzewei.nestrefresh.RefreshLayout;
import com.github.anzewei.nestrefresh.widget.CommonFooter;
import com.github.anzewei.nestrefresh.widget.NestHeader;
import com.github.anzewei.refreshtest.dummy.DummyContent;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnPullListener {

    private int mCount = 0;
    private RecyclerView mView;
    private OnListFragmentInteractionListener mListener = new OnListFragmentInteractionListener() {
        @Override
        public void onListFragmentInteraction(DummyContent.DummyItem item) {
            Toast.makeText(MainActivity.this,item.content,Toast.LENGTH_SHORT).show();
        }
    };

    private ArrayList<DummyContent.DummyItem> mDummyItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ((RefreshLayout) findViewById(R.id.refresh_layout)).setOnPullListener(this);
        mView = (RecyclerView) findViewById(R.id.text);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mDummyItems.addAll(DummyContent.ITEMS);
        mView.setAdapter(new MyItemRecyclerViewAdapter(mDummyItems, mListener));
    }


    @Override
    public void onLoading(final RefreshLayout listLoader, final int headerType) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (headerType == 2)
                    mCount++;
                else mCount=0;
                if (mCount==0){
                    mDummyItems.clear();
                }
                for (int i=0;i<20;i++){
                    mDummyItems.add(DummyContent.createDummyItem(i+1+mCount*20));
                }
                mView.getAdapter().notifyDataSetChanged();
                if (headerType == 2)
                mView.smoothScrollBy(0,300);
                if (mCount == 10)
                    listLoader.onLoadAll();
                else
                    listLoader.onLoadFinished();
            }
        }, 2000);
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyContent.DummyItem item);
    }
}
