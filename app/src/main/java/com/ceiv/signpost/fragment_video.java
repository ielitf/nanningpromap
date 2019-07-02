package com.ceiv.signpost;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

public class fragment_video extends Fragment {

    private final static String TAG = "fragment_video";

    private VideoView videoView;

    /*
     *  视频播放的flag， 0: 代表还未调整过layout参数
     *  1: 代表当前layout参数适合较宽的视频播放  2: 代表当前layout参数适合较窄的视频播放
     */
    private int scaleFlag = 0;
    //videoView长宽参数
    private int viewWidth;
    private int viewHeight;
    //videoView长宽参数是否已经测得
    private boolean whMeasured = false;

    //正在播放的视频的长宽参数
    private int curWidth;
    private int curHeight;

    //视频列表
    private ArrayList<File> videoList;
    //视频列表总数目
    private int videoCount;
    //当前正在播放的视频的标号
    private int curVideoIndex;
    //播放标志 true:正在播放视屏，false:还未开始播放
    private boolean isPlaying;

    private boolean fileExist = false;
    private int maxVideoTime = 0;
    private int curVideoTime = 0;
    private String videoPath;

    private MainActivity mainActivity;

    //视屏fragment的操作接口，主要是提供给MainActivity使用
    public interface VideoFragmentOperation {
        //停止所有视屏播放，关闭打开的视频文件
        public void stopVideo(boolean playDefRes);
        //刷新视频列表
        public void refreshVieoList();
        //开始播放视频列表的文件
        public void startVideo();
    }

