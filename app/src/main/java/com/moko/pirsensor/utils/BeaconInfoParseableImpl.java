package com.moko.pirsensor.utils;

import android.os.SystemClock;
import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.pirsensor.entity.BeaconInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoParseable;

import java.util.Arrays;
import java.util.HashMap;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2019/5/22
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.pirsensor.utils.BeaconXInfoParseableImpl
 */
public class BeaconInfoParseableImpl implements DeviceInfoParseable<BeaconInfo> {
    private HashMap<String, BeaconInfo> beaconXInfoHashMap;

    public BeaconInfoParseableImpl() {
        this.beaconXInfoHashMap = new HashMap<>();
    }

    @Override
    public BeaconInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        byte[] data = record.getManufacturerSpecificData(0x620A);
        if (data == null || data.length != 23)
            return null;
        int battery = -1;
        String pirState = "";
        String state = "";
        String sensitivity = "";
        String doorState = "";
        String delay = "";
        int major = 0;
        int minor = 0;
        int rssi = 0;
        int txPower = record.getTxPowerLevel();
        pirState = MokoUtils.hexString2binaryString(MokoUtils.bytesToHexString(Arrays.copyOfRange(data, 2, 3)));
        state = pirState.substring(7);
        sensitivity = pirState.substring(5, 7);
        doorState = pirState.substring(4, 5);
        delay = pirState.substring(2, 4);
        battery = MokoUtils.toInt(Arrays.copyOfRange(data, 16, 18));
        major = MokoUtils.toInt(Arrays.copyOfRange(data, 18, 20));
        minor = MokoUtils.toInt(Arrays.copyOfRange(data, 20, 22));
        rssi = data[22];
        // avoid repeat
        BeaconInfo beaconInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            beaconInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            if (!TextUtils.isEmpty(deviceInfo.name)) {
                beaconInfo.name = deviceInfo.name;
            }
            beaconInfo.rssi = deviceInfo.rssi;
            if (battery >= 0) {
                beaconInfo.battery = battery;
            }
            beaconInfo.pirState = "1".equals(state) ? 1 : 0;
            if ("00".equals(sensitivity)) {
                beaconInfo.pirSensitivity = 0;
            } else if ("01".equals(sensitivity)) {
                beaconInfo.pirSensitivity = 1;
            } else if ("10".equals(sensitivity)) {
                beaconInfo.pirSensitivity = 2;
            }
            beaconInfo.doorState = "1".equals(doorState) ? 1 : 0;
            if ("00".equals(delay)) {
                beaconInfo.pirDelay = 0;
            } else if ("01".equals(delay)) {
                beaconInfo.pirDelay = 1;
            } else if ("10".equals(delay)) {
                beaconInfo.pirDelay = 2;
            }
            beaconInfo.major = major;
            beaconInfo.minor = minor;
            beaconInfo.txPower = txPower;
            beaconInfo.rssi1m = rssi;
            if (result.isConnectable())
                beaconInfo.connectState = 1;
            beaconInfo.scanRecord = deviceInfo.scanRecord;
            long currentTime = SystemClock.elapsedRealtime();
            long intervalTime = currentTime - beaconInfo.scanTime;
            beaconInfo.intervalTime = intervalTime;
            beaconInfo.scanTime = currentTime;
        } else {
            beaconInfo = new BeaconInfo();
            beaconInfo.name = deviceInfo.name;
            beaconInfo.mac = deviceInfo.mac;
            beaconInfo.rssi = deviceInfo.rssi;
            if (battery < 0) {
                beaconInfo.battery = -1;
            } else {
                beaconInfo.battery = battery;
            }
            beaconInfo.pirState = "1".equals(state) ? 1 : 0;
            if ("00".equals(sensitivity)) {
                beaconInfo.pirSensitivity = 0;
            } else if ("01".equals(sensitivity)) {
                beaconInfo.pirSensitivity = 1;
            } else if ("10".equals(sensitivity)) {
                beaconInfo.pirSensitivity = 2;
            }
            beaconInfo.doorState = "1".equals(doorState) ? 1 : 0;
            if ("00".equals(delay)) {
                beaconInfo.pirDelay = 0;
            } else if ("01".equals(delay)) {
                beaconInfo.pirDelay = 1;
            } else if ("10".equals(delay)) {
                beaconInfo.pirDelay = 2;
            }
            beaconInfo.major = major;
            beaconInfo.minor = minor;
            beaconInfo.txPower = txPower;
            beaconInfo.rssi1m = rssi;
            if (result.isConnectable()) {
                beaconInfo.connectState = 1;
            } else {
                beaconInfo.connectState = 0;
            }
            beaconInfo.scanRecord = deviceInfo.scanRecord;
            beaconInfo.scanTime = SystemClock.elapsedRealtime();
            beaconXInfoHashMap.put(deviceInfo.mac, beaconInfo);
        }
        return beaconInfo;
    }
}
