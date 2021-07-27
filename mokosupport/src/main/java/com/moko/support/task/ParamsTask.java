package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import java.util.Calendar;

import androidx.annotation.IntRange;


public class ParamsTask extends OrderTask {
    public byte[] data;

    public ParamsTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        switch (key) {
            case GET_MAC:
            case GET_BATTERY_POWER:
            case GET_RUNNING_TIME:
            case GET_CHIP_MODEL:
            case GET_BUTTON_POWER:
            case GET_PIR_SENSITIVITY:
            case GET_PIR_DELAY:
            case GET_TIME:
                createGetConfigData(key.getParamsKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{(byte) 0xEA, (byte) configKey, (byte) 0x00, (byte) 0x00};
    }

    public void setClose() {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_CLOSE.getParamsKey(),
                (byte) 0x00,
                (byte) 0x00
        };
    }

    public void setDefault() {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_DEFAULT.getParamsKey(),
                (byte) 0x00,
                (byte) 0x00
        };
    }

    public void setButtonPower(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_BUTTON_POWER.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) enable,
        };
    }

    public void setPIRSensitivity(@IntRange(from = 0, to = 2) int sensitivity) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_PIR_SENSITIVITY.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) sensitivity,
        };
    }

    public void setPIRDelay(@IntRange(from = 0, to = 2) int delay) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_PIR_DELAY.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) delay,
        };
    }

    public void setTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_TIME.getParamsKey(),
                (byte) 0x00,
                (byte) 0x06,
                (byte) (year - 2000),
                (byte) (month + 1),
                (byte) day,
                (byte) hour,
                (byte) min,
                (byte) second
        };
    }

}
