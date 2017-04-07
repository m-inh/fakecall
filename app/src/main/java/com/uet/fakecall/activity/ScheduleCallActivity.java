package com.uet.fakecall.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.uet.fakecall.R;
import com.uet.fakecall.fragment.FakeCallFragment;
import com.uet.fakecall.fragment.FakeSMSFragment;
import com.uet.fakecall.untils.CallLogUntilities;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;


public class ScheduleCallActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final int FILE_SELECT = 1002;
    private static final int DURATION = 63;
    private static final String TAG = "ScheduleCallActivity";

    private RadioGroup rdCallType;
    private Button btnSetCallSchedule;
    private Bitmap photo;

    private EditText edtTimePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_call);

        rdCallType = (RadioGroup) findViewById(R.id.callTypeRadioGroup);
        btnSetCallSchedule = (Button) findViewById(R.id.btn_set_call_schedule);

        View btnTimePicker = findViewById(R.id.btn_time_picker);
        edtTimePicker = (EditText) findViewById(R.id.edt_time_picker);

        edtTimePicker.setEnabled(false);

        // show time picker
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                int h = now.get(Calendar.HOUR_OF_DAY);
                int m = now.get(Calendar.MINUTE);
                int s = now.get(Calendar.SECOND);
                TimePickerDialog timePicker = TimePickerDialog.newInstance(ScheduleCallActivity.this, h, m, s, true);

                timePicker.setMinTime(h, m, s);
                timePicker.show(getFragmentManager(), "timepickerdialog");
            }
        });

        btnSetCallSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = getIntent().getExtras();
                String nameCaller = extras.getString(FakeCallFragment.FAKE_NAME);
                String numberPhoneCaller = extras.getString(FakeCallFragment.FAKE_NUMBER);
                photo = (Bitmap) getIntent().getParcelableExtra(FakeCallFragment.FAKE_PHOTO);

                EditText edtDurationInput = (EditText) findViewById(R.id.edt_call_duration_input);
                EditText edtTimePicker = (EditText) findViewById(R.id.edt_time_picker);

                if (edtTimePicker.getText().toString().equals("")) {
                    Toast.makeText(ScheduleCallActivity.this, "Please set Time Schedule", Toast.LENGTH_LONG).show();
                    return;
                }

                if (edtDurationInput.getText().toString().equals("")) {
                    Toast.makeText(ScheduleCallActivity.this, "Please set call duration", Toast.LENGTH_LONG).show();
                    return;
                }

                String duration = edtDurationInput.getText().toString();

                if (nameCaller.equals("")) {
                    nameCaller = getResources().getString(R.string.unknown);
                }
                if (duration.equals("")) {
                    duration = Integer.toString(DURATION);
                }

                RadioButton radioButton = (RadioButton) findViewById(rdCallType.getCheckedRadioButtonId());

                int radioButtonIndex = rdCallType.indexOfChild(radioButton);

                if (radioButtonIndex == 0) {
                    Intent intent = new Intent(ScheduleCallActivity.this, FakeCallRingerActivity.class);

                    intent.putExtra(FakeCallFragment.FAKE_NAME, nameCaller);
                    intent.putExtra(FakeCallFragment.FAKE_NUMBER, "Mobile " + numberPhoneCaller);
                    intent.putExtra("duration", Integer.parseInt(duration) * 1000);
                    intent.putExtra(FakeCallFragment.FAKE_PHOTO, photo);

                    final int fakeCallID = (int) System.currentTimeMillis();

                    PendingIntent pendingIntent = PendingIntent.getActivity(ScheduleCallActivity.this, fakeCallID, intent, PendingIntent.FLAG_ONE_SHOT);

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeMilis, pendingIntent);

                    Toast.makeText(ScheduleCallActivity.this, "Fake call scheduled", Toast.LENGTH_SHORT).show();

                    finish();
                } else if (radioButtonIndex == 1) {
                    CallLogUntilities.addCallToLog(getContentResolver(),
                            nameCaller, Integer.parseInt(duration),
                            CallLog.Calls.OUTGOING_TYPE, timeMilis,
                            getApplicationContext());

                    Toast.makeText(getApplicationContext(), "Fake outgoing call added to log", Toast.LENGTH_SHORT).show();
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

                break;

        }

    }

    private long timeMilis;

    @Override
    public void onTimeSet(TimePickerDialog view, int h, int m, int s) {
        Calendar now = Calendar.getInstance();

        Log.d(TAG, now.getTime().toString());

        now.set(now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DATE),
                h,
                m);
        now.getTimeInMillis();

        Log.d(TAG, now.getTime().toString());

        String timeScheduleAt = String.format(
                "%02d:%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND));
        edtTimePicker.setText(timeScheduleAt);

        timeMilis = now.getTimeInMillis();
    }
}
