package com.ceiv.communication.utils;

/**
 * Created by zhangdawei on 2018/8/16.
 */

public class DeviceInfo {

    public DeviceInfo() {
        flag = 0x0;
    }

    // bit0
    public String identify;
    // bit1
    public String serverIp;
    // bit2
    public int serverPort;
    // bit3
    public String infoPublishServer;
    // bit4
    public int devType;
    // bit5
    public int currentStationId;
    // bit6
    public int nextStationId;
    // bit7
    public int themeStyle;
    // bit8
    public int picDispTime;
    // bit9
    public int videoPlayTime;


    // long 为64位，可表示64个字段的状态， 0表示未设置， 1表示已设置
    private long flag;

    public void setIdentify(String identify) {
            if (null == identify || "".equals(identify)) {
                this.identify = null;
                flag &= ~(0x01L);
            } else {
                this.identify = identify;
                flag |= 0x01;
            }
    }

    public void setServerIp(String serverIp) {
        if (null == serverIp || "".equals(serverIp)) {
            this.serverIp = null;
            flag &= ~(0x02L);
        } else {
            this.serverIp = serverIp;
            flag |= 0x02;
        }
    }

    public void setServerPort(int serverPort) {
        if (serverPort <= 0) {
            this.serverPort = -1;
            flag &= ~(0x04L);
        } else {
            this.serverPort = serverPort;
            flag |= 0x04;
        }
    }

    public void setInfoPublishServer(String infoPublishServer) {
        if (null == infoPublishServer || "".equals(infoPublishServer)) {
            this.infoPublishServer = null;
            flag &= ~(0x08L);
        } else {
            this.infoPublishServer = infoPublishServer;
            flag |= 0x08;
        }
    }

    public void setDevType(int devType) {
        if (devType <= 0) {
            this.devType = -1;
            flag &= ~(0x10L);
        } else {
            this.devType = devType;
            flag |= 0x10;
        }
    }

    public void setCurrentStationId(int currentStationId) {
        if (currentStationId < 0) {
            this.currentStationId = -1;
            flag &= ~(0x20L);
        } else {
            this.currentStationId = currentStationId;
            flag |= 0x20;
        }
    }

    public void setNextStationId(int nextStationId) {
        if (nextStationId < 0) {
            this.nextStationId = -1;
            flag &= ~(0x40L);
        } else {
            this.nextStationId = nextStationId;
            flag |= 0x40;
        }
    }

    public void setThemeStyle(int themeStyle) {
        if (1 != themeStyle && 2 != themeStyle && 3 != themeStyle && 4 != themeStyle) {
            this.themeStyle = -2;
            flag &= ~(0x80L);
        } else {
            this.themeStyle = themeStyle;
            flag |= 0x80;
        }
    }

    public void setPicDispTime(int picDispTime) {
        if (picDispTime <= 0) {
            this.picDispTime = -1;
            flag &= ~(0x100L);
        } else {
            this.picDispTime = picDispTime;
            flag |= 0x100;
        }
    }

    public void setVideoPlayTime(int videoPlayTime) {
        if (videoPlayTime <= 0) {
            this.videoPlayTime = -1;
            flag &= ~(0x200L);
        } else {
            this.videoPlayTime = videoPlayTime;
            flag |= 0x200;
        }
    }

    public boolean isInfoComplete() {
        return (0x3ff == flag);
    }

}
