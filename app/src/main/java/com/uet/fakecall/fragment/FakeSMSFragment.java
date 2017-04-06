package com.uet.fakecall.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uet.fakecall.R;
import com.uet.fakecall.broadcast.FakeSMSReceiver;

import java.text.DateFormat;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class FakeSMSFragment extends Fragment {
    private static final String NAME_FAKE = "name";
    private static final String NUMBER_FAKE = "phone";
    private static final String MESS_FAKE = "message";
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;
    private Context contextOfApplication;

    private EditText edtSMSerName;
    private EditText edtSMSerPhone;
    private Button btnLoadContact;
    private Button btnMakeSMS;
    private EditText edtContentMess;
    private TextView tvDisplayTimeFakeSMS;

    public FakeSMSFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fakesms, container, false);

        contextOfApplication = getActivity().getApplicationContext();

        edtSMSerPhone = (EditText) view.findViewById(R.id.edt_phone_fake_sms);
        edtSMSerName = (EditText) view.findViewById(R.id.edt_name_fake_sms);
        btnLoadContact = (Button) view.findViewById(R.id.btn_load_contact_sms);
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

                Intent mIntent = new Intent(contextOfApplication, FakeSMSReceiver.class);
                mIntent.putExtra(NAME_FAKE, edtSMSerName.getText().toString());
                mIntent.putExtra(NUMBER_FAKE, edtSMSerPhone.getText().toString());
                mIntent.putExtra(MESS_FAKE, edtContentMess.getText().toString());

                final int fakeSMSID = (int) System.currentTimeMillis();

                PendingIntent pi = PendingIntent.getBroadcast(contextOfApplication, fakeSMSID, mIntent, PendingIntent.FLAG_ONE_SHOT);

                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pi);

                Toast.makeText(contextOfApplication, "Fake SMS Scheduled", Toast.LENGTH_SHORT).show();
            }
        });

        tvDisplayTimeFakeSMS = (TextView) view.findViewById(R.id.tv_display_time_reciever_mess);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        tvDisplayTimeFakeSMS.setText(currentDateTimeString);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == Activity.RESULT_OK) {
            uriContact = data.getData();

            retrieveContactNumber();
            retrieveContactName();
        }
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


}
