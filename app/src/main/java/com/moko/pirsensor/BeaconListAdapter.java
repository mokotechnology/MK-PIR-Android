package com.moko.pirsensor;

import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.pirsensor.entity.BeaconInfo;

public class BeaconListAdapter extends BaseQuickAdapter<BeaconInfo, BaseViewHolder> {
    public BeaconListAdapter() {
        super(R.layout.list_item_device_pir);
    }

    @Override
    protected void convert(BaseViewHolder helper, BeaconInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, String.format("%ddBm", item.rssi));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.setText(R.id.tv_battery, item.battery < 0 ? "N/A" : String.format("%dmV", item.battery));
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState > 0);
        helper.setText(R.id.tv_tx_power, String.format("Tx Power:%ddBm", item.txPower));
        helper.setText(R.id.tv_rssi_1m, String.format("%ddBm", item.rssi1m));
        helper.setText(R.id.tv_status, item.pirState == 0 ? "Motion not detected" : "Motion detected");
        String sensitivity = "Low";
        if (item.pirSensitivity == 1) {
            sensitivity = "Medium";
        } else if (item.pirSensitivity == 2) {
            sensitivity = "High";
        }
        helper.setText(R.id.tv_sensitivity, sensitivity);
        helper.setText(R.id.tv_door_status, item.doorState == 0 ? "Closed" : "Open");
        String delay = "Low";
        if (item.pirDelay == 1) {
            delay = "Medium";
        } else if (item.pirDelay == 2) {
            delay = "High";
        }
        helper.setText(R.id.tv_delay, delay);
        helper.setText(R.id.tv_major, String.valueOf(item.major));
        helper.setText(R.id.tv_minor, String.valueOf(item.minor));
    }
}
