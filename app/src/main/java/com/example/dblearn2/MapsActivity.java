package com.example.dblearn2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    long bpresstime;
    AutoCompleteTextView searchtext;
    FirebaseFirestore dbm;
    CollectionReference ref;
    String grpname,pername;
    FusedLocationProviderClient client;
    Double srlati,srlongi;
    String origin;
    String follow="";
    Polyline line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getfirtdet();
        dbm= FirebaseFirestore.getInstance();
        ref = dbm.collection(grpname);
        client= LocationServices.getFusedLocationProviderClient(this);


        searchtext=(AutoCompleteTextView) findViewById(R.id.input_search);
        searchtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_DONE||
                        event.getAction()==KeyEvent.ACTION_DOWN||
                        event.getAction()==KeyEvent.KEYCODE_ENTER){
                    geolocate();
                }
                return false;
            }
        });


    }


    private void getfirtdet() {
        Bundle firstdet=getIntent().getExtras();
        if(firstdet==null){
            return;
        }
        grpname=firstdet.getString("groupname");
        pername=firstdet.getString("personname");
    }

    private void geolocate() {

        String searchstr=searchtext.getText().toString();
        Geocoder geocoder=new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list=geocoder.getFromLocationName(searchstr,1);
        }
        catch (Exception e){

        }
        if(list.size()>0){
            Address address=list.get(0);
            Toast.makeText(getApplicationContext(),"found"+address.toString(),Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onBackPressed() {

        if(bpresstime+2000>System.currentTimeMillis()){
            new AlertDialog.Builder(this).setTitle("Are you sure younwant to exit").setMessage("It will remove you from  group")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbm.collection(grpname).document(pername).delete();

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("Exit me", true);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();

        }
        else {
            Toast.makeText(getApplicationContext(),"Press again to exit",Toast.LENGTH_SHORT).show();
        }
        bpresstime=System.currentTimeMillis();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setlocation();

    }

    private void setlocation() {
        CollectionReference ref = dbm.collection(grpname);
        ref.get().addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String name = documentSnapshot.getId();
                    Log.d("data", documentSnapshot.toString());
                    if (name.equals(pername)) {
                        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                srlati = location.getLatitude();
                                srlongi = location.getLongitude();
                                origin = srlati + "," + srlongi;
                                LatLng loc = new LatLng(srlati, srlongi);

                                mMap.addMarker(new MarkerOptions().position(new LatLng(srlati, srlongi)).title("" + pername)).showInfoWindow();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17), 2500, null);

                            }
                        });
                    }
                }
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String nvame = documentSnapshot.getId();
                    if (nvame.equals(pername)) {
                        continue;
                    } else {
                        data loc = documentSnapshot.toObject(data.class);
                        Double lati = loc.getLati();
                        Double longi = loc.getLongi();
                        LatLng myloc = new LatLng(lati, longi);


                        mMap.addMarker(new MarkerOptions().position(new LatLng(lati, longi)).title("" + nvame));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myloc, 17), 2500, null);


                    }
                }

            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Unsuccessfull", Toast.LENGTH_LONG).show();

            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (line != null) {
                    line.remove();
                }
                if(follow.equals(marker.getTitle())){
                    line.remove();
                }
                else {


                    follow = marker.getTitle();
                    LatLng mcord = marker.getPosition();
                    Double dlati = mcord.latitude;
                    Double dlongi = mcord.longitude;
                    String destination = dlati + "," + dlongi;
                    String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&sensor=false&dmode=driving&key=AIzaSyBuTVT5_NTL7KXUHIMfCxyBzqo_DGKnxHk";
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);

                }
                return false;
            }
        });

        ref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                mMap.clear();
            if(e!=null){
                Toast.makeText(getApplicationContext(),"Error while fetching realtime data",Toast.LENGTH_SHORT).show();
                return;
            }
                for (final QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                    String name = documentSnapshot1.getId();
                    Log.d("data", documentSnapshot1.toString());
                    if (name.equals(pername)) {
                        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                srlati = location.getLatitude();
                                srlongi = location.getLongitude();
                                origin = srlati + "," + srlongi;
                                data check = documentSnapshot1.toObject(data.class);
                                Double lati = check.getLati();
                                Double longi = check.getLongi();
                                if(srlati!=lati||srlongi!=longi) {
                                    data dc = new data();
                                    dc.setLati(srlati);
                                    dc.setLongi(srlongi);
                                    dbm.collection(grpname).document(pername).set(dc).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {


                                        }
                                    });
                                }
                                LatLng loc = new LatLng(srlati, srlongi);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(srlati, srlongi)).title("" + pername)).showInfoWindow();


                            }
                        });
                    }
                }
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String nvame = documentSnapshot.getId();
                    if (nvame.equals(pername)) {
                        continue;
                    } else {

                        data loc = documentSnapshot.toObject(data.class);
                        Double lati = loc.getLati();
                        Double longi = loc.getLongi();
                        LatLng myloc = new LatLng(lati, longi);
                        if(nvame.equals(follow)){
                            setroute(myloc);
                        }

                        mMap.addMarker(new MarkerOptions().position(new LatLng(lati, longi)).title("" + nvame));
                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myloc, 17), 2500, null);


                    }

                }
            }
        });



    }

    private void setroute(LatLng myloc) {
        Double flati,flongi;
        flati=myloc.latitude;
        flongi=myloc.longitude;
        String fdest=flati+","+flongi;
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + fdest + "&sensor=false&dmode=driving&key=AIzaSyBuTVT5_NTL7KXUHIMfCxyBzqo_DGKnxHk";
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            Log.d("sd", jsonData.toString());
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("vd", routes.toString());

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            Log.d("sdf", result.toString());

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);

            }

// Drawing polyline in the Google Map for the i-th route
            if(lineOptions!=null){
                line=mMap.addPolyline(lineOptions);
            }
            else{
                Toast.makeText(getApplicationContext(),"Direction not found",Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

   }
}
