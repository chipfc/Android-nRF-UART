
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//https://gourmet-technology-crypto.jp/en/technology/android-things-on-raspberry-pi3-uart-and-bluetooth-coexistence/
//https://materialdesignicons.com/
package com.nordicsemi.nrfUARTv2;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.github.ybq.android.spinkit.SpinKitView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.icu.text.LocaleDisplayNames;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import static com.nordicsemi.nrfUARTv2.DeviceModel.SPEED_HIGH;
import static com.nordicsemi.nrfUARTv2.DeviceModel.SPEED_LOW;
import static com.nordicsemi.nrfUARTv2.DeviceModel.SPEED_STOP;
import static com.nordicsemi.nrfUARTv2.Helper.TOUCH_ACTION_DOWN_DELAY;
import static com.nordicsemi.nrfUARTv2.Helper.TOUCH_ACTION_DOWN_DELAY_SEAT;
import static com.nordicsemi.nrfUARTv2.Helper.TOUCH_ACTION_UP_DELAY;
import static com.nordicsemi.nrfUARTv2.NPNConstants.BLE_FRAGMENT_INDEX;
import static com.nordicsemi.nrfUARTv2.NPNConstants.REGISTER_FRAGMENT_INDEX;
import static com.nordicsemi.nrfUARTv2.NPNConstants.WIFI_FRAGMENT_INDEX;

public class MainActivity extends FragmentActivity implements TextToSpeech.OnInitListener, RadioGroup.OnCheckedChangeListener, View.OnTouchListener, View.OnClickListener, UpdateAPK.UpdateAPKListener, LocationListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private SpinKitView loading_next = null;

    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend;
    private EditText edtMessage;


    Button btnSetting;
    Button btnCode;
    ImageButton btnHomeSearchVoice;
    Context mContext;




    @SuppressLint("ClickableViewAccessibility")


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Cài đặt quyền")
                        .setMessage("Vị trí")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    String provider;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
