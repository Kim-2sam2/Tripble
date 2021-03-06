package com.impact.tripble;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NFC extends AppCompatActivity {

    Button clearButton;
    boolean isClear;
    Intent recvIntent;

    TextView readResult;

    ImageView nfc1, nfc2, nfc3;
    TextView error;
    Animation startAnimation;
    Vibe vibe;
    long[] pattern = {0,150,100,150};

    /*NFC*/
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechList;
    private Tag tag;
    private IsoDep tagcomm;
    private static String tagNum = null;
    private final int REQUEST_MISSION = 300;

    private final String KEY_A = "04BD47021B5C80";
    private final String KEY_B = "044655021B5C80";
    private final String KEY_C = "04217F021B5C80";
    private final String KEY_SPARE = "04D451021B5C80";
    private boolean TAG_A = false;
    private boolean TAG_B = false;
    private boolean TAG_C = false;
    //protected boolean success = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc);

        recvIntent = new Intent();
        clearButton = (Button)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NFC.this, Popup.class);
                startActivityForResult(intent,REQUEST_MISSION);
            }
        });

        //NFC//
        readResult = (TextView) findViewById(R.id.tagDesc);
        nfc1 = (ImageView)findViewById(R.id.nfc1);
        nfc2 = (ImageView)findViewById(R.id.nfc2);
        nfc3 = (ImageView)findViewById(R.id.nfc3);
        error = (TextView)findViewById(R.id.error);

        startAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink);
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibe = new Vibe(vibrator);


        mAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent targetIntent = new Intent(this, NFC.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (Exception e) {
            throw new RuntimeException("fail", e);
        }
    }

    public void popUp(){

        AlertDialog.Builder builder = new AlertDialog.Builder(NFC.this);
        LayoutInflater factory = LayoutInflater.from(NFC.this);
        View view = factory.inflate(R.layout.clear,null);

        builder.setTitle("미션 완료!!");
        builder.setView(view);
        builder.setNegativeButton("받기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(NFC.this,"미션을 완수하셨습니다.",Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent;
        boolean temp;

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case REQUEST_MISSION:
                    temp = data.getBooleanExtra("isClear", false);
                    intent = new Intent(NFC.this, Mission_list.class);
                    intent.putExtra("isClear", temp);
                    intent.putExtra("key", 4);
                    startActivityForResult(intent, REQUEST_MISSION);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    //NFC//
    @Override
    protected void onPause() {
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    //NFC//
    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }
    }

    //NFC//
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        tag = intent.getParcelableExtra(mAdapter.EXTRA_TAG);
        if (tag != null) {
            byte[] tagId = tag.getId();
            //readResult.setText("TagID: " + toHexString(tagId));
            tagNum = toHexString(tagId);
        }

        isRightTag(tagNum);

        if (TAG_A == true && TAG_B == true && TAG_C == true) {
            //todo 3개 성공시 버튼 등장
        }

    }

    //NFC//
    public static final String CHARS = "0123456789ABCDEF";

    public static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(CHARS.charAt((data[i] >> 4) & 0x0F)).append(
                    CHARS.charAt(data[i] & 0x0F));
        }
        return sb.toString();
    }

    //NFC//
    private void isRightTag(String Key) {
        error.setVisibility(View.INVISIBLE);
        switch (Key) {
            case KEY_A:
                if (TAG_A == false && TAG_B == false && TAG_C == false) {
                    TAG_A = true;
                    nfc1.setImageResource(R.drawable.im_nfc_on);
                    vibe.successVibe();
                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;
                    nfc1.setImageResource(R.drawable.im_nfc_off);
                    nfc2.setImageResource(R.drawable.im_nfc_off);
                    nfc3.setImageResource(R.drawable.im_nfc_off);

                    error.setVisibility(View.VISIBLE);
                    error.startAnimation(startAnimation);
                    vibe.failVibe();

                }
                break;

            case KEY_B:
                if (TAG_A == true && TAG_B == false && TAG_C == false) {
                    TAG_B = true;
                    nfc2.setImageResource(R.drawable.im_nfc_on);
                    vibe.successVibe();

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;

                    nfc1.setImageResource(R.drawable.im_nfc_off);
                    nfc2.setImageResource(R.drawable.im_nfc_off);
                    nfc3.setImageResource(R.drawable.im_nfc_off);
                    error.setVisibility(View.VISIBLE);
                    error.startAnimation(startAnimation);
                    vibe.failVibe();

                }
                break;

            case KEY_C:
                if (TAG_A == true && TAG_B == true && TAG_C == false) {
                    TAG_C = true;
                    nfc3.setImageResource(R.drawable.im_nfc_on);
                    vibe.successVibe();

                    isClear = true;
                    if(isClear){
                        clearButton.setVisibility(View.VISIBLE);
                        clearButton.setClickable(true);
                        clearButton.startAnimation(startAnimation);

                    }

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;

                    nfc1.setImageResource(R.drawable.im_nfc_off);
                    nfc2.setImageResource(R.drawable.im_nfc_off);
                    nfc3.setImageResource(R.drawable.im_nfc_off);
                    error.setVisibility(View.VISIBLE);
                    error.startAnimation(startAnimation);
                    vibe.failVibe();

                    isClear = false;
                    if(!isClear){
                        clearButton.setVisibility(View.INVISIBLE);
                        clearButton.setClickable(false);

                    }
                }
                break;

            default:
                if(Key.equals(KEY_SPARE)){
                    if (TAG_A == false && TAG_B == false && TAG_C == false) {
                        TAG_A = true;
                        nfc1.setImageResource(R.drawable.im_nfc_on);

                        break;

                    }

                    if (TAG_A == true && TAG_B == false && TAG_C == false) {
                        TAG_B = true;
                        nfc2.setImageResource(R.drawable.im_nfc_on);

                        break;

                    }

                    if (TAG_A == true && TAG_B == true && TAG_C == false) {
                        TAG_C = true;
                        nfc3.setImageResource(R.drawable.im_nfc_on);

                        isClear = true;
                        if(isClear){
                            clearButton.setVisibility(View.VISIBLE);
                            clearButton.setClickable(true);
                            clearButton.startAnimation(startAnimation);
                        }

                    }
                    break;
                }else{
                    Toast.makeText(this, "등록되지 않은 카드", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}


