package me.sheepyang.ardemo.location.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;
import android.text.TextUtils;
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
    private int mRadius;
    private int mMode = MODE_MULTI;

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
                drawSingle(canvas);
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
        for (int i = 0; i < mARPointList.size(); i++) {
            double distance = LocationHelper.getDistance(currentLocation, mARPointList.get(i).getLocation());
            BigDecimal bdDD = new BigDecimal(distance);
            BigDecimal dd = bdDD.setScale(3, BigDecimal.ROUND_HALF_UP);
            if (mDistance == -1 || dd.intValue() < mDistance) {
                float[] currentLocationInECEF = LocationHelper.switchWSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.switchWSG84toECEF(mARPointList.get(i).getLocation());
                float[] pointInENU = LocationHelper.switchECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
                // if z > 0, the point will display on the opposite
                if (cameraCoordinateVector[2] < 0) {
                    float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();
                    canvas.drawCircle(x + mRadius, y - mRadius, mRadius, mPaint);
                    String addrStr = mARPointList.get(i).getLocation().getAddrStr();
                    if (TextUtils.isEmpty(addrStr)) {
                        addrStr = "暂无";
                    }
                    canvas.drawText(addrStr, x - (30 * addrStr.length() / 2), y - 80, mPaint);
//                canvas.drawText("lat:" + mARPointList.get(i).getLocation().getLatitude(), x - (30 * ("lat:" + mARPointList.get(i).getLocation().getLatitude()).length() / 2), y - 150, paint);
//                canvas.drawText("lng:" + mARPointList.get(i).getLocation().getLongitude(), x - (30 * ("lng:" + mARPointList.get(i).getLocation().getLongitude()).length() / 2), y - 220, paint);
                    canvas.drawText("距离:" + dd.toString() + "米", x - (30 * ("距离:" + dd.toString() + "米").length() / 2), y - 150, mPaint);
                }
            }
        }
    }

    private void drawSingle(Canvas canvas) {
        if (mARPointList == null || mARPointList.isEmpty()) {
            return;
        }
        ARPoint point = mARPointList.get(0);
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
                canvas.drawText(addrStr, x - (30 * addrStr.length() / 2), y - 80, mPaint);
//                canvas.drawText("lat:" + mARPointList.get(i).getLocation().getLatitude(), x - (30 * ("lat:" + mARPointList.get(i).getLocation().getLatitude()).length() / 2), y - 150, paint);
//                canvas.drawText("lng:" + mARPointList.get(i).getLocation().getLongitude(), x - (30 * ("lng:" + mARPointList.get(i).getLocation().getLongitude()).length() / 2), y - 220, paint);
                canvas.drawText("距离:" + dd.toString() + "米", x - (30 * ("距离:" + dd.toString() + "米").length() / 2), y - 150, mPaint);
                float left = 0;
                float top = 0;
                float right = 0;
                float bottom = 0;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    public void setARMode(int mode) {
        mMode = mode;
    }
}