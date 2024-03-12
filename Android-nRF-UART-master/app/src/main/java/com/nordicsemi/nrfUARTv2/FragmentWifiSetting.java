package com.nordicsemi.nrfUARTv2;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.nordicsemi.nrfUARTv2.NPNConstants.BLE_FRAGMENT_INDEX;
import static com.nordicsemi.nrfUARTv2.NPNConstants.REGISTER_FRAGMENT_INDEX;
import static org.xmlpull.v1.XmlPullParser.TEXT;

/**
 * Created by KAI on 12/17/2022.
 */

public class FragmentWifiSetting extends Fragment implements View.OnClickListener{

    Button btnTest;
    int timer_counter = 0;
    int timer_flag = 0;
    ImageButton btnBack, btnNext, btnRefresh;
    TimePicker picker;
    TextView txtGyroX, txtGyroY, txtGyroZ;
    TextView txtAccelX, txtAccelY, txtAccelZ;
    TextView txtDistance;

    private void setTimer(int duration){
        timer_flag = 0;
        timer_counter = duration;
    }

    private void timerRun(){
        if(timer_counter > 0){
            timer_counter --;
            if(timer_counter == 0)
                timer_flag = 1;
        }
    }
    int status = 0;
    List<String> results = new ArrayList<>();
    private void DFARun(){
        switch (status){
            case 0:
                if(results.size() > 0) {
                    sendBLEData(results.get(0));
                    setTimer(3);
                    status = 1;
                    results.remove(0);
                }
                break;
            case 1:
                if(timer_flag == 1){
                    status = 0;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_setting, container, false);

        btnTest = view.findViewById(R.id.btnTest);
        newDevicesListWifiView = view.findViewById(R.id.list_wifi);
        btnTest.setOnClickListener(this);
        populateListWifi();

        btnBack = view.findViewById(R.id.btnBackWifi);
        btnNext = view.findViewById(R.id.btnNextWifi);
        btnRefresh = view.findViewById(R.id.btnRefreshWifi);

        btnBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);


        picker =(TimePicker)view.findViewById(R.id.timePicker);
        picker.setIs24HourView(true);


        final Timer initTimer = new Timer();
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        validJsonData = "";
                        sendBLEData("{\"type\":0}");
                        status_recv = 0;
                        deviceListWifi.clear();
                        deviceAdapter.notifyDataSetChanged();
                        Log.d("nRFUART", "Init the system");
                    }
                });


                initTimer.cancel();
            }
        };
        //initTimer.schedule(aTask, 2000, 300000);


        Timer DFATimer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //DFARun();
                timerRun();
            }
        };
        DFATimer.schedule(timerTask, 1000,100);

        results.clear();

        txtGyroX = view.findViewById(R.id.txtGyroX);
        txtGyroY = view.findViewById(R.id.txtGyroY);
        txtGyroZ = view.findViewById(R.id.txtGyroZ);


        txtAccelX = view.findViewById(R.id.txtAccelX);
        txtAccelY = view.findViewById(R.id.txtAccelY);
        txtAccelZ = view.findViewById(R.id.txtAccelZ);

        txtDistance = view.findViewById(R.id.txtDistance);

        return view;
    }


    private void sendBLEData(String data){
        //((MainActivity)getActivity()).sendMessage(2);
        ((MainActivity)getActivity()).sendMessage(data);
    }
    String validJsonData = "";
    private static final Gson gson = new Gson();
    public void receiveBLEData(String data){
        Log.d("nRFUART", "Fragment: " + data);
        validJsonData = validJsonData + data;
        try {
            if(status_recv == 0) {

                WifiDevice objWifiDevice = gson.fromJson(validJsonData, WifiDevice.class);
                Log.d("nRFUART", "Valid Json: " + validJsonData);
                //Log.d("nRFUART", "Parse Json: " + objWifiDevice.getData().getSsid());

                validJsonData = "";

                deviceListWifi.add(objWifiDevice);
                deviceAdapter.notifyDataSetChanged();
            }else if(status_recv == 1){
                WifiDevice objWifiDevice = gson.fromJson(validJsonData, WifiDevice.class);
                Log.d("nRFUART", "Valid json2: " + validJsonData);
                if(objWifiDevice.getError().contains("Success")){
                    Log.d("nRFUART", "Wifi connected successfully!!!");
                    ((MainActivity)getActivity()).selectFragment(REGISTER_FRAGMENT_INDEX);
                }
                validJsonData = "";
            }

        } catch(com.google.gson.JsonSyntaxException ex) {
            Log.d("nRFUART", "Invalid Json: " + validJsonData);
            //Log.d("nRFUART", "Error: " + ex.getMessage());
            int num = Integer.parseInt(data);
            picker.setHour(num/60);
            picker.setMinute(num%60);
        }
    }
    List<WifiDevice> deviceListWifi;
    WifiAdapter deviceAdapter;
    ListView newDevicesListWifiView;

    private void populateListWifi() {
        /* Initialize device list container */

        deviceListWifi = new ArrayList<WifiDevice>();
        deviceAdapter = new WifiAdapter(getActivity(), deviceListWifi);


        //newDevicesListWifiView = (ListView) getActivity().findViewById(R.id.list_wifi);
        newDevicesListWifiView.setAdapter(deviceAdapter);
        newDevicesListWifiView.setOnItemClickListener(mWifiClickListener);
        //newDevicesListView.setOnItemClickListener(mDeviceClickListener);

    }
    int test_counter = 11;
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btnTest){
            validJsonData = "";
            sendBLEData("{\"type\":10}");
            deviceListWifi.clear();
            deviceAdapter.notifyDataSetChanged();
            //showPasswordDialog("123");
        }else if(view.getId() == R.id.btnBackWifi){
            //((MainActivity)(getActivity())).selectFragment(BLE_FRAGMENT_INDEX);

            validJsonData = "";
            test_counter = -1;
            sendBLEData("{\"read\":" + test_counter + "}");
            test_counter ++;
            status_recv = 0;
            deviceListWifi.clear();
            deviceAdapter.notifyDataSetChanged();

        }else if(view.getId() == R.id.btnNextWifi){
            //((MainActivity)(getActivity())).selectFragment(REGISTER_FRAGMENT_INDEX);

            validJsonData = "";
            test_counter = picker.getHour() * 60 + picker.getMinute();
            sendBLEData("{\"type\":" + test_counter + "}");
            test_counter ++;
            status_recv = 0;
            deviceListWifi.clear();
            deviceAdapter.notifyDataSetChanged();
        }else if(view.getId() == R.id.btnRefreshWifi){
            validJsonData = "";
            test_counter = 0;
            sendBLEData("{\"type\":" + test_counter + "}");
            test_counter ++;
            status_recv = 0;
            deviceListWifi.clear();
            deviceAdapter.notifyDataSetChanged();
        }
    }
    private int status_recv = 0;

    private void sendBLEWifiData(String username, String pass, int possition){
        Log.d("nRFUART", username + "     " + pass);
        deviceListWifi.get(possition).getData().setPassword(pass);
        deviceListWifi.get(possition).setType("1");
//        Gson gson = new Gson();
//        String json = gson.toJson( deviceListWifi.get(possition));

        String json2 = "{" +
                "\"type\": 1," +
                "\"data\": {" +
                "\"ssid\": \"AAAAAA\"," +
                "\"password\": \"BBBBBB\"," +
                "\"auth\": CCCCCC" +
                "}" +
                "}";

        //List<String> results = SplitStringEveryNthChar.usingPattern(TEXT, 5);


        String  a =  Arrays.toString(json2.split("(?<=\\G.{4})"));



        json2 = json2.replaceAll("AAAAAA", deviceListWifi.get(possition).getData().getSsid());
        json2 = json2.replaceAll("BBBBBB", deviceListWifi.get(possition).getData().getPassword());
        json2 = json2.replaceAll("CCCCCC", deviceListWifi.get(possition).getData().getAuth() + "");
        //sendBLEData(json2);
        Log.d("nRFUART",json2);



        int length = json2.length();
        int n = 20;

        for (int i = 0; i < length; i += n) {
            results.add(json2.substring(i, Math.min(length, i + n)));
        }
        status_recv = 1;
//        for(int i = 0; i < results.size(); i++){
//            try {
//                //Log.d("nRFUART", "Splitter: " + results.get(i));
//                sendBLEData(results.get(i));
//                Thread.sleep(500);
//            }catch (Exception e){}
//        }
    }

    private AdapterView.OnItemClickListener mWifiClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            WifiDevice device = deviceListWifi.get(position);
            showPasswordDialog(device.getData().getSsid(), position);
        }
    };

    class WifiAdapter extends BaseAdapter {
        Context context;
        List<WifiDevice> devices;
        LayoutInflater inflater;

        public WifiAdapter(Context context, List<WifiDevice> devices) {
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
                vg = (ViewGroup) inflater.inflate(R.layout.wifi_list, null);
            }

            WifiDevice device = devices.get(position);
            final TextView wifi_name = ((TextView) vg.findViewById(R.id.wifi_name));
            final TextView wifi_status = ((TextView) vg.findViewById(R.id.wifi_status));
            final TextView wifi_rssi = (TextView) vg.findViewById(R.id.wifi_rssi);

            wifi_name.setText(device.getData().getSsid());
            wifi_rssi.setText(device.getData().getAuth() + "");

            wifi_name.setTextColor(Color.WHITE);
            wifi_rssi.setTextColor(Color.WHITE);

            return vg;
        }
    }


    public static Point getScreenSize(Activity activity) {
        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        return screenSize;
    }
    public void showPasswordDialog(final String code, final int possition) {
        String content = code;//server.getIpAddress();
        try {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_password);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


            Point size = getScreenSize(getActivity());
            //dialog.getWindow().setLayout((int) ((float) size.x * 0.5), (int) ((float) size.y * 1.0));

            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
            //TextView txtQR = dialog.findViewById(R.id.text_serial);
            //txtQR.setText(content);
            Button btnOK = dialog.findViewById(R.id.btn_ok);
            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView txtPass = dialog.findViewById(R.id.txtPassword);
                    String pass = txtPass.getText().toString();
                    sendBLEWifiData(code, pass, possition);
                    dialog.cancel();

                }
            });
            Button btnCancel = dialog.findViewById(R.id.btn_close);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });

            dialog.show();

        } catch (Throwable t) {
            //Log.e(TAG, "create QR code fail. ");
        }
    }
    public void updateIMUData(byte[] data){
        if(data.length >=6){
            txtGyroX.setText(data[0] + "");
            txtGyroY.setText(data[1] + "");
            txtGyroZ.setText(data[2] + "");


            txtAccelX.setText(data[3] + "");
            txtAccelY.setText(data[4] + "");
            txtAccelZ.setText(data[5] + "");

        }
    }
    public long getUnsignedInt(int x) {
        return x & (-1L >>> 32);
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
    public void updateDistanceData(byte[] data){
        if(data.length >= 4){

            int V0 = data[0] & 0xFF;
            int V1 = data[1] & 0xFF;
            int V2 = data[2] & 0xFF;
            int V3 = data[3] & 0xFF;

            int distance = V3 + (V2 << 8) + (V1 << 16) + (V0 << 24);
            double value = Float.intBitsToFloat(distance) ;
            //Log.d("DCAR", "Data " + data[0] + "**" + data[1] + "**" + data[2] + "**" + data[3]);
            //Log.d("DCAR", "Distance is "  + bytesToHex(data));

            txtDistance.setText( String.format("%.2f", value));
        }
    }

}
