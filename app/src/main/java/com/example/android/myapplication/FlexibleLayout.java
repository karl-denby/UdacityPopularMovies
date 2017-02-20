package com.example.android.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class FlexibleLayout extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean mTablet = getResources().getBoolean(R.bool.isTablet);

        // Display code that show if we are a tablet or phone
        Toast toast;
        if (mTablet) {
            Intent intent = new Intent(FlexibleLayout.this, MovieGridAndDetail.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(FlexibleLayout.this, MovieGrid.class);
            startActivity(intent);
        }
        finish();
    }
}
