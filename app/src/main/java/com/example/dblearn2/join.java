package com.example.dblearn2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class join extends AppCompatActivity {

    Button join;
    EditText group,admin;
    FusedLocationProviderClient client;
    FirebaseFirestore db;
    String pername;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        group = (EditText) findViewById(R.id.groupnam2);
        admin = (EditText) findViewById(R.id.pname2);
        join = (Button) findViewById(R.id.join);
        db = FirebaseFirestore.getInstance();
        Bundle firstdet=getIntent().getExtras();
        if(firstdet==null){
            return;
        }

        pername=firstdet.getString("usname");
        client= LocationServices.getFusedLocationProviderClient(this);


        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getloc();

            }
        });
    }

    private void getloc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(com.example.dblearn2.join.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(this).setTitle("Location permission is needed").setMessage("Allow Location permission to use this app")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(com.example.dblearn2.join.this, new String[]{
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
                ActivityCompat.requestPermissions(com.example.dblearn2.join.this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 10);
            }
        }
        else {
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        final Double latitude = location.getLatitude();
                        final Double lofitude = location.getLongitude();
                        final String gname=group.getText().toString().trim();
                        final String adname=admin.getText().toString().trim();
                        if(gname.isEmpty()||adname.isEmpty()){
                            Toast.makeText(getApplicationContext(),"Enter credentials",Toast.LENGTH_SHORT).show();

                        }
                        else {
                            db.collection(gname).document(adname).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        data da=new data();
                                        da.setLati(latitude);
                                        da.setLongi(lofitude);
                                        db.collection(gname).document(pername).set(da).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                                Intent dn = new Intent(getApplicationContext(),MapsActivity.class);
                                                dn.putExtra("groupname",gname);
                                                dn.putExtra("personname",pername);
                                                startActivity(dn);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "unsuccess", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Wrong group name or admin name", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Wrong group name or admin name", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
