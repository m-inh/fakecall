package com.uet.fakecall.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.uet.fakecall.MainActivity;
import com.uet.fakecall.R;

public class SettingActivity extends AppCompatActivity {

    private static final ComponentName LAUNCHER_COMPONENT_NAME = new ComponentName("com.uet.fakecall", "com.uet.fakecall.Launcher");
    private static final String NUMBER_TO_DIAL = "numberToDial";
    private static final String REQUEST_STRING = "111";

    private Toolbar toolbar;
    private SharedPreferences appSettings;
    private SharedPreferences.Editor preferencesEditor;
    private Switch swShowIconSwitch;
    private EditText edtNumberToDialInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle("Settings");

        swShowIconSwitch = (Switch) findViewById(R.id.show_icon_switch);
        edtNumberToDialInput = (EditText) findViewById(R.id.edt_number_to_dial_input);
        edtNumberToDialInput.setVisibility(View.GONE);

        appSettings = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        preferencesEditor = appSettings.edit();

        boolean showIcon = appSettings.getBoolean("showIcon", true);
        String numberToDial = appSettings.getString(NUMBER_TO_DIAL, REQUEST_STRING);

        swShowIconSwitch.setChecked(showIcon);
        edtNumberToDialInput.setEnabled(!showIcon);
        edtNumberToDialInput.setText(numberToDial);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent mIntent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(mIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickShowIcon(View view) {

        PackageManager packageManager = getPackageManager();
        boolean showIcon = swShowIconSwitch.isChecked();

        if (!showIcon) {
            packageManager.setComponentEnabledSetting(LAUNCHER_COMPONENT_NAME, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(LAUNCHER_COMPONENT_NAME, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        Toast.makeText(this, "You may need to restart your launcher for action to take effect", Toast.LENGTH_SHORT).show();

        preferencesEditor.putBoolean("showIcon", showIcon);
        edtNumberToDialInput.setEnabled(!swShowIconSwitch.isChecked());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String numberToDial = edtNumberToDialInput.getText().toString();
        preferencesEditor.putString("numberToDial", numberToDial);
        preferencesEditor.apply();
    }
}
