package com.moko.pirsensor.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.pirsensor.databinding.FragmentDevicePirBinding;

public class DeviceFragment extends Fragment {

    private FragmentDevicePirBinding mBind;
    private static final String TAG = "DeviceFragment";


    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentDevicePirBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public void setDeviceMac(String macShow) {
        mBind.tvMacAddress.setText(macShow);
    }

    public void setManufacturer(byte[] value) {
        String manufacturer = new String(value).trim();
        mBind.tvManufacturer.setText(manufacturer);
    }

    public void setDeviceModel(byte[] value) {
        String deviceModel = new String(value).trim();
        mBind.tvDeviceModel.setText(deviceModel);
    }

    public void setProductDate(byte[] value) {
        String productDate = new String(value).trim();
        mBind.tvProductDate.setText(productDate);
    }

    public void setHardwareVersion(byte[] value) {
        String hardwareVersion = new String(value).trim();
        mBind.tvHardwareVersion.setText(hardwareVersion);
    }

    public void setFirmwareVersion(byte[] value) {
        String firmwareVersion = new String(value).trim();
        mBind.tvFirmwareVersion.setText(firmwareVersion);
    }

    public void setSoftwareVersion(byte[] value) {
        String softwareVersion = new String(value).trim();
        mBind.tvSoftwareVersion.setText(softwareVersion);
    }

    public void setBattery(byte[] value) {
        String battery = String.format("%dmV", MokoUtils.toInt(value));
        mBind.tvSoc.setText(battery);
    }

    public void setRunningTime(byte[] value) {
        int seconds = MokoUtils.toInt(value);
        int day = 0, hours = 0, minutes = 0;
        day = seconds / (60 * 60 * 24);
        seconds -= day * 60 * 60 * 24;
        hours = seconds / (60 * 60);
        seconds -= hours * 60 * 60;
        minutes = seconds / 60;
        seconds -= minutes * 60;
        mBind.tvRunningTime.setText(String.format("%dD%dH%dM%dS", day, hours, minutes, seconds));
    }

    public void setChipModel(byte[] value) {
        String chipModel = new String(value);
        mBind.tvChipModel.setText(chipModel);
    }
}
