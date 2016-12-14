package com.github.anzewei.refreshtest;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anzewei.refreshtest.dummy.DummyContent;

import java.util.ArrayList;

import cn.appsdream.nestrefresh2.OnPullListener;
import cn.appsdream.nestrefresh2.RefreshLayout;

public class HorizontalActivity extends BaseControlActivity implements OnPullListener {

    private int mCount = 0;
    private LinearLayoutCompat mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horizontal_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mRefreshLayout=((RefreshLayout) findViewById(R.id.refresh_layout));
        mRefreshLayout.setOnPullListener(this);
        mLinearLayout = (LinearLayoutCompat) findViewById(R.id.ll_content);
        for (int i=0;i<20;i++){
            mLinearLayout.addView(getView(DummyContent.createDummyItem(i+1+mCount*20).content));
        }
    }


    @Override
    public void onLoading(final RefreshLayout listLoader, final int headerType) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (headerType == 4)
                    mCount++;
                else mCount=0;
                if (mCount==0){
                    mLinearLayout.removeAllViewsInLayout();
                }
                for (int i=0;i<20;i++){
//                    mDummyItems.add(DummyContent.createDummyItem(i+1+mCount*20));
                    mLinearLayout.addView(getView(DummyContent.createDummyItem(i+1+mCount*20).content));
                }

                if (mCount == 10)
                    listLoader.onLoadAll();
                else
                    listLoader.onLoadFinished();
            }
        }, 2000);
    }

    private View getView(String title){
        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setPadding(20,20,20,20);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

}
