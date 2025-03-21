package com.moko.support.pir.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.pir.entity.OrderCHAR;

public class SetMinorTask extends OrderTask {

    public byte[] data;

    public SetMinorTask() {
        super(OrderCHAR.CHAR_MINOR, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int minor) {
        this.data = MokoUtils.toByteArray(minor, 2);
    }
}
