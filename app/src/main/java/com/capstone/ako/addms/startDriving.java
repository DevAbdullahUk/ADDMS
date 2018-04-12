package com.capstone.ako.addms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.capstone.ako.addms.R;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemClock;
import android.widget.Chronometer;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class startDriving extends AppCompatActivity implements LocationListener {
    // Components form the XML
    TextView elappsedTime, speedLimit, distanceCovered, currentSpeed;
    // for the GPS connection
    protected LocationManager locationManager;
    Location oldLocation;
    static String theAddress;
    String speedList[];
    boolean firstRun = true;
    double newDistance = 0;
    int total = 0;
    Chronometer c;
    DecimalFormat f = new DecimalFormat("###.#");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_driving);
        // xml elements
        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        speedLimit = (TextView) findViewById(R.id.speedLimit);
        distanceCovered = (TextView) findViewById(R.id.distanceCoverd);
        elappsedTime = (TextView) findViewById(R.id.time);
        c = (Chronometer) findViewById(R.id.ch);
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
        c.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer cArg) {

                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                total = (h*60)+m;
                elappsedTime.setText(total + "");
            }
        });
        c.start();
    }

        /*
    when the Location is Changed (new Latitude and Longitude) this little fucker will be called!
     */

    @Override
    public void onLocationChanged(Location location) {

        if (!firstRun) { // calculate the new distance
            newDistance = location.distanceTo(oldLocation) + newDistance;
            distanceCovered.setText(f.format(newDistance/1000));

        }
        currentSpeed.setText(Math.round(3.6*location.getSpeed()) + "");
        theAddress = get_theAddress(location);
        firstRun = false;
        speedLimit.setText(get_speedLimit() + "");
        oldLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {/* I wont be using the sh*t for now*/}

    @Override
    public void onProviderEnabled(String provider) {/* I wont be using the sh*t for now*/}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {/* I wont be using the sh*t for now*/}


    public void home(View view) {
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
}
