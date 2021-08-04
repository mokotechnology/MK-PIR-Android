package com.moko.pirsensor.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.pirsensor.AppConstants;
import com.moko.pirsensor.R;
import com.moko.pirsensor.dialog.LoadingMessageDialog;
import com.moko.pirsensor.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class PIRHallSettingActivity extends BaseActivity {


    @BindView(R.id.tv_pir_status)
    TextView tvPirStatus;
    @BindView(R.id.npv_pir_sensitivity)
    NumberPickerView npvPirSensitivity;
    @BindView(R.id.npv_pir_delay)
    NumberPickerView npvPirDelay;
    @BindView(R.id.tv_update_date)
    TextView tvUpdateDate;
    @BindView(R.id.tv_door_status)
    TextView tvDoorStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pir_hall);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        npvPirSensitivity.setMaxValue(2);
        npvPirSensitivity.setMinValue(0);
        npvPirDelay.setMaxValue(2);
        npvPirDelay.setMinValue(0);
        MokoSupport.getInstance().enableDoorStateNotify();
        showSyncingProgressDialog();
        tvPirStatus.postDelayed(() -> {
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getPIRSensitivity());
            orderTasks.add(OrderTaskAssembler.getPIRDelay());
            orderTasks.add(OrderTaskAssembler.getTime());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 500);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_HALL_STATUS:
                        int doorStatus = value[0] & 0xFF;
                        int pirStatus = value[1] & 0xFF;
                        tvDoorStatus.setText(doorStatus == 1 ? "Open" : "Close");
                        tvPirStatus.setText(pirStatus == 1 ? "Motion detected" : "Motion not detected");
                        break;
                }
            }
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
                    case CHAR_PARAMS:
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[3] & 0xFF;
                            switch (configKeyEnum) {
                                case GET_PIR_SENSITIVITY:
                                    if (length > 0) {
                                        int sensitivity = value[4] & 0xFF;
                                        npvPirSensitivity.setValue(sensitivity);
                                    }
                                    break;
                                case GET_PIR_DELAY:
                                    if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                                        if (length > 0) {
                                            int delay = value[4] & 0xFF;
                                            npvPirDelay.setValue(delay);
                                        }
                                    }
                                    if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                                        ToastUtils.showToast(PIRHallSettingActivity.this, "Success!");
                                    }
                                    break;
                                case GET_TIME:
                                    if (length >= 6) {
                                        int year = value[4] & 0xFF;
                                        int month = value[5] & 0xFF;
                                        int day = value[6] & 0xFF;
                                        int hour = value[7] & 0xFF;
                                        int minute = value[8] & 0xFF;
                                        int second = value[9] & 0xFF;
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(Calendar.YEAR, 2000 + year);
                                        calendar.set(Calendar.MONTH, month - 1);
                                        calendar.set(Calendar.DAY_OF_MONTH, day);
                                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                                        calendar.set(Calendar.MINUTE, minute);
                                        calendar.set(Calendar.SECOND, second);
                                        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS, Locale.US);
                                        String time = sdf.format(calendar.getTime());
                                        tvUpdateDate.setText(time);
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        MokoSupport.getInstance().disableDoorStateNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onSyncTime(View view) {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(
                OrderTaskAssembler.setTime(),
                OrderTaskAssembler.getTime());
    }

    public void onSave(View view) {
        int sensitivity = npvPirSensitivity.getValue();
        int delay = npvPirDelay.getValue();
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(
                OrderTaskAssembler.setPIRSensitivity(sensitivity),
                OrderTaskAssembler.setPIRDelay(delay));
    }
}
