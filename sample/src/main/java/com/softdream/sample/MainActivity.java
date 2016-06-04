package com.softdream.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_list).setOnClickListener(this);
        findViewById(R.id.btn_page).setOnClickListener(this);
        findViewById(R.id.btn_nest).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_list:
                startActivity(new Intent(this,ListActivity.class));
                break;
            case R.id.btn_page:
                startActivity(new Intent(this,PageListActivity.class));
                break;
            case R.id.btn_nest:
                startActivity(new Intent(this,NestedScrollingActivity.class));
                break;
        }
    }
}
