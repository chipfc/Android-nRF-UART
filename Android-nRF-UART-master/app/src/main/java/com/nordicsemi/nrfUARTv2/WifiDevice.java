package com.nordicsemi.nrfUARTv2;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by KAI on 1/6/2023.
 */

public class WifiDevice {




    @SerializedName("data")
    private WifiData data;
    @SerializedName("error")
    private String error;
    @SerializedName("type")
    private String type;
    public WifiData getData() {
        return data;
    }

    public void setData(WifiData data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public class WifiData{



        @SerializedName("auth")
        private int auth;
        @SerializedName("password")
        private String password;
        @SerializedName("ssid")
        private String ssid;


        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public int getAuth() {
            return auth;
        }

        public void setAuth(int auth) {
            this.auth = auth;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

