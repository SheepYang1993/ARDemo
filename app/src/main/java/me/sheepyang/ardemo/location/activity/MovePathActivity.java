package me.sheepyang.ardemo.location.activity;

import android.os.Bundle;
import android.view.Menu;

import me.sheepyang.ardemo.BaseActivity;
import me.sheepyang.ardemo.R;

public class MovePathActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_path);
        setTitle("距离绘制");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_move_path, menu);
        return true;
    }
}
