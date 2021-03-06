package me.sheepyang.ardemo.location.util;

import android.location.Location;

import com.baidu.location.BDLocation;

/**
 * Created by ntdat on 1/13/17.
 */

public class LocationHelper {
    private final static double WGS84_A = 6378137.0;//赤道半径
    private final static double WGS84_E2 = 0.00669437999014;          // square of WGS 84 eccentricity

    public static float[] switchWSG84toECEF(Location location) {
        return switchWSG84toECEF(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    public static float[] switchECEFtoENU(Location currentLocation, float[] ecefCurrentLocation, float[] ecefPOI) {
        return switchECEFtoENU(currentLocation.getLatitude(), currentLocation.getLongitude(), ecefCurrentLocation, ecefPOI);
    }

    public static float[] switchWSG84toECEF(BDLocation location) {
        return switchWSG84toECEF(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    public static float[] switchECEFtoENU(BDLocation currentLocation, float[] ecefCurrentLocation, float[] ecefPOI) {
        return switchECEFtoENU(currentLocation.getLatitude(), currentLocation.getLongitude(), ecefCurrentLocation, ecefPOI);
    }

    public static float[] switchWSG84toECEF(double lat, double lng, double alt) {
        double radLat = Math.toRadians(lat);
        double radLon = Math.toRadians(lng);

        float clat = (float) Math.cos(radLat);
        float slat = (float) Math.sin(radLat);
        float clon = (float) Math.cos(radLon);
        float slon = (float) Math.sin(radLon);

        float N = (float) (WGS84_A / Math.sqrt(1.0 - WGS84_E2 * slat * slat));

        float x = (float) ((N + alt) * clat * clon);
        float y = (float) ((N + alt) * clat * slon);
        float z = (float) ((N * (1.0 - WGS84_E2) + alt) * slat);

        return new float[]{x, y, z};
    }

    public static float[] switchECEFtoENU(double lat, double lng, float[] ecefCurrentLocation, float[] ecefPOI) {
        double radLat = Math.toRadians(lat);
        double radLon = Math.toRadians(lng);

        float clat = (float) Math.cos(radLat);
        float slat = (float) Math.sin(radLat);
        float clon = (float) Math.cos(radLon);
        float slon = (float) Math.sin(radLon);

        float dx = ecefCurrentLocation[0] - ecefPOI[0];
        float dy = ecefCurrentLocation[1] - ecefPOI[1];
        float dz = ecefCurrentLocation[2] - ecefPOI[2];

        float east = -slon * dx + clon * dy;

        float north = -slat * clon * dx - slat * slon * dy + clat * dz;

        float up = clat * clon * dx + clat * slon * dy + slat * dz;

        return new float[]{east, north, up, 1};
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static double getDistance(Location location1, Location location2) {
        return getDistance(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude());//单位米
    }

    public static double getDistance(BDLocation location1, BDLocation location2) {
        return getDistance(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude());//单位米
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * WGS84_A;
        return s;//单位米
    }
}