    //接口实现
    private VideoFragmentOperation videoOpt = new VideoFragmentOperation() {
        @Override
        public void stopVideo(boolean playDefRes) {
            if (videoView != null) {
                videoView.stopPlayback();
                videoList.clear();
                if (playDefRes) {
                    playDefaultVideo();
                }
            }
        }

        @Override
        public void refreshVieoList() {
            stopVideo(false);
            startVideo();
        }

        @Override
        public void startVideo() {

            if (null == videoView) {
                Log.d(TAG, "Get VideoView failed!");
                return;
            }

            //首先搜索sd卡的/media/video目录，重新建立播放列表
            File pathFile = new File(videoPath);
            if (!pathFile.isDirectory()) {
                Log.d(TAG, "VideoPath: " + videoPath + " is not a directory!");
                playDefaultVideo();
                return;
            }
            //生成播放列表
            videoList = new ArrayList<File>();
            for(File tmp : pathFile.listFiles()) {
                if (tmp.isFile()) {
                    videoList.add(tmp);
                }
            }
            //初始化播放视频标志
            videoCount = videoList.size();
            curVideoIndex = 0;

            //如果本地没有视屏文件，则循环播放自带的视频文件
            if (videoCount == 0) {
                playDefaultVideo();
                return;
            }

            videoView.setVideoURI(Uri.parse(videoList.get(curVideoIndex).getAbsolutePath()));
            isPlaying = true;
            maxVideoTime = videoView.getDuration();
            curVideoTime = 0;
            videoView.setBackgroundColor(0);

            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            retr.setDataSource(videoList.get(curVideoIndex).getAbsolutePath());
            curWidth = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            curHeight = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            Log.d(TAG, "video: " + videoList.get(curVideoIndex).getName() +
                    " width: " + curWidth + " height: " + curHeight);
            adjustLayoutParams();

            //设置播放结束监听器
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "video:" + videoList.get(curVideoIndex).getName() + " play finished!");
                    //列表循环播放
                    if (curVideoIndex < videoCount - 1) {
                        curVideoIndex++;
                    } else {
                        curVideoIndex = 0;
                    }

                    videoView.setVideoURI(Uri.parse(videoList.get(curVideoIndex).getAbsolutePath()));

                    MediaMetadataRetriever retr = new MediaMetadataRetriever();
                    retr.setDataSource(videoList.get(curVideoIndex).getAbsolutePath());
                    curWidth = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    curHeight = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    Log.d(TAG, "video: " + videoList.get(curVideoIndex).getName() +
                            " width: " + curWidth + " height: " + curHeight);
                    adjustLayoutParams();

                    maxVideoTime = videoView.getDuration();
                    curVideoTime = 0;
                    Log.d(TAG, "gona to play video:" + videoList.get(curVideoIndex).getName());
                    videoView.start();
                    videoView.seekTo(curVideoTime);
                }
            });

            Log.d(TAG, "gona to play video:" + videoList.get(curVideoIndex).getName());
            videoView.start();
            videoView.seekTo(curVideoTime);
        }
    };

    //调整videoView布局参数，以便使视频在长宽比不变的情况下，最大化播放区域
    private void adjustLayoutParams() {

        if (isPlaying && whMeasured) {
            /*
             *  如果视频已经正在播放，需要查看是否需要调整videoVIew的参数，
             *  以便在视频长宽比不变的情况下，最大化播放区域
             */
            if ((viewWidth * 1.0f / viewHeight) > (curWidth * 1.0f / curHeight)) {
                //播放的视频比较窄
                if (scaleFlag != 2) {
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
//                    layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
//                    layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    videoView.setLayoutParams(layoutParams);
                    Log.d(TAG, "adjust VideoView layout params to TOP BOTTOM");
                    scaleFlag = 2;
                }

            } else {
                //播放的视频比较宽
                if (scaleFlag != 1) {
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
//                    layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
//                    layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                    videoView.setLayoutParams(layoutParams);
                    Log.d(TAG, "adjust VideoView layout params to START END");
                    scaleFlag = 1;
                }
            }
        }
    }

    private void playDefaultVideo() {
        Log.d(TAG, "gona to play default video!");
        String uri = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.nngj;
        if (null == videoView) {
            Log.d(TAG, "Get VideoView failed!");
            return;
        }

        videoView.setVideoURI(Uri.parse(uri));

        isPlaying = true;
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(videoView.getContext(), Uri.parse(uri));
        curWidth = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        curHeight = Integer.valueOf(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        Log.d(TAG, "default video: width: " + curWidth + " height: " + curHeight);
        adjustLayoutParams();

        maxVideoTime = videoView.getDuration();
        curVideoTime = 0;
        videoView.setBackgroundColor(0);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "video play finished, replay!");
                curVideoTime = 0;
                videoView.start();
                videoView.seekTo(curVideoTime);
            }
        });

        videoView.start();
        videoView.seekTo(curVideoTime);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "Video Fragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        videoView = view.findViewById(R.id.videoView);

        if (videoView == null) {
            Log.e(TAG, "Get VideoView failed!");
            return view;
        }

        viewWidth = -1;
        viewHeight = -1;
        whMeasured = false;
        isPlaying = false;
        scaleFlag = 0;
        //videoView绘制完毕后，获取view的长宽参数
        this.videoView.post(new Runnable() {
            @Override
            public void run() {
                viewWidth = videoView.getMeasuredWidth();
                viewHeight = videoView.getMeasuredHeight();
                whMeasured = true;
                Log.d("video_debug", "video getWidth: " + videoView.getWidth());
                Log.d("video_debug", "video getHeight: " + videoView.getHeight());
                Log.d("video_debug", "video getMeasuredWidth: " + videoView.getMeasuredWidth());
                Log.d("video_debug", "video getMeasuredHeight: " + videoView.getMeasuredHeight());
                adjustLayoutParams();
            }
        });

        videoPath = Environment.getExternalStorageDirectory() + "/media/video";
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "Video Fragment onStart");
        super.onStart();
        videoOpt.startVideo();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Video Fragment onResume");
        super.onResume();

        if (videoView != null && !videoView.isPlaying()) {
            Log.d(TAG, "continue to play video!");
            //videoView.start();
            //videoView.seekTo(curVideoTime);
            videoView.resume();

        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "Video Fragment onPause");
        super.onPause();

        if (videoView != null && videoView.canPause()) {
            Log.d(TAG, "video play pause!");
            videoView.pause();
            //curVideoTime = videoView.getCurrentPosition();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Video Fragment onStop");
        super.onStop();
        videoOpt.stopVideo(false);
    }

    //android 不同版本对下面的两个函数调用可能不一样
    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Video Fragment onAttach(Context)");
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        mainActivity.setVideoOperation(videoOpt);
    }

    //在当前项目中（android5.1）系统会调用下面的函数
    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "Video Fragment onAttach(Activity)");
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
        mainActivity.setVideoOperation(videoOpt);
    }










