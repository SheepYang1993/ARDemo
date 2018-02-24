package me.sheepyang.ardemo.location.model;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;

/**
 * Created by ntdat on 1/16/17.
 */

public class ARPoint {
    BDLocation location;

    public ARPoint(String name, double lat, double lon, double altitude) {
        location = new BDLocation();
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        Address.Builder addr = new Address.Builder();
        addr.district(name);
        location.setAddr(addr.build());
    }

    public BDLocation getLocation() {
        return location;
    }
}
