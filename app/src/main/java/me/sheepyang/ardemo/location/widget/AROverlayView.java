package me.sheepyang.ardemo.location.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.baidu.location.BDLocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import me.sheepyang.ardemo.location.model.ARPoint;
import me.sheepyang.ardemo.location.util.LocationHelper;


/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    public static final int MODE_MULTI = 0x0001;
    public static final int MODE_SINGLE = 0x0002;
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private BDLocation currentLocation;
    private List<ARPoint> mARPointList;
    private Paint mPaint;
    private Paint mRectPaint;
    private int mRadius;
    private int mMode = MODE_MULTI;
    private Rect mRect = new Rect();

    public AROverlayView(Context context) {
        super(context);
        this.context = context;
        initPaint();
    }


    private int mDistance = -1;

    private void initPaint() {
        mRadius = 30;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        mPaint.setTextSize(60);

        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.RED);
        mRectPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        mRectPaint.setTextSize(60);
    }

    public void setARPointList(List<ARPoint> pointList) {
        if (pointList != null) {
            mARPointList = pointList;
        } else {
            mARPointList = new ArrayList<>();
        }
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(BDLocation currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, canvas.getHeight() / 2f, canvas.getWidth(), canvas.getHeight() / 2f, mPaint);
        canvas.drawLine(canvas.getWidth() / 2f, 0, canvas.getWidth() / 2f, canvas.getHeight(), mPaint);
        if (currentLocation == null) {
            return;
        }
        switch (mMode) {
            case MODE_SINGLE:
                if (mARPointList == null || mARPointList.isEmpty()) {
                    return;
                }
                ARPoint point = mARPointList.get(0);
                drawSingle(canvas, point);
                break;
            case MODE_MULTI:
                drawMulti(canvas);
                break;
            default:
                drawMulti(canvas);
                break;
        }
    }

    private void drawMulti(Canvas canvas) {
        if (mARPointList == null || mARPointList.isEmpty()) {
            return;
        }
        for (ARPoint point :
                mARPointList) {
            drawSingle(canvas, point);
        }
    }

    private void drawSingle(Canvas canvas, ARPoint point) {
        if (point == null) {
            return;
        }
        double distance = LocationHelper.getDistance(currentLocation, point.getLocation());
        BigDecimal bdDD = new BigDecimal(distance);
        BigDecimal dd = bdDD.setScale(3, BigDecimal.ROUND_HALF_UP);
        if (mDistance == -1 || dd.intValue() < mDistance) {
            float[] currentLocationInECEF = LocationHelper.switchWSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.switchWSG84toECEF(point.getLocation());
            float[] pointInENU = LocationHelper.switchECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();
                canvas.drawCircle(x + mRadius, y - mRadius, mRadius, mPaint);


                String addrStr = point.getLocation().getAddrStr();
                if (TextUtils.isEmpty(addrStr)) {
                    addrStr = "暂无";
                }
                mPaint.getTextBounds(addrStr, 0, addrStr.length(), mRect);
                int addrWidth = mRect.width();//文字宽
                int addrHeight = mRect.height();//文字高
                canvas.drawText(addrStr, x - addrWidth / 2 + mRadius, y - 2 * mRadius - 20, mPaint);


                String rangeStr = "距离:" + dd.toString() + "米";
                mPaint.getTextBounds(rangeStr, 0, rangeStr.length(), mRect);
                int rangeWidth = mRect.width();//文字宽
                int rangeHeight = mRect.height();//文字高
                canvas.drawText(rangeStr, x - rangeWidth / 2 + mRadius, y - 2 * mRadius - 40 - rangeHeight, mPaint);
                float right = x + rangeWidth / 2 + mRadius;
                float top = y - 2 * mRadius - 40 - 2 * rangeHeight;
                float left = x - rangeWidth / 2 + mRadius;
                float bottom = y;
                canvas.drawRect(left, top, right, bottom, mRectPaint);

                if (left < 0) {
                    Log.i("SheepYang", "左边框超出屏幕");
                }
                if (right > canvas.getWidth()) {
                    Log.i("SheepYang", "右边框超出屏幕");
                }
                if (top < 0) {
                    Log.i("SheepYang", "上边框超出屏幕");
                }
                if (bottom > canvas.getHeight()) {
                    Log.i("SheepYang", "下边框超出屏幕");
                }
//                Log.i("SheepYang", "width:" + canvas.getWidth() + ", height:" + canvas.getHeight());
//                Log.i("SheepYang", "toptt:" + top + ", bottom:" + bottom);
            }
        }
    }

    public void setARMode(int mode) {
        mMode = mode;
    }
}