package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetConnectableTask extends OrderTask {

    public byte[] data;

    public GetConnectableTask() {
        super(OrderCHAR.CHAR_CONNECTION, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
