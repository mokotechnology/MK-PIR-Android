package com.moko.pirsensor.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.pirsensor.R;
import com.moko.pirsensor.activity.DeviceInfoActivity;
import com.moko.pirsensor.databinding.FragmentAdvBinding;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.TxPowerEnum;

import java.util.ArrayList;

public class AdvFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private FragmentAdvBinding mBind;
    private static final String TAG = "AdvFragment";
    private final String FILTER_ASCII = "[ -~]*";

    private DeviceInfoActivity activity;

    public AdvFragment() {
    }

    public static AdvFragment newInstance() {
        AdvFragment fragment = new AdvFragment();
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
        mBind = FragmentAdvBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }

            return null;
        };
        mBind.etDeviceName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), inputFilter});
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        upgdateData(seekBar.getId(), progress);
    }


    public void upgdateData(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_rssi:
                int rssi = progress - 100;
                mBind.tvRssi.setText(String.format("%ddBm", rssi));
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
                mBind.tvTxPower.setText(String.format("%ddBm", txPower));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setMajor(int major) {
        mBind.etMajor.setText(String.valueOf(major));
    }

    public void setMinor(int minor) {
        mBind.etMinor.setText(String.valueOf(minor));
    }

    public void setRssi(int rssi) {
        int progress = 100 + rssi;
        mBind.sbRssi.setProgress(progress);
    }

    public void setTxPower(int txPower) {
        int grade = 7 - txPower;
        mBind.sbTxPower.setProgress(grade);
    }


    public void setAdvInterval(int interval) {
        mBind.etAdvInterval.setText(String.valueOf(interval));
    }

    public void setAdvName(String advName) {
        mBind.etDeviceName.setText(advName);
    }

    public void setSerialID(String serialID) {
        mBind.etSerialId.setText(serialID);
    }

    public boolean isValid() {
        final String majorStr = mBind.etMajor.getText().toString();
        final String minorStr = mBind.etMinor.getText().toString();
        final String advIntervalStr = mBind.etAdvInterval.getText().toString();
        final String deviceNameStr = mBind.etDeviceName.getText().toString();
        final String serialIdStr = mBind.etSerialId.getText().toString();
        if (TextUtils.isEmpty(majorStr))
            return false;
        if (TextUtils.isEmpty(minorStr))
            return false;
        if (TextUtils.isEmpty(advIntervalStr))
            return false;
        if (TextUtils.isEmpty(deviceNameStr))
            return false;
        if (TextUtils.isEmpty(serialIdStr))
            return false;
        final int major = Integer.parseInt(majorStr);
        if (major < 0 || major > 65535)
            return false;
        if (TextUtils.isEmpty(minorStr))
            return false;
        final int minor = Integer.parseInt(minorStr);
        if (minor < 0 || minor > 65535)
            return false;
        final int advInterval = Integer.parseInt(advIntervalStr);
        if (advInterval < 1 || advInterval > 100)
            return false;
        return true;
    }

    public void saveParams() {
        final String majorStr = mBind.etMajor.getText().toString();
        final String minorStr = mBind.etMinor.getText().toString();
        final String advIntervalStr = mBind.etAdvInterval.getText().toString();
        final String deviceNameStr = mBind.etDeviceName.getText().toString();
        final String serialIdStr = mBind.etSerialId.getText().toString();
        final int major = Integer.parseInt(majorStr);
        final int minor = Integer.parseInt(minorStr);
        final int advInterval = Integer.parseInt(advIntervalStr);
        final int rssi = mBind.sbRssi.getProgress();
        byte[] rssiBytes = MokoUtils.toByteArray(Math.abs(rssi - 100), 1);
        final int txPower = mBind.sbTxPower.getProgress();
        byte[] txPowerBytes = MokoUtils.toByteArray(7 - txPower, 1);
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setMajor(major));
        orderTasks.add(OrderTaskAssembler.setMinor(minor));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(advInterval));
        orderTasks.add(OrderTaskAssembler.setAdvName(deviceNameStr));
        orderTasks.add(OrderTaskAssembler.setRSSI(rssiBytes));
        orderTasks.add(OrderTaskAssembler.setAdvTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setSerialID(serialIdStr));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
