package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import static java.security.AccessController.getContext;

public abstract class BaseActivity extends AppCompatActivity {
    protected Clinic mClinic;

    MaterialButton logoutButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mClinic = (Clinic) this.getApplicationContext();


        logoutButton = findViewById(R.id.logout_button);

        Intent intent = getIntent();
        // Set up the toolbar
        setUpToolbar();


        // Setting up received email to TextView.
        //Email.setText(Email.getText().toString()+ EmailHolder);

        // Adding click listener to Log Out button.

        logoutButton.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                //Finishing current DashBoard activity on button click.
                finish();
                // Toast.makeText(DashboardActivity.this, "Log Out Successfull", Toast.LENGTH_LONG).show();
            }
        });


        // Set cut corner background for API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById(R.id.product_grid).setBackgroundResource(R.drawable.shr_product_grid_background_shape);
        }
    }

    protected void onResume() {
        super.onResume();
        mClinic.setCurrentActivity(this);
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = mClinic.getCurrentActivity();
        if (this.equals(currActivity))
            mClinic.setCurrentActivity(null);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Activity currentActivity = ((Clinic)getApplicationContext()).getCurrentActivity();
        Toast.makeText(BaseActivity.this, "Test", Toast.LENGTH_SHORT).show();
        // Scroll the Forward view down (so that we can see the bottom part) Animation 2
        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                //getContext(),
                this,
                findViewById(R.id.product_grid),
                new AccelerateDecelerateInterpolator(),
                this.getDrawable(R.drawable.shr_branded_menu), // Menu open icon
                BaseActivity.this.getDrawable(R.drawable.shr_close_menu))); // Menu close icon
    }

    //@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);
        // super.onCreateOptionsMenu(menu, menuInflater);
    }


}