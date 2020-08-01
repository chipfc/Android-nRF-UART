package com.nordicsemi.nrfUARTv2;

/**
 * Created by Le Trong Nhan on 01/08/2020.
 */

public class DeviceModel {
    public static final int MAX_SEND_ERROR_COUNT = 3;

    public static final int SYS_TIMER_PERIOD = 50;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_SEND = 1;
    public static final int STATUS_WAIT = 4;

    public static final byte SPEED_HIGH = (byte) (0xff);
    public static final byte SPEED_LOW = (byte) (0x00);
    public static final byte SPEED_STOP = (byte) (0x7f);

    private byte speed;
    private byte[] data;
    private int dfaState;
    private int timer_cn;
    private boolean timeOut;
    private int errorCount;
    private boolean on;
    private boolean needStop;
    private int stopTimeOut;
    private byte speedStop;

    public void setSpeed(byte speed) {
        this.speed = speed;
        if (this.data.length >= 3)
            this.data[2] = speed;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataString() {
        return Helper.bytesArrayToHexString(data, data.length);
    }

    public void setDFAState(int dfaState) {
        this.dfaState = dfaState;
    }

    public int getDFAState() {
        return dfaState;
    }

    public void setTimer(int timer_cn) {
        this.timer_cn = timer_cn / SYS_TIMER_PERIOD;
        this.timeOut = false;
    }

    public void timerCountDown() {
        if (timer_cn > 0) {
            timer_cn--;
            if (timer_cn == 0) timeOut = true;
        }
    }

    public boolean isTimeOut() {
        return timeOut;
    }

    public void resetErrorCount() {
        this.errorCount = 0;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void errorCountUp() {
        errorCount++;
    }

    public void setOn() {
        this.on = true;
    }

    public void setOff() {
        this.on = false;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isNeedStop() {
        return needStop;
    }

    public byte getSpeedStop() {
        return speedStop;
    }

    public void setNeedStop(boolean needStop) {
        this.needStop = needStop;
    }

    public void setNeedStop(int stopTimeOut) {
        setNeedStop(stopTimeOut, SPEED_STOP);
    }

    public void setNeedStop(int stopTimeOut, byte speedStop) {
        this.needStop = true;
        this.stopTimeOut = stopTimeOut;
        this.speedStop = speedStop;
    }

    public int getStopTimeOut() {
        return stopTimeOut;
    }

    public void configForSendData(byte speed, int delay) {
        this.setSpeed(speed);
        this.setDFAState(STATUS_SEND);
        this.setTimer(delay);
        this.resetErrorCount();
    }

    // constructor
    public DeviceModel() {
        this((byte) (0x01), (byte) (0xF0));
    }

    public DeviceModel(byte address, byte port) {
        dfaState = STATUS_IDLE;
        speed = SPEED_STOP;
        data = new byte[]{address, port, speed};
        errorCount = 0;
        timer_cn = 0;
        timeOut = false;
        on = false;
        needStop = false;
        speedStop = SPEED_STOP;
    }
}
