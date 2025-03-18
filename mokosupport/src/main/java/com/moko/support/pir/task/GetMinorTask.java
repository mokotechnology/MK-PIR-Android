package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.pir.entity.OrderCHAR;

public class GetMinorTask extends OrderTask {

    public byte[] data;

    public GetMinorTask() {
        super(OrderCHAR.CHAR_MINOR, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
