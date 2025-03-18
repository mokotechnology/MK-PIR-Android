package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.pir.entity.OrderCHAR;

public class GetRSSITask extends OrderTask {

    public byte[] data;

    public GetRSSITask() {
        super(OrderCHAR.CHAR_RSSI, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
