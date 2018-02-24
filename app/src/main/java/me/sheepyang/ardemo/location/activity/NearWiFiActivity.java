package me.sheepyang.ardemo.location.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;

import org.simple.eventbus.Subscriber;

import java.util.Arrays;
import java.util.List;

import me.sheepyang.ardemo.BaseActivity;
import me.sheepyang.ardemo.R;
import me.sheepyang.ardemo.location.model.ARPoint;
import me.sheepyang.ardemo.location.widget.ARCamera;
import me.sheepyang.ardemo.location.widget.AROverlayView;

public class NearWiFiActivity extends BaseActivity implements SensorEventListener, LocationListener {

    final static String TAG = "NearWiFiActivity";
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView mArOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;

    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private LocationManager locationManager;
    public BDLocation mLocation;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    private List<ARPoint> mARPointList = Arrays.asList(
            new ARPoint("乐都汇", 24.518142, 118.166792, 0),
            new ARPoint("南二门", 24.492097, 118.18387, 0),
            new ARPoint("南门", 24.488867, 118.185218, 0),
            new ARPoint("东二门", 24.489014, 118.193703, 0),
            new ARPoint("东门", 24.491354, 118.193738, 0),
            new ARPoint("西门", 24.495615, 118.182676, 0),
            new ARPoint("梦幻世界", 24.504572, 118.204622, 0),
            new ARPoint("音乐学校", 24.496002, 118.199805, 0),
            new ARPoint("五缘湾大桥", 24.545984, 118.18186, 0),
            new ARPoint("五通小区", 24.513868, 118.198736, 0),
            new ARPoint("会展酒店", 24.472435, 118.191478, 0),
            new ARPoint("何厝", 24.492068, 118.199239, 0),
            new ARPoint("古楼", 24.472501, 118.176207, 0),
            new ARPoint("美图", 24.496057, 118.187068, 0)
    );
    private boolean mIsLocationMode = true;
    private ARPoint[] mARPoints = new ARPoint[]{
            new ARPoint("众联世纪", 24.494226, 118.19133, 0),
            new ARPoint("蔡塘学校", 24.49011, 118.164706, 0),
            new ARPoint("软件园工商银行", 24.49108, 118.187433, 0),
            new ARPoint("宝龙广场(建设中...)", 24.492115, 118.17854, 0),
    };
    private int[] mDistances = new int[]{
            100,
            500,
            1000,
            1500,
            2000,
            3500,
            5000,
            10000,
            15000,
            25000,
            50000,
            100000
    };
    private int mCurrentLocationIndex = 0;
    private int mDistanceIndex = 0;
    private boolean mIsUseDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_wifi);
        setTitle("附近WiFi");
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);
        mArOverlayView = new AROverlayView(this);
        mArOverlayView.setARPointList(mARPointList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_near_wifi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switch_loaction_mode:
                mIsLocationMode = !mIsLocationMode;
                switchLocation();
                return true;
            case R.id.menu_switch_loaction:
                switchLocation();
                return true;
            case R.id.menu_switch_wifi_list:
                Toast.makeText(this, "暂未开发...", Toast.LENGTH_SHORT).show();
                switchWiFiList();
                return true;
            case R.id.menu_cancel_range:
                mArOverlayView.setDistance(-1);
                mDistanceIndex = 0;
                mIsUseDistance = false;
                updateLatestLocation();
                return true;
            case R.id.menu_switch_range:
                switchRange();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void switchWiFiList() {

    }

    private void switchRange() {
        if (mDistanceIndex + 1 < mDistances.length) {
            mDistanceIndex++;
        } else {
            mDistanceIndex = 0;
        }
        mArOverlayView.setDistance(mDistances[mDistanceIndex]);
        mIsUseDistance = true;
        updateLatestLocation();
    }

    private void switchLocation() {
        if (mIsLocationMode) {
            Toast.makeText(this, "请先切换定位置模式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCurrentLocationIndex + 1 < mARPoints.length) {
            mCurrentLocationIndex++;
        } else {
            mCurrentLocationIndex = 0;
        }
        mLocation = mARPoints[mCurrentLocationIndex].getLocation();
        updateLatestLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
//            initLocationService();
        }
    }

    public void initAROverlayView() {
        if (mArOverlayView.getParent() != null) {
            ((ViewGroup) mArOverlayView.getParent()).removeView(mArOverlayView);
        }
        cameraContainerLayout.addView(mArOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(null);
            }
        });
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.mArOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }

    private void initLocationService() {
//
//        if (Build.VERSION.SDK_INT >= 23 &&
//                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        try {
//            this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//
//            // Get GPS and network status
//            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (!isNetworkEnabled && !isGPSEnabled) {
//                // cannot get location
//                this.locationServiceAvailable = false;
//            }
//
//            this.locationServiceAvailable = true;
//
//            if (isNetworkEnabled) {
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                        MIN_TIME_BW_UPDATES,
//                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                if (locationManager != null) {
//                    mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                    updateLatestLocation();
//                }
//            }
//
//            if (isGPSEnabled) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                        MIN_TIME_BW_UPDATES,
//                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//
//                if (locationManager != null) {
//                    mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    updateLatestLocation();
//                }
//            }
//        } catch (Exception ex) {
//            Log.e(TAG, ex.getMessage());
//
//        }
    }

    @Subscriber

    public void onReceiveLocation(BDLocation bdLocation) {
        if (mIsLocationMode) {
            mLocation = bdLocation;
            updateLatestLocation();
        }
    }

    private void updateLatestLocation() {
        if (mArOverlayView != null && mLocation != null) {
            mArOverlayView.updateCurrentLocation(mLocation);
            StringBuilder sbHint = new StringBuilder();
            sbHint.append("当前位置：");
            sbHint.append(mLocation.getAddrStr());
            if (!TextUtils.isEmpty(mLocation.getLocationDescribe())) {
                sbHint.append(mLocation.getLocationDescribe());
            }
            sbHint.append("\n");
            sbHint.append(
                    String.format(
                            "lat：%s\nlng：%s\naltitude：%s\n"
                            , mLocation.getLatitude()
                            , mLocation.getLongitude()
                            , mLocation.getAltitude()));
            if (mIsUseDistance) {
                sbHint.append("距离范围：");
                sbHint.append(mDistances[mDistanceIndex]);
                sbHint.append("米以内\n");
            } else {
                sbHint.append("距离范围：无限制\n");
            }
            sbHint.append("定位模式：");
            if (mIsLocationMode) {
                sbHint.append("自动定位");
            } else {
                sbHint.append("固定位置");
            }
            tvCurrentLocation.setText(sbHint.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLatestLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
