package com.moko.support.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    GET_MAC(0x57),
    SET_MAC(0x6F),
    GET_BATTERY_POWER(0x58),
    GET_RUNNING_TIME(0x59),
    GET_CHIP_MODEL(0x5B),
    SET_CLOSE(0x6D),
    SET_DEFAULT(0x6E),
    SET_BUTTON_POWER(0x70),
    GET_BUTTON_POWER(0x71),
    SET_PIR_SENSITIVITY(0x72),
    GET_PIR_SENSITIVITY(0x73),
    SET_PIR_DELAY(0x74),
    GET_PIR_DELAY(0x75),
    SET_TIME(0x76),
    GET_TIME(0x77),
    ;

    private int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
