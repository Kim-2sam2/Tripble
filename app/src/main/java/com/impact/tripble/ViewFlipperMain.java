package com.impact.tripble;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

public class ViewFlipperMain extends AppCompatActivity implements ViewFlipperAction.ViewFlipperCallback  {

    //뷰플리퍼
    ViewFlipper flipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fliper);

        //bottomNavigator();

        flipper = (ViewFlipper)findViewById(R.id.flipper);
        //xml을 inflate 하여 flipper view에 추가하기
        //inflate
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View view1 = inflater.inflate(R.layout.introduce_1, flipper, false);
        View view2 = inflater.inflate(R.layout.introduce_2, flipper, false);
        View view3 = inflater.inflate(R.layout.introduce_3, flipper, false);
        //inflate 한 view 추가
        flipper.addView(view1);
        flipper.addView(view2);
        flipper.addView(view3);

        //좌우 터치시 화면넘어가기
        flipper.setOnTouchListener(new ViewFlipperAction(this, flipper));

        Button button = (Button)findViewById(R.id.start);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewFlipperMain.this, select_HostUser.class);
                startActivity(intent);
            }
        });

    }

    public void onFlipperActionCallback(int position) {
        Log.d("ddd", "" + position);
    }

}
