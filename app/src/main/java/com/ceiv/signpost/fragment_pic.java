package com.ceiv.signpost;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class fragment_pic extends Fragment {

//    private int fileIndex;

    private final static int CHANGE_IMAGE = 0x01;

    private final static String TAG = "fragment_pic";

    private Handler handler;

    //fragment对应的Activity
    private MainActivity mainActivity;

    //图片View
    private ImageView imageView;

    //本地图片所在目录
    private String picPath;
    //图片列表
    private ArrayList<File> picList;
    //图片列表总数目
    private int picCount;
    //当前正在显示的图片的标号
    private int curPicIndex;
    //图片轮播的定时器
    private Timer picTimer;
    //图片切换时间,单位ms
    private int changeTime;

    //广告图片fragment的操作接口，主要是提供给MainActivity使用
    public interface PictureFragmentOperation {
        //停止图片的轮播显示，关闭打开的图片文件，参数disDefRes来指示是否需要先显示默认内置的资源
        public void stopPicDisp(boolean disDefRes);
        //刷新图片列表
        public void refreshPicList();
        //开始轮播图片列表
        public void startPicDisp();
    }

    //接口实现
    private PictureFragmentOperation picOpt = new PictureFragmentOperation() {
        @Override
        public void stopPicDisp(boolean disDefRes) {
            //判断是否需要先显示默认内置的图片
            if (disDefRes) {
                DispDefaultPic();
                picList.clear();
            }
            if (picTimer != null) {
                picTimer.cancel();
                picTimer = null;
            }
        }

        @Override
        public void refreshPicList() {
            startPicDisp();
        }

        @Override
        public void startPicDisp() {

            if (null == imageView) {
                Log.d(TAG, "Get ImageView failed!");
                return;
            }
            File pathFile = new File(picPath);
            if (!pathFile.isDirectory()) {
                Log.d(TAG, "PicturePath: " + picPath + " is not a directory!");
                //若预定的目录不为目录类型的话，显示apk自带的默认图片
                DispDefaultPic();
                return;
            }
            //生成播放列表
            picList = new ArrayList<File>();
            for(File tmp : pathFile.listFiles()) {
                if (tmp.isFile()) {
                    picList.add(tmp);
                }
            }
            //初始化图片显示标志
            picCount = picList.size();
            curPicIndex = 0;

            //如果本地没有图片文件，则显示apk自带的图片
            if (picCount == 0) {
                DispDefaultPic();
                return;
            }

            if (null == picTimer) {
                picTimer = new Timer();
            }

            //设置图片轮播
            picTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = CHANGE_IMAGE;
                    msg.arg1 = curPicIndex;
                    handler.sendMessage(msg);

                    if (curPicIndex < picCount - 1) {
                        curPicIndex++;
                    } else {
                        curPicIndex = 0;
                    }
                }
            },0,changeTime);
        }
    };




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Picture Fragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_pic, container, false);

        imageView = view.findViewById(R.id.imageView);
        picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/media/picture";
        picTimer = new Timer();

        changeTime = 5000;

        handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case CHANGE_IMAGE:
                        String curPicPath = picList.get(msg.arg1).getAbsolutePath();
                        Log.d(TAG, "gona to display image:" + curPicPath);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        BitmapFactory.decodeFile(curPicPath, options);
                        double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);
                        options.inSampleSize = (int) Math.ceil(ratio);
                        options.inJustDecodeBounds = false;
                        Bitmap photoImg = BitmapFactory.decodeFile(curPicPath, options);

                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setImageBitmap(photoImg);
                        break;

                    case 0x02:
                        break;

                    default:
                        break;


                }

            }


        };
        return view;
    }

    @Override
    public void onStart(){
        Log.d(TAG, "Picture Fragment onStart");
        super.onStart();
        picOpt.startPicDisp();

    }

    @Override
    public void onResume() {
        Log.d(TAG, "Picture Fragment onResume");
        super.onResume();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "Picture Fragment onPause");
        super.onPause();

    }

    @Override
    public void onStop() {
        Log.d(TAG, "Picture Fragment onStop");
        super.onStop();
        picOpt.stopPicDisp(false);

    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Picture Fragment onAttach(Context)");
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        mainActivity.setPicOperation(picOpt);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "Picture Fragment onAttach(Activity)");
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
        mainActivity.setPicOperation(picOpt);
    }

    private void DispDefaultPic() {

        Log.d(TAG, "gona to display default image!");
        imageView.setImageResource(R.drawable.ad);

    }

//    public String getPicFiles() {
//        File[] files = new File(picPath).listFiles();
//        int fileCount = files.length;
//        if(fileCount < 1) {
//            return null;
//        }
//        else if(fileIndex == 0){
//            fileIndex = fileCount - 1;
//        }
//        else {
//            fileIndex--;
//        }
//        return picPath + files[fileIndex].getName();
//    }
}
