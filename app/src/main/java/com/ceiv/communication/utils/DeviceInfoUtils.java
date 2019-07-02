package com.ceiv.communication.utils;

import android.os.Environment;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by zhangdawei on 2018/8/16.
 */

public class DeviceInfoUtils {

    private final static String TAG = "DeviceInfoUtils";

    public final static String MediaPath = "media";
    public final static String VideoPath = "video";
    public final static String PicturePath = "picture";
    public final static String TextPath = "text";

    public final static int IdentifyLen = 20;

    private static String configFilePath = null;
    private final static String defaultContent =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<AppConfig>\n" +
                    "\t<Identify>0123456789ab00200701</Identify> <!-- 002线路ID， 002站点双程号， 01屏号 -->\n" +
                    "\t<ServerIp>192.168.1.100</ServerIp>\n" +
                    "\t<ServerPort>8899</ServerPort>\n" +
                    "\t<InfoPublishServer>tcp://192.168.1.11:1883</InfoPublishServer>\n" +
                    "\t<DevType>11</DevType> <!-- 11:videoPost 12:signPost 13:linePost 十六进制 -->\n" +
                    "\t<CurrentStationID>2</CurrentStationID>\n" +
                    "\t<NextStationID>2</NextStationID>\n" +
                    "\t<ThemeStyle>1</ThemeStyle> <!-- 1默认， 2镜像， 3第二主题， 4第二主题镜像 -->\n" +
                    "\t<PicDispTime>5000</PicDispTime> <!-- microsecond -->\n" +
                    "\t<VideoPlayTime>600</VideoPlayTime> <!-- second, default invalid 0 -->\n" +
                    "</AppConfig>\n";

    private final static String formatString =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<AppConfig>\n" +
                    "\t<Identify>%s</Identify> <!-- 0011站台号， 0003屏号 -->\n" +
                    "\t<ServerIp>%s</ServerIp>\n" +
                    "\t<ServerPort>%d</ServerPort>\n" +
                    "\t<InfoPublishServer>%s</InfoPublishServer>\n" +
                    "\t<DevType>%x</DevType> <!-- 11:videoPost 12:signPost 13:linePost 十六进制 -->\n" +
                    "\t<CurrentStationID>%d</CurrentStationID>\n" +
                    "\t<NextStationID>%d</NextStationID>\n" +
                    "\t<ThemeStyle>%d</ThemeStyle> <!-- 1默认， 2镜像， 3第二主题， 4第二主题镜像 -->\n" +
                    "\t<PicDispTime>%d</PicDispTime> <!-- microsecond -->\n" +
                    "\t<VideoPlayTime>%d</VideoPlayTime> <!-- second, default invalid 0 -->\n" +
                    "</AppConfig>\n";

    public static void setDeviceInfoFilePath(String path) {
        configFilePath = path;
    }

    //解析设备配置xml文件
    public static DeviceInfo getDeviceInfoFromFile() {

        if (null == configFilePath) {
            Log.d(TAG, "must run \"setDeviceInfoFilePath\" at first!");
            return null;
        }

        File devInfoXmlFile = new File(configFilePath);
        if (!devInfoXmlFile.exists()) {
            try {
                File fileParent = devInfoXmlFile.getParentFile();
                if (fileParent != null) {
                    if (!fileParent.exists()) {
                        fileParent.mkdirs();
                    }
                }
                devInfoXmlFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Create new deviceInfoFile failed");
                e.printStackTrace();
                return null;
            }

            boolean writeSuccess = true;
            FileWriter writer = null;
            try {
                writer = new FileWriter(devInfoXmlFile, false);
                writer.write(defaultContent);
                writer.flush();
            } catch (IOException e) {
                Log.e(TAG, "Write default deviceInfo to XmlFile failed");
                e.printStackTrace();
                writeSuccess = false;
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close deviceInfo file failed");
                    e.printStackTrace();
                    writeSuccess = false;
                }
            }
            if (!writeSuccess) {
                return null;
            }
        }

        Long fileLen = devInfoXmlFile.length();
        byte[] fileContent = new byte[fileLen.intValue()];
        try {
            FileInputStream fileInputStream = new FileInputStream(devInfoXmlFile);
            fileInputStream.read(fileContent);
            fileInputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Read deviceInfo xml file failed");
            e.printStackTrace();
            return null;
        }

