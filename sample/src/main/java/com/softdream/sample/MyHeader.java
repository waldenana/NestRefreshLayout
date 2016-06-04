package com.softdream.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;


/**
 * Created by zewei on 2016-04-28.
 */
public class MyHeader extends TextView implements NestRefreshLayout.LoaderDecor {
    public MyHeader(Context context) {
        super(context);
    }

    public MyHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void scrollRate(int y) {

    }

    @Override
    public void setState(int state) {

    }
}
