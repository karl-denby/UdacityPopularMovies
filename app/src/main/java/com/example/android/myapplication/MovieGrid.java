package com.example.android.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MovieGrid extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        Button test_btn = (Button) findViewById(R.id.test_button);
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailsForMovie(1);
            }
        });
    }

    void showDetailsForMovie(int movieId) {
        Context context = this;

        Intent intent = new Intent(context, MovieDetail.class);
        intent.putExtra("ID", movieId);
        startActivity(intent);
    }
}
