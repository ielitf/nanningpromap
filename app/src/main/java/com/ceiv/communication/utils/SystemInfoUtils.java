package com.ceiv.communication.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Created by zhangdawei on 2018/8/15.
 */

public class SystemInfoUtils {

    private final static String TAG = "SystemInfoUtils";

    public static final String UPDATE_APK_NAME = "update.apk";

    public final static String ACTION_CONFIG_IP = "com.ceiv.CONFIG_IP";
    public final static String ACTION_UPDATE_APK = "com.ceiv.UPDATE_APK";
//    public final static String ACTION_CHANGE_BL = "com.ceiv.CHANGE_BL";
//    public final static String ACTION_REBOOT = "com.ceiv.REBOOT";

    //调试模式是否开启
    private static boolean debug_mode_on;

    //媒体操作使用的Object
    private static Object mediaSyncObj;
    public static Object getMediaOptObject() {
        if (null == mediaSyncObj) {
            mediaSyncObj = new Object();
        }
        return mediaSyncObj;
    }

    private final static long KB = 1024;
    private final static long MB = 1024 * 1024;
    private final static long GB = 1024 * 1024 * 1024;
    private final static long TB = 1024 * 1024 * 1024 * 1024;

    //屏幕方向相关参数
    //真实的旋转方向
    private static int realOrientation;
    //表面上的旋转方向
    private static int fakeOrientation;

    /*
    *   调试模式控制初始化， 默认关闭
    * */
    public static void debugModeInit() {
        debug_mode_on = false;
    }

    /*
    *   handler: Activity中定义的Handler，用来向app主线程发送消息，实现界面的变化（例如增加本机IP的显示）
    *   trunOn: 是否打开调试模式
    * */
    public static void debugModeControl(Handler handler, boolean turnOn) {
        if (turnOn != debug_mode_on) {
            debug_mode_on = turnOn;
            Message debug_msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putBoolean("debug_mode", debug_mode_on);
            debug_msg.what = com.ceiv.communication.ProtocolMessageProcess.MsgWhatDebugMode;
            debug_msg.setData(bundle);
            handler.sendMessage(debug_msg);
        }
    }

    //判断外部存储/SD卡是否可用
    public static boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    //获取手机内部存储空间
    public static String getInternalMemorySize(Context content) {
        File file = Environment.getDataDirectory();
        //    /data目录
        Log.d(TAG, "getDataDirectory:" + file.getPath());
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        long size = blockCountLong * blockSizeLong;
        return Formatter.formatFileSize(content, size);
    }

    //获取手机内部可用存储空间
    public static String getAvailableInternalMemorySize(Context content) {
        File file = Environment.getDataDirectory();
        //    /data目录
        Log.d(TAG, "getDataDirectory:" + file.getPath());
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long availableBlockCountLong = statFs.getAvailableBlocksLong();
        long size = availableBlockCountLong * blockSizeLong;
        return Formatter.formatFileSize(content, size);
    }

    //获取手机外部存储空间
    public static String getExternalMemorySize(Context content) {
        File file = Environment.getExternalStorageDirectory();
        //    /mnt/sdcard -> /mnt/internal_sd目录
        Log.d(TAG, "getExternalStorageDirectory:" + file.getPath());
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        long size = blockCountLong * blockSizeLong;
        return Formatter.formatFileSize(content, size);
    }

    //获取手机外部可用存储空间，以字符串形式输出
    public static String getAvailableExternalMemorySize(Context content) {
        File file = Environment.getExternalStorageDirectory();
        //    /mnt/sdcard -> /mnt/internal_sd目录
        Log.d(TAG, "getExternalStorageDirectory:" + file.getPath());
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long availableBlockCountLong = statFs.getAvailableBlocksLong();
        long size = availableBlockCountLong * blockSizeLong;
        return Formatter.formatFileSize(content, size);
    }

    //获取手机外部可用存储空间，以字节数目返回
    public static long getAvailableExternalMemorySize() {
        File file = Environment.getExternalStorageDirectory();
        //    /mnt/sdcard -> /mnt/internal_sd目录
        Log.d(TAG, "getExternalStorageDirectory:" + file.getPath());
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long availableBlockCountLong = statFs.getAvailableBlocksLong();
        return availableBlockCountLong * blockSizeLong;
    }

