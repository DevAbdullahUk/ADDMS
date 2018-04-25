package com.capstone.ako.addms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class accountHomePage extends AppCompatActivity {
    // elements on the screen
    TextView userName_tv, alerts, avgSpeed, tripTime, distance;

    //Firebase
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_home_page);
        userName_tv = findViewById(R.id.userName);
        alerts = findViewById(R.id.txt_Alerts);
        avgSpeed = findViewById(R.id.txt_AvgSpeed);
        tripTime = findViewById(R.id.txt_Time);
        distance = findViewById(R.id.txt_Distance);
        // Save the login data
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(this);
        userName_tv.setText( login_Data.getString("userName","0"));

        // FirebaseFirestore object
        db = FirebaseFirestore.getInstance();


        db.collection("MobToWeb").document(login_Data.getString("userName","0")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {

                    alerts.setText(documentSnapshot.getData().get("Alerts").toString());
                    avgSpeed.setText(documentSnapshot.getData().get("AverageSpeed").toString()+" \nkm/h");
                    distance.setText(documentSnapshot.getData().get("Distance").toString()+" \nKm");
                    tripTime.setText(documentSnapshot.getData().get("Time").toString()+ " \nmin");

                } catch (Exception e) {
                    System.out.println(e);
                    Toast.makeText(accountHomePage.this, "No available trip history ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void account(View view) {
        finish();
        startActivity(new Intent(accountHomePage.this, accountScreen.class));
    }


    public void drivingScreen(View view) {
        finish();
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


