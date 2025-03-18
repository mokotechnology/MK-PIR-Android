package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.pir.entity.OrderCHAR;

public class SetMajorTask extends OrderTask {

    public byte[] data;

    public SetMajorTask() {
        super(OrderCHAR.CHAR_MAJOR, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int major) {
        this.data = MokoUtils.toByteArray(major, 2);
    }
}
