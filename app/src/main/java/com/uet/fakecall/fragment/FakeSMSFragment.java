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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.uet.fakecall.R;
import com.uet.fakecall.broadcast.FakeSMSReceiver;

import java.io.IOException;
import java.io.InputStream;

import static android.content.Context.ALARM_SERVICE;

public class FakeSMSFragment extends Fragment {
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
    private ImageView ivPhotoFakeSMS;
    private Bitmap photo;

    private String body;
    private String sender;
    private long timeMilis;

    public FakeSMSFragment() {}

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
        btnLoadContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                        REQUEST_CODE_PICK_CONTACTS);
            }
        });

        edtContentMess = (EditText) view.findViewById(R.id.edt_mess);

        btnMakeSMS = (Button) view.findViewById(R.id.btn_make_sms);
        btnMakeSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSMSerPhone.getText().toString().equals("")) {
                    Toast.makeText(contextOfApplication, "Number Phone cant be empty!!!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (edtContentMess.getText().toString().equals("")) {
                    Toast.makeText(contextOfApplication, "Mess cant be empty!!!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (edtTimePicker.getText().toString().equals("")) {
                    Toast.makeText(contextOfApplication, "Please enter time!", Toast.LENGTH_LONG).show();
                    return;
                }
                long timeScheduleAt = System.currentTimeMillis() + Long.parseLong(edtTimePicker.getText().toString()) * 1000;
                int fakeSMSID = (int) System.currentTimeMillis();

                Intent mIntent = new Intent(contextOfApplication, FakeSMSReceiver.class);
                mIntent.putExtra(NAME_FAKE, edtSMSerName.getText().toString());
                mIntent.putExtra(NUMBER_FAKE, edtSMSerPhone.getText().toString());
                mIntent.putExtra(MESS_FAKE, edtContentMess.getText().toString());
                mIntent.putExtra(FakeCallFragment.FAKE_PHOTO, photo);

                PendingIntent pi = PendingIntent.getBroadcast(contextOfApplication, fakeSMSID, mIntent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeScheduleAt, pi);

                checkAppAndScheduleSms(edtContentMess.getText().toString(), edtSMSerPhone.getText().toString(), timeScheduleAt);

                Toast.makeText(contextOfApplication, "Fake sms scheduled", Toast.LENGTH_SHORT).show();

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
                    writeSms(body, sender, timeMilis);
                }
            }
        }
    }

    private void checkAppAndScheduleSms(String body, String sender, long timeMilis) {

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
            writeSms(body, sender, timeMilis);
        }
    }

    //Write the sms
    private void writeSms(String message, String phoneNumber, long timeMilis) {

        //Put content values
        ContentValues values = new ContentValues();
        values.put("address", phoneNumber);
        values.put("date", timeMilis);
        values.put("body", message);
        values.put("read", 0);

        // insert sms to db
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getActivity().getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
        else getActivity().getContentResolver().insert(Uri.parse("content://sms/inbox"), values);

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


}
