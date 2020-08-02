package com.nordicsemi.nrfUARTv2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Le Trong Nhan on 01/08/2020.
 */

public class Helper {
    private static final String TAG = "Helper";

    public static final int SYS_TIMER_PERIOD = 50;

    public static final int TOUCH_ACTION_UP_DELAY = 100;
    public static final int TOUCH_ACTION_DOWN_DELAY = 500;

    public static final int TOUCH_ACTION_DOWN_DELAY_SEAT = 50;

    public static final String SOCKET_IO_SERVER_URL = "https://socket1.npnlab.com";
    public static final String SOCKET_IO_PRIVATE_KEY = "NfpHS0OfHqpDxFqMFcdb0WGB7qo5LDam4ScAuDd7t83wpntS47vPr6sbbTxD2Ye2ZSlzE63K9Y5doUDrJ3to4L0T4NUbvdppFxJbV7RPVvba36f4Sxo6TqqUFmYQgUvg";
    public static final String SOCKET_IO_EVENT_COMMAND = "tv_remote_command_fw";

    public static String SOCKET_IO_CODE;

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String hashTvCode(String hexCode) {
        String hash = "";
        final int length = 6;
        if (hexCode.isEmpty()) {
            Random random = new Random();
            int d;
            for (int i = 0; i < length; i++) {
                d = random.nextInt(10);
                hash = hash + d;
            }
        } else {
            if (hexCode.length() > length)
                hexCode = hexCode.substring(0, length);
            long code = Long.parseLong(hexCode, 16);
            hash = code + "000000";
            hash = hash.substring(0, length);
        }

        return hash;
    }

    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesArrayToHexString(byte[] bytes, int noBytes) {

        // make sure that: len=min(noBytes,bytes.length)
        int len = noBytes;
        if (len > bytes.length)
            len = bytes.length;

        char[] hexChars = new char[len * 2];
        for (int j = 0; j < len; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }


    private static Map<String, String> getCpuInfoMap() {
        Map<String, String> map = new HashMap<>();
        try {
            Scanner s = new Scanner(new File("/proc/cpuinfo"));
            while (s.hasNextLine()) {
                String[] vals = s.nextLine().split(": ");
                if (vals.length > 1) map.put(vals[0].trim(), vals[1].trim());
            }
        } catch (Exception e) {
            Log.e(TAG, "getCpuInfoMap: " + e.getMessage());
        }
        return map;
    }

    public static String getCPUSerial() {
        String strData = getCpuInfoMap().toString();
        try {
            String[] splitData = strData.split(Pattern.quote(","));
            for (String splitDatum : splitData) {
                Log.d(TAG, "getCPUSerial: " + splitDatum);
                if (splitDatum.length() > 1 && splitDatum.contains("Serial")) {
                    String[] splitSerial = splitDatum.split(Pattern.quote("="));
                    return splitSerial[1];
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCPUSerial: " + e.getMessage());
        }
        return "00000000";
    }

    public static String getDeviceCode() {
        String cpuSerial = getCPUSerial();
        try {
            if (cpuSerial.length() > 8)
                cpuSerial = cpuSerial.substring(cpuSerial.length() - 8);

            String cpuCode = "" + Long.parseLong(cpuSerial, 16);

            if (cpuCode.length() > 8)
                cpuCode = cpuCode.substring(cpuCode.length() - 8);

            if (cpuCode.length() == 8) {
                cpuCode = cpuCode.substring(0, 4) + "-" + cpuCode.substring(4);
            }
            return cpuCode;
        } catch (Exception e) {
            Log.e(TAG, "getDeviceCode: " + e.getMessage());
        }

        return "0000-0000";
    }


    public static void saveTVCode(Activity activity, String key, String code) {
        if (key.isEmpty()) return;
        SharedPreferences settings = activity.getSharedPreferences(NPNConstants.SETTING_REFKEY_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, code);
        editor.commit();
    }

    public static String loadTVCode(Activity activity, String key) {
        SharedPreferences settings = activity.getSharedPreferences(NPNConstants.SETTING_REFKEY_NAME, Context.MODE_PRIVATE);

        return settings.getString(key, "00000");

    }
}
