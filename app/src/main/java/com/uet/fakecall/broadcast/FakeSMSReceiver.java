package com.uet.fakecall.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.uet.fakecall.R;

public class FakeSMSReceiver extends BroadcastReceiver {
    private static final String NAME_FAKE = "name";
    private static final String NUMBER_FAKE = "phone";
    private static final String MESS_FAKE = "message";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context);

        String name = intent.getStringExtra(NAME_FAKE);
        String numberphone = intent.getStringExtra(NUMBER_FAKE);
        String message = intent.getStringExtra(MESS_FAKE);

        Intent showSMS = new Intent(Intent.ACTION_MAIN);
        showSMS.addCategory(Intent.CATEGORY_DEFAULT);
        showSMS.setType("vnd.android-dir/mms-sms");

        PendingIntent showSMSIntent = PendingIntent.getActivity(context, 0, showSMS, PendingIntent.FLAG_CANCEL_CURRENT);

        if (name == null) {
            notifyBuilder.setContentTitle(numberphone);
        } else {
            notifyBuilder.setContentTitle(name);
        }

        notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
        notifyBuilder.setSmallIcon(R.mipmap.sms, 1);
        notifyBuilder.setContentText(message);
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notifyBuilder.setColor(Color.rgb(0, 172, 193));

        notifyBuilder.addAction(R.mipmap.ic_reply, "Reply", showSMSIntent);
        notifyBuilder.addAction(R.mipmap.ic_mark_read, "Read", showSMSIntent);
        notifyBuilder.addAction(R.mipmap.ic_call, "Call", showSMSIntent);

        notifyBuilder.setContentIntent(showSMSIntent);

        notificationManager.notify(2001, notifyBuilder.build());
    }
}
