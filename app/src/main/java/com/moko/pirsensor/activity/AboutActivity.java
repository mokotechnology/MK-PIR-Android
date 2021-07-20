package com.moko.pirsensor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.moko.pirsensor.BaseApplication;
import com.moko.pirsensor.R;
import com.moko.pirsensor.utils.ToastUtils;
import com.moko.pirsensor.utils.Utils;

import java.io.File;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AboutActivity extends BaseActivity {
    @BindView(R.id.app_version)
    TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        appVersion.setText(String.format("Version:%s", Utils.getVersionInfo(this)));
    }

    @OnClick({R.id.tv_back, R.id.tv_company_website, R.id.tv_feedback_log})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_company_website:
                Uri uri = Uri.parse("https://" + getString(R.string.company_website));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.tv_feedback_log:
                File trackerLog = new File(BaseApplication.PATH_LOGCAT + File.separator + "PIRMotionSensor.txt");
                File trackerLogBak = new File(BaseApplication.PATH_LOGCAT + File.separator + "PIRMotionSensor.txt.bak");
                File trackerCrashLog = new File(BaseApplication.PATH_LOGCAT + File.separator + "crash_log.txt");
                if (!trackerLog.exists() || !trackerLog.canRead()) {
                    ToastUtils.showToast(this, "File is not exists!");
                    return;
                }
                String address = "Development@mokotechnology.com";
                StringBuilder mailContent = new StringBuilder("PIRMotionSensor_");
                Calendar calendar = Calendar.getInstance();
                String date = Utils.calendar2strDate(calendar, "yyyyMMdd");
                mailContent.append(date);
                String title = mailContent.toString();
                if ((!trackerLogBak.exists() || !trackerLogBak.canRead())
                        && (!trackerCrashLog.exists() || !trackerCrashLog.canRead())) {
                    Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog);
                } else if (!trackerCrashLog.exists() || !trackerCrashLog.canRead()) {
                    Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerLogBak);
                } else if (!trackerLogBak.exists() || !trackerLogBak.canRead()) {
                    Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerCrashLog);
                } else {
                    Utils.sendEmail(this, address, "", title, "Choose Email Client", trackerLog, trackerLogBak, trackerCrashLog);
                }
                break;
        }
    }
}
