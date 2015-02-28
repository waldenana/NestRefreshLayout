package com.softdream.pagelistloader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * ClassName : PullFooter <br>
 * 功能描述： <br>
 * History <br>
 * Create User: An Zewei <br>
 * Create Date: 2013-12-31 下午4:52:17 <br>
 * Update User: <br>
 * Update Date:
 */
public class PullFooter extends RelativeLayout implements PageListLoader.LoaderDecor {
    public final static int STATE_GONE = 3;

    private View mProgressBar;
    private TextView mHintView;
    public PullFooter(Context context) {
        super(context);
        initView(context);
    }

    public PullFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    public void setState(int state) {
        mHintView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        if (state == STATE_READY) {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_load_ready);
        } else if (state == STATE_REFRESHING) {
            mHintView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_loading);
        } else if (state == STATE_NORMAL) {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_load_more);
        } else {
            mHintView.setVisibility(View.GONE);
        }
    }

    /**
     * normal status
     */
    public void normal() {
        mHintView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * loading status
     */
    public void loading() {
        mHintView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void initView(Context context) {
        inflate(getContext(), R.layout.pull_footer, this);
        mProgressBar = findViewById(R.id.footer_progressbar);
        mHintView = (TextView) findViewById(R.id.footer_hint_textview);
        final float scale = context.getResources().getDisplayMetrics().density;
        int padding = (int) (10 / scale + 0.5f);
        setPadding(0, padding, 0, padding);
    }

	@Override
	public void scrollRate(int y) {
	}
}