    //获取指定接口IP地址，默认接口为：eth0
    public static String getNetworkInterfaceIp(String netIfName) {
        String netInterfaceName;
        if ("".equals(netIfName) || null == netIfName) {
            netInterfaceName = "eth0";
        } else {
            netInterfaceName = netIfName;
        }

        String ipAddr = null;
        try {
            NetworkInterface ni = NetworkInterface.getByName(netInterfaceName);

            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress inetAddress = address.nextElement();
                if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                    ipAddr = inetAddress.getHostAddress().toString();
                    Log.d(TAG, "Network Interface \"" + netInterfaceName + "\" has IP:" + ipAddr);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "get Network Interface\"" + netInterfaceName + "\" info failed");
            e.printStackTrace();
            return null;
        }
        return ipAddr;
    }

    //获取指定接口MAC地址，默认接口为：eth0
    public static String getNetworkInterfaceMac(String netIfName) {
        String netInterfaceName;
        if ("".equals(netIfName) || null == netIfName) {
            netInterfaceName = "eth0";
        } else {
            netInterfaceName = netIfName;
        }

        String macStr = null;
        try {
            NetworkInterface ni = NetworkInterface.getByName(netInterfaceName);
            byte[] mac = ni.getHardwareAddress();
            macStr = MACByte2Hex(mac);
        } catch (Exception e) {
            Log.e(TAG, "get Network Interface\"" + netInterfaceName + "\" info failed");
            e.printStackTrace();
            return null;
        }
        return macStr;
    }

    //将字节数组存储的mac地址装换成字符串形式
    private static String MACByte2Hex(byte[] mac) {

        StringBuilder stringBuilder = new StringBuilder();
        String stmp = null;
        int len = mac.length;
        for (int n = 0; n < len; n++) {
            stmp = Integer.toHexString(mac[n] & 0xFF);
            if (stmp.length() == 1) {
                stringBuilder.append("0").append(stmp);
            } else {
                stringBuilder.append(stmp);
            }
            if (n < len - 1) {
                stringBuilder.append(":");
            }
        }
        return stringBuilder.toString().toUpperCase().trim();
    }

    /*
    *   配置系统网络参数
    *
    *   context:    上下文
    *   ip:         要设置的静态IP地址
    *   nm:         子网掩码
    *   gw:         网关地址
    *   dns:        域名服务器地址
    *
    * */
    public static void setSystemNetworkInfo(Context context, String ip, String nm, String gw, String dns) {

        if (null == context) {
            Log.e(TAG, "Context can't be null");
            return;
        }

        if (!checkIPConfigration(ip, nm, gw, dns)) {
            Log.e(TAG, "Invalid arguments!");
            return;
        }

        Intent intent = new Intent();
        intent.setAction(ACTION_CONFIG_IP);
        intent.putExtra("ip", ip);
        intent.putExtra("nm", nm);
        intent.putExtra("gw", gw);
        intent.putExtra("dns", dns);
        Log.d(TAG, "send broadcast to Launcher to change net config!");
        context.sendBroadcast(intent);
    }

    public static boolean checkIPConfigration(String ip, String nm, String gw, String dns) {

        //判断IP地址是否是合法的
        if (!isIpAddr(ip) || !isIpAddr(gw) || !isIpAddr(dns) || !isNetmask(nm)) {
            return false;
        }
        return true;
    }

    /*
    *   调整系统背光参数
    *
    *   context:    上下文
    *   value:      背光值 最小0 最大255
    *   save        是否保存
    *
    * */
    public static void setSystemBackLight(Context context, int value, boolean save) {

        //南宁项目的设备背光是由单片机控制的，所以这里我们不做操作。
//        if (null == context) {
//            Log.e(TAG, "Context can't be null");
//            return;
//        }
//        if (value < 0) {
//            value = 0;
//        }
//        if (value > 255) {
//            value = 255;
//        }
//        Intent intent = new Intent();
//        intent.setAction(ACTION_CHANGE_BL);
//        intent.putExtra("blValue", value);
//        intent.putExtra("save", save);
//
//        context.sendBroadcast(intent);
    }

    /*
    *   请求进行apk升级
    *
    *   context:    上下文
    *   apkPath:    待升级的apk路径
    *
    * */
    public static void requestUpdateApk(Context context, String apkPath) {

        if (null == context) {
            Log.e(TAG, "Context can't be null");
            return;
        }
        if (null == apkPath || "".equals(apkPath)) {
            Log.e(TAG, "Invalid argument: apkFile path");
        }
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_APK);
        intent.putExtra("path", apkPath);
        context.sendBroadcast(intent);
    }

    //重启设备
    public static void rebootDevice(Context context) {

        //设备重启采用三全视讯提供的接口，所以这里不在向我们的Launcher发送广播
        Intent intent = new Intent();
        intent.setAction("android.intent.action.sendkey");
        intent.putExtra("keycode", 1234);
        context.sendBroadcast(intent);


//        if (null == context) {
//            Log.e(TAG, "Context can't be null");
//            return;
//        }
//
//        Intent intent = new Intent();
//        intent.setAction(ACTION_REBOOT);
//        context.sendBroadcast(intent);
    }

