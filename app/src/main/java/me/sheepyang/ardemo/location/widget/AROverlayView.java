package me.sheepyang.ardemo.location.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.sheepyang.ardemo.location.model.ARPoint;
import me.sheepyang.ardemo.location.util.LocationHelper;


/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> mARPointList;


    public AROverlayView(Context context) {
        super(context);
        this.context = context;
    }

    public void setARPointList(List<ARPoint> pointList) {
        if (pointList != null) {
            mARPointList = pointList;
        } else {
            mARPointList = new ArrayList<>();
        }
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        for (int i = 0; i < mARPointList.size(); i++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(mARPointList.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                canvas.drawLine(0, canvas.getHeight() / 2f, canvas.getWidth(), canvas.getHeight() / 2f, paint);
                canvas.drawLine(canvas.getWidth() / 2f, 0, canvas.getWidth() / 2f, canvas.getHeight(), paint);
                canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(mARPointList.get(i).getName(), x - (30 * mARPointList.get(i).getName().length() / 2), y - 80, paint);
//                canvas.drawText("lat:" + mARPointList.get(i).getLocation().getLatitude(), x - (30 * ("lat:" + mARPointList.get(i).getLocation().getLatitude()).length() / 2), y - 150, paint);
//                canvas.drawText("lng:" + mARPointList.get(i).getLocation().getLongitude(), x - (30 * ("lng:" + mARPointList.get(i).getLocation().getLongitude()).length() / 2), y - 220, paint);
                int distance = (int) LocationHelper.getDistance(currentLocation, mARPointList.get(i).getLocation());
                canvas.drawText("距离:" + distance + "米", x - (30 * ("距离:" + distance + "米").length() / 2), y - 150, paint);
            }
        }
    }
}