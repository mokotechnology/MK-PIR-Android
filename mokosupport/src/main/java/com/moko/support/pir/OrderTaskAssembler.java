package com.moko.support.pir;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.pir.entity.ParamsKeyEnum;
import com.moko.support.pir.task.GetAdvIntervalTask;
import com.moko.support.pir.task.GetAdvNameTask;
import com.moko.support.pir.task.GetAdvTxPowerTask;
import com.moko.support.pir.task.GetConnectableTask;
import com.moko.support.pir.task.GetFirmwareRevisionTask;
import com.moko.support.pir.task.GetHardwareRevisionTask;
import com.moko.support.pir.task.GetMajorTask;
import com.moko.support.pir.task.GetManufacturerNameTask;
import com.moko.support.pir.task.GetMinorTask;
import com.moko.support.pir.task.GetModelNumberTask;
import com.moko.support.pir.task.GetRSSITask;
import com.moko.support.pir.task.GetSerialIDTask;
import com.moko.support.pir.task.GetSerialNumberTask;
import com.moko.support.pir.task.GetSoftwareRevisionTask;
import com.moko.support.pir.task.ParamsTask;
import com.moko.support.pir.task.SetAdvIntervalTask;
import com.moko.support.pir.task.SetAdvNameTask;
import com.moko.support.pir.task.SetAdvTxPowerTask;
import com.moko.support.pir.task.SetConnectableTask;
import com.moko.support.pir.task.SetMajorTask;
import com.moko.support.pir.task.SetMinorTask;
import com.moko.support.pir.task.SetPasswordTask;
import com.moko.support.pir.task.SetRSSITask;
import com.moko.support.pir.task.SetResetTask;
import com.moko.support.pir.task.SetSerialIDTask;

import androidx.annotation.IntRange;

public class OrderTaskAssembler {
    /**
     * @Description 获取制造商
     */
    public static OrderTask getManufacturer() {
        GetManufacturerNameTask task = new GetManufacturerNameTask();
        return task;
    }

    /**
     * @Description 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        GetModelNumberTask task = new GetModelNumberTask();
        return task;
    }

    /**
     * @Description 获取生产日期
     */
    public static OrderTask getProductDate() {
        GetSerialNumberTask task = new GetSerialNumberTask();
        return task;
    }

