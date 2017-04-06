package com.uet.fakecall.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uet.fakecall.R;
import com.uet.fakecall.fragment.FakeCallFragment;
import com.uet.fakecall.untils.CallLogUntilities;

import java.util.Locale;

public class FakeCallRingerActivity extends AppCompatActivity {

    private static final int INCOMING_CALL_NOTIFICATION = 1001;
    private static final int MISSED_CALL_NOTIFICATION = 1002;
    private static final String DURATION = "duration";
    private static final int NO_ANSWER = 1;
    private static final int COUNT_TIME_UPDATE = 2;

    private TextView callStatus;
    private TextView callDuration;
    private Button btnDeline;
    private Button btnAnswer;
    private Button btnEndCall;

    private RelativeLayout mainLayout;
    private RelativeLayout callActionButtonsLayout;

    private AudioManager audioManager;

    private long secs;
    private int duration;

    private Ringtone ringtone;
    private Vibrator vibrator;

    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private ContentResolver contentResolver;
    private MediaPlayer voicePlayer;
    private Resources resources;
    private int currentRingerMode;
    private int currentRingerVolume;
    private String contactImageString;
    private int currentMediaVolume;

    private boolean isAnswer = false;
    private String callNumber;
    private String callName;

    private Handler handlerCall = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NO_ANSWER:
//                    secs = -1;
                    FakeCallRingerActivity.this.finish();
                    break;
                case COUNT_TIME_UPDATE:
                    String dur = msg.getData().getString("CURRENT_TIME");
                    callDuration.setText(dur);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomming_call);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Window window = getWindow();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        callNumber = getContactNumber();
        callName = getContactName();

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);

        final Animation ringExpandAnimation = AnimationUtils.loadAnimation(this, R.anim.ring_expand);

        final Animation ringShrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.ring_shrink);

        //final Drawable bg2 = getDrawable(R.drawable.answered_bg);

        contentResolver = getContentResolver();
        resources = getResources();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Tag");
        currentRingerMode = audioManager.getRingerMode();
        currentRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        currentMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        callStatus = (TextView) findViewById(R.id.tv_call_status);
        callDuration = (TextView) findViewById(R.id.tv_call_duration);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        duration = extras.getInt(DURATION);

        callDuration.setVisibility(View.INVISIBLE);

        btnEndCall = (Button) findViewById(R.id.btn_deny_after_answer);
        btnEndCall.setVisibility(View.GONE);

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnDeline = (Button) findViewById(R.id.btn_deline);
        btnDeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                secs = -1;
                finish();
            }
        });

        btnAnswer = (Button) findViewById(R.id.btn_answer_call);
        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callStatus.setText("");
                callStatus.setVisibility(View.INVISIBLE);
                btnAnswer.setVisibility(View.GONE);
                btnDeline.setVisibility(View.GONE);
                btnEndCall.setVisibility(View.VISIBLE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                callDuration.setVisibility(View.VISIBLE);
                stopRinging();

                wakeLock.acquire();

                //// endcall when duration <= 0
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!isAnswer && duration >= 0) {
                            duration = duration - 1000;
                            long min = secs / 60;
                            long seconds = secs % 60;
                            secs++;

                            String dur = String.format(Locale.US, "%02d:%02d", min, seconds);

                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("CURRENT_TIME", dur);
                            msg.setData(bundle);
                            msg.what = COUNT_TIME_UPDATE;
                            msg.setTarget(handlerCall);

                            msg.sendToTarget();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!isAnswer){
                            Message msg = new Message();
                            msg.what = NO_ANSWER;
                            msg.setTarget(handlerCall);
                            msg.sendToTarget();
                        }
                    }
                }).start();

            }
        });

        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        wakeLock.setReferenceCounted(false);

        nBuilder.setSmallIcon(R.mipmap.ic_call);
        nBuilder.setOngoing(true);
        nBuilder.setContentTitle(callName);
        nBuilder.setColor(Color.rgb(4, 137, 209));
        nBuilder.setContentText(resources.getString(R.string.incoming_call));
        notificationManager.notify(INCOMING_CALL_NOTIFICATION, nBuilder.build());

        muteAll();

        TextView fakeName = (TextView) findViewById(R.id.tv_caller_name_fake_call_ring);
        TextView fakeNumber = (TextView) findViewById(R.id.tv_number_fake_call_ring);

        String callNumber = getContactNumber();
        String callName = getContactName();

        fakeName.setText(callName);
        fakeNumber.setText(callNumber);

        Animation animCallStatusPulse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.call_status_pulse);

        callStatus.startAnimation(animCallStatusPulse);

        Uri ringtoneURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneURI);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ringtone.play();

        long[] pattern = {1000, 1000, 1000, 1000, 1000};
        vibrator.vibrate(pattern, 0);

    }

    private String getContactNumber() {
        String contact = null;
        Intent myIntent = getIntent();
        Bundle mIntent = myIntent.getExtras();
        if (mIntent != null) {
            contact = mIntent.getString(FakeCallFragment.FAKE_NUMBER);
        }
        return contact;
    }

    private String getContactName() {
        String contactName = null;
        Intent myIntent = getIntent();
        Bundle mIntent = myIntent.getExtras();
        if (mIntent != null) {
            contactName = mIntent.getString(FakeCallFragment.FAKE_NAME);
        }
        return contactName;
    }

    private void muteAll() {

        audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);

    }

    private void unMuteAll() {

        audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

    }

    private void stopVoice() {
        if (voicePlayer != null && voicePlayer.isPlaying()) {
            voicePlayer.stop();
        }
    }

    private void stopRinging() {
        vibrator.cancel();
        ringtone.stop();
    }

    // adds a missed call to the log and shows a notification
    private void missedCall() {

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);

        nBuilder.setSmallIcon(android.R.drawable.stat_notify_missed_call);
        nBuilder.setContentTitle(callName);
        nBuilder.setContentText(resources.getString(R.string.missed_call));
        nBuilder.setColor(Color.rgb(4, 137, 209));
        nBuilder.setAutoCancel(true);

        Intent showCallLog = new Intent(Intent.ACTION_VIEW);

        showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, showCallLog, PendingIntent.FLAG_CANCEL_CURRENT);
        nBuilder.setContentIntent(pendingIntent);
        showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
        notificationManager.notify(MISSED_CALL_NOTIFICATION, nBuilder.build());

        CallLogUntilities.addCallToLog(contentResolver, callNumber, 0, CallLog.Calls.MISSED_TYPE, System.currentTimeMillis(), getApplicationContext());

    }

    private void incomingCall() {
        CallLogUntilities.addCallToLog(contentResolver, callNumber, secs, CallLog.Calls.INCOMING_TYPE, System.currentTimeMillis(), getApplicationContext());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopVoice();

        notificationManager.cancel(INCOMING_CALL_NOTIFICATION);

        if (secs > 0) {
            incomingCall();
        } else {
            missedCall();
        }

        wakeLock.release();
        audioManager.setRingerMode(currentRingerMode);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, currentRingerVolume, 0);
        stopRinging();
        unMuteAll();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentMediaVolume, 0);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }


}
