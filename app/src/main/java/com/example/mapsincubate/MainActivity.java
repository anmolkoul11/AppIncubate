package com.example.mapsincubate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import static com.example.mapsincubate.Constants.ERROR_DIALOG_REQUEST;
import static com.example.mapsincubate.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.mapsincubate.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity {
    private boolean mLocationP=false;
    Button b1,b2,bs,bf;
    database db;
    ActionBar ab;
    ProgressBar progressBar;
    int status=0;
    MapsActivity mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=(Button)findViewById(R.id.btn_1);
        b2=(Button)findViewById(R.id.btn_2);
        bs=(Button)findViewById(R.id.btn_s);
        bf=(Button)findViewById(R.id.btn_f);
        ab=getSupportActionBar();
        ab.hide();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE}, PackageManager.PERMISSION_GRANTED);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkMapServices()){
                    if(mLocationP){
                        Intent i = new Intent(MainActivity.this,MapsActivity.class);
                        startActivity(i);
                    }
                    else{
                        getLocationPermission();
                    }
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ContactActivity.class);
                startActivity(i);
            }
        });


        db = new database(MainActivity.this);
        try {
            final Cursor record = db.get_record();



        bs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    record.moveToFirst();
                    while (record.moveToNext()) {
                        String ph = record.getString(2);
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(ph, null, "Hi", null, null);

//                        smsManager.sendTextMessage(ph, null, "My Location is \n http://maps.google.com/?q=", null, null);
//                smsManager.sendTextMessage("9461937350", null, "My Location is \n http://maps.google.com/?q="+lat+","+lon, null, null);
//                        status++;
//                        progressBar.setProgress(status);
//                        Thread th = new Thread();
//                        try {
//                            th.sleep(4000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        th.start();
                    }

                    Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();

                }
            });

        }catch(Exception ty){
            Toast.makeText(MainActivity.this,"Error"+ty,Toast.LENGTH_LONG).show();
        }
        mp=new MapsActivity(MainActivity.this);
        bf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location=mp.Loccall();
                Toast.makeText(MainActivity.this,"Lat"+location.getLatitude()+",Lon"+location.getLongitude(),Toast.LENGTH_LONG).show();
            }
        });

    }
    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                dialog.dismiss();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationP = true;
            getMaps();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
//        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
//            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
//            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationP = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationP = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationP){
                    getMaps();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
void getMaps(){
        Intent i=new Intent(MainActivity.this,MapsActivity.class);
        startActivity(i);
}


}
