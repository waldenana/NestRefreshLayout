package com.github.anzewei.refreshtest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anzewei.refreshtest.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<CharSequence[]> mArrayList = new ArrayList<>();

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mBaseAdapter);
        mArrayList.addAll(queryActivity());
        mBaseAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(this);
    }

    private List<CharSequence[]> queryActivity() {
        PackageManager manager = getPackageManager();
        ArrayList<CharSequence[]> activitys = new ArrayList<>();
        Intent intent = new Intent("android.intent.action.Demo");
        List<ResolveInfo> resolveInfos = manager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : resolveInfos) {
            activitys.add(new CharSequence[]{resolveInfo.loadLabel(manager), resolveInfo.activityInfo.name});
        }
        return activitys;
    }


    private BaseAdapter mBaseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public CharSequence[] getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.fragment_item, parent, false);
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            ((TextView) convertView).setText(getItem(position)[0]);
            return convertView;
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CharSequence name = mArrayList.get(position)[1];
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName(name.toString()));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
