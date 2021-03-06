package com.impact.tripble;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NFC_NoBluetooth extends AppCompatActivity {

    private static final int REQUEST_MISSION = 300;
    Button clearButton;
    boolean isClear;
    Intent recvIntent;

    /*NFC*/
    TextView readResult;
    TextView clear;
    TextView state;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechList;
    private Tag tag;
    private IsoDep tagcomm;
    private static String tagNum = null;

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

        Log.d("test", "onCreate");
        recvIntent = new Intent();
        clearButton = (Button)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NFC_NoBluetooth.this, Popup.class);
                startActivityForResult(intent,REQUEST_MISSION);
            }
        });

        //NFC//
        readResult = (TextView) findViewById(R.id.tagDesc);


        mAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent targetIntent = new Intent(this, NFC_NoBluetooth.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (Exception e) {
            throw new RuntimeException("fail", e);
        }

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
            readResult.setText("TagID: " + toHexString(tagId));
            tagNum = toHexString(tagId);
        }

        isRightTag(tagNum);

        if (TAG_A == true && TAG_B == true && TAG_C == true) {
            clear.setText("성공");
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

        switch (Key) {
            case KEY_A:
                if (TAG_A == false && TAG_B == false && TAG_C == false) {
                    TAG_A = true;
                    state.setText("A성공");
                    //sendIntent(TAG_A);

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;
                    state.setText("순서가 틀렸습니다. 처음부터 시작하세요.");
                }
                break;

            case KEY_B:
                if (TAG_A == true && TAG_B == false && TAG_C == false) {
                    TAG_B = true;
                    state.setText("A,B성공");
                    //sendIntent(TAG_B);

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;
                    state.setText("순서가 틀렸습니다. 처음부터 시작하세요.");
                }
                break;

            case KEY_C:
                if (TAG_A == true && TAG_B == true && TAG_C == false) {
                    TAG_C = true;
                    state.setText("A,B,C성공");
                    //sendIntent(TAG_C);
                    isClear = true;
                    if(isClear){
                        clearButton.setVisibility(View.VISIBLE);
                        clearButton.setClickable(true);
                    }

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;
                    state.setText("순서가 틀렸습니다. 처음부터 시작하세요.");

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
                        state.setText("A성공");
                        break;
                    }

                    if (TAG_A == true && TAG_B == false && TAG_C == false) {
                        TAG_B = true;
                        state.setText("A,B성공");
                        break;

                    }

                    if (TAG_A == true && TAG_B == true && TAG_C == false) {
                        TAG_C = true;
                        state.setText("A,B,C성공");
                        isClear = true;
                        if(isClear){
                            clearButton.setVisibility(View.VISIBLE);
                            clearButton.setClickable(true);
                        }

                    }
                    break;
                }else{
                    Toast.makeText(this, "등록되지 않은 카드", Toast.LENGTH_LONG).show();
                }
                break;
        }
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
                    intent = new Intent(NFC_NoBluetooth.this, Mission_list.class);
                    intent.putExtra("isClear", temp);
                    intent.putExtra("key", 3);
                    startActivityForResult(intent, REQUEST_MISSION);
            }
        }
    }

}




