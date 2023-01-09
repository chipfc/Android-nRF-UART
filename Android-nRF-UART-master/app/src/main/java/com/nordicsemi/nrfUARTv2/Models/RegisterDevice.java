package com.nordicsemi.nrfUARTv2.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by KAI on 1/8/2023.
 */

public class RegisterDevice {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("enable")
    private boolean enable;
    @SerializedName("data")
    private int[] data;

    public RegisterDevice(){
        data = new int[8];
        for (int i = 0; i < 8; i++){
            data[i] = i;
        }
        enable = true;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }
}
