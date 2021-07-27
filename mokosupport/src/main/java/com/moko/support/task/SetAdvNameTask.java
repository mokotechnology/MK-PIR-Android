package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class SetAdvNameTask extends OrderTask {

    public byte[] data;

    public SetAdvNameTask() {
        super(OrderCHAR.CHAR_ADV_NAME, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
