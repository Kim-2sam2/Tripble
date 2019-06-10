package com.impact.tripble;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class NFC_Game extends AppCompatActivity {

    TextView state1, timer;
    Button btn;
    private int initTime = 30;
    TimerTask tt;
    Handler handler;

    Vibe vibe;
    Animation animation;

    /*NFC*/
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

    boolean isClear = false;
    //protected boolean success = false;



    /*bluetooth*/
    private final int REQUEST_ENABLE_BT = 100;
    private static final String TAG = "BluetoothClient";
    private ArrayAdapter<String> mConversationArrayAdapter;
    String[] recvTest;

    ConnectedTask mConnectedTask;
    static boolean isConnectionError = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket0;
    private BluetoothDevice B0;
    private ConnectTask BC0;

    final String B0MA = "B0:FC:36:29:89:98";
    String B1MA = "0C:54:15:0A:EF:41";  //원규
    //String B2MA = "F8:63:3F:13:C1:0C";  //이슬
    //String B2MA = "0C:96:E6:C9:99:7C";


    final int deviceCount = 2;
    final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_game);

        //NFC//
        state1 = (TextView)findViewById(R.id.state);
        timer = (TextView)findViewById(R.id.timer);
        btn = (Button) findViewById(R.id.btn);

        Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        vibe = new Vibe(vibrator);
        animation = AnimationUtils.loadAnimation(this,R.anim.count);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!isClear) {
                    initTime =30;
                    handler = new Handler(){
                        public void handleMessage(Message msg){

                            btn.setVisibility(View.INVISIBLE);
                            btn.setClickable(false);
                            btn.setText("종료!");
                            initTime--;
                            handler.sendEmptyMessageDelayed(0,1000);
                            timer.setText(Integer.toString(initTime));
                            if(initTime <11){
                                vibe.failVibe();
                                timer.setTextColor(Color.parseColor("#ff0000"));
                                timer.startAnimation(animation);
                            }
                            if(initTime == 0){
                                btn.setText("다시하기!");
                                btn.setVisibility(View.VISIBLE);
                                btn.setClickable(true);
                                handler.removeMessages(0);
                            }
                            if(isClear){
                                btn.setVisibility(View.VISIBLE);
                                btn.setClickable(true);
                                handler.removeMessages(0);
                            }
                        }
                    };

                    boolean send = handler.sendEmptyMessage(0);
                }else{
                    Intent intent = new Intent(NFC_Game.this, Mission_list.class);
                    startActivity(intent);

                    finish();
                }
            }
        });

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent targetIntent = new Intent(this, NFC_Game.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mPendingIntent = PendingIntent.getActivity(this, 0, targetIntent, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (Exception e) {
            throw new RuntimeException("fail", e);
        }

        //bluetooth//
        Log.d(TAG, "Initalizing Bluetooth adapter...");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConversationArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);


        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.d(TAG, "Initialisation Successful");
            pairingDevice();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //BlueTooth is now Enabled
                pairingDevice();
            }

        }
        if (resultCode == RESULT_CANCELED) {
            //showQuitDialog( "You need to enable bluetooth");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( mConnectedTask != null ) {

            mConnectedTask.cancel(true);
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
            //readResult.setText("TagID: " + toHexString(tagId));
            tagNum = toHexString(tagId);
        }

        isRightTag(tagNum);

        if (isClear) {
            btn.setVisibility(View.VISIBLE);
            btn.setClickable(true);
            sendMessage(isClear);
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
                    state1.setText("첫번째 카드 입니다.");
                    vibe.successVibe();
                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;
                    state1.setText("순서가 잘못 되었습니다.");
                    vibe.failVibe();

                }
                break;

            case KEY_B:
                if (TAG_A == true && TAG_B == false && TAG_C == false) {
                    TAG_B = true;
                    state1.setText("두번째 카드 입니다.");
                    vibe.successVibe();

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;
                    state1.setText("순서가 잘못 되었습니다.");
                    vibe.failVibe();
                }
                break;

            case KEY_C:
                if (TAG_A == true && TAG_B == true && TAG_C == false) {
                    TAG_C = true;
                    state1.setText("세번째 카드 입니다.");
                    vibe.successVibe();

                    isClear = true;
                    if(isClear){
                        btn.setVisibility(View.VISIBLE);
                        btn.setClickable(true);
                    }

                } else {
                    TAG_A = false;
                    TAG_B = false;
                    TAG_C = false;

                    state1.setText("순서가 잘못 되었습니다.");
                    vibe.failVibe();
                    isClear = false;
                    if(!isClear){
                        btn.setVisibility(View.INVISIBLE);
                        btn.setClickable(false);
                    }
                }
                break;

            default:
                //todo 꽝인 경우도 세팅
                //todo 첫번째 성공인 경우 순서잘못 / 처음은 문제출제
                break;
        }
    }

    public void pairingDevice() {
        B0 = mBluetoothAdapter.getRemoteDevice(B0MA);


        Log.d(TAG, "pairing Successful");

        BC0 = new ConnectTask(B0);

        BC0.execute();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket BS;
        private BluetoothDevice BD;

        ConnectTask(BluetoothDevice BD) {
            this.BD = BD;

            try {
                mSocket0 = BD.createRfcommSocketToServiceRecord(uuid);
                Log.d(TAG, "create socket for Device");

            } catch (IOException e) {
                Log.e(TAG, "socket create failed " + e.getMessage());
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            mBluetoothAdapter.cancelDiscovery();
            try {

                mSocket0.connect();
            } catch (IOException e) {
                try {
                    mSocket0.close();
                } catch (IOException e1) {
                    Log.e(TAG, "unable to close() " + " socket during connection failure", e1);
                }
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (isSuccess) {
                connected(mSocket0);
            } else {
                Log.d(TAG, "Unable to connect device");
            }
        }

        public void connected(BluetoothSocket BS) {
            mConnectedTask = new ConnectedTask(mSocket0);
            mConnectedTask.execute();
        }

    }

    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private BluetoothSocket BS;

        ConnectedTask(BluetoothSocket BS) {
            this.BS = BS;

            try {
                mInputStream = mSocket0.getInputStream();
                mOutputStream = mSocket0.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "socket not created", e);
            }
            Log.d(TAG, "Connected to Device");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            while (true) {

                if (isCancelled()) return false;

                try {

                    int bytesAvailable = mInputStream.available();

                    if (bytesAvailable > 0) {

                        byte[] packetBytes = new byte[bytesAvailable];

                        mInputStream.read(packetBytes);

                        for (int i = 0; i < bytesAvailable; i++) {

                            byte b = packetBytes[i];
                            if (b == '\n') {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                String recvMessage = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;

                                Log.d(TAG, "recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {

                    Log.e(TAG, "disconnected", e);
                    return false;
                }
            }

        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {
            mConversationArrayAdapter.insert(recvMessage[0],0 );
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if (!isSucess) {


                closeSocket();
                Log.d(TAG, "Device connection was lost");
                isConnectionError = true;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            closeSocket();
        }

        void closeSocket() {

            try {

                mSocket0.close();
                Log.d(TAG, "close socket()");

            } catch (IOException e2) {

                Log.e(TAG, "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg) {

            msg += "\n";

            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during send", e);
            }

        }
    }

    void sendMessage(Boolean key) {
        String msg;
        if (key) {
            msg = "true";
        } else {
            msg = "false";
        }

        mConnectedTask.write(msg);
        Log.d(TAG, "send message: " + msg);
    }

}

