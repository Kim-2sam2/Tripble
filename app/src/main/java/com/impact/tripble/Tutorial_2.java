package com.impact.tripble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Tutorial_2 extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduce_1);
    }

    public void Next(View view){
        Intent intent = new Intent(Tutorial_2.this, Tutorial_3.class);
        startActivity(intent);
    }
}