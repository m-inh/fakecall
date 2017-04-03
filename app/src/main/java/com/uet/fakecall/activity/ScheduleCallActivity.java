package com.uet.fakecall.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.uet.fakecall.R;
import com.uet.fakecall.fragment.FakeCallFragment;
import com.uet.fakecall.untils.CallLogUntilities;

import java.util.Date;
import java.util.Locale;


public class ScheduleCallActivity extends AppCompatActivity {

    private static final int FILE_SELECT = 1002;
    private static final int HANA_UP_AFTER = 15;
    private static final int DURATION = 63;


    private RadioGroup rdCallType;
    private Button btnSetCallSchedule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_call);

        rdCallType = (RadioGroup) findViewById(R.id.callTypeRadioGroup);
        btnSetCallSchedule = (Button) findViewById(R.id.btn_set_call_schedule);

        btnSetCallSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = getIntent().getExtras();
                String nameCaller = extras.getString(FakeCallFragment.FAKE_NAME);
                String numberPhoneCaller = extras.getString(FakeCallFragment.FAKE_NUMBER);

                EditText edtDurationInput = (EditText)findViewById(R.id.edt_call_duration_input);
                EditText edtHangUpAfterInput = (EditText)findViewById(R.id.hangup_after_input);
                EditText edtTimePicker = (EditText)  findViewById(R.id.edt_time_picker);

                if(edtTimePicker.getText().toString().equals("")){
                    Toast.makeText(ScheduleCallActivity.this, "Please set Time Schedule", Toast.LENGTH_LONG).show();
                    return;
                }

                int timeSchedule = Integer.parseInt(String.valueOf(edtTimePicker.getText().toString()));

                String duration = edtDurationInput.getText().toString();
                String hangUpAfter = edtHangUpAfterInput.getText().toString();

                if(nameCaller.equals("")){
                    nameCaller = getResources().getString(R.string.unknown);
                }
                if (duration.equals("")) {
                    duration = Integer.toString(DURATION);
                }
                if (hangUpAfter.equals("")) {
                    hangUpAfter = Integer.toString(HANA_UP_AFTER);
                }

                RadioButton radioButton = (RadioButton)findViewById(rdCallType.getCheckedRadioButtonId());

                int radioButtonIndex = rdCallType.indexOfChild(radioButton);

                //ContentResolver contentResolver = getContentResolver();

                if (radioButtonIndex == 0) {
                    Intent intent = new Intent(ScheduleCallActivity.this, FakeCallRingerActivity.class);

                    intent.putExtra(FakeCallFragment.FAKE_NAME, nameCaller);
                    intent.putExtra(FakeCallFragment.FAKE_NUMBER, "Mobile " + numberPhoneCaller);
                    intent.putExtra("duration", Integer.parseInt(duration));
                    intent.putExtra("hangUpAfter", Integer.parseInt(hangUpAfter));

                    final int fakeCallID = (int) System.currentTimeMillis();

                    PendingIntent pendingIntent = PendingIntent.getActivity(ScheduleCallActivity.this, fakeCallID, intent, PendingIntent.FLAG_ONE_SHOT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + timeSchedule  , pendingIntent);

                    Toast.makeText(ScheduleCallActivity.this, "Fake call scheduled", Toast.LENGTH_SHORT).show();

                    finish();
                } else if (radioButtonIndex == 1) {
                    CallLogUntilities.addCallToLog(getContentResolver(), nameCaller, Integer.parseInt(duration), CallLog.Calls.OUTGOING_TYPE, System.currentTimeMillis()+ timeSchedule);
                    Toast.makeText(getApplicationContext(), "Fake outgoing call added to log", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (radioButtonIndex == 2) {
                    CallLogUntilities.addCallToLog(getContentResolver(), numberPhoneCaller, 0, CallLog.Calls.MISSED_TYPE, System.currentTimeMillis() + timeSchedule);
                    Toast.makeText(getApplicationContext(), "Fake missed call added to log", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {

            case FILE_SELECT:

//                voice = data.getDataString();
//
//                voiceInput.setText(voice);

                break;

        }

    }
}