//                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    public void onClick(View v) {
        String cmd = "ABC";
        int vid = v.getId();

        Log.d(TAG, "CMD: " + cmd);
        if (cmd.length() > 0) {
            sendBLEData("#" + cmd + "!");
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        String cmd = "";
        int vid = v.getId();


        return false;
    }






    private static final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);

                    Helper.saveTVCode((MainActivity) mContext, NPNConstants.SETTING_BLE_MAC, deviceAddress);

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.size() > 0) {

                    }
                } else {
                    Log.d(TAG, "Error code 2");
                    //talkToMe("Tôi không hiểu lệnh này, xin vui lòng thử lại!");
                }
                break;

            case DATA_CHECKING:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    niceTTS = new TextToSpeech(this, this);
                    //no data, prompt to install it
                    Log.d(TAG, "Activating TTS ok");
                } else {
                    Intent promptInstall = new Intent();
                    promptInstall.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(promptInstall);
                }
                break;

            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }
    LinearLayout main_content;
    FrameLayout fragment_content;
    Fragment fragment;
    TextView txtTitleCaption;
    ImageView imgTitileIcon;

    public void selectFragment(int position){

        Class fragmentClass = null;
        String fragmentTag = "";
        currentFragmentClass = position;
        switch (position){
            case BLE_FRAGMENT_INDEX:

                main_content.setVisibility(View.VISIBLE);
                fragment_content.setVisibility(View.GONE);
                txtTitleCaption.setText("KẾT NỐI BLE");
                imgTitileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bluetooth_icon));

                break;
            case WIFI_FRAGMENT_INDEX:
                main_content.setVisibility(View.GONE);
                fragment_content.setVisibility(View.VISIBLE);
                fragmentClass = FragmentWifiSetting.class;
                fragmentTag = "WifiSeting";
                txtTitleCaption.setText("CÀI ĐẶT WIFI");
                imgTitileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.wifi_icon));

                break;
            case REGISTER_FRAGMENT_INDEX:
                main_content.setVisibility(View.GONE);
                fragment_content.setVisibility(View.VISIBLE);
                fragmentClass = FragmentRegisterSetting.class;
                fragmentTag = "RegisterSeting";
                txtTitleCaption.setText("CÀI ĐẶT THANH GHI");
                imgTitileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.register_icon));

                break;

        }

        if(position != BLE_FRAGMENT_INDEX){
            try {
                fragment = (Fragment)fragmentClass.newInstance();

                Bundle bundle = new Bundle();
                bundle.putString("fragmentTag", fragmentTag);

                fragment.setArguments(bundle);
                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_content, fragment).commitAllowingStateLoss();
            } catch (Exception e) {
                Log.e(TAG, "selectFragment " + e.getMessage());
            }
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        mContext = this;

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        btnSend = (Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();


        loading_next = findViewById(R.id.loading_next);

        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btnConnectDisconnect.getText().equals("Connect")) {

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();

                        }
                    }
                }
            }
        });
        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        final Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFragment(REGISTER_FRAGMENT_INDEX);
            }
        });



        initUiFlags();
        goFullscreen();

        checkLocationPermission();

        populateList();
        main_content = findViewById(R.id.main_content);
        fragment_content = findViewById(R.id.fragment_content);

        txtTitleCaption = findViewById(R.id.txtTitleCaption);
        imgTitileIcon = findViewById(R.id.imgTitleIcon);
    }

    private void sendBLEData(String message) {
        Log.d("nRFUART", "Sending: " + message);
        if (isBLEConnected == false) {
            showMessage("Thiết bị chưa kết nối");
            return;
        }
        byte[] value;
        try {
            //send data to service
            value = message.getBytes("UTF-8");
            mService.writeRXCharacteristic(value);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            String strBLEMac = Helper.loadTVCode((MainActivity) mContext, NPNConstants.SETTING_BLE_MAC);
            //connectBLE("C7:43:12:D8:E6:9E");
            if (strBLEMac.indexOf("00000") < 0) {
                Log.d(TAG, "Connecting to " + strBLEMac);
                //connectBLE(strBLEMac);
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
            Log.d(TAG, "Receive a mesage " + msg);
        }
    };
    Timer reconnectTimer;

    public void setupReconnectTimer() {
        reconnectTimer = new Timer();
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                String strBLEMac = Helper.loadTVCode((MainActivity) mContext, NPNConstants.SETTING_BLE_MAC);
                //connectBLE("C7:43:12:D8:E6:9E");
                if (strBLEMac.indexOf("00000") < 0) {
                    Log.d(TAG, "Connecting to " + strBLEMac);
                    //connectBLE(strBLEMac);
                }
            }
        };
        reconnectTimer.schedule(aTask, 5000, 5000);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void sendMessage(String mess){
//        if(type == 2){
//            String mess = "{\"type\":2}";
//            sendBLEData(mess);
//        }
        sendBLEData(mess);
    }

    private boolean isBLEConnected = false;
    private int currentFragmentClass = 0;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;
                        if (reconnectTimer != null)
                            reconnectTimer.cancel();
                        showMessage("Kết nối thành công!");
                        //sendBLEData("{\"type\":0}");
                        isBLEConnected = true;
                        loading_next.setVisibility(View.GONE);
                        newDevicesListView.setEnabled(true);
                        selectFragment(WIFI_FRAGMENT_INDEX);
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                        //setupReconnectTimer();
                        isBLEConnected = false;
                        loading_next.setVisibility(View.INVISIBLE);
                        newDevicesListView.setEnabled(true);
                        //selectFragment(WIFI_FRAGMENT_INDEX);
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            //String text = new String(txValue, "UTF-8");
                            //Log.d("DCAR", "Received: " + text);
                            //Log.d("DCAR", "Received: " + bytesToHex(txValue));

                            String text = new String(txValue, "UTF-8");
                            //Log.d(TAG, "Received: " + text);
                            //Log.d(TAG, "Received: " + txValue.length);

                            if (currentFragmentClass ==  BLE_FRAGMENT_INDEX) {

                            }else if(currentFragmentClass == WIFI_FRAGMENT_INDEX){
                                ((FragmentWifiSetting) fragment).receiveBLEData(text);
                            }else{

                            }


                            //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            //listAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                            //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Lỗi kết nối. Vui lòng TẮT và MỞ LẠI chương trình!");
                //setupReconnectTimer();
                mService.disconnect();
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        goFullscreen();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void connectBLE(String address) {
        String deviceAddress = address;
        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

        Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
        mService.connect(deviceAddress);
        Log.d(TAG, "Bluetooth connection...");
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }


    public void goFullscreen() {
        setUiFlags(true);
        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions |= View.STATUS_BAR_HIDDEN;

        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void exitFullscreen() {

        //setUiFlags(false);

    }

    /**
     * Correctly sets up the fullscreen flags to avoid popping when we switch
     * between fullscreen and not
     */
    private void initUiFlags() {
        int flags = View.SYSTEM_UI_FLAG_VISIBLE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.setSystemUiVisibility(flags);
            //decorView.setOnSystemUiVisibilityChangeListener(fullScreenListener);
        }
    }

    /**
     * Applies the correct flags to the windows decor view to enter
     * or exit fullscreen mode
     *
     * @param fullscreen True if entering fullscreen mode
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUiFlags(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(fullscreen ? getFullscreenUiFlags() : View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    /**
     * Determines the appropriate fullscreen flags based on the
     * systems API version.
     *
     * @return The appropriate decor view flags to enter fullscreen mode when supported
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getFullscreenUiFlags() {
        int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        return flags;
    }


    @Override
    public void onDownloadApkToUpdate() {

    }

    public static final int REQUEST_INSTALL = 12345;

    private void installApk() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Intent i = new Intent();
//                i.setAction(Intent.ACTION_INSTALL_PACKAGE);
//
//                i.setDataAndType(Uri.fromFile(new File(NPNConstants.apkUpdate)), "application/vnd.android.package-archive");
//                Log.d("NPNapp", "About to install new .apk");
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getApplicationContext().startActivity(i);
//            }
//        });

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(Uri.fromFile(new File(NPNConstants.apkUpdate)));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationInfo().packageName);
        startActivityForResult(intent, REQUEST_INSTALL);

    }

    public void requestAPKRepo(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        final Request request = builder.url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONObject objObject = new JSONObject(data);
                        int versionCode = objObject.getInt("versionCode");
                        String mess = objObject.getString("message");
                        String link = objObject.getString("link");
                        Log.d(TAG, "Parse data:" + versionCode + mess + link);
                        if (versionCode > BuildConfig.VERSION_CODE) {
                            new UpdateAPK(mContext, new UpdateAPK.UpdateAPKListener() {
                                @Override
                                public void onDownloadApkToUpdate() {
                                    displayUpdateDialog();
                                }
                            }).execute(link);
                        }
                    } catch (Exception e) {
                    }
                    Log.d(TAG, data);
                }
            }
        });

    }

    Timer mUpdateTimer;

    private void setupUpdateTimer() {
        mUpdateTimer = new Timer();
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                if (Helper.checkLanConnected(mContext) || Helper.checkWifiConnected(mContext)) {
                    requestAPKRepo(NPNConstants.mainUrl);
                    mUpdateTimer.cancel();
                }
            }
        };
        mUpdateTimer.schedule(aTask, 5000, 60000);
    }


    public void displayUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("ĐỒNG Ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //installApk();
            }
        });
        builder.setNegativeButton("HỦY BỎ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_alert_dialog, null);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private final int DATA_CHECKING = 50;
    private TextToSpeech niceTTS;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            niceTTS.setLanguage(Locale.getDefault());
            Log.d("NPNLauncher", "set language here");

        }
    }

    public void talkToMe(String sentence) {
        //Log.d("NPNLauncher", "Talk to me: " + sentence);
        String speakWords = sentence;
        niceTTS.speak(speakWords, TextToSpeech.QUEUE_FLUSH, null);
    }




    List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds

    private boolean mScanning;
    ListView newDevicesListView = null;
    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        //final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtAdapter.stopLeScan(mLeScanCallback);

                    //cancelButton.setText(R.string.scan);

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBtAdapter.startLeScan(mLeScanCallback);
            //cancelButton.setText(R.string.cancel);
        } else {
            mScanning = false;
            mBtAdapter.stopLeScan(mLeScanCallback);
            //cancelButton.setText(R.string.scan);
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            addDevice(device,rssi);
                        }
                    });
                }
            };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }


        devRssiValues.put(device.getAddress(), rssi);
        if(rssi > -60)
            Log.d(TAG, device.getAddress() + "  " + rssi);
        if (!deviceFound) {
            deviceList.add(device);
            //mEmptyList.setVisibility(View.GONE);

            deviceAdapter.notifyDataSetChanged();
        }
    }


    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
            Log.d(TAG, deviceList.get(position).getAddress());

//            Intent result = new Intent();
//            result.putExtras(b);
//            setResult(Activity.RESULT_OK, result);
            connectBLE(deviceList.get(position).getAddress());
            loading_next.setVisibility(View.VISIBLE);
            newDevicesListView.setEnabled(false);
//            finish();

        }
    };

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("RSSI: " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText("MAC: " + device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);

            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }
}

