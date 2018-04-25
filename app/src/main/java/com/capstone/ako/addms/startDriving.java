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
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class startDriving extends AppCompatActivity implements LocationListener{
    // Components from the XML
    TextView speedLimit, distanceCovered, currentSpeed;
    LinearLayout lay;
    Location location1 ;

    // Firebase
    FirebaseFirestore db;

    //Variables for TextToSpeech
    static String parent="", message = "";
    String oldMsg,oldParent;
    TextToSpeech tts;

    // for the GPS connection
    protected LocationManager locationManager;
    Location oldLocation;
    static String theAddress, userName;
    String speedList[];
    boolean firstRun = true, x = false;
    double newDistance = 0, initSpeed=0, finSpeed=0;
    int total = 0;
    static int alerts = 0;
    Chronometer c;
    DecimalFormat f = new DecimalFormat("###.#");

    // create and object of type Verification to save data to the sql
    Verification verification = new Verification(this);
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_driving);
        x = false;

        // Keep the screen On (Awake)
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
        lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alerts++;
                Intent intent = new Intent(getBaseContext(), Alert.class);
                intent.putExtra("ID", "PLEASE DON'T USE YOUR PHONE WHILE DRIVING!");
                startActivity(intent);
            }
        });

        // check if driver is over speed limit every 60s using check_Limit()
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                   check_Limit();
                   initSpeed = Double.parseDouble(currentSpeed.getText().toString());
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(t, 0, 30000);
    }

        /*
    when the Location is Changed (new Latitude and Longitude) !
     */

    @Override
    public void onLocationChanged(Location location) {

        if(!x){

        if (!firstRun) { // calculate the new distance
            newDistance = location.distanceTo(oldLocation) + newDistance;
            distanceCovered.setText(f.format(newDistance/1000));
        }
        currentSpeed.setText(Math.round(3.6*location.getSpeed()) + "");
        theAddress = get_theAddress(location);
        firstRun = false;
        speedLimit.setText(get_speedLimit() + "");
        oldLocation = location;
        finSpeed = Double.parseDouble(currentSpeed.getText().toString());
        check_Speed();
        location1 = location;
        fb_data(false, location);

        //Retrieve message from firebase to convert to speech

                db.collection("WebToMob").document(userName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            Log.w("Hii", ""+documentSnapshot);
                            message = documentSnapshot.getData().get("Message").toString();
                            parent = documentSnapshot.getData().get("Parent").toString();

                            if(!(message.equals(oldMsg)) || !(parent.equals(oldParent))) {
                                speak(message, parent);
                            }
                            oldMsg = message;
                            oldParent = parent;
                        } catch (Exception e) {
                            System.out.println(e);
                        }
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
        x = true;
        fb_data(true, location1);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        verification.saveTripHistoy(alerts, Double.parseDouble(f.format(newDistance/1000)), total,Double.parseDouble(f.format((newDistance / 1000) / (total * 0.0166667))),userName);
        finish();
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
            alerts++;
            Intent intent = new Intent(getBaseContext(), Alert.class);
            intent.putExtra("ID", "PLEASE SLOW DOWN AND OBEY THE SPEED LIMIT PROVIDED!");
            startActivity(intent);
        }

    }

    // check if the driver is speeding up/down to send an alert
    public void check_Speed(){

        if(Math.abs(finSpeed - initSpeed) > 50){
            alerts++;
            initSpeed = Double.parseDouble(currentSpeed.getText().toString());
            Intent intent = new Intent(getBaseContext(), Alert.class);
            intent.putExtra("ID", "PLEASE DRIVE CAREFULLY AND AVOID SPEEDING UP/DOWN!");
            startActivity(intent);
        }

    }

    public void fb_data(boolean last, Location location) {
        // Send data to firebase
        Map<String, Object> dataToSave = new HashMap<>();

        // if statement to check if location is null which means the driver finished the trip
        if (!last) {
            dataToSave.put("Speed", Double.parseDouble(currentSpeed.getText().toString()));
        } else {
            double timeHr = total * 0.0166667;
            if(timeHr == 0){
                dataToSave.put("AverageSpeed", 0);
            }
            else {
                dataToSave.put("AverageSpeed", Double.parseDouble(f.format((newDistance / 1000) / timeHr)));
            }
        }
        dataToSave.put("Time", total);
        dataToSave.put("Alerts", alerts);
        dataToSave.put("Distance", distanceCovered.getText().toString());
        if(location != null) {
            dataToSave.put("Longitude", location.getLongitude());
            dataToSave.put("Latitude", location.getLatitude());
        }

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


    public void speak(String localMessage, String localParent){
        final String localMessage2 = localMessage;
        final String localParent2 = localParent;
            tts = new TextToSpeech(startDriving.this, new TextToSpeech.OnInitListener() {

                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = tts.setLanguage(Locale.US);
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("error", "This Language is not supported");
                        } else {
                            if (message != null || !("".equals(message))) {
                                tts.speak("From " + localParent2 + ", " + localMessage2, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                    }
                    else {
                        Log.e("error", "Initilization Failed!");
                    }
                }

            });

    }
}
