/**
 *ClassName : PullFooter</br>
 *
 *
 */
package cn.appsdream.nestrefresh.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.anzewei.pagelist.R;

import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;


/**
 * ClassName : PullFooter <br>
 * 功能描述： <br>
 * History <br>
 * Create User: An Zewei <br>
 * Create Date: 2013-12-31 下午4:52:17 <br>
 * Update User: <br>
 * Update Date:
 */
public class NestFooter extends RelativeLayout implements NestRefreshLayout.LoaderDecor {
    private static final String TAG = NestFooter.class.getSimpleName();
    private int mStatus = STATE_NORMAL;
    private View mProgressBar;
    private TextView mHintView;
    public NestFooter(Context context) {
        super(context);
        initView(context);
    }

    public NestFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    public void setState(int state) {
        if (state == mStatus)
            return;
        mStatus = state;
        Log.d(TAG, "setState: "+state);
        if (state == STATE_READY) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_load_ready);
        } else if (state == STATE_REFRESHING) {
            mHintView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_loading);
        } else if (state == STATE_NORMAL) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_load_more);
        }  else if (state == STATE_ALL) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(R.string.loader_no_more);
        } else {
            mHintView.setVisibility(View.GONE);
        }
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