
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

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MainActivity extends Activity implements TextToSpeech.OnInitListener, RadioGroup.OnCheckedChangeListener, View.OnTouchListener, View.OnClickListener, UpdateAPK.UpdateAPKListener, LocationListener {
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

    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend;
    private EditText edtMessage;

    LocationManager locationManager;
    ////////Add source code/////////
    DeviceModel[] deviceModels = new DeviceModel[]{
            /** Board 1 - Motor */
            new DeviceModel((byte) 0x01, (byte) 0xf0), /* 0. LEFT_TABLE           */
            new DeviceModel((byte) 0x01, (byte) 0xf1), /* 1. RIGHT_TABLE          */
            new DeviceModel((byte) 0x01, (byte) 0xf2), /* 2. CONTROL_PANEL        */

            /** Board 2 - Motor */
            new DeviceModel((byte) 0x02, (byte) 0xf0), /* 3. CURTAIN_REAR_LEFT    */
            new DeviceModel((byte) 0x02, (byte) 0xf1), /* 4. CURTAIN_REAR_CENTER  */
            new DeviceModel((byte) 0x02, (byte) 0xf2), /* 5. CURTAIN_REAR_RIGHT   */

            /** Board 3 - Motor */
            new DeviceModel((byte) 0x03, (byte) 0xf0), /* 6. CURTAIN_FRONT_LEFT   */
            new DeviceModel((byte) 0x03, (byte) 0xf1), /* 7. CURTAIN_FRONT_RIGHT  */
            new DeviceModel((byte) 0x03, (byte) 0xf2), /* 8. TV                   */

            /** Board 4 - Relay */
            new DeviceModel((byte) 0x04, (byte) 0xf0), /* 9.  LEFT_SEAT_A         */
            new DeviceModel((byte) 0x04, (byte) 0xf1), /* 10. LEFT_SEAT_B         */
            new DeviceModel((byte) 0x04, (byte) 0xf2), /* 11. LIGHT_4X            */

            /** Board 5 - Relay */
            new DeviceModel((byte) 0x05, (byte) 0xf0), /* 12. RIGHT_SEAT_A        */
            new DeviceModel((byte) 0x05, (byte) 0xf1), /* 13. RIGHT_SEAT_B        */
            new DeviceModel((byte) 0x05, (byte) 0xf2), /* 14. LIGHT_SIDE          */

            /** Board 6 - Relay */
            new DeviceModel((byte) 0x06, (byte) 0xf0), /* 15. LIGHT_CEILING       */
            new DeviceModel((byte) 0x06, (byte) 0xf1), /* 16. LIGHT_DRAWERS       */
            new DeviceModel((byte) 0x06, (byte) 0xf2), /* 17. LIGHT_WATER_DROP    */

    };
    private static final int INDEX_LEFT_SEAT_A = 9;
    private static final int INDEX_LEFT_SEAT_B = 10;
    private static final int INDEX_RIGHT_SEAT_A = 12;
    private static final int INDEX_RIGHT_SEAT_B = 13;

    private static final int INDEX_LIGHT_SIDE = 14;
    private static final int INDEX_LIGHT_4X = 11;
    private static final int INDEX_LIGHT_CEILING = 15;
    private static final int INDEX_LIGHT_DRAWERS = 16;
    private static final int INDEX_LIGHT_WATER_DROP = 17;

    private static final int INDEX_CURTAIN_FRONT_LEFT = 6;
    private static final int INDEX_CURTAIN_FRONT_RIGHT = 7;
    private static final int INDEX_CURTAIN_REAR_LEFT = 3;
    private static final int INDEX_CURTAIN_REAR_RIGHT = 5;
    private static final int INDEX_CURTAIN_REAR_CENTER = 4;

    private static final int INDEX_LEFT_TABLE = 0;
    private static final int INDEX_CENTER_TABLE = 0;  // DCAR3
    private static final int INDEX_RIGHT_TABLE = 1;

    private static final int INDEX_TV = 8;

    private static final int INDEX_CONTROL_PANEL = 2;

    // Left Seat
    ImageView imgLeftSeat;
    ImageButton btnLeftSeatUp;
    ImageButton btnLeftSeatDown;

    ImageView imgRightSeat;
    ImageButton btnRightSeatUp;
    ImageButton btnRightSeatDown;

    ImageView imgCurtainFrontLeft;
    ImageButton btnCertainFrontLeftUp;
    ImageButton btnCertainFrontLeftDown;

    ImageView imgCurtainFrontRight;
    ImageButton btnCertainFrontRightUp;
    ImageButton btnCertainFrontRightDown;

    ImageView imgCurtainRearLeft;
    ImageButton btnCurtainRearLeftUp;
    ImageButton btnCurtainRearLeftDown;

    ImageView imgCurtainRearRight;
    ImageButton btnCurtainRearRightUp;
    ImageButton btnCurtainRearRightDown;

    ImageView imgCurtainRearCenter;
    ImageButton btnCurtainRearCenterUp;
    ImageButton btnCurtainRearCenterDown;

    ImageView imgLeftTable;
    ImageView imgRightTable;
    ImageView imgTV;

    ImageView imgCenterTable;
    ImageButton btnCenterTableUp;
    ImageButton btnCenterTableDown;

    ImageView imgLightCeiling;
    ImageView imgLightTv;
    ImageView imgLightSkyStars;
    ImageView imgLightLed;
    ImageView imgLightWaterDrop;

    ImageButton imgLightSkyStarsMask;

    String ipAddress = "";

    Button btnSetting;
    Button btnCode;
    ImageButton btnHomeSearchVoice;
    Context mContext;
    ////////////////////////////////
    ///////Methods and Function////

    private static final int UI_DCAR1 = 100;
    private static final int UI_DCAR3 = 300;
    int currentUI = -1;

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
        String cmd = "";
        int vid = v.getId();

        if (vid == imgLightCeiling.getId()) {

            if (deviceModels[INDEX_LIGHT_SIDE].isOn()) {
                imgLightCeiling.setActivated(false);

                deviceModels[INDEX_LIGHT_SIDE].setOff();
                deviceModels[INDEX_LIGHT_SIDE].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_LIGHT_SIDE_OFF;

            } else {
                imgLightCeiling.setActivated(true);

                deviceModels[INDEX_LIGHT_SIDE].setOn();
                deviceModels[INDEX_LIGHT_SIDE].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_LIGHT_SIDE_ON;

            }

        }
        else if (vid == imgLightTv.getId() && currentUI == UI_DCAR1 ) {

            if (deviceModels[INDEX_LIGHT_4X].isOn()) {
                imgLightTv.setActivated(false);

                deviceModels[INDEX_LIGHT_4X].setOff();
                deviceModels[INDEX_LIGHT_4X].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_LIGHT_TOP_OFF;

            } else {
                imgLightTv.setActivated(true);

                deviceModels[INDEX_LIGHT_4X].setOn();
                deviceModels[INDEX_LIGHT_4X].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_LIGHT_TOP_ON;
            }
        }
        else if ((vid == imgLightSkyStars.getId() || vid == imgLightSkyStarsMask.getId()) && currentUI == UI_DCAR1) {

            if (deviceModels[INDEX_LIGHT_CEILING].isOn()) {
                imgLightSkyStars.setActivated(false);


                deviceModels[INDEX_LIGHT_CEILING].setOff();
                deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_LIGHT_CEILLING_OFF;
            } else {
                imgLightSkyStars.setActivated(true);

                deviceModels[INDEX_LIGHT_CEILING].setOn();
                deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_LIGHT_CEILLING_ON;
            }

        }
        else if ((vid == imgLightSkyStars.getId() || vid == imgLightSkyStarsMask.getId()
                || vid == imgLightTv.getId()) && currentUI == UI_DCAR3) {

            if (deviceModels[INDEX_LIGHT_CEILING].isOn()) {
                imgLightSkyStars.setActivated(false);
                imgLightTv.setActivated(false);

                deviceModels[INDEX_LIGHT_CEILING].setOff();
                deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_LIGHT_CEILLING_OFF;
            } else {
                imgLightSkyStars.setActivated(true);
                imgLightTv.setActivated(true);

                deviceModels[INDEX_LIGHT_CEILING].setOn();
                deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_LIGHT_CEILLING_ON;
            }

        } else if (vid == imgLightLed.getId()) {

            if (deviceModels[INDEX_LIGHT_DRAWERS].isOn()) {
                imgLightLed.setActivated(false);

                deviceModels[INDEX_LIGHT_DRAWERS].setOff();
                deviceModels[INDEX_LIGHT_DRAWERS].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_LIGHT_DRAWERS_OFF;
            } else {
                imgLightLed.setActivated(true);

                deviceModels[INDEX_LIGHT_DRAWERS].setOn();
                deviceModels[INDEX_LIGHT_DRAWERS].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_LIGHT_DRAWERS_ON;
            }

        } else if (vid == imgLightWaterDrop.getId()) {

            if (deviceModels[INDEX_LIGHT_WATER_DROP].isOn()) {
                imgLightWaterDrop.setActivated(false);

                deviceModels[INDEX_LIGHT_WATER_DROP].setOff();
                deviceModels[INDEX_LIGHT_WATER_DROP].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_LIGHT_WATER_DROP_OFF;


            } else {
                imgLightWaterDrop.setActivated(true);

                deviceModels[INDEX_LIGHT_WATER_DROP].setOn();
                deviceModels[INDEX_LIGHT_WATER_DROP].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_LIGHT_WATER_DROP_ON;


            }

        } else if (vid == imgLeftTable.getId()) {

            if (deviceModels[INDEX_LEFT_TABLE].isOn()) {
                imgLeftTable.setActivated(false);

                deviceModels[INDEX_LEFT_TABLE].setOff();
                deviceModels[INDEX_LEFT_TABLE].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_TABLE_LEFT_OFF;
            } else {
                imgLeftTable.setActivated(true);

                deviceModels[INDEX_LEFT_TABLE].setOn();
                deviceModels[INDEX_LEFT_TABLE].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_TABLE_LEFT_ON;
            }
            deviceModels[INDEX_LEFT_TABLE].setNeedStop(40000);

        } else if (vid == imgRightTable.getId()) {
            if (deviceModels[INDEX_RIGHT_TABLE].isOn()) {
                imgRightTable.setActivated(false);

                deviceModels[INDEX_RIGHT_TABLE].setOff();
                deviceModels[INDEX_RIGHT_TABLE].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_TABLE_RIGHT_OFF;
            } else {
                imgRightTable.setActivated(true);

                deviceModels[INDEX_RIGHT_TABLE].setOn();
                deviceModels[INDEX_RIGHT_TABLE].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_TABLE_RIGHT_ON;
            }
            deviceModels[INDEX_RIGHT_TABLE].setNeedStop(40000);

        } else if (vid == imgTV.getId()) {

            if (deviceModels[INDEX_TV].isOn()) {
                imgTV.setActivated(false);

                deviceModels[INDEX_TV].setOff();
                deviceModels[INDEX_TV].configForSendData(SPEED_LOW, 500);
                cmd = NPNConstants.CMD_TIVI_OFF;

            } else {
                imgTV.setActivated(true);

                deviceModels[INDEX_TV].setOn();
                deviceModels[INDEX_TV].configForSendData(SPEED_HIGH, 500);
                cmd = NPNConstants.CMD_TIVI_ON;
            }
            deviceModels[INDEX_TV].setNeedStop(12000);

        }

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

        if (vid == btnLeftSeatUp.getId()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgLeftSeat.setActivated(true);
                deviceModels[INDEX_LEFT_SEAT_A].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY_SEAT);
                cmd = NPNConstants.CMD_LEFT_SEAT_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgLeftSeat.setActivated(false);
                deviceModels[INDEX_LEFT_SEAT_A].configForSendData(SPEED_LOW, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_LEFT_SEAT_STOP;
            }

        } else if (vid == btnLeftSeatDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgLeftSeat.setActivated(true);
                deviceModels[INDEX_LEFT_SEAT_B].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY_SEAT);
                cmd = NPNConstants.CMD_LEFT_SEAT_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgLeftSeat.setActivated(false);
                deviceModels[INDEX_LEFT_SEAT_B].configForSendData(SPEED_LOW, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_LEFT_SEAT_STOP;
            }

        } else if (vid == btnRightSeatUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgRightSeat.setActivated(true);
                deviceModels[INDEX_RIGHT_SEAT_A].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY_SEAT);
                cmd = NPNConstants.CMD_RIGHT_SEAT_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgRightSeat.setActivated(false);
                deviceModels[INDEX_RIGHT_SEAT_A].configForSendData(SPEED_LOW, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_RIGHT_SEAT_STOP;
            }

        } else if (vid == btnRightSeatDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgRightSeat.setActivated(true);
                deviceModels[INDEX_RIGHT_SEAT_B].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY_SEAT);
                cmd = NPNConstants.CMD_RIGHT_SEAT_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgRightSeat.setActivated(false);
                deviceModels[INDEX_RIGHT_SEAT_B].configForSendData(SPEED_LOW, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_RIGHT_SEAT_STOP;
            }

        } else if (vid == btnCertainFrontLeftUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainFrontLeft.setActivated(true);
                deviceModels[INDEX_CURTAIN_FRONT_LEFT].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_LEFT_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainFrontLeft.setActivated(false);
                deviceModels[INDEX_CURTAIN_FRONT_LEFT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_LEFT_STOP;
            }

        } else if (vid == btnCertainFrontLeftDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainFrontLeft.setActivated(true);
                deviceModels[INDEX_CURTAIN_FRONT_LEFT].configForSendData(SPEED_LOW, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_LEFT_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainFrontLeft.setActivated(false);
                deviceModels[INDEX_CURTAIN_FRONT_LEFT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_LEFT_STOP;
            }

        } else if (vid == btnCertainFrontRightUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainFrontRight.setActivated(true);
                deviceModels[INDEX_CURTAIN_FRONT_RIGHT].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_RIGHT_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainFrontRight.setActivated(false);
                deviceModels[INDEX_CURTAIN_FRONT_RIGHT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_RIGHT_STOP;
            }

        } else if (vid == btnCertainFrontRightDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainFrontRight.setActivated(true);
                deviceModels[INDEX_CURTAIN_FRONT_RIGHT].configForSendData(SPEED_LOW, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_RIGHT_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainFrontRight.setActivated(false);
                deviceModels[INDEX_CURTAIN_FRONT_RIGHT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_FRONT_RIGHT_STOP;
            }

        } else if (vid == btnCurtainRearLeftUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainRearLeft.setActivated(true);
                deviceModels[INDEX_CURTAIN_REAR_LEFT].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_LEFT_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainRearLeft.setActivated(false);
                deviceModels[INDEX_CURTAIN_REAR_LEFT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_LEFT_STOP;
            }

        } else if (vid == btnCurtainRearLeftDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainRearLeft.setActivated(true);
                deviceModels[INDEX_CURTAIN_REAR_LEFT].configForSendData(SPEED_LOW, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_LEFT_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainRearLeft.setActivated(false);
                deviceModels[INDEX_CURTAIN_REAR_LEFT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_LEFT_STOP;
            }

        } else if (vid == btnCurtainRearRightUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainRearRight.setActivated(true);
                deviceModels[INDEX_CURTAIN_REAR_RIGHT].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_RIGHT_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainRearRight.setActivated(false);
                deviceModels[INDEX_CURTAIN_REAR_RIGHT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_RIGHT_STOP;
            }

        } else if (vid == btnCurtainRearRightDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainRearRight.setActivated(true);
                deviceModels[INDEX_CURTAIN_REAR_RIGHT].configForSendData(SPEED_LOW, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_RIGHT_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainRearRight.setActivated(false);
                deviceModels[INDEX_CURTAIN_REAR_RIGHT].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_RIGHT_STOP;
            }

        } else if (vid == btnCurtainRearCenterUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainRearCenter.setActivated(true);
                deviceModels[INDEX_CURTAIN_REAR_CENTER].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_CENTER_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainRearCenter.setActivated(false);
                deviceModels[INDEX_CURTAIN_REAR_CENTER].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_CENTER_STOP;
            }

        } else if (vid == btnCurtainRearCenterDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCurtainRearCenter.setActivated(true);
                deviceModels[INDEX_CURTAIN_REAR_CENTER].configForSendData(SPEED_LOW, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_CENTER_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCurtainRearCenter.setActivated(false);
                deviceModels[INDEX_CURTAIN_REAR_CENTER].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_CURTAIN_REAR_CENTER_STOP;
            }

        } else if (vid == btnCenterTableUp.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCenterTable.setActivated(true);
                deviceModels[INDEX_CENTER_TABLE].configForSendData(SPEED_HIGH, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_TABLE_CENTER_UP;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCenterTable.setActivated(false);
                deviceModels[INDEX_CENTER_TABLE].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_TABLE_CENTER_STOP;
            }

        } else if (vid == btnCenterTableDown.getId()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imgCenterTable.setActivated(true);
                deviceModels[INDEX_CENTER_TABLE].configForSendData(SPEED_LOW, TOUCH_ACTION_DOWN_DELAY);
                cmd = NPNConstants.CMD_TABLE_CENTER_DOWN;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imgCenterTable.setActivated(false);
                deviceModels[INDEX_CENTER_TABLE].configForSendData(SPEED_STOP, TOUCH_ACTION_UP_DELAY);
                cmd = NPNConstants.CMD_TABLE_CENTER_STOP;
            }

        }

        Log.d(TAG, "CMD is:" + cmd);
        if (cmd.length() > 0) {
            cmdQueue.add(cmd);
            //sendBLEData("#" + cmd + "!");
        }
        return false;
    }
    private List<String> cmdQueue = new ArrayList<>();
    Timer queueTimer;
    TimerTask queueTask;
    private void setupQueueTimer(){
        queueTimer = new Timer();
        queueTask = new TimerTask() {
            @Override
            public void run() {
                if(cmdQueue.size() > 2){
                    for(int i = 0; i < cmdQueue.size() - 2; i++){
                        cmdQueue.remove(0);
                    }
                }
                if(cmdQueue.size() > 0){
                    String cmd = cmdQueue.get(0);
                    sendBLEData("#" + cmd + "!");
                    cmdQueue.remove(0);
                }
            }
        };
        queueTimer.schedule(queueTask,100,200);
    }


    void initDeviceFirstState() {

        deviceModels[INDEX_LIGHT_4X].setOn();
        //deviceModels[INDEX_LIGHT_4X].configForSendData(SPEED_HIGH, 50);
        imgLightTv.setActivated(true);

        deviceModels[INDEX_LIGHT_CEILING].setOn();
        //deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_HIGH, 50);
        imgLightSkyStars.setActivated(true);

        deviceModels[INDEX_LIGHT_DRAWERS].setOn();
        //deviceModels[INDEX_LIGHT_DRAWERS].configForSendData(SPEED_HIGH, 50);
        imgLightLed.setActivated(true);

        deviceModels[INDEX_LIGHT_SIDE].setOn();
        //deviceModels[INDEX_LIGHT_SIDE].configForSendData(SPEED_HIGH, 50);
        imgLightCeiling.setActivated(true);

        imgLightWaterDrop.setActivated(true);




        /* Always turn on all lights */
//        deviceModels[INDEX_LIGHT_4X].setOn();
//        deviceModels[INDEX_LIGHT_4X].configForSendData(SPEED_HIGH, 1000);
//        imgLightTv.setActivated(true);
//
//        deviceModels[INDEX_LIGHT_CEILING].setOn();
//        deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_HIGH, 1000);
//        imgLightSkyStars.setActivated(true);
//
//        deviceModels[INDEX_LIGHT_DRAWERS].setOn();
//        deviceModels[INDEX_LIGHT_DRAWERS].configForSendData(SPEED_HIGH, 1000);
//        imgLightLed.setActivated(true);
//
//        deviceModels[INDEX_LIGHT_SIDE].setOn();
//        deviceModels[INDEX_LIGHT_SIDE].configForSendData(SPEED_HIGH, 1000);
//        imgLightCeiling.setActivated(true);

        /* Always close all curtains */
        deviceModels[INDEX_CURTAIN_FRONT_LEFT].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_CURTAIN_FRONT_LEFT].setNeedStop(15000);

        deviceModels[INDEX_CURTAIN_FRONT_RIGHT].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_CURTAIN_FRONT_RIGHT].setNeedStop(15000);

        deviceModels[INDEX_CURTAIN_REAR_LEFT].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_CURTAIN_REAR_LEFT].setNeedStop(15000);

        deviceModels[INDEX_CURTAIN_REAR_RIGHT].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_CURTAIN_REAR_RIGHT].setNeedStop(15000);

        deviceModels[INDEX_CURTAIN_REAR_CENTER].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_CURTAIN_REAR_CENTER].setNeedStop(15000);

        /* Remember last state of TV */
        deviceModels[INDEX_TV].setOff();
        deviceModels[INDEX_TV].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_TV].setNeedStop(12000);
        imgTV.setActivated(false);

        deviceModels[INDEX_LEFT_TABLE].setOff();
        deviceModels[INDEX_LEFT_TABLE].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_LEFT_TABLE].setNeedStop(40000);
        imgLeftTable.setActivated(false);

        deviceModels[INDEX_RIGHT_TABLE].setOff();
        deviceModels[INDEX_RIGHT_TABLE].configForSendData(SPEED_LOW, 1000);
        deviceModels[INDEX_RIGHT_TABLE].setNeedStop(40000);
        imgRightTable.setActivated(false);
    }


    private void startVoiceInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói \"MỞ ĐÈN\" hoặc \"MỞ CỬA SỔ\"");


        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }


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
                        String ubc_voice = result.get(0).toLowerCase();
                        final String data_voice = ubc_voice;
                        String messageSocket = "";
                        Log.d("DCAR_TEST", "Message: " + ubc_voice);
                        if (ubc_voice.indexOf("1") >= 0 && ubc_voice.indexOf("4") >= 0 && ubc_voice.indexOf("7") >= 0) {

                            String mode = Helper.loadTVCode((MainActivity) mContext, NPNConstants.SETTING_UI);
                            if (mode.equals("100") == true) mode = "300";
                            else mode = "100";
                            Helper.saveTVCode((MainActivity) mContext, NPNConstants.SETTING_UI, mode);
                            if (Helper.loadTVCode((MainActivity) mContext, NPNConstants.SETTING_UI).equals("300") == true) {
                                //initUI(UI_DCAR3);
                            } else {
                                //initUI(UI_DCAR1);
                            }

                        } else if ((ubc_voice.indexOf("mở") >= 0 && ubc_voice.indexOf("rèm") >= 0)
                                || (ubc_voice.indexOf("mở") >= 0 && ubc_voice.indexOf("game") >= 0)
                                || (ubc_voice.indexOf("mở") >= 0 && ubc_voice.indexOf("cửa") >= 0)) {
                            messageSocket = "DCAR_CURTAIN_UP";
                        } else if ((ubc_voice.indexOf("đóng") >= 0 && ubc_voice.indexOf("rèm") >= 0)
                                || (ubc_voice.indexOf("đón") >= 0 && ubc_voice.indexOf("rằm") >= 0)
                                || (ubc_voice.indexOf("đóng") >= 0 && ubc_voice.indexOf("cửa") >= 0)) {
                            messageSocket = "DCAR_CURTAIN_DOWN";
                        } else if (ubc_voice.indexOf("tắt") >= 0 && ubc_voice.indexOf("đèn") >= 0) {
                            messageSocket = "DCAR_LED_OFF";
                            updateUIVoiceCommand(messageSocket);
                        } else if (ubc_voice.indexOf("mở") >= 0 && ubc_voice.indexOf("đèn") >= 0 ||
                                ubc_voice.indexOf("bật") >= 0 && ubc_voice.indexOf("đèn") >= 0) {
                            messageSocket = "DCAR_LED_ON";
                            updateUIVoiceCommand(messageSocket);
                        } else if (ubc_voice.indexOf("truyền hình") >= 0 || ubc_voice.indexOf("tivi") >= 0 || ubc_voice.indexOf("ti vi") >= 0) {
                            if (ubc_voice.indexOf("mở") >= 0 || ubc_voice.indexOf("bật") >= 0) {
                                //((MainActivity)(getActivity())).sendKeyCommand("DCAR_TIVI_ON");
                                messageSocket = "TIVI_ON";
                            } else if (ubc_voice.indexOf("tắt") >= 0 || ubc_voice.indexOf("đóng") >= 0) {
                                //((MainActivity)(getActivity())).sendKeyCommand("DCAR_TIVI_OFF");
                                messageSocket = "TIVI_OFF";
                            }
                        }

                        if (messageSocket.length() > 0) {
                            Log.d(TAG, "Mess from tablet: " + messageSocket);
                            sendBLEData("#" + messageSocket + "!");
                            if (messageSocket.equals("DCAR_CURTAIN_UP")) {
                                talkToMe("Cửa sổ đang mở");
                            } else if (messageSocket.equals("DCAR_CURTAIN_DOWN")) {
                                talkToMe("Cửa sổ đang đóng");
                            } else if (messageSocket.equals("DCAR_LED_ON")) {
                                talkToMe("Đèn đang bật");
                            } else if (messageSocket.equals("DCAR_LED_OFF")) {
                                talkToMe("Đèn đang tắt");
                            } else if (messageSocket.equals("TIVI_ON")) {
                                talkToMe("Đang khởi động tivi");
                            } else if (messageSocket.equals("TIVI_OFF")) {
                                talkToMe("Đang đóng tivi");
                            }
                        } else {
                            //Log.d(TAG, "Error code 1");
                            talkToMe("Tôi không hiểu lệnh này, xin vui lòng thử lại!");
                        }

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

    public void updateUIVoiceCommand(String cmd) {
        final String mess = cmd;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (mess) {
                    case "DCAR_LED_ON":
                        deviceModels[INDEX_LIGHT_4X].setOn();
                        //deviceModels[INDEX_LIGHT_4X].configForSendData(SPEED_HIGH, 50);
                        imgLightTv.setActivated(true);

                        deviceModels[INDEX_LIGHT_CEILING].setOn();
                        //deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_HIGH, 50);
                        imgLightSkyStars.setActivated(true);

                        deviceModels[INDEX_LIGHT_DRAWERS].setOn();
                        //deviceModels[INDEX_LIGHT_DRAWERS].configForSendData(SPEED_HIGH, 50);
                        imgLightLed.setActivated(true);

                        deviceModels[INDEX_LIGHT_SIDE].setOn();
                        //deviceModels[INDEX_LIGHT_SIDE].configForSendData(SPEED_HIGH, 50);
                        imgLightCeiling.setActivated(true);

                        imgLightWaterDrop.setActivated(true);
                        break;

                    case "DCAR_LED_OFF":
                        deviceModels[INDEX_LIGHT_4X].setOff();
                        //deviceModels[INDEX_LIGHT_4X].configForSendData(SPEED_LOW, 50);
                        imgLightTv.setActivated(false);

                        deviceModels[INDEX_LIGHT_CEILING].setOff();
                        //deviceModels[INDEX_LIGHT_CEILING].configForSendData(SPEED_LOW, 50);
                        imgLightSkyStars.setActivated(false);

                        deviceModels[INDEX_LIGHT_DRAWERS].setOff();
                        //deviceModels[INDEX_LIGHT_DRAWERS].configForSendData(SPEED_LOW, 50);
                        imgLightLed.setActivated(false);

                        deviceModels[INDEX_LIGHT_SIDE].setOff();
                        //deviceModels[INDEX_LIGHT_SIDE].configForSendData(SPEED_LOW, 50);
                        imgLightCeiling.setActivated(false);
                        imgLightWaterDrop.setActivated(false);
                        break;
                }
            }
        });
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




        initUiFlags();
        goFullscreen();

        checkLocationPermission();

        populateList();


    }

    private void sendBLEData(String message) {
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
                connectBLE(strBLEMac);
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
                    connectBLE(strBLEMac);
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

    private boolean isBLEConnected = false;
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
                        isBLEConnected = true;
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
                            Log.d("DCAR", "Received: " + bytesToHex(txValue));

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
    private void populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
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

