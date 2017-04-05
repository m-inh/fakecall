package com.uet.fakecall.fragment;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.uet.fakecall.MainActivity;
import com.uet.fakecall.R;
import com.uet.fakecall.activity.ScheduleCallActivity;

import java.io.IOException;
import java.io.InputStream;

public class FakeCallFragment extends Fragment{
    public static final String FAKE_NAME = "Fake name";
    public static final String FAKE_NUMBER = "Fake number";
    private static final String TAG = Fragment.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;

    private Context contextOfApplication;
    private EditText edtCallerName;
    private EditText edtCallerNumber;
    private ImageView ivLoadContact;
    private Button btnMakeCall;

    public FakeCallFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_fake_call,container,false);

        contextOfApplication = MainActivity.getContextOfApp();
        edtCallerName = (EditText) view.findViewById(R.id.edt_name_fake_call);
        edtCallerNumber = (EditText) view.findViewById(R.id.edt_phone_fake_call);
        ivLoadContact = (ImageView) view.findViewById(R.id.iv_load_contact_call);
        btnMakeCall = (Button) view.findViewById(R.id.btn_make_call);

        ivLoadContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                        REQUEST_CODE_PICK_CONTACTS);
            }
        });

        btnMakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( edtCallerNumber.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Number can't be empty!", Toast.LENGTH_SHORT).show();

                    return;
                }
                Intent intent = new Intent(getActivity(), ScheduleCallActivity.class);
                intent.putExtra(FAKE_NAME, edtCallerName.getText().toString());
                intent.putExtra(FAKE_NUMBER, edtCallerNumber.getText().toString());
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactNumber();
            retrieveContactName();
        }
    }

//    private void retrieveContactPhoto() {
//
//        Bitmap photo = null;
//
//        try {
//            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(contextOfApplication.getContentResolver(),
//                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));
//
//            if (inputStream != null) {
//                photo = BitmapFactory.decodeStream(inputStream);
////                ImageView imageView = (ImageView) findViewById(R.id.img_contact);
////                imageView.setImageBitmap(photo);
//            }
//
//            assert inputStream != null;
//            inputStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

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
        edtCallerNumber.setText(contactNumber);
    }

    private void retrieveContactName(){

        String contactName = null;
        Cursor cursor = contextOfApplication.getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        edtCallerName.setText(contactName);
    }


}
