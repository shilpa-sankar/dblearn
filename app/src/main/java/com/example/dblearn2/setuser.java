package com.example.dblearn2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class setuser extends AppCompatActivity {

    Button b1;
    EditText user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setuser);
        user=(EditText)findViewById(R.id.username);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder name = new AlertDialog.Builder(com.example.dblearn2.setuser.this);
                name.setMessage("Make sure you enter single name only")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });
        b1=(Button)findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username=user.getText().toString();
                AlertDialog.Builder name = new AlertDialog.Builder(com.example.dblearn2.setuser.this);
                name.setMessage("Do remember the name ")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sha = getSharedPreferences("username",MODE_PRIVATE);
                                SharedPreferences.Editor edi=sha.edit();
                                edi.putString("name",username);
                                edi.commit();
                                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(i);
                            }
                        })
                        .create().show();


            }
        });
    }
}
