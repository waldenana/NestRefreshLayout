package com.github.anzewei.refreshtest;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import cn.appsdream.nestrefresh2.RefreshLayout;

/**
 * RefreshTest
 *
 * @author An Zewei (anzewei88[at]gmail[dot]com)
 * @since ${VERSION}
 */

public class BaseControlActivity extends AppCompatActivity {

    protected RefreshLayout mRefreshLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nestscroll, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_footer) {
            item.setChecked(!item.isChecked());
            mRefreshLayout.setEnablePullLoad(item.isChecked());
            return true;
        }
        if (item.getItemId() == R.id.action_header) {
            item.setChecked(!item.isChecked());
            mRefreshLayout.setEnableRefresh(item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
