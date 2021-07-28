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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.moko.pirsensor.dialog.AlertMessageDialog;
import com.moko.pirsensor.dialog.LoadingMessageDialog;
import com.moko.pirsensor.fragment.AdvFragment;
import com.moko.pirsensor.fragment.DeviceFragment;
import com.moko.pirsensor.fragment.SettingFragment;
import com.moko.pirsensor.service.DfuService;
import com.moko.pirsensor.utils.FileUtils;
import com.moko.pirsensor.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.IdRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;
    @BindView(R.id.rg_options)
    RadioGroup rgOptions;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_save)
    ImageView ivSave;
    @BindView(R.id.radioBtn_adv)
    RadioButton radioBtnAdv;
    @BindView(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    private FragmentManager fragmentManager;
    private AdvFragment advFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        fragmentManager = getFragmentManager();
        initFragment();
        rgOptions.setOnCheckedChangeListener(this);
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

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                if (mIsClose)
                    return;
                if (MokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        tvTitle.postDelayed(() -> {
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
                                settingFragment.setConnectable(value);
                            }
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                        }
                        break;
                    case CHAR_PASSWORD:
                        if (2 == (MokoUtils.toInt(value))) {
                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
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
                                        boolean enable = value[4] == 1;
                                        settingFragment.setButtonPower(enable);
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
                    starter.start(this, DfuService.class);
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

    @OnClick({R.id.tv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
        }
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
        ivSave.setVisibility(View.VISIBLE);
        if (advFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(advFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.advertisement_title));
    }

    private void showSettingFragment() {
        ivSave.setVisibility(View.GONE);
        if (settingFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(advFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        ivSave.setVisibility(View.GONE);
        if (deviceFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(advFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioBtn_adv:
                showAdvFragment();
                getAdvInfo();
                break;
            case R.id.radioBtn_setting:
                showSettingFragment();
                getDeviceInfo();
                break;
            case R.id.radioBtn_device:
                showDeviceFragment();
                getSystemInfo();
                break;
        }
    }


    public void modifyPassword(String password) {
        if (isWindowLocked())
            return;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setPassword(password));
    }

    public void resetDevice() {
        if (isWindowLocked())
            return;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice(mPassword));
    }


    public void setConnectable(boolean isConneacted) {
        if (isWindowLocked())
            return;
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setConnectable(isConneacted));
        orderTasks.add(OrderTaskAssembler.getConnectable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setButtonPower(boolean enable) {
        if (isWindowLocked())
            return;
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setButtonPower(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getButtonPower());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setClose() {
        if (isWindowLocked())
            return;
        mIsClose = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
    }

    public void onPIRHallSetting() {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, PIRHallSettingActivity.class));
    }


    public void onSave(View view) {
        if (isWindowLocked())
            return;
        if (radioBtnAdv.isChecked()) {
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
    public void chooseFirmwareFile() {
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
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
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
