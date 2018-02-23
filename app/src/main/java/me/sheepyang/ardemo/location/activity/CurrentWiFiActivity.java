package me.sheepyang.ardemo.location.activity;

import android.os.Bundle;

import me.sheepyang.ardemo.BaseActivity;
import me.sheepyang.ardemo.R;

public class CurrentWiFiActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_wifi);
        setTitle("当前热点");
    }
}
