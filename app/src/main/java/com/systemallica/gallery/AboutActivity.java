package com.systemallica.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.app_name) TextView app_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //Add version number to the TextView
        String version = getResources().getString(R.string.about_name) + " " + BuildConfig.VERSION_NAME;
        app_name.setText(version);

        // Change navBar colour
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int app_primary = ContextCompat.getColor(this, R.color.app_primary);
            getWindow().setNavigationBarColor(app_primary);
        }
    }

    //Send email
    @OnClick(R.id.email) public void email() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:systemallica.apps@gmail.com"));
        startActivity(emailIntent);
    }

    //Open Play Store
    @OnClick(R.id.rate) public void rate() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.systemallica.gallery"));
        startActivity(browserIntent);
    }

}
