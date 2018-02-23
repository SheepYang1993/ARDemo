package me.sheepyang.ardemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.sheepyang.ardemo.location.activity.CurrentWiFiActivity;
import me.sheepyang.ardemo.location.activity.NearWiFiActivity;
import me.sheepyang.ardemo.skyball.activity.SkyBallActivity;

/**
 * TODO 描述
 *
 * @author SheepYang
 * @since 2018/2/23 9:44
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
