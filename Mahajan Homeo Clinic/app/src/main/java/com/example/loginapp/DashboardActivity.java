package com.example.loginapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends BaseActivity {

    String EmailHolder;
    TextView Email;
    MaterialButton logoutButton ;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseUser mUser;
    //@SuppressLint("SetTextI18n")
    public static final String TAG="LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        Intent intent = getIntent();
        // Set up the toolbar
        // Receiving User Email Send By MainActivity.
        EmailHolder = intent.getStringExtra(MainActivity.userEmail);


        // Setting up received email to TextView.
        //Email.setText(Email.getText().toString()+ EmailHolder);

    }

}