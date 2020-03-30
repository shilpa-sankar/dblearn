package com.example.dblearn2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button create,join;
    LocationManager lmang;
    Boolean gpsenabled=false,netenabled=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        create=(Button)findViewById(R.id.create);
        join=(Button)findViewById(R.id.join);
        SharedPreferences sh = getSharedPreferences("username",MODE_PRIVATE);
        SharedPreferences.Editor ed=sh.edit();
        final String frname=sh.getString("name","none");
        if(frname.equals("none")){
                Intent kn= new Intent(getApplicationContext(),setuser.class);
                startActivity(kn);
        }
        enableloc();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gpsenabled==false){
                    enableloc();
                }
                else {
                    Intent an = new Intent(getApplicationContext(), create.class);
                    an.putExtra("usname",frname);
                    startActivity(an);
                }
            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gpsenabled==false){
                    enableloc();
                }
                else {
                    Intent bn = new Intent(getApplicationContext(), join.class);
                    bn.putExtra("usname",frname);

                    startActivity(bn);
                }
            }
        });
        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return;
        }
    }

    private void enableloc() {
        lmang=(LocationManager)getSystemService(LOCATION_SERVICE);
        gpsenabled=lmang.isProviderEnabled(lmang.GPS_PROVIDER);

        if(gpsenabled==false){
            AlertDialog.Builder gpsno = new AlertDialog.Builder(com.example.dblearn2.MainActivity.this);
            gpsno.setMessage("Enable gps")
                    .setPositiveButton("Open gps settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent gpsgo = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsgo);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }

        }
    }

