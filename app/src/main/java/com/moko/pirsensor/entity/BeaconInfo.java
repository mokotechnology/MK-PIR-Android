package com.moko.pirsensor.entity;

import java.io.Serializable;
import java.util.HashMap;

public class BeaconInfo implements Serializable {

    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public int battery;
    public int connectState;
    public int txPower;
    public int rssi1m;
    public int pirState;
    public int pirSensitivity;
    public int doorState;
    public int pirDelay;
    public int major;
    public int minor;
    public long intervalTime;
    public long scanTime;

    @Override
    public String toString() {
        return "BeaconXInfo{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
