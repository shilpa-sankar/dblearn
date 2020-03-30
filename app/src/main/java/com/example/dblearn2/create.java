package com.example.dblearn2;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class create extends AppCompatActivity {



    Button create;
    EditText group;
    TextView user;
    FusedLocationProviderClient client;
    FirebaseFirestore db;
    String pername;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==10){
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        group = (EditText) findViewById(R.id.groupnam);
        user=(TextView)findViewById(R.id.username);
        create = (Button) findViewById(R.id.create);
        db = FirebaseFirestore.getInstance();
        Bundle firstdet=getIntent().getExtras();
        if(firstdet==null){
            return;
        }

        pername=firstdet.getString("usname");
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setText(pername);
            }
        });
        client= LocationServices.getFusedLocationProviderClient(this);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getloc();

            }
        });


    }


    private void getloc() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           if (ActivityCompat.shouldShowRequestPermissionRationale(com.example.dblearn2.create.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
               new AlertDialog.Builder(this).setTitle("Location permission is needed").setMessage("Allow Location permission to use this app")
                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               ActivityCompat.requestPermissions(com.example.dblearn2.create.this, new String[]{
                                       Manifest.permission.ACCESS_COARSE_LOCATION
                               }, 10);
                           }
                       })
                       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               dialog.dismiss();
                           }
                       }).create().show();
           } else {
               ActivityCompat.requestPermissions(com.example.dblearn2.create.this, new String[]{
                       Manifest.permission.ACCESS_COARSE_LOCATION
               }, 10);
           }
       }
         else {
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Double latitude = location.getLatitude();
                        Double lofitude = location.getLongitude();
                        final String gname=group.getText().toString().trim();
                        if(gname.isEmpty()) {
                            Toast.makeText(getApplicationContext(),"Enter Group name",Toast.LENGTH_SHORT).show();

                        }
                        else {
                            data dab=new data();
                            dab.setLati(latitude);
                            dab.setLongi(lofitude);
                            db.collection(gname).document(pername).set(dab).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                    Intent cn = new Intent(getApplicationContext(), MapsActivity.class);
                                    cn.putExtra("groupname",gname);
                                    cn.putExtra("personname",pername);
                                    startActivity(cn);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "unsuccess", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                }
            });
        }
    }


}