//    public static void screenShotRequest(Activity activity, String path) {
//
//        if (null == activity || null == path) {
//            Log.e(TAG, "");
//            return;
//        }
//
//
//        Bitmap bitmap = shotActivity(activity);
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
//        String date = simpleDateFormat.format(new Date());
//        String fileName = "Screenshot_" + date + ".jpg";
//        File file = new File(path + "/" + fileName);
//        FileOutputStream out = null;
//        try {
//            out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//        } catch (Exception e) {
//            Log.e(TAG, "");
//        } finally {
//            try {
//                if (null != out) {
//                    out.flush();
//                    out.close();
//                }
//            } catch (IOException e) {
//
//            }
//
//        }
//
//    }

//    private static Bitmap shotActivity(Activity activity) {
//        View view = activity.getWindow().getDecorView();
//        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
//
//        Bitmap bp = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
//
//        view.setDrawingCacheEnabled(false);
//        view.destroyDrawingCache();
//
//
//        return bp;
//    }

    //将文件大小自动转换成字符串模式
    public static String fileSizeToString(long size) {

        if (size < 0) {
            return null;
        }
        if (size > KB) {
            if (size > MB) {
                if (size > GB) {
                    if (size > TB) {
                        return String.format("%.2f", (size / (double)TB)) + "TB";
                    } else {
                        return String.format("%.2f", (size / (double)GB)) + "GB";
                    }
                } else {
                    return String.format("%.2f", (size / (double)MB)) + "MB";
                }
            } else {
                return String.format("%.2f", (size / (double)KB)) + "KB";
            }
        } else {
            return size + "Byte";
        }
    }

    //IP地址是否符合规则
    public static boolean isIpAddr(String ip) {
        try {
            InetAddress ia = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return false;
        }
        return true;
    }

    //子网掩码是否符合规则
    public static boolean isNetmask(String mask) {

        String regEx1 = "255\\.255\\.255\\.(0|128|192|224|240|248|252|254)";
        String regEx2 = "255\\.255\\.(0|128|192|224|240|248|252|254|255)\\.0";
        String regEx3 = "255\\.(0|128|192|224|240|248|252|254|255)\\.0\\.0";

        Pattern pattern1 = Pattern.compile(regEx1);
        Pattern pattern2 = Pattern.compile(regEx2);
        Pattern pattern3 = Pattern.compile(regEx3);

        return pattern1.matcher(mask).matches() || pattern2.matcher(mask).matches() || pattern3.matcher(mask).matches();
    }

    public static boolean orientationInit(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        String rotStr = null;
        boolean getOrientation = false;
        if (rotation == Surface.ROTATION_0) {
            getOrientation = true;
            rotStr = "ROTATION_0";
        } else if (rotation == Surface.ROTATION_90) {
            getOrientation = true;
            rotStr = "ROTATION_90";
        } else if (rotation == Surface.ROTATION_180) {
            getOrientation = true;
            rotStr = "ROTATION_180";
        } else if (rotation == Surface.ROTATION_270) {
            getOrientation = true;
            rotStr = "ROTATION_270";
        } else {
            getOrientation = false;
        }
        if (getOrientation) {
            realOrientation = rotation;
            fakeOrientation = rotation;
            Log.d(TAG, "Current Rotation: " + rotStr);
            return true;
        } else {
            Log.d(TAG, "Can't get Start Rotation");
            return false;
        }
    }

    public static void setRotation(Activity activity, int rotation) {
        String action = "android.intent.action.sendkey";

        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("keycode", 1242);
        switch (rotation) {
            case 0:
                intent.putExtra("screen_num", Surface.ROTATION_0);
                break;
            case 90:
                intent.putExtra("screen_num", Surface.ROTATION_90);
                break;
            case 180:
                intent.putExtra("screen_num", Surface.ROTATION_180);
                break;
            case 270:
                intent.putExtra("screen_num", Surface.ROTATION_270);
                break;
            default:
                Log.d(TAG, "Invalid rotation!");
                return;
        }
        activity.sendBroadcast(intent);
    }

    public static void changeOrientation(Activity activity, int orientation) {

        String action = "android.intent.action.sendkey";

        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                if (realOrientation == Surface.ROTATION_270) {
                    realOrientation = Surface.ROTATION_90;
                    fakeOrientation = Surface.ROTATION_270;
                    Intent intent = new Intent();
                    intent.setAction(action);
                    intent.putExtra("keycode", 1242);
                    intent.putExtra("screen_num", realOrientation);
                    activity.sendBroadcast(intent);
                }
                activity.setRequestedOrientation(orientation);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                if (realOrientation == Surface.ROTATION_90) {
                    realOrientation = Surface.ROTATION_270;
                    fakeOrientation = Surface.ROTATION_90;
                    Intent intent = new Intent();
                    intent.setAction(action);
                    intent.putExtra("keycode", 1242);
                    intent.putExtra("screen_num", realOrientation);
                    activity.sendBroadcast(intent);
                }
                activity.setRequestedOrientation(orientation);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                if (realOrientation == Surface.ROTATION_180) {
                    realOrientation = Surface.ROTATION_0;
                    fakeOrientation = Surface.ROTATION_180;
                    Intent intent = new Intent();
                    intent.setAction(action);
                    intent.putExtra("keycode", 1242);
                    intent.putExtra("screen_num", realOrientation);
                    activity.sendBroadcast(intent);
                }
                activity.setRequestedOrientation(orientation);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                if (realOrientation == Surface.ROTATION_0) {
                    realOrientation = Surface.ROTATION_180;
                    fakeOrientation = Surface.ROTATION_0;
                    Intent intent = new Intent();
                    intent.setAction(action);
                    intent.putExtra("keycode", 1242);
                    intent.putExtra("screen_num", realOrientation);
                    activity.sendBroadcast(intent);
                }
                activity.setRequestedOrientation(orientation);
                break;
            default:
                Log.d(TAG, "Unsupport orientaion!");
                break;
        }
    }

    /*
     *  该功能仅在三全视讯RK3288板卡上有效
     *  目前函数没有进行参数检验，需调用者自己确保数据合法、准确
     *
     */
    public static void setSystemTime(Context context, int year, int mon, int day, int hour, int min, int sec) {

        Intent intent = new Intent("android.intent.action.sendkey");
        intent.putExtra("keycode", 1243);
        intent.putExtra("year", year);
        intent.putExtra("month", mon);
        intent.putExtra("day", day);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", min);
        intent.putExtra("second", sec);
        context.sendBroadcast(intent);
    }

    /*
     *  目前该功能仅在三全视讯RK3288板卡上有效
     *  函数暂时不进行参数检验，需调用者自己确保数据合法、准确
     *  timeZone的例子：GMT+02:00
     */
    public static void setSystemTimeZone(Context context, String timeZone) {

        Intent intent = new Intent("android.intent.action.sendkey");
        intent.putExtra("keycode", 1244);
        intent.putExtra("timezone", timeZone);
        context.sendBroadcast(intent);
    }

    public interface ApplicationOperation {
        //该接口用来实现子线程获取Activity或者上下文Context
        //主要用来在子线程中发送广播，进行截图
        public Activity getActivity();

        //该接口用来获取界面Activity的handler，然后子线程可以通知主界面
        //那些显示的内容需要改变。
        public Handler getMainHandler();
    }

}













