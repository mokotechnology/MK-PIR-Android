package com.moko.pirsensor.able;

public interface ISlotDataAction {
    boolean isValid();

    void sendData();

    void resetParams();
}
