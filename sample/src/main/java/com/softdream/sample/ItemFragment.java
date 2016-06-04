package com.softdream.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softdream.sample.dummy.DummyContent;
import com.softdream.sample.dummy.DummyContent.DummyItem;

import java.util.ArrayList;

import cn.appsdream.nestrefresh.base.AbsRefreshLayout;
import cn.appsdream.nestrefresh.base.OnPullListener;
import cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class ItemFragment extends Fragment implements OnPullListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private ArrayList<DummyItem> mDummyItems = new ArrayList<>();
    private NestRefreshLayout mNestRefreshLayout;

    private RecyclerView.Adapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycle_canrefresh, container, false);
        mNestRefreshLayout = (NestRefreshLayout) view;
        mNestRefreshLayout.setOnLoadingListener(this);
        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.can_content_view);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mDummyItems.addAll(DummyContent.ITEMS);
        recyclerView.setAdapter(mAdapter = new MyItemRecyclerViewAdapter(mDummyItems));
        return view;
    }

    @Override
    public void onRefresh(AbsRefreshLayout listLoader) {
        getData(true);
    }

    @Override
    public void onLoading(AbsRefreshLayout listLoader) {
        getData(false);
    }

    private void getData(boolean refresh) {
        if (refresh)
            mDummyItems.clear();
        int count = mDummyItems.size();
        for (int i = 0; i < 20; i++) {
            mDummyItems.add(DummyContent.createDummyItem(count + i));
        }
        mAdapter.notifyDataSetChanged();
        if (mDummyItems.size() >= 100) {
            mNestRefreshLayout.onLoadAll();
        } else
            mNestRefreshLayout.onLoadFinished();
        Log.d("List", "load finish");
    }

}
