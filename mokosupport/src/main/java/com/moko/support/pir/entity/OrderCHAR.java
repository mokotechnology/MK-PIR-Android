package com.moko.support.pir.entity;

import java.io.Serializable;
import java.util.UUID;

public enum OrderCHAR implements Serializable {
    // 180A
    CHAR_MODEL_NUMBER(UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB")),
    CHAR_SERIAL_NUMBER(UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB")),
    CHAR_MANUFACTURER_NAME(UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB")),
    CHAR_FIRMWARE_REVISION(UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB")),
    CHAR_HARDWARE_REVISION(UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB")),
    CHAR_SOFTWARE_REVISION(UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB")),
    // FF00
    CHAR_MAJOR(UUID.fromString("0000FF02-0000-1000-8000-00805F9B34FB")),
    CHAR_MINOR(UUID.fromString("0000FF03-0000-1000-8000-00805F9B34FB")),
    CHAR_RSSI(UUID.fromString("0000FF04-0000-1000-8000-00805F9B34FB")),
    CHAR_TX_POWER(UUID.fromString("0000FF05-0000-1000-8000-00805F9B34FB")),
    CHAR_PASSWORD(UUID.fromString("0000FF06-0000-1000-8000-00805F9B34FB")),
    CHAR_ADV_INTERVAL(UUID.fromString("0000FF07-0000-1000-8000-00805F9B34FB")),
    CHAR_SERIAL_ID(UUID.fromString("0000FF08-0000-1000-8000-00805F9B34FB")),
    CHAR_ADV_NAME(UUID.fromString("0000FF09-0000-1000-8000-00805F9B34FB")),
    CHAR_MAC(UUID.fromString("0000FF0C-0000-1000-8000-00805F9B34FB")),
    CHAR_CONNECTION(UUID.fromString("0000FF0E-0000-1000-8000-00805F9B34FB")),
    CHAR_RESET(UUID.fromString("0000FF0F-0000-1000-8000-00805F9B34FB")),
    CHAR_HALL_STATUS(UUID.fromString("0000FF12-0000-1000-8000-00805F9B34FB")),
    CHAR_PARAMS(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")),
    ;

    private UUID uuid;

    OrderCHAR(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
