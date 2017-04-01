package com.uet.fakecall.untils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;

import com.uet.fakecall.MainActivity;

public class CallLogUntilities {
    public static void addCallToLog(ContentResolver contentResolver, String number, long duration, int type, long time) {
        Context context = MainActivity.getContextOfApp();

        ContentValues values = new ContentValues();

        values.put(CallLog.Calls.NUMBER, number);

        values.put(CallLog.Calls.DATE, time);

        values.put(CallLog.Calls.DURATION, duration);

        values.put(CallLog.Calls.TYPE, type);

        values.put(CallLog.Calls.NEW, 1);

        values.put(CallLog.Calls.CACHED_NAME, "");

        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);

        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");

        contentResolver.insert(CallLog.Calls.CONTENT_URI, values);

    }

}
