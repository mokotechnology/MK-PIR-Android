package com.moko.pirsensor.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.pirsensor.R;
import com.moko.pirsensor.activity.DeviceInfoActivity;
import com.moko.pirsensor.databinding.FragmentSettingBinding;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    private FragmentSettingBinding mBind;
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
        mBind = FragmentSettingBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
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


    public void setConnectable(byte[] value) {
        int connectable = MokoUtils.toInt(value);
        boolean isConnected = connectable == 0;
        if (isConnected) {
            mBind.ivConnectable.setImageResource(R.drawable.connectable_checked);
        } else {
            mBind.ivConnectable.setImageResource(R.drawable.connectable_unchecked);
        }
    }

    public void setClose() {
        mBind.ivPower.setImageResource(R.drawable.connectable_unchecked);
    }


    public void setButtonPower(boolean enable) {
        mBind.ivButtonPower.setImageResource(enable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked);
    }
}