    /**
     * @Description 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        GetHardwareRevisionTask task = new GetHardwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        GetFirmwareRevisionTask task = new GetFirmwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        GetSoftwareRevisionTask task = new GetSoftwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取设备MAC
     */
    public static OrderTask getDeviceMac() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_MAC);
        return task;
    }

    /**
     * @Description 获取连接状态
     */
    public static OrderTask getConnectable() {
        GetConnectableTask task = new GetConnectableTask();
        return task;
    }

    /**
     * @Description 设置连接状态
     */
    public static OrderTask setConnectable(boolean isConnectable) {
        SetConnectableTask task = new SetConnectableTask();
        task.setData(isConnectable ? MokoUtils.toByteArray(0, 1) : MokoUtils.toByteArray(1, 1));
        return task;
    }

    /**
     * @Description 获取Major
     */
    public static OrderTask getMajor() {
        GetMajorTask task = new GetMajorTask();
        return task;
    }

    /**
     * @Description 设置Major
     */
    public static OrderTask setMajor(@IntRange(from = 0, to = 65535) int major) {
        SetMajorTask task = new SetMajorTask();
        task.setData(major);
        return task;
    }

    /**
     * @Description 获取Minor
     */
    public static OrderTask getMinor() {
        GetMinorTask task = new GetMinorTask();
        return task;
    }

    /**
     * @Description 设置Minor
     */
    public static OrderTask setMinor(@IntRange(from = 0, to = 65535) int minor) {
        SetMinorTask task = new SetMinorTask();
        task.setData(minor);
        return task;
    }

    /**
     * @Description 获取电池电量
     */
    public static OrderTask getBatteryPower() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_BATTERY_POWER);
        return task;
    }

    /**
     * @Description 获取运行时长
     */
    public static OrderTask getRunningTime() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_RUNNING_TIME);
        return task;
    }

    /**
     * @Description 获取芯片类型
     */
    public static OrderTask getChipModel() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_CHIP_MODEL);
        return task;
    }

    /**
     * @Description 获取按键关键
     */
    public static OrderTask getButtonPower() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_BUTTON_POWER);
        return task;
    }

    /**
     * @Description 设置按键关键
     */
    public static OrderTask setButtonPower(int enable) {
        ParamsTask task = new ParamsTask();
        task.setButtonPower(enable);
        return task;
    }


    /**
     * @Description 获取信号强度
     */
    public static OrderTask getRSSI() {
        GetRSSITask task = new GetRSSITask();
        return task;
    }

    /**
     * @Description 设置信号强度
     */
    public static OrderTask setRSSI(byte[] data) {
        SetRSSITask task = new SetRSSITask();
        task.setData(data);
        return task;
    }

    /**
     * @Description 获取广播间隔
     */
    public static OrderTask getAdvInterval() {
        GetAdvIntervalTask task = new GetAdvIntervalTask();
        return task;
    }

    /**
     * @Description 设置广播间隔
     */
    public static OrderTask setAdvInterval(int interval) {
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 1);
        SetAdvIntervalTask task = new SetAdvIntervalTask();
        task.setData(intervalBytes);
        return task;
    }

    /**
     * @Description 获取广播名称
     */
    public static OrderTask getAdvName() {
        GetAdvNameTask task = new GetAdvNameTask();
        return task;
    }

    /**
     * @Description 设置广播名称
     */
    public static OrderTask setAdvName(String advName) {
        SetAdvNameTask task = new SetAdvNameTask();
        task.setData(advName.getBytes());
        return task;
    }

    /**
     * @Description 获取序列号
     */
    public static OrderTask getSerialID() {
        GetSerialIDTask task = new GetSerialIDTask();
        return task;
    }

    /**
     * @Description 设置序列号
     */
    public static OrderTask setSerialID(String serialID) {
        SetSerialIDTask task = new SetSerialIDTask();
        task.setData(serialID.getBytes());
        return task;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask setAdvTxPower(byte[] data) {
        SetAdvTxPowerTask advTxPowerTask = new SetAdvTxPowerTask();
        advTxPowerTask.setData(data);
        return advTxPowerTask;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask getAdvTxPower() {
        GetAdvTxPowerTask task = new GetAdvTxPowerTask();
        return task;
    }

    /**
     * @Description 获取PIR灵敏度
     */
    public static OrderTask getPIRSensitivity() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_PIR_SENSITIVITY);
        return task;
    }

    /**
     * @Description 设置PIR灵敏度
     */
    public static OrderTask setPIRSensitivity(@IntRange(from = 0, to = 2) int sensitivity) {
        ParamsTask task = new ParamsTask();
        task.setPIRSensitivity(sensitivity);
        return task;
    }

    /**
     * @Description 获取PIR延迟时间
     */
    public static OrderTask getPIRDelay() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_PIR_DELAY);
        return task;
    }

    /**
     * @Description 设置PIR延迟时间
     */
    public static OrderTask setPIRDelay(@IntRange(from = 0, to = 2) int delay) {
        ParamsTask task = new ParamsTask();
        task.setPIRDelay(delay);
        return task;
    }

    /**
     * @Description 获取当前时间
     */
    public static OrderTask getTime() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_TIME);
        return task;
    }

    /**
     * @Description 设置当前时间
     */
    public static OrderTask setTime() {
        ParamsTask task = new ParamsTask();
        task.setTime();
        return task;
    }

    /**
     * @Description 关机
     */
    public static OrderTask setClose() {
        ParamsTask task = new ParamsTask();
        task.setClose();
        return task;
    }

    /**
     * @Description 保存出厂设置
     */
    public static OrderTask setDefault() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.SET_DEFAULT);
        return task;
    }

    /**
     * @Description 恢复出厂设置
     */
    public static OrderTask resetDevice(String password) {
        SetResetTask task = new SetResetTask();
        task.setData(password.getBytes());
        return task;
    }

    public static OrderTask setPassword(String password) {
        SetPasswordTask task = new SetPasswordTask();
        task.setData(password.getBytes());
        return task;
    }

}