        DeviceInfo deviceInfo = new DeviceInfo();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(new String(fileContent, "UTF-8")));

            int eventType = xmlPullParser.getEventType();
            Log.d(TAG, "start parse xml");
            while (eventType != xmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        Log.d(TAG, "start doc");
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("Identify")) {
                            String identify = xmlPullParser.nextText();
                            Log.d(TAG, "Identify:" + identify);
                            deviceInfo.setIdentify(identify.trim());
                        } else if (xmlPullParser.getName().equals("ServerIp")) {
                            String serverIp = xmlPullParser.nextText();
                            Log.d(TAG, "ServerIp:" + serverIp);
                            deviceInfo.setServerIp(serverIp.trim());
                        } else if (xmlPullParser.getName().equals("ServerPort")) {
                            int serverPort = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "ServerPort:" + serverPort);
                            deviceInfo.setServerPort(serverPort);
                        } else if (xmlPullParser.getName().equals("InfoPublishServer")) {
                            String infoPublishServer = xmlPullParser.nextText();
                            Log.d(TAG, "InfoPublishServer:" + infoPublishServer);
                            deviceInfo.setInfoPublishServer(infoPublishServer.trim());
                        } else if (xmlPullParser.getName().equals("DevType")) {
                            //十六进制转换
                            int devType = Integer.valueOf(xmlPullParser.nextText(), 16);
                            Log.d(TAG, "DevType:" + devType);
                            deviceInfo.setDevType(devType);
                        } else if (xmlPullParser.getName().equals("CurrentStationID")) {
                            int currentStationID = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "CurrentStationID:" + currentStationID);
                            deviceInfo.setCurrentStationId(currentStationID);
                        } else if (xmlPullParser.getName().equals("NextStationID")) {
                            int nextStationID = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "NextStationID:" + nextStationID);
                            deviceInfo.setNextStationId(nextStationID);
                        } else if (xmlPullParser.getName().equals("ThemeStyle")) {
                            int themeStyle = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "ThemeStyle:" + themeStyle);
                            deviceInfo.setThemeStyle(themeStyle);
                        } else if (xmlPullParser.getName().equals("PicDispTime")) {
                            int picDispTime = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "PicDispTime:" + picDispTime);
                            deviceInfo.setPicDispTime(picDispTime);
                        } else if (xmlPullParser.getName().equals("VideoPlayTime")) {
                            int videoPlayTime = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "VideoPlayTime:" + videoPlayTime);
                            deviceInfo.setVideoPlayTime(videoPlayTime);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse XML file error!");
            e.printStackTrace();
            return null;
        }
        Log.d(TAG, "Read complete Info? " + (deviceInfo.isInfoComplete() ? "yes" : "no"));
        if (!deviceInfo.isInfoComplete()) {
            return null;
        }
        return deviceInfo;
    }

    public static boolean saveDeviceInfoToFile(DeviceInfo deviceInfo) {

        if (null == configFilePath) {
            Log.d(TAG, "must run \"setDeviceInfoFilePath\" at first!");
            return false;
        }

        File file = new File(configFilePath);
        if (!file.exists()) {
            try {
                File fileParent = file.getParentFile();
                if (fileParent != null) {
                    if (!fileParent.exists()) {
                        fileParent.mkdirs();
                    }
                }
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Create new deviceInfoFile failed");
                e.printStackTrace();
                return false;
            }
        }

        FileWriter fileWriter = null;
        try {
            //以覆盖形式写入
            fileWriter = new FileWriter(file, false);
            //构建配置文本
            String content = String.format(formatString, deviceInfo.identify, deviceInfo.serverIp, deviceInfo.serverPort,
                    deviceInfo.infoPublishServer, deviceInfo.devType, deviceInfo.currentStationId,
                    deviceInfo.nextStationId, deviceInfo.themeStyle, deviceInfo.picDispTime, deviceInfo.videoPlayTime);

            fileWriter.write(content);
            fileWriter.flush();
        } catch (IOException e) {
            Log.d(TAG, "write device information to xml file failed");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                Log.d(TAG, "close device information file failed");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean checkDeviceMediaDirectory() {

        if (!SystemInfoUtils.isExternalStorageAvailable()) {
            Log.e(TAG, "Device media directory unavailable!");
            return false;
        }

        String videoFullPath = Environment.getExternalStorageDirectory() + "/" + MediaPath + "/" + VideoPath;
        String pictureFullPath = Environment.getExternalStorageDirectory() + "/" + MediaPath + "/" + PicturePath;
        String textFullPath = Environment.getExternalStorageDirectory() + "/" + MediaPath + "/" + TextPath;

        File[] files = new File[]{new File(videoFullPath), new File(pictureFullPath), new File(textFullPath)};

        for (File tmp : files) {
            if (tmp.exists()) {
                //如果已经存在
                if (!tmp.isDirectory()) {
                    //如果不是目录
                    tmp.delete();
                    tmp.mkdirs();
                }
            } else {
                //如果不存在
                tmp.mkdirs();
            }
        }

        return true;
    }

    public static int getStationIDFromIdentify(String identify)
    {
        int len = identify.length();
        Log.d(TAG, "Identify: " + identify);
        if (IdentifyLen != len) {
            Log.d(TAG, "Invalid Identify!");
            return 1;
        }
        String sub = identify.substring(len - 5, len - 2);
        Log.d(TAG, "substring:" + sub);
        Log.d(TAG, "stationID:" + Integer.valueOf(sub));
        return Integer.valueOf(identify.substring(len - 5, len - 2));
    }

}
