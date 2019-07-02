package com.ceiv.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.ceiv.Utils;
import com.ceiv.signpost.R;
import com.ceiv.signpost.VideoPositionListener;
import com.ceiv.signpost.util.FileUtils;
import com.ceiv.signpost.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther wjt
 * @date 2019/5/5
 */
public class VideoFragment extends Fragment implements View.OnClickListener {
    private VideoPositionListener positionListener;
    View view;
    VideoView videoView;
    LinearLayout videovv;
    private static int videoHeight,crrentFile;
    private static int video_position;
    private List<File> mFileList = new ArrayList<>();
    private Handler handler;
    public VideoFragment() {
    }

    @SuppressLint("ValidFragment")
    public VideoFragment(Handler handler, int video_position,int crrentFile) {
        this.handler = handler;
        this.video_position = video_position;
        this.crrentFile = crrentFile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);
        init();
        return view;
    }

    private void init() {
        /**
         * 视频播放
         */
        videovv = view.findViewById(R.id.videovv);
        TextView tv = view.findViewById(R.id.tv_tip);
        tv.setSelected(true);
        videoHeight = Utils.getWidthPixel(getActivity()) * 9 / 16;
        videoView = view.findViewById(R.id.videoView);

//        playVideoFromRaw();//获取视频文件按路径
        playVideoFromSDCard();

        videoView.setMediaController(new android.widget.MediaController(getActivity()));
        videoView.setMinimumHeight(videoHeight);
        videoView.seekTo(video_position);
        videoView.start();
        videoView.setMediaController(null);
        videoView.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mediaPlayer) {
                videoView.seekTo(0);
                crrentFile = crrentFile + 1;
                if(crrentFile >= mFileList.size()){
                    crrentFile = 0;
                }
                videoView.setVideoPath(mFileList.get(crrentFile).getAbsolutePath());
                videoView.start();
            }
        });

        videovv.setOnClickListener(this);
    }

    private void playVideoFromSDCard() {
//        根据文件路径播放
        String[] asd = FileUtils.getExtSDCardPath(getActivity());
        String asdad = asd[0];//内置
        String waizhisd = asd[1];//外置

//        String waifilename=waizhisd+"/"+"QinYangVideo"+"/qinyang.mp4";

        File file = new File(waizhisd + "/" + "播放");
        if(file.exists()){
            mFileList = getmFileList(file);
            if (mFileList.size() > 0) {
                videoView.setVideoPath(mFileList.get(crrentFile).getAbsolutePath());
            } else {
                ToastUtil.show(getActivity(), "暂无视频");
            }
        }
        else {
            file.mkdir();
        }
    }

    //读取放在raw目录下的文件
    private void playVideoFromRaw() {
        videoView.setVideoURI(Uri.parse("android.resource://com.ceiv.signpost/" + R.raw.nngj));
    }

    //读取放在手机存储目录下的制定视频文件
    private void playVideoFromPhone() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            videoView.setVideoPath(Environment.getExternalStorageDirectory() + "/test.mp4");
        }
    }

    private List<File> getmFileList(File file) {
        List<File> list = new ArrayList<>();
        File[] fileArray = file.listFiles();
        for (File f : fileArray) {
            if (f.isFile()) {
                list.add(f);
            } else {
                getmFileList(f);
            }
        }
        return list;
    }

    @Override
    public void onPause() {
        super.onPause();
        videoView.pause();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.videovv) {
            positionListener.videoPosition(videoView.getCurrentPosition(),crrentFile);
            Message message = new Message();
            message.what = 0x2121;
            handler.sendMessage(message);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoPositionListener) {
            positionListener = (VideoPositionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        positionListener = null;
    }
}
