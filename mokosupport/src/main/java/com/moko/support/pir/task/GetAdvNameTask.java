package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.pir.entity.OrderCHAR;

public class GetAdvNameTask extends OrderTask {

    public byte[] data;

    public GetAdvNameTask() {
        super(OrderCHAR.CHAR_ADV_NAME, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
