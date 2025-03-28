package com.moko.pirsensor.activity;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.pirsensor.AppConstants;
import com.moko.pirsensor.R;
import com.moko.pirsensor.databinding.ActivityDeviceInfoPirBinding;
import com.moko.pirsensor.dialog.AlertMessageDialog;
import com.moko.pirsensor.dialog.LoadingMessageDialog;
import com.moko.pirsensor.dialog.ModifyPasswordDialog;
import com.moko.pirsensor.fragment.AdvFragment;
import com.moko.pirsensor.fragment.DeviceFragment;
import com.moko.pirsensor.fragment.SettingFragment;
import com.moko.pirsensor.service.DfuServicePir;
import com.moko.pirsensor.utils.FileUtils;
import com.moko.pirsensor.utils.ToastUtils;
import com.moko.support.pir.MokoSupport;
import com.moko.support.pir.OrderTaskAssembler;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import com.moko.support.pir.entity.OrderCHAR;
import com.moko.support.pir.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.IdRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DeviceInfoActivity extends BaseActivity<ActivityDeviceInfoPirBinding> implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    private FragmentManager fragmentManager;
    private AdvFragment advFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;
    private boolean mIsReset;
    private boolean mIsModifyPassword;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        fragmentManager = getFragmentManager();
        initFragment();
        mBind.rgOptions.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            getAdvInfo();
        }
    }

    @Override
    protected ActivityDeviceInfoPirBinding getViewBinding() {
        return ActivityDeviceInfoPirBinding.inflate(getLayoutInflater());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                if (mIsClose || mIsReset || mIsModifyPassword)
                    return;
                if (MokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        mBind.tvTitle.postDelayed(() -> {
                            dismissDFUProgressDialog();
                        }, 2000);
                    } else {
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setMessage("The device disconnected!");
                        dialog.setConfirm("Exit");
                        dialog.setCancelGone();
                        dialog.setOnAlertConfirmListener(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {

            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_MAJOR:
                        if (value.length > 0) {
                            int major = MokoUtils.toInt(value);
                            advFragment.setMajor(major);
                        }
                        break;
                    case CHAR_MINOR:
                        if (value.length > 0) {
                            int minor = MokoUtils.toInt(value);
                            advFragment.setMinor(minor);
                        }
                        break;
                    case CHAR_RSSI:
                        if (value.length > 0) {
                            int rssi = (value[0] & 0xFF) * -1;
                            advFragment.setRssi(rssi);
                        }
                        break;
                    case CHAR_ADV_INTERVAL:
                        if (value.length > 0) {
                            int interval = value[0] & 0xFF;
                            advFragment.setAdvInterval(interval);
                        }
                        break;
                    case CHAR_TX_POWER:
                        if (value.length > 0) {
                            int txPower = value[0] & 0xFF;
                            advFragment.setTxPower(txPower);
                        }
                        break;
                    case CHAR_ADV_NAME:
                        if (value.length > 0) {
                            String advName = new String(value);
                            advFragment.setAdvName(advName);
                        }
                        break;
                    case CHAR_SERIAL_ID:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            if (value.length > 0) {
                                String serialID = new String(value);
                                advFragment.setSerialID(serialID);
                            }
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                        }
                        break;
                    case CHAR_CONNECTION:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            if (value.length > 0) {
                                isConnected = MokoUtils.toInt(value) == 0;
                                settingFragment.setConnectable(value);
                            }
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                        }
                        break;
                    case CHAR_PASSWORD:
                        if (2 == (MokoUtils.toInt(value))) {
                            mIsModifyPassword = true;
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setMessage("Modify password success!\nPlease reconnect the Device.");
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                back();
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        }
                        break;
                    case CHAR_RESET:
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            mIsReset = true;
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setMessage("Reset success!\nPlease reconnect the Device.");
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                back();
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                        }
                        break;
                    case CHAR_PARAMS:
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[3] & 0xFF;
                            switch (configKeyEnum) {
                                case GET_BATTERY_POWER:
                                    if (length == 2) {
                                        byte[] batteryBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        deviceFragment.setBattery(batteryBytes);
//                                        validParams.battery = "1";
                                    }
                                    break;
                                case GET_MAC:
                                    if (length >= 6) {
                                        String valueStr = MokoUtils.bytesToHexString(value);
                                        String mac = valueStr.substring(valueStr.length() - 12).toUpperCase();
                                        StringBuffer stringBuffer = new StringBuffer(mac);
                                        stringBuffer.insert(2, ":");
                                        stringBuffer.insert(5, ":");
                                        stringBuffer.insert(8, ":");
                                        stringBuffer.insert(11, ":");
                                        stringBuffer.insert(14, ":");
                                        String macShow = stringBuffer.toString();
                                        deviceFragment.setDeviceMac(macShow);
                                        mDeviceMac = macShow;
                                    }
                                    break;
                                case GET_BUTTON_POWER:
                                    if (length >= 1) {
                                        enableButtonPower = value[4] == 1;
                                        settingFragment.setButtonPower(enableButtonPower);
                                    }
                                    break;
                                case SET_BUTTON_POWER:
                                    if (length == 1 && (0xAA == (value[4] & 0xFF))) {
                                        ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                                    }
                                    break;
                                case GET_CHIP_MODEL:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        deviceFragment.setChipModel(rawDataBytes);
                                    }
                                    break;
                                case GET_RUNNING_TIME:
                                    if (length > 0) {
                                        byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                        deviceFragment.setRunningTime(rawDataBytes);
                                    }
                                    break;
                                case SET_CLOSE:
                                    if (length == 1 && (0xAA == (value[4] & 0xFF))) {
                                        ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                                        settingFragment.setClose();
                                        back();
                                    }
                                    break;
                            }
                        }
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        deviceFragment.setManufacturer(value);
                        break;
                    case CHAR_MODEL_NUMBER:
                        deviceFragment.setDeviceModel(value);
                        break;
                    case CHAR_SERIAL_NUMBER:
                        deviceFragment.setProductDate(value);
                        break;
                    case CHAR_HARDWARE_REVISION:
                        deviceFragment.setHardwareVersion(value);
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        deviceFragment.setFirmwareVersion(value);
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        deviceFragment.setSoftwareVersion(value);
                        break;
                }
            }
        });
    }

    private void getAdvInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getMajor());
        orderTasks.add(OrderTaskAssembler.getMinor());
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getSerialID());
        orderTasks.add(OrderTaskAssembler.getAdvInterval());
        orderTasks.add(OrderTaskAssembler.getRSSI());
        orderTasks.add(OrderTaskAssembler.getAdvTxPower());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void getDeviceInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getConnectable());
        orderTasks.add(OrderTaskAssembler.getButtonPower());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void getSystemInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getProductDate());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getBatteryPower());
        orderTasks.add(OrderTaskAssembler.getChipModel());
        orderTasks.add(OrderTaskAssembler.getRunningTime());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setTitle("Dismiss");
                            dialog.setCancelGone();
                            dialog.setMessage("The current system of bluetooth is not available!");
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            getDeviceInfo();
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(firmwareFilePath)) {
                    return;
                }
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setDeviceName(mDeviceName)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuServicePir.class);
                    showDFUProgressDialog("Waiting...");
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        MokoSupport.getInstance().disConnectBle();
        mIsClose = false;
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void initFragment() {
        advFragment = AdvFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, advFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(advFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();

    }

    private void showAdvFragment() {
        mBind.ivSave.setVisibility(View.VISIBLE);
        if (advFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(advFragment)
                    .commit();
        }
        mBind.tvTitle.setText(getString(R.string.advertisement_title));
    }

    private void showSettingFragment() {
        mBind.ivSave.setVisibility(View.GONE);
        if (settingFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(advFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        mBind.tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        mBind.ivSave.setVisibility(View.GONE);
        if (deviceFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(advFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        mBind.tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtn_adv) {
            showAdvFragment();
            getAdvInfo();
        } else if (checkedId == R.id.radioBtn_setting) {
            showSettingFragment();
            getDeviceInfo();
        } else if (checkedId == R.id.radioBtn_device) {
            showDeviceFragment();
            getSystemInfo();
        }
    }


    public void onModifyPassword(View view) {
        if (isWindowLocked())
            return;
        final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog();
        modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setPassword(password));
            }

            @Override
            public void onPasswordNotMatch() {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setMessage("Password do not match!\nPlease try again.");
                dialog.setConfirm(R.string.ok);
                dialog.setCancelGone();
                dialog.show(getSupportFragmentManager());
            }
        });
        modifyPasswordDialog.show(getSupportFragmentManager());
    }

    public void onResetDevice(View view) {
        if (isWindowLocked())
            return;
        final AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
        resetDeviceDialog.setMessage("Are you sure to reset the device？");
        resetDeviceDialog.setConfirm(R.string.ok);
        resetDeviceDialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice(mPassword));
        });
        resetDeviceDialog.show(getSupportFragmentManager());

    }

    boolean isConnected;

    public void onSetConnectable(View view) {
        if (isWindowLocked()) return;
        if (isConnected) {
            final AlertMessageDialog connectAlertDialog = new AlertMessageDialog();
            connectAlertDialog.setMessage("Are you sure to set the device non-connectable？");
            connectAlertDialog.setConfirm(R.string.ok);
            connectAlertDialog.setOnAlertConfirmListener(() -> {
                showSyncingProgressDialog();
                ArrayList<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.setConnectable(false));
                orderTasks.add(OrderTaskAssembler.getConnectable());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            });
            connectAlertDialog.show(getSupportFragmentManager());
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setConnectable(true));
            orderTasks.add(OrderTaskAssembler.getConnectable());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    private boolean enableButtonPower;

    public void onSetButtonPower(View view) {
        if (isWindowLocked()) return;
        if (enableButtonPower) {
            final AlertMessageDialog buttonPowerAlertDialog = new AlertMessageDialog();
            buttonPowerAlertDialog.setMessage("If disable Button Power OFF, then it  cannot power off beacon by press button operation.");
            buttonPowerAlertDialog.setConfirm(R.string.ok);
            buttonPowerAlertDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                @Override
                public void onClick() {
                    showSyncingProgressDialog();
                    ArrayList<OrderTask> orderTasks = new ArrayList<>();
                    orderTasks.add(OrderTaskAssembler.setButtonPower(0));
                    orderTasks.add(OrderTaskAssembler.getButtonPower());
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                }
            });
            buttonPowerAlertDialog.show(getSupportFragmentManager());
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setButtonPower(1));
            orderTasks.add(OrderTaskAssembler.getButtonPower());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }

    }

    public void onSetClose(View view) {
        if (isWindowLocked()) return;
        final AlertMessageDialog powerAlertDialog = new AlertMessageDialog();
        powerAlertDialog.setMessage("Are you sure to turn off the device?Please make sure the device has a button to turn on!");
        powerAlertDialog.setConfirm(R.string.ok);
        powerAlertDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                mIsClose = true;
                showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
            }
        });
        powerAlertDialog.show(getSupportFragmentManager());

    }

    public void onPIRHallSetting(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, PIRHallSettingActivity.class));
    }


    public void onSave(View view) {
        if (isWindowLocked())
            return;
        if (mBind.radioBtnAdv.isChecked()) {
            if (advFragment.isValid()) {
                advFragment.saveParams();
            } else {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // DFU
    ///////////////////////////////////////////////////////////////////////////
    public void onChooseFirmwareFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (isUpgradeCompleted) {
            dialog.setMessage("DFU Successfully!\nPlease reconnect the device.");
        } else {
            dialog.setMessage("Opps!DFU Failed.\nPlease try again!");
        }
        dialog.setCancelGone();
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(() -> {
            isUpgrading = false;
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }


    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(DeviceInfoActivity.this, "Error:DFU Failed");
                MokoSupport.getInstance().disConnectBle();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuServicePir.BROADCAST_ACTION);
                abortAction.putExtra(DfuServicePir.EXTRA_ACTION, DfuServicePir.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            isUpgrading = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            XLog.w("onDfuCompleted...");
            isUpgradeCompleted = true;
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            String progress = String.format("Progress:%d%%", percent);
            XLog.i(progress);
            mDFUDialog.setMessage(progress);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            XLog.i("DFU Error:" + message);
        }
    };
}
