package com.capstone.ako.addms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemClock;
import android.widget.Chronometer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class startDriving extends AppCompatActivity implements LocationListener {
    // Components form the XML
    TextView elapsedTime, speedLimit, distanceCovered, currentSpeed;
    LinearLayout lay;

    //Firebase
    FirebaseFirestore db;

    // for the GPS connection
    protected LocationManager locationManager;

    Location oldLocation;
    static String theAddress, userName;
    String speedList[];
    boolean firstRun = true;
    double newDistance = 0;
    int total = 0;
    int alerts = 0;
    Chronometer c;
    List<Double> myList = new ArrayList<>();
    DecimalFormat f = new DecimalFormat("###.#");
    // create and object of type Verification to save data to the sql
    Verification verification = new Verification(this);
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_driving);

        //Keep the screen On (Awake)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Firebase Firestore object
        db = FirebaseFirestore.getInstance();

        // xml elements
        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        speedLimit = (TextView) findViewById(R.id.speedLimit);
        distanceCovered = (TextView) findViewById(R.id.distanceCoverd);
        c = (Chronometer) findViewById(R.id.ch);
        lay = (LinearLayout) findViewById(R.id.main_layout);
        // initialize resources
        speedList = getResources().getStringArray(R.array.speed100);
        // initialize the locationManager object and set the GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else  {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        // Set the user name from Preferences
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(this);
        userName = login_Data.getString("userName","0");

        // set chronometer listener and start it
        c.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {

                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                total = (h*60)+m;
                cArg.setText(total + "");
            }
        });
        c.start();


        // set onTouch listener to the main screen to detect touch activities
        lay.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(getBaseContext(), Alert.class);
                intent.putExtra("ID", "PLEASE DON'T USE YOUR PHONE WHILE DRIVING!");
                startActivity(intent);
                alerts++;
                return false;
            }
        });

        // check if driver is over speed limit every 60s using check_Limit()
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                   check_Limit();
            }
        };

        Timer timer = new Timer();
        timer.schedule(t, 60000, 60000);
    }

        /*
    when the Location is Changed (new Latitude and Longitude) this little fucker will be called!
     */

    @Override
    public void onLocationChanged(Location location) {
        if(Double.parseDouble(f.format(newDistance/1000)) != 0.0) {
        if (!firstRun) { // calculate the new distance
            newDistance = location.distanceTo(oldLocation) + newDistance;
            distanceCovered.setText(f.format(newDistance/1000));
        }
        currentSpeed.setText(Math.round(3.6*location.getSpeed()) + "");
        if(myList.size() < 10){
            myList.add(Double.parseDouble(currentSpeed.getText().toString()));
            if(myList.size() == 10){
                check_Speed();
            }
        }
        else{
            myList.clear();
        }
        theAddress = get_theAddress(location);
        firstRun = false;
        speedLimit.setText(get_speedLimit() + "");
        oldLocation = location;

            //Send data to firebase
            Map<String, Object> dataToSave = new HashMap<>();

            // if statement to check if location is null which means the driver finished the trip
            if (location != null) {
                dataToSave.put("Speed", get_speedLimit());
            } else {
                double timeHr = total * 0.0166667;
                dataToSave.put("AverageSpeed", (Double.parseDouble(f.format(newDistance / 1000))) / timeHr);
            }
            dataToSave.put("Time", total);
            dataToSave.put("Alerts", alerts);
            dataToSave.put("Distance", distanceCovered.getText().toString());
            db.collection("MobToWeb").document(userName).set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Hi", "Saved");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Hi", "Not Saved");
                }
            });
        }
    }

    @Override
    public void onProviderDisabled(String provider) {/* I wont be using the sh*t for now*/}

    @Override
    public void onProviderEnabled(String provider) {/* I wont be using the sh*t for now*/}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {/* I wont be using the sh*t for now*/}


    public void home(View view) {
        onLocationChanged(null);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        verification.saveTripHistoy(alerts, Double.parseDouble(f.format(newDistance/1000)), total,75,userName);
        startActivity(new Intent(startDriving.this, accountHomePage.class));
    }

    /*
This function will show alert to user in case GPS service is closed in order to ask for the permission
*/
    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
/*
This function will return the speed limit using the string a (the current address)
 */
    public String get_speedLimit () {
        for (int i = 0; i < speedList.length; i++){
            if (speedList[i].toLowerCase().contains(theAddress.toLowerCase())) {
                return  "100";
            }
        }
        return "70";
    }

    /*
This function will return the street name using the location object
 */
    public String  get_theAddress (Location loc){
        // Set the Latitude and Longitude
        double lan =  loc.getLatitude();
        double ot =  loc.getLongitude();
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(lan, ot, 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    // check if the driver is over the speed limit to send an alert
    public void check_Limit(){

        if(Double.parseDouble(currentSpeed.getText().toString()) > Double.parseDouble(speedLimit.getText().toString())){
            Intent intent = new Intent(getBaseContext(), Alert.class);
            intent.putExtra("ID", "PLEASE SLOW DOWN AND OBEY THE SPEED LIMIT PROVIDED!");
            startActivity(intent);
            alerts++;
        }

    }

    // check if the driver is speeding up/down to send an alert
    public void check_Speed(){

        if(Math.abs((myList.get(9) - myList.get(0))) > 10){
            Intent intent = new Intent(getBaseContext(), Alert.class);
            intent.putExtra("ID", "PLEASE DRIVE CAREFULLY AND AVOID SPEEDING UP/DOWN!");
            startActivity(intent);
            alerts++;
        }

    }
}
