package me.sheepyang.ardemo.skyball.activity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import me.sheepyang.ardemo.BaseActivity;
import me.sheepyang.ardemo.R;
import me.sheepyang.ardemo.skyball.util.SkySphere;

public class SkyBallActivity extends BaseActivity implements SurfaceHolder.Callback, GLSurfaceView.Renderer, SensorEventListener {
    private GLSurfaceView mGLView;
    private SensorManager mSensorManager;
    private Sensor mRotation;
    private SkySphere mSkySphere;
    private String[] mSkyDrawableArrys = new String[]{"vr/360sp.jpg", "vr/360sp2.png", "vr/360sp6.jpg", "vr/360sp7.jpg"};
    private float[] matrix = new float[16];
    private SurfaceView surfaceView;//预览摄像头
    private SurfaceHolder surfaceHolder;
    private Button button;//拍照按钮
    private Camera camera;
    private Camera.AutoFocusCallback myAutoFocusCallback1 = null;//只对焦不拍照
    public static final int only_auto_focus = 110;
    int issuccessfocus = 0;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case only_auto_focus:
                    if (camera != null) {
                        camera.autoFocus(myAutoFocusCallback1);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private int mSkyDrawableIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sky_ball);
        setTitle("天空球");
        initView();
        initData();
        initListener();
        initGLView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sky_ball, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pre_scene:
                mSensorManager.unregisterListener(this);
                mGLView.onPause();
                if (mSkyDrawableIndex - 1 >= 0) {
                    mSkyDrawableIndex--;
                } else {
                    mSkyDrawableIndex = mSkyDrawableArrys.length - 1;
                }
                mSkySphere = new SkySphere(this.getApplicationContext(), mSkyDrawableArrys[mSkyDrawableIndex]);
                mSkySphere.create();
                mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);
                mGLView.onResume();
                return true;
            case R.id.menu_next_scene:
                mSensorManager.unregisterListener(this);
                mGLView.onPause();
                if (mSkyDrawableIndex + 1 < mSkyDrawableArrys.length) {
                    mSkyDrawableIndex++;
                } else {
                    mSkyDrawableIndex = 0;
                }
                mSkySphere = new SkySphere(this.getApplicationContext(), mSkyDrawableArrys[mSkyDrawableIndex]);
                mSkySphere.create();
                mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);
                mGLView.onResume();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initGLView() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //todo 判断是否存在rotation vector sensor
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mGLView = (GLSurfaceView) findViewById(R.id.mGLView);
        //设置透明模式
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLView.setZOrderOnTop(true);

        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(this);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mSkySphere = new SkySphere(this.getApplicationContext(), mSkyDrawableArrys[mSkyDrawableIndex]);
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.main_surface_view);
        button = (Button) findViewById(R.id.main_button);
    }

    private void initData() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        myAutoFocusCallback1 = new Camera.AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if (success)//success表示对焦成功
                {
                    issuccessfocus++;
                    if (issuccessfocus <= 1) {
                        mHandler.sendEmptyMessage(only_auto_focus);
                    }
                    Log.i("qtt", "myAutoFocusCallback1: success..." + issuccessfocus);
                } else {
                    //if (issuccessfocus == 0) {
                    mHandler.sendEmptyMessage(only_auto_focus);
                    //}
                    Log.i("qtt", "myAutoFocusCallback1: 失败...");
                }
            }
        };
    }

    private void initListener() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.autoFocus(myAutoFocusCallback1);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SkyBallActivity.this, "button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_GAME);
        mGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGLView.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSkySphere.create();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);
        gl.glClearColor(0, 0, 0, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mSkySphere != null) {
            mSkySphere.setSize(width, height);
        }
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(1, 1, 1, 1);
        if (mSkySphere != null) {
            mSkySphere.draw();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(matrix, event.values);
        if (mSkySphere != null) {
            mSkySphere.setMatrix(matrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void initCamera() {
        Camera.Parameters parameters = camera.getParameters();//获取camera的parameter实例
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
        Camera.Size optionSize = getOptimalPreviewSize(sizeList, surfaceView.getWidth(), surfaceView.getHeight());//获取一个最为适配的屏幕尺寸
        parameters.setPreviewSize(optionSize.width, optionSize.height);//把只存设置给parameters
        camera.setParameters(parameters);//把parameters设置给camera上
        camera.startPreview();//开始预览
        camera.setDisplayOrientation(90);//将预览旋转90度
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            if (null != camera) {
                camera.release();
                camera = null;
            }
            e.printStackTrace();
            Toast.makeText(SkyBallActivity.this, "启动摄像头失败,请开启摄像头权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
