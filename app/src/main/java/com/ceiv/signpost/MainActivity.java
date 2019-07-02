package com.ceiv.signpost;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ceiv.communication.NetMgrDefine;
import com.ceiv.communication.NetMgrMsg;
import com.ceiv.communication.SystemInitThread;
import com.ceiv.communication.utils.SystemInfoUtils;
import com.ceiv.communication.ProtocolMessageProcess;

public class MainActivity extends AppCompatActivity implements SystemInfoUtils.ApplicationOperation {

    private TextView textDebugInfo;

    private fragment_video.VideoFragmentOperation videoOpt;
    private fragment_pic.PictureFragmentOperation picOpt;

    //在fragment的onAttach函数中会设置操作回调
    public void setVideoOperation(fragment_video.VideoFragmentOperation videoOpt) {
        this.videoOpt = videoOpt;
    }

    //在fragment的onAttach函数中会设置操作回调
    public void setPicOperation(fragment_pic.PictureFragmentOperation picOpt) {
        this.picOpt = picOpt;
    }



//    private fragment_video.OnCtrlVideoLisener videoCtrl;
//
//    //在fragment的onAttach函数中会设置操作回调
//    public void setVideoOperation(fragment_video.OnCtrlVideoLisener videoCtrl) {
//        this.videoCtrl = videoCtrl;
//    }


    public static Context context;
    private final static String TAG = "MainActivity";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ProtocolMessageProcess.MsgWhatDebugMode:
                    //调试模式请求
                    boolean debugOn = false;
                    debugOn = msg.getData().getBoolean("debug_mode");
                    Log.d(TAG, "DebugMode " + (debugOn ? "On" : "Off"));
                    if (debugOn) {
                        textDebugInfo.setText(SystemInfoUtils.getNetworkInterfaceIp(null));
                        textDebugInfo.setVisibility(View.VISIBLE);
                        //调整到最前面
                        textDebugInfo.bringToFront();
                    } else {
                        textDebugInfo.setVisibility(View.INVISIBLE);
                    }
                    break;
                case ProtocolMessageProcess.MsgWhatMediaOpt:
                    Log.d(TAG, "Request Media operation");
                    NetMgrMsg.HMediaSetting hMediaSetting = null;
                    //媒体文件相关操作
                    switch (msg.arg1) {
                        case ProtocolMessageProcess.MsgArg1ReloadMedia:
                            //子线程已完成媒体操作，现在需要扫描新的媒体文件和播放参数，重新进行显示
                            Log.d(TAG, "Reload media");
                            videoOpt.refreshVieoList();
                            picOpt.refreshPicList();
//                            videoCtrl.replay();
//                            hMediaSetting = (NetMgrMsg.HMediaSetting) msg.obj;
//                            if (null != hMediaSetting) {
//                                if (0 != (msg.arg2 & ProtocolMessageProcess.MsgArg2VideoBit)) {
//                                    Log.d(TAG, "gona to refresh video list!");
//                                    videoOpt.refreshVieoList();
//                                }
//                                if (0 != (msg.arg2 & ProtocolMessageProcess.MsgArg2PicBit)) {
//                                    Log.d(TAG, "gona to refresh picture list!");
//                                    picOpt.refreshPicList();
//                                }
//                            }
                            break;
                        case ProtocolMessageProcess.MsgArg1MediaRemoveByName:
                            /*
                             *  马上需要进行媒体文件删除操作，现在请先做相应的预处理，
                             *  比如，暂停正在播放的视屏，关闭打开的文件
                             */
                            Log.d(TAG, "Remove media by name preprocess");
                            hMediaSetting = (NetMgrMsg.HMediaSetting) msg.obj;
                            if (null != hMediaSetting) {
                                boolean hasImage = false;
                                boolean hasVideo = false;
                                for (NetMgrMsg.MediaResouce tmp : hMediaSetting.getMediaList()) {
                                    if (tmp.getMediaType() == NetMgrDefine.MediaTypeEnum.VIDEO) {
                                        hasVideo = true;
                                    } else if (tmp.getMediaType() == NetMgrDefine.MediaTypeEnum.PIC) {
                                        hasImage = true;
                                    }
                                }
                                if (hasImage) {
                                    picOpt.stopPicDisp(true);
                                }
                                if (hasVideo) {
                                    videoOpt.stopVideo(true);
                                }
                            }
                            synchronized (SystemInfoUtils.getMediaOptObject()) {
                                //通知子线程可以进行后续的操作了
                                SystemInfoUtils.getMediaOptObject().notify();
                            }

                            break;
                        case ProtocolMessageProcess.MsgArg1MediaRemoveByType:
                            /*
                             *  马上需要进行媒体文件删除操作，现在请先做相应的预处理，
                             *  比如，暂停正在播放的视屏，关闭打开的文件
                             */
                            Log.d(TAG, "Remove media by type preprocess");
                            hMediaSetting = (NetMgrMsg.HMediaSetting) msg.obj;
                            if (null != hMediaSetting) {
                                switch (hMediaSetting.getMedia(0).getMediaType()) {
                                    case TEXT:
                                        //地图apk没有文本资源需求
                                        break;
                                    case PIC:
                                        Log.d(TAG, "gona to delete all local image file!");
                                        picOpt.stopPicDisp(true);
                                        break;
                                    case VIDEO:
                                        Log.d(TAG, "gona to delete all local video file!");
                                        videoOpt.stopVideo(true);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            synchronized (SystemInfoUtils.getMediaOptObject()) {
                                //通知子线程可以进行后续的操作了
                                SystemInfoUtils.getMediaOptObject().notify();
                            }
                            break;
                        case ProtocolMessageProcess.MsgArg1MediaDownload:
                            /*
                             *  马上需要进行媒体文件删除操作，现在请先做相应的预处理，
                             *  比如，暂停正在播放的视屏，关闭打开的文件
                             */
                            Log.d(TAG, "Download media preprocess");
                            hMediaSetting = (NetMgrMsg.HMediaSetting) msg.obj;
                            if (null != hMediaSetting) {
                                boolean hasImage = false;
                                boolean hasVideo = false;
                                for (NetMgrMsg.MediaResouce tmp : hMediaSetting.getMediaList()) {
                                    if (tmp.getMediaType() == NetMgrDefine.MediaTypeEnum.VIDEO) {
                                        hasVideo = true;
                                    } else if (tmp.getMediaType() == NetMgrDefine.MediaTypeEnum.PIC) {
                                        hasImage = true;
                                    }
                                }
                                if (hasImage) {
                                    picOpt.stopPicDisp(true);
                                }
                                if (hasVideo) {
                                    videoOpt.stopVideo(true);
                                }
                            }
                            synchronized (SystemInfoUtils.getMediaOptObject()) {
                                //通知子线程可以进行后续的操作了
                                SystemInfoUtils.getMediaOptObject().notify();
                            }
                            break;
                        case ProtocolMessageProcess.MsgArg1DispSetting:
                            /*
                             *   即将修改相应的媒体播放参数，先做相应的预处理
                             *
                             */
                            Log.d(TAG, "DispSetting preprocess");


                            synchronized (SystemInfoUtils.getMediaOptObject()) {
                                //通知子线程可以进行后续的操作了
                                SystemInfoUtils.getMediaOptObject().notify();
                            }
                            break;
                    }

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        textDebugInfo = findViewById(R.id.textDebugInfo);
        textDebugInfo.setVisibility(View.INVISIBLE);

        new Thread(new SystemInitThread(this)).start();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "new orientation: LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "new orientation: PORTRAIT");
        }

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Handler getMainHandler() {
        return handler;
    }
}

























