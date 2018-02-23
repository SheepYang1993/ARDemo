package me.sheepyang.ardemo.location.activity;

import android.os.Bundle;

import me.sheepyang.ardemo.BaseActivity;
import me.sheepyang.ardemo.R;

public class NearWiFiActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_wifi);
        setTitle("附近WiFi");
    }
}
