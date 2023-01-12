package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.hardware.input.InputManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nordicsemi.nrfUARTv2.Models.RegisterDevice;

import java.util.ArrayList;
import java.util.List;

import static com.nordicsemi.nrfUARTv2.NPNConstants.WIFI_FRAGMENT_INDEX;

/**
 * Created by KAI on 12/17/2022.
 */

public class FragmentRegisterSetting extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_setting, container, false);
        newDevicesListRegisterView = view.findViewById(R.id.list_register);
        populateListRegister();
        ImageButton btnAdd = view.findViewById(R.id.btnAddReg);
        ImageButton btnRemove = view.findViewById(R.id.btnRemoveReg);
        ImageButton btnConfirm = view.findViewById(R.id.btnConfirmRegs);
        ImageButton btnBack = view.findViewById(R.id.btnBackFreg);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).selectFragment(WIFI_FRAGMENT_INDEX);
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceListRegister.add(new RegisterDevice());
                deviceAdapter.notifyDataSetChanged();
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(deviceListRegister.size() > 0){
                    deviceListRegister.remove(deviceListRegister.size() - 1);
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(int i = 0; i < deviceListRegister.size(); i++){
                    deviceListRegister.get(i).setId(i);
                }

                Gson gson = new Gson();
                String json = gson.toJson(deviceListRegister);
                Log.d("nRFUART", json);
            }
        });

        return view;
    }
    List<RegisterDevice> deviceListRegister;
    RegisterAdapter deviceAdapter;
    ListView newDevicesListRegisterView;
    private void populateListRegister() {
        /* Initialize device list container */

        deviceListRegister = new ArrayList<RegisterDevice>();
        deviceAdapter = new RegisterAdapter(getActivity(), deviceListRegister);


        //newDevicesListWifiView = (ListView) getActivity().findViewById(R.id.list_wifi);
        newDevicesListRegisterView.setAdapter(deviceAdapter);

        //newDevicesListRegisterView.setOnItemClickListener(mWifiClickListener);
        RegisterDevice firstDevice = new RegisterDevice();
        firstDevice.setData(new int[]{3, 3, 0, 0, 0, 1, 133, 232});
        deviceListRegister.add(firstDevice);

        RegisterDevice secondDevice = new RegisterDevice();
        secondDevice.setData(new int[]{3, 3, 0, 1, 0, 1, 212, 40});
        deviceListRegister.add(new RegisterDevice());
        //deviceListRegister.add(new RegisterDevice());

        deviceAdapter.notifyDataSetChanged();

    }

    class RegisterAdapter extends BaseAdapter {
        Context context;
        List<RegisterDevice> devices;
        LayoutInflater inflater;
        public RegisterAdapter(Context context, List<RegisterDevice> devices) {
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
                vg = (ViewGroup) inflater.inflate(R.layout.register_element, null);
            }

            final RegisterDevice device = devices.get(position);
            final EditText txt0 = (EditText) vg.findViewById(R.id.txt0);
            final EditText txt1 = (EditText) vg.findViewById(R.id.txt1);
            final EditText txt2 = (EditText) vg.findViewById(R.id.txt2);
            final EditText txt3 = (EditText) vg.findViewById(R.id.txt3);

            final EditText txt4 = (EditText) vg.findViewById(R.id.txt4);
            final EditText txt5 = (EditText) vg.findViewById(R.id.txt5);
            final EditText txt6 = (EditText) vg.findViewById(R.id.txt6);
            final EditText txt7 = (EditText) vg.findViewById(R.id.txt7);

            CheckBox chkEnable = (CheckBox)vg.findViewById(R.id.chkEnable);

            txt0.setText(device.getData()[0] + "");
            txt1.setText(device.getData()[1] + "");
            txt2.setText(device.getData()[2] + "");
            txt3.setText(device.getData()[3] + "");

            txt4.setText(device.getData()[4] + "");
            txt5.setText(device.getData()[5] + "");
            txt6.setText(device.getData()[6] + "");
            txt7.setText(device.getData()[7] + "");

            chkEnable.setChecked(device.isEnable());

            txt0.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[0] = Integer.parseInt(txt0.getText().toString());
                }
            });
            txt1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[1] = Integer.parseInt(txt1.getText().toString());
                }
            });
            txt2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[2] = Integer.parseInt(txt2.getText().toString());
                }
            });
            txt3.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[3] = Integer.parseInt(txt3.getText().toString());
                }
            });
            txt4.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[4] = Integer.parseInt(txt4.getText().toString());
                }
            });
            txt5.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[5] = Integer.parseInt(txt5.getText().toString());
                }
            });
            txt6.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[6] = Integer.parseInt(txt6.getText().toString());
                }
            });
            txt7.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    device.getData()[7] = Integer.parseInt(txt7.getText().toString());
                }
            });

            return vg;
        }
    }
}
