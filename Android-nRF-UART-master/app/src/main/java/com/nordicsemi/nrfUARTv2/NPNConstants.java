package com.nordicsemi.nrfUARTv2;

import android.os.Environment;

/**
 * Created by Le Trong Nhan on 01/08/2020.
 */

public class NPNConstants {
    public static final String apiHeaderKey = "User-Agent";
    public static final String apiHeaderValue = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36 NPNLab";

    public static final String rootStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";//    "/storage/sdcard0/";

    public static final String mainUrl = "https://ubc.sgp1.cdn.digitaloceanspaces.com/dcar_files/BLE_UART_TABLET/ble_version.txt";

    public static final String apkUpdate = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/dcar_update.apk";

    public static final String CMD_LIGHT_SIDE_ON = "LIGHT_SIDE_ON";
    public static final String CMD_LIGHT_SIDE_OFF = "LIGHT_SIDE_OFF";

    public static final String CMD_LIGHT_TOP_ON = "LIGHT_TOP_ON";
    public static final String CMD_LIGHT_TOP_OFF = "LIGHT_TOP_OFF";

    //public static final String CMD_LIGHT_WATER_DROP_ON = "LIGHT_DROP_ON";
    //public static final String CMD_LIGHT_WATER_DROP_OFF = "LIGHT_DROP_OFF";

    public static final String CMD_LIGHT_WATER_DROP_ON = "LIGHT_TOP_ON";
    public static final String CMD_LIGHT_WATER_DROP_OFF = "LIGHT_TOP_OFF";


    public static final String CMD_LIGHT_CEILLING_ON = "LIGHT_CEIL_ON";
    public static final String CMD_LIGHT_CEILLING_OFF = "LIGHT_CEIL_OFF";

    public static final String CMD_LIGHT_DRAWERS_ON = "LIGHT_DRAWERS_ON";
    public static final String CMD_LIGHT_DRAWERS_OFF = "LIGHT_DRAWERS_OFF";

    public static final String CMD_TABLE_LEFT_ON = "TABLE_LEFT_ON";
    public static final String CMD_TABLE_LEFT_OFF = "TABLE_LEFT_OFF";
    public static final String CMD_TABLE_LEFT_STOP = "TABLE_LEFT_STOP";

    public static final String CMD_TABLE_RIGHT_ON = "TABLE_RIGHT_ON";
    public static final String CMD_TABLE_RIGHT_OFF = "TABLE_RIGHT_OFF";
    public static final String CMD_TABLE_RIGHT_STOP = "TABLE_RIGHT_STOP";

    //public static final String CMD_TABLE_CENTER_UP = "TABLE_CENTER_UP";
    //public static final String CMD_TABLE_CENTER_DOWN = "TABLE_CENTER_DOWN";
    //public static final String CMD_TABLE_CENTER_STOP = "TABLE_CENTER_STOP";

    public static final String CMD_TABLE_CENTER_UP = "TABLE_LEFT_ON";
    public static final String CMD_TABLE_CENTER_DOWN = "TABLE_LEFT_OFF";
    public static final String CMD_TABLE_CENTER_STOP = "TABLE_LEFT_STOP";



    public static final String CMD_TIVI_ON = "TIVI_ON";
    public static final String CMD_TIVI_OFF = "TIVI_OFF";

    public static final String CMD_LEFT_SEAT_UP = "LEFT_SEAT_UP";
    public static final String CMD_LEFT_SEAT_DOWN = "LEFT_SEAT_DOWN";
    public static final String CMD_LEFT_SEAT_STOP = "LEFT_SEAT_STOP";

    public static final String CMD_RIGHT_SEAT_UP = "RIGHT_SEAT_UP";
    public static final String CMD_RIGHT_SEAT_DOWN = "RIGHT_SEAT_DOWN";
    public static final String CMD_RIGHT_SEAT_STOP = "RIGHT_SEAT_STOP";


    public static final String CMD_CURTAIN_FRONT_LEFT_UP = "CUR_FRONT_LT_UP";
    public static final String CMD_CURTAIN_FRONT_LEFT_DOWN = "CUR_FRONT_LT_DOWN";
    public static final String CMD_CURTAIN_FRONT_LEFT_STOP = "CUR_FRONT_LT_STOP";

    public static final String CMD_CURTAIN_FRONT_RIGHT_UP = "CUR_FRONT_RT_UP";
    public static final String CMD_CURTAIN_FRONT_RIGHT_DOWN = "CUR_FRONT_RT_DOWN";
    public static final String CMD_CURTAIN_FRONT_RIGHT_STOP = "CUR_FRONT_RT_STOP";


    public static final String CMD_CURTAIN_REAR_LEFT_UP = "CUR_REAR_LT_UP";
    public static final String CMD_CURTAIN_REAR_LEFT_DOWN = "CUR_REAR_LT_DOWN";
    public static final String CMD_CURTAIN_REAR_LEFT_STOP = "CUR_REAR_LT_STOP";

    public static final String CMD_CURTAIN_REAR_RIGHT_UP = "CUR_REAR_RT_UP";
    public static final String CMD_CURTAIN_REAR_RIGHT_DOWN = "CUR_REAR_RT_DOWN";
    public static final String CMD_CURTAIN_REAR_RIGHT_STOP = "CUR_REAR_RT_STOP";


    public static final String CMD_CURTAIN_REAR_CENTER_UP = "CUR_REAR_CR_UP";
    public static final String CMD_CURTAIN_REAR_CENTER_DOWN = "CUR_REAR_CR_DOWN";
    public static final String CMD_CURTAIN_REAR_CENTER_STOP = "CUR_REAR_CR_STOP";

    public static String SETTING_REFKEY_NAME = "dcar_settings";

    public static String SETTING_BLE_MAC = "setting_tv_mac";
    public static String SETTING_UI = "setting_ui";


    public static final int BLE_FRAGMENT_INDEX = 14;
    public static final int WIFI_FRAGMENT_INDEX = 15;
    public static final int REGISTER_FRAGMENT_INDEX = 16;

}
