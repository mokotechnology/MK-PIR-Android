package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.pir.entity.OrderCHAR;

public class SetPasswordTask extends OrderTask {

    public byte[] data;

    public SetPasswordTask() {
        super(OrderCHAR.CHAR_PASSWORD, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
