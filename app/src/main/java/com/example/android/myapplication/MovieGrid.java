package com.example.android.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MovieGrid extends AppCompatActivity {

    Toast mToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if (mToast!=null) {
                    mToast.cancel();
                }

                mToast = Toast.makeText(MovieGrid.this, "" + position,Toast.LENGTH_SHORT);
                mToast.show();

                Intent intent = new Intent(MovieGrid.this, MovieDetail.class);
                intent.putExtra(EXTRA_MESSAGE, String.valueOf(position));
                startActivity(intent);
            }
        });
    }

}