//
//    private final static String TAG = "fragment_video";
//    private VideoView videoView;
//    private int maxVideoTime = 0;
//    private int curVideoTime = 0;
//    private final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/media/video/";
//    private File[] Files;
//    private int fileCount;
//    private int index = 0;
//    OnCtrlVideoLisener onCtrlVideoLisener;
////    private MediaController mediaController;
//    private Handler handler;
////    private Timer timer;
//private MainActivity mainActivity;
//
//    public interface OnCtrlVideoLisener {
//        void replay();
//        void stop();
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        final View view = inflater.inflate(R.layout.fragment_video, container, false);
//        videoView = view.findViewById(R.id.videoView);
////        mediaController = new MediaController(this.getActivity());
////        videoView.setMediaController(mediaController);
//
//        if (videoView == null) {
//            Log.e(TAG, "获取VideoView失败！");
//            return view;
//        }
//
//        Log.d(TAG, "获取视频文件");
//
//        handler = new Handler(){
//            public void handleMessage(Message message){
//                if(message.what==0x01) {
//                    videoView.stopPlayback();
//                    if (videoView != null && (playVideoList() == 0)) {
//                        Log.d(TAG,"start playing");
//                    }
//                    else {
//                        Log.e(TAG,"no video");
//                    }
//                }
//            }
//        };
//
//        onCtrlVideoLisener = new OnCtrlVideoLisener() {
//            @Override
//            public void replay() {
//                Message message = new Message();
//                message.what = 0x01;
//                handler.sendMessage(message);
//            }
//
//            @Override
//            public void stop() {
//                videoView.stopPlayback();
//            }
//        };
//
//
//
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
////                try {
////                    Thread.sleep(10000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////                onCtrlVideoLisener.replay();
////            }
////        }).start();
//
////        Timer timer = new Timer();
////        timer.schedule(new TimerTask() {
////            @Override
////            public void run() {
////                if(!videoView.isPlaying()) {
////                    Log.d(TAG,"detect folder for video");
////                    Message message = new Message();
////                    message.what=0x01;
////                    handler.sendMessage(message);
////                }
////            }
////        }, 5000, 5000);
//
//
//
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Log.d(TAG, "播放完毕，播放下一个");
//                if (videoView != null && (playVideoList() == 0)) {
//                    Log.d(TAG,"start playing");
//                }
//                else {
//                    Log.e(TAG,"no video");
//                }
//            }
//        });
//        return view;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (videoView != null && (playVideoList() == 0)) {
//            Log.d(TAG, "start playing");
//        }
//        else {
//            Log.e(TAG,"no video");
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (videoView != null) {
//            Log.d(TAG, "继续播放");
//            videoView.start();
//            Log.d(TAG, "设置当前视频进度");
//            videoView.seekTo(curVideoTime);
//        }
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//
//        if (videoView != null) {
//            Log.d(TAG, "暂停播放");
//            videoView.pause();
//            curVideoTime = videoView.getCurrentPosition();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//    }
//
//    private int playVideoList() {
//        File file = new File(path);
//        if (file.exists()) {
//            Files = file.listFiles();
//            fileCount = Files.length;
//            if(fileCount < 1) {
//                return -1;
//            }
//            else if(index == 0){
//                index = fileCount - 1;
//            }
//            else {
//                index--;
//            }
//            videoView.setVideoURI(Uri.parse(Files[index].getAbsolutePath()));
//            maxVideoTime = videoView.getDuration();
//            curVideoTime = 0;
//            videoView.setBackgroundColor(0);
//            videoView.start();
//        }
//        return 0;
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        mainActivity = (MainActivity) context;
//        mainActivity.setVideoOperation(onCtrlVideoLisener);
//    }

}
