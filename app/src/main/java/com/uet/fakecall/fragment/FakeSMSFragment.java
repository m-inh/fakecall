package com.uet.fakecall.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.uet.fakecall.R;
import com.uet.fakecall.broadcast.FakeSMSReceiver;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class FakeSMSFragment extends Fragment implements TimePickerDialog.OnTimeSetListener {
    private static final String NAME_FAKE = "name";
    private static final String NUMBER_FAKE = "phone";
    private static final String MESS_FAKE = "message";
    private static final int REQUEST_CODE_PICK_CONTACTS = 12;
    private static final String TAG = "FakeSMSFragment";

    private Uri uriContact;
    private String contactID;
    private Context contextOfApplication;

    private EditText edtSMSerName;
    private EditText edtSMSerPhone;
    private EditText edtTimePicker;
    private Button btnLoadContact;
    private Button btnMakeSMS;
    private EditText edtContentMess;

    private RadioGroup rgType;

    private ImageView ivPhotoFakeSMS;
    private Bitmap photo;

    private String typeSchedule;

    private String name;
    private String body;
    private String sender;
    private long timeMilis;

    public FakeSMSFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fakesms, container, false);

        contextOfApplication = getActivity().getApplicationContext();

        edtSMSerPhone = (EditText) view.findViewById(R.id.edt_phone_fake_sms);
        edtSMSerName = (EditText) view.findViewById(R.id.edt_name_fake_sms);
        edtTimePicker = (EditText) view.findViewById(R.id.edt_time_picker);
        btnLoadContact = (Button) view.findViewById(R.id.btn_load_contact_sms);
        ivPhotoFakeSMS = (ImageView) view.findViewById(R.id.iv_photo_fake_sms);

        View btnTimePicker = view.findViewById(R.id.btn_time_picker);

        edtTimePicker.setEnabled(false);

        rgType = (RadioGroup) view.findViewById(R.id.rg_type);

        // show time picker
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                int h = now.get(Calendar.HOUR_OF_DAY);
                int m = now.get(Calendar.MINUTE);
                int s = now.get(Calendar.SECOND);
                TimePickerDialog timePicker = TimePickerDialog.newInstance(FakeSMSFragment.this, h, m, s, true);

                timePicker.setMinTime(h, m, s);
                timePicker.show(getActivity().getFragmentManager(), "timepickerdialog");
            }
        });

        btnLoadContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                        REQUEST_CODE_PICK_CONTACTS);
            }
        });

        edtContentMess = (EditText) view.findViewById(R.id.edt_mess);

        rgType.setEnabled(true);
        RadioButton rbType = (RadioButton) view.findViewById(rgType.getChildAt(0).getId());
        rbType.setChecked(true);

        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                Log.d(TAG, checkedId + "");
            }
        });

        btnMakeSMS = (Button) view.findViewById(R.id.btn_make_sms);
        btnMakeSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSMSerPhone.getText().toString().equals("")) {
                    Toast.makeText(contextOfApplication, "Number phone can't be empty!!!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (edtContentMess.getText().toString().equals("")) {
                    Toast.makeText(contextOfApplication, "Message can't be empty!!!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (edtTimePicker.getText().toString().equals("")) {
                    Toast.makeText(contextOfApplication, "Please select time!", Toast.LENGTH_LONG).show();
                    return;
                }

                RadioButton rbType = (RadioButton) view.findViewById(rgType.getCheckedRadioButtonId());

                typeSchedule = rbType.getText().toString();

                checkAppAndScheduleSms(
                        edtSMSerName.getText().toString(),
                        edtContentMess.getText().toString(),
                        edtSMSerPhone.getText().toString(),
                        timeMilis);

                edtSMSerName.setText("");
                edtContentMess.setText("");
                edtSMSerPhone.setText("");
                edtTimePicker.setText("");
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == Activity.RESULT_OK) {
            uriContact = data.getData();

            retrieveContactNumber();
            retrieveContactName();
            retrieveContactPhoto();
        }

        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {

                final String myPackageName = getActivity().getPackageName();
                if (Telephony.Sms.getDefaultSmsPackage(getActivity()).equals(myPackageName)) {

                    //Write to the default sms app
                    writeSms(name, body, sender, timeMilis);
                }
            }
        }
    }

    private void checkAppAndScheduleSms(String name, String body, String sender, long timeMilis) {

        this.name = name;
        this.body = body;
        this.sender = sender;
        this.timeMilis = timeMilis;

        //Get my package name
        final String myPackageName = getActivity().getPackageName();

        //Check if my app is the default sms app
        if (!Telephony.Sms.getDefaultSmsPackage(getActivity()).equals(myPackageName)) {
            //Change the default sms app to my app
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getActivity().getPackageName());
            startActivityForResult(intent, 1);
        } else {
            //Write the sms
            writeSms(name, body, sender, timeMilis);
        }
    }

    //Write the sms
    private void writeSms(String nameSender, String message, String phoneNumber, long timeMilis) {

        // push notification
        int fakeSMSID = (int) System.currentTimeMillis();
        Intent mIntent = new Intent(contextOfApplication, FakeSMSReceiver.class);
        mIntent.putExtra(NAME_FAKE, nameSender);
        mIntent.putExtra(NUMBER_FAKE, phoneNumber);
        mIntent.putExtra(MESS_FAKE, message);
        mIntent.putExtra(FakeCallFragment.FAKE_PHOTO, photo);

        PendingIntent pi = PendingIntent.getBroadcast(contextOfApplication, fakeSMSID, mIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeMilis, pi);

        // write to db
        Uri dbUri;
        if (typeSchedule.equalsIgnoreCase("sent")) {
            // uri for sent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                dbUri = Telephony.Sms.Sent.CONTENT_URI;
            else dbUri = Uri.parse("content://sms/sent");
        } else {
            // uri for receive
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                dbUri = Telephony.Sms.Inbox.CONTENT_URI;
            else dbUri = Uri.parse("content://sms/inbox");
        }

        //Put content values
        ContentValues values = new ContentValues();
        values.put("address", phoneNumber);
        values.put("date", timeMilis);
        values.put("body", message);
        values.put("read", 0);

        // insert sms to db
        getActivity().getContentResolver().insert(dbUri, values);

        Toast.makeText(contextOfApplication, "Fake sms scheduled", Toast.LENGTH_SHORT).show();
    }

    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = contextOfApplication.getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = contextOfApplication.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();
        edtSMSerPhone.setText(contactNumber);
    }

    private void retrieveContactName() {

        String contactName = null;
        Cursor cursor = contextOfApplication.getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        edtSMSerName.setText(contactName);
    }

    private void retrieveContactPhoto() {

        photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contextOfApplication.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                if (photo != null) {
                    ivPhotoFakeSMS.setImageBitmap(photo);
                } else {
                    ivPhotoFakeSMS.setImageResource(R.mipmap.ic_user);
                }

            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (inputStream == null) {
                photo = null;
                ivPhotoFakeSMS.setImageResource(R.mipmap.ic_user);
            }
            assert inputStream != null;


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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
