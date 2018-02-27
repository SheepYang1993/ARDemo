package me.sheepyang.ardemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


import org.simple.eventbus.EventBus;

import me.sheepyang.ardemo.location.activity.CurrentWiFiActivity;
import me.sheepyang.ardemo.location.activity.MovePathActivity;
import me.sheepyang.ardemo.location.activity.NearWiFiActivity;
import me.sheepyang.ardemo.skyball.activity.SkyBallActivity;

/**
 * @author SheepYang
 * @since 2018/2/23 9:44
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // register the receiver object
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        // Don’t forget to unregister !!
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_skyball:
                startActivity(new Intent(this, SkyBallActivity.class));
                finish();
                return true;
            case R.id.menu_location_near:
                startActivity(new Intent(this, NearWiFiActivity.class));
                finish();
                return true;
            case R.id.menu_location_current:
                startActivity(new Intent(this, CurrentWiFiActivity.class));
                finish();
                return true;
            case R.id.menu_location_move_path:
                startActivity(new Intent(this, MovePathActivity.class));
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
