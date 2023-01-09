package com.nordicsemi.nrfUARTv2.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by KAI on 1/8/2023.
 */

public class RegisterModel {
    @SerializedName("type")
    private String type;
    @SerializedName("error")
    private String error;
    @SerializedName("data")
    private List<RegisterMemory> data;

    public void RegisterMemory(){

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

    public List<RegisterMemory> getData() {
        return data;
    }

    public void setData(List<RegisterMemory> data) {
        this.data = data;
    }

    public class RegisterMemory{
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("enable")
        private boolean enable;
        @SerializedName("data")
        private int[] data;

        public void RegisterMemory(){
            setData(new int[8]);
            for (int i = 0; i < 1; i++){
                getData()[i] = i;
            }
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
}
