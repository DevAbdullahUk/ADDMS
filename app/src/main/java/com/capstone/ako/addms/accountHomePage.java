package com.capstone.ako.addms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class accountHomePage extends AppCompatActivity {
    // elements on the screen
    TextView userName_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_home_page);
        userName_tv = findViewById(R.id.userName);
        // Save the login data
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(this);
        userName_tv.setText( login_Data.getString("userName","0"));

    }

    public void account(View view) {
        startActivity(new Intent(accountHomePage.this, accountScreen.class));
    }


    public void drivingScreen(View view) {
        startActivity(new Intent(accountHomePage.this, startDriving.class));
    }

    public void logout(View view) {
        // Save the login data
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor userName_Pref = login_Data.edit();

        userName_Pref.putString("userName", null);
        userName_Pref.commit(); // close editor
        startActivity(new Intent(accountHomePage.this, Login.class));
        finish();
    }
}


