package com.example.android.myapplication;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FlexibleLayout extends AppCompatActivity {

    boolean mTablet;
    int mGridSelectedIndex = 0;
    private static final int GRID_VIEW_ID = 10101010;
    private static final int DETAIL_VIEW_ID = 10101011;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTablet = getResources().getBoolean(R.bool.isTablet);

        // Display code that show if we are a tablet or phone
        Toast toast;
        if (mTablet) {
            toast = Toast.makeText(this, "Tablet", Toast.LENGTH_SHORT);

            FrameLayout frame = new FrameLayout(this);
            frame.setId(GRID_VIEW_ID);
            setContentView(frame, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            if (savedInstanceState == null) {
                Fragment newFragment = new MovieGridFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(GRID_VIEW_ID, newFragment).commit();
            }

        } else {
            toast = Toast.makeText(this, "Phone", Toast.LENGTH_SHORT);

            FrameLayout frame = new FrameLayout(this);
            frame.setId(GRID_VIEW_ID);
            setContentView(frame, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            if (savedInstanceState == null) {
                Fragment newFragment = new MovieGridFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(GRID_VIEW_ID, newFragment).commit();
            }
        }
        toast.show();

    }
}
