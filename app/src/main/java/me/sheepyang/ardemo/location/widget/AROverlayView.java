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
    private static final int ARROW_TYPE_LEFT = 0x0001;
    private static final int ARROW_TYPE_TOP = 0x0002;
    private static final int ARROW_TYPE_RIGHT = 0x0003;
    private static final int ARROW_TYPE_BOTTOM = 0x0004;
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private BDLocation currentLocation;
    private List<ARPoint> mARPointList;
    private Paint mPaint;
    private Paint mRectPaint;
    private int mRadius;
    private int mMode = MODE_MULTI;
    private Rect mRect = new Rect();
    private boolean mIsTop = false;
    private boolean mIsBottom = false;
    private boolean mIsLeft = false;
    private boolean mIsRight = false;

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
        mRectPaint.setStrokeWidth(30);
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
                //文字宽
                int addrWidth = mRect.width();
                //文字高
                int addrHeight = mRect.height();
                canvas.drawText(addrStr, x - addrWidth / 2 + mRadius, y - 2 * mRadius - 20, mPaint);


                String rangeStr = "距离:" + dd.toString() + "米";
                mPaint.getTextBounds(rangeStr, 0, rangeStr.length(), mRect);
                //文字宽
                int rangeWidth = mRect.width();
                //文字高
                int rangeHeight = mRect.height();
                canvas.drawText(rangeStr, x - rangeWidth / 2 + mRadius, y - 2 * mRadius - 40 - rangeHeight, mPaint);
                float right = x + rangeWidth / 2 + mRadius + mRectPaint.getStrokeWidth() / 2;
                float top = y - 2 * mRadius - 40 - 2 * rangeHeight - mRectPaint.getStrokeWidth() / 2;
                float left = x - rangeWidth / 2 + mRadius - mRectPaint.getStrokeWidth() / 2;
                float bottom = y + mRectPaint.getStrokeWidth() / 2;
                canvas.drawRect(left, top, right, bottom, mRectPaint);

                if (MODE_SINGLE != mMode) {
                    return;
                }
                mIsTop = false;
                mIsBottom = false;
                mIsLeft = false;
                mIsRight = false;
                if (right < -mRectPaint.getStrokeWidth() / 2) {
                    mIsLeft = true;
                }
                if (left > canvas.getWidth() + mRectPaint.getStrokeWidth() / 2) {
                    mIsRight = true;
                }
                if (bottom < -mRectPaint.getStrokeWidth() / 2) {
                    mIsTop = true;
                }
                if (top > canvas.getHeight() + mRectPaint.getStrokeWidth() / 2) {
                    mIsBottom = true;
                }
                if (mIsLeft || mIsRight || mIsTop || mIsBottom) {
                    if (mIsLeft && mIsTop) {
                        Log.i("SheepYang", "图形在屏幕左上");
                        if (left >= top) {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_LEFT);
                        } else {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_TOP);
                        }
                    } else if (mIsRight && mIsTop) {
                        Log.i("SheepYang", "图形在屏幕右上");
                        if ((right - canvas.getWidth()) >= top) {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_RIGHT);
                        } else {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_TOP);
                        }
                    } else if (mIsLeft && mIsBottom) {
                        Log.i("SheepYang", "图形在屏幕左下");
                        if (left >= (bottom - canvas.getHeight())) {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_LEFT);
                        } else {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_BOTTOM);
                        }
                    } else if (mIsRight && mIsBottom) {
                        Log.i("SheepYang", "图形在屏幕右下");
                        if (right >= (bottom - canvas.getHeight())) {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_RIGHT);
                        } else {
                            drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_BOTTOM);
                        }
                    } else if (mIsTop) {
                        Log.i("SheepYang", "图形在屏幕上");
                        drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_TOP);
                    } else if (mIsBottom) {
                        Log.i("SheepYang", "图形在屏幕下");
                        drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_BOTTOM);
                    } else if (mIsLeft) {
                        Log.i("SheepYang", "图形在屏幕左");
                        drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_LEFT);
                    } else if (mIsRight) {
                        Log.i("SheepYang", "图形在屏幕右");
                        drawArrow(canvas, top, bottom, left, right, ARROW_TYPE_RIGHT);
                    }
                } else {
                    Log.i("SheepYang", "图形在屏幕中间");
                }
            }
        }
    }

    private void drawArrow(Canvas canvas, float top, float bottom, float left, float right, int type) {
        float x = left / 2 + right / 2;
        if (x < mRectPaint.getStrokeWidth() / 2) {
            x = mRectPaint.getStrokeWidth() / 2;
        }
        if (x > canvas.getWidth() - mRectPaint.getStrokeWidth() / 2) {
            x = canvas.getWidth() - mRectPaint.getStrokeWidth() / 2;
        }
        float y = top / 2 + bottom / 2;
        if (y < mRectPaint.getStrokeWidth() / 2) {
            y = mRectPaint.getStrokeWidth() / 2;
        }
        if (y > canvas.getHeight() - mRectPaint.getStrokeWidth() / 2) {
            y = canvas.getHeight() - mRectPaint.getStrokeWidth() / 2;
        }
        switch (type) {
            case ARROW_TYPE_LEFT:
                canvas.drawLine(0, y, 90, y, mRectPaint);
                break;
            case ARROW_TYPE_TOP:
                canvas.drawLine(x, 0, x, 90, mRectPaint);
                break;
            case ARROW_TYPE_RIGHT:
                canvas.drawLine(canvas.getWidth() - 90, y, canvas.getWidth(), y, mRectPaint);
                break;
            case ARROW_TYPE_BOTTOM:
                canvas.drawLine(x, canvas.getHeight() - 90, x, canvas.getHeight(), mRectPaint);
                break;
            default:
                break;
        }
    }

    public void setARMode(int mode) {
        mMode = mode;
    }
}