package com.ceiv.communication;

import android.os.Environment;
import android.util.Log;

import com.ceiv.communication.utils.DeviceInfo;
import com.ceiv.communication.utils.DeviceInfoUtils;
import com.ceiv.communication.utils.SystemInfoUtils;


/**
 * Created by zhangdawei on 2018/8/16.
 */

public class SystemInitThread implements Runnable {

    private final static String TAG = "SystemInitThread";

    private final static String deviceInfoXmlName = "DeviceInfo.xml";

    private DeviceInfo deviceInfo = null;

    private SystemInfoUtils.ApplicationOperation applicationOperation;

    //SystemInfoUtils.ApplicationOperation该接口目的是为了能够获取上下文Context，Activity
    //主要用在发送广播、截图等功能
    public SystemInitThread(SystemInfoUtils.ApplicationOperation applicationOperation) {
        this.applicationOperation = applicationOperation;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public void run() {

        Log.d(TAG, "SystemInitThread start... ");

        //读取本地配置文件，没有的话生成默认的配置。
        String configFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + deviceInfoXmlName;
        DeviceInfoUtils.setDeviceInfoFilePath(configFile);
        deviceInfo = DeviceInfoUtils.getDeviceInfoFromFile();
        if (null == deviceInfo) {
            Log.d(TAG, "Can't get Device information from file:" + configFile);
//            return;
        }

        //检查本地媒体目录，如果没有相关目录，则创建
        if (!DeviceInfoUtils.checkDeviceMediaDirectory()) {
            Log.e(TAG, "check device media directory error!");
            return;
        }

        //默认调试模式关闭
        SystemInfoUtils.debugModeInit();

        //开启组播线程
        Log.d(TAG, "Start MulticastThread... ");
        new MulticastThread("238.10.21.100", 8999, deviceInfo, applicationOperation).start();

    }
}
