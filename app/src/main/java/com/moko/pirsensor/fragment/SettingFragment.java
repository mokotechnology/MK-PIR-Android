package com.moko.pirsensor.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moko.pirsensor.R;
import com.moko.pirsensor.activity.DeviceInfoActivity;
import com.moko.pirsensor.dialog.AlertMessageDialog;
import com.moko.pirsensor.dialog.ModifyPasswordDialog;
import com.moko.ble.lib.utils.MokoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";
    @BindView(R.id.iv_connectable)
    ImageView ivConnectable;
    @BindView(R.id.iv_power)
    ImageView ivPower;
    @BindView(R.id.iv_button_power)
    ImageView ivButtonPower;

    private DeviceInfoActivity activity;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.rl_pir_hall, R.id.rl_update_firmware, R.id.rl_reset_factory, R.id.iv_connectable,
            R.id.iv_power, R.id.iv_button_power, R.id.rl_modify_password})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_modify_password:
                final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog();
                modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
                    @Override
                    public void onEnsureClicked(String password) {
                        activity.modifyPassword(password);
                    }
                });
                modifyPasswordDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.rl_update_firmware:
                activity.chooseFirmwareFile();
                break;
            case R.id.rl_reset_factory:
                final AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
                resetDeviceDialog.setMessage("Are you sure to reset the device？");
                resetDeviceDialog.setConfirm(R.string.ok);
                resetDeviceDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                    @Override
                    public void onClick() {
                        activity.resetDevice();
                    }
                });
                resetDeviceDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.iv_connectable:
                if (isConneacted) {
                    final AlertMessageDialog connectAlertDialog = new AlertMessageDialog();
                    connectAlertDialog.setMessage("Are you sure to set the device non-connectable？");
                    connectAlertDialog.setConfirm(R.string.ok);
                    connectAlertDialog.setOnAlertConfirmListener(() -> {
                        activity.setConnectable(false);
                    });
                    connectAlertDialog.show(activity.getSupportFragmentManager());
                } else {
                    activity.setConnectable(true);
                }
                break;
            case R.id.iv_power:
                final AlertMessageDialog powerAlertDialog = new AlertMessageDialog();
                powerAlertDialog.setMessage("Are you sure to turn off the device?Please make sure the device has a button to turn on!");
                powerAlertDialog.setConfirm(R.string.ok);
                powerAlertDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                    @Override
                    public void onClick() {
                        activity.setClose();
                    }
                });
                powerAlertDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.iv_button_power:
                if (enableButtonPower) {
                    final AlertMessageDialog buttonPowerAlertDialog = new AlertMessageDialog();
                    buttonPowerAlertDialog.setMessage("If disable Button Power OFF, then it  cannot power off beacon by press button operation.");
                    buttonPowerAlertDialog.setConfirm(R.string.ok);
                    buttonPowerAlertDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                        @Override
                        public void onClick() {
                            activity.setButtonPower(false);
                        }
                    });
                    buttonPowerAlertDialog.show(activity.getSupportFragmentManager());
                } else {
                    activity.setButtonPower(true);
                }
                break;
            case R.id.rl_pir_hall:
                activity.onPIRHallSetting();
                break;
        }
    }

    boolean isConneacted;

    public void setConnectable(byte[] value) {
        int connectable = MokoUtils.toInt(value);
        isConneacted = connectable == 0;
        if (isConneacted) {
            ivConnectable.setImageResource(R.drawable.connectable_checked);
        } else {
            ivConnectable.setImageResource(R.drawable.connectable_unchecked);
        }
    }

    public void setClose() {
        ivPower.setImageResource(R.drawable.connectable_unchecked);
    }

    private boolean enableButtonPower;

    public void setButtonPower(boolean enable) {
        this.enableButtonPower = enable;
        ivButtonPower.setImageResource(enable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked);
    }
}
