package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.pir.entity.OrderCHAR;

public class SetRSSITask extends OrderTask {

    public byte[] data;

    public SetRSSITask() {
        super(OrderCHAR.CHAR_RSSI, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
