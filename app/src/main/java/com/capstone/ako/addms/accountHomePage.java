package com.capstone.ako.addms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class accountHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_home_page);
    }

    public void account(View view) {
        startActivity(new Intent(accountHomePage.this, accountScreen.class));
    }


    public void drivingScreen(View view) {
        startActivity(new Intent(accountHomePage.this, startDriving.class));
    }
}
