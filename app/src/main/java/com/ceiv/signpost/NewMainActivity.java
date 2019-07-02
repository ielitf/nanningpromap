package com.ceiv.signpost;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ceiv.communication.utils.SystemInfoUtils;
import com.ceiv.fragment.BusMesageFragment;
import com.ceiv.fragment.GlideImageLoader;
import com.ceiv.fragment.PassengerNoticeFragment;
import com.ceiv.fragment.TuntouFragment;
import com.ceiv.fragment.VideoFragment;
import com.youth.banner.Banner;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import static com.ceiv.fragment.PassengerNoticeFragment.getStringFromDrawableRes;

/**
 * @auther wjt
 * @date 2019/4/24
 */
public class NewMainActivity extends AppCompatActivity implements FragmentInteractionListener, SystemInfoUtils.ApplicationOperation, View.OnClickListener ,VideoPositionListener{
    private PassengerNoticeFragment passengerNoticeFragment;
    private fragment_map mapFragment;
    private TuntouFragment tuntouFragment;
    private BusMesageFragment busMesageFragment;
    private FragmentManager fragmentManager;

    private TextView tv;
    private Button passenger_notice;
    private Button map_chaxun;
    private Button tundou;
    private Button busMessage;
    private LinearLayout check_system;
    private LinearLayout mainfragment;
    private LinearLayout mainfragment_ll;

    private Banner banner;
    private List imgs = new ArrayList<>();

    private String path = "http://we.taagoo.com/vrplayer/VR.mp4";//备用视频网络播放地址
    private LinearLayout fl_controller;
    private LinearLayout ll;
    private LinearLayout left_lin;

    private static int myVideoPosition,mCurrentFile;//点击视频进入乘客查询系统时视频播放位置
    private static final String TAG = "===NewMainActivity===";

    private Timer timer;
    private MyTask task;
    private long delayTime = 60000;//经过多长时间自动切换视频显示

    //handler对象可以用于发送、处理消息
    private Handler handler = new Handler() {

        // 处理消息
        public void handleMessage(Message msg) {
            String text = (String) msg.obj;
            switch (msg.what) {
                case 0x123:
                    if(left_lin.getVisibility()==View.VISIBLE){
                        left_lin.setVisibility(View.GONE);
                    }
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    VideoFragment videoFragment=new VideoFragment(handler,myVideoPosition,mCurrentFile);
                    fragmentTransaction.replace(R.id.mainfragment,videoFragment);
                    fragmentTransaction.commit();
                    break;
                case 0x2121:
                    if(left_lin.getVisibility()==View.GONE){
                        left_lin.setVisibility(View.VISIBLE);
                    }
                    FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                    passengerNoticeFragment = new PassengerNoticeFragment();
                    fragmentTransaction2.replace(R.id.mainfragment,passengerNoticeFragment);
                    fragmentTransaction2.commit();
                    timingAgain();
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Handler getMainHandler() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        deleteTitleBar();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //隐藏底部键盘，一直不会弹出
        layoutParams.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(layoutParams);
        setContentView(R.layout.activity_main_new);
        fragmentManager = getSupportFragmentManager();
        left_lin=findViewById(R.id.left_lin);
        inintView();
    }

    private void inintView() {
        passenger_notice = findViewById(R.id.passenger_notice);
        map_chaxun = findViewById(R.id.map_chaxun);
        tundou = findViewById(R.id.tundou);
        busMessage = findViewById(R.id.bus);
        check_system = findViewById(R.id.check_system);
        mainfragment = findViewById(R.id.mainfragment);
        mainfragment.setOnClickListener(this);
        mainfragment_ll = findViewById(R.id.mainfragment_ll);
        mainfragment_ll.setOnClickListener(this);
        ll = findViewById(R.id.mainfragment);

        passenger_notice.setOnClickListener(this);
        map_chaxun.setOnClickListener(this);
        tundou.setOnClickListener(this);
        busMessage.setOnClickListener(this);

        if(left_lin.getVisibility()==View.VISIBLE){
            left_lin.setVisibility(View.GONE);
        }
        FragmentTransaction fragmentTransaction6 = getSupportFragmentManager().beginTransaction();
        VideoFragment videoFragment=new VideoFragment(handler,myVideoPosition,mCurrentFile);
        fragmentTransaction6.replace(R.id.mainfragment,videoFragment);
        fragmentTransaction6.commit();

        imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.line_yaotou));
        imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.line_1));
        imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.line_3));
        imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.line_5));
        imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.line_6));
        /**
         * 公交路线轮播图
         */
        banner = (Banner) findViewById(R.id.banner);
        //设置banner样式
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(imgs);
        //banner设置方法全部调用完毕时最后调用
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(5000);
        banner.start();

    }

    protected void deleteTitleBar() {
        //去掉标题栏；
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        switch (v.getId()) {

            //乘车通知fragment
            case R.id.passenger_notice:

                if(left_lin.getVisibility()==View.GONE){
                    left_lin.setVisibility(View.VISIBLE);
                }
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                passengerNoticeFragment = new PassengerNoticeFragment();
                fragmentTransaction2.replace(R.id.mainfragment,passengerNoticeFragment);
                fragmentTransaction2.commit();

                timingAgain();

                break;
            //地图
            case R.id.map_chaxun:
                if(left_lin.getVisibility()==View.GONE){
                    left_lin.setVisibility(View.VISIBLE);
                }
                FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                mapFragment = new fragment_map();
                fragmentTransaction3.replace(R.id.mainfragment,mapFragment);
                fragmentTransaction3.commit();

                timingAgain();
                break;
            //屯头服务器
            case R.id.tundou:
                if(left_lin.getVisibility()==View.GONE){
                    left_lin.setVisibility(View.VISIBLE);
                }
                FragmentTransaction fragmentTransaction4 = getSupportFragmentManager().beginTransaction();
                tuntouFragment = new TuntouFragment();
                fragmentTransaction4.replace(R.id.mainfragment,tuntouFragment);
                fragmentTransaction4.commit();

                timingAgain();
                break;

            case R.id.bus:
                if(left_lin.getVisibility()==View.GONE){
                    left_lin.setVisibility(View.VISIBLE);
                }
                FragmentTransaction fragmentTransaction5 = getSupportFragmentManager().beginTransaction();
                busMesageFragment = new BusMesageFragment();
                fragmentTransaction5.replace(R.id.mainfragment,busMesageFragment);
                fragmentTransaction5.commit();

                timingAgain();
                break;

        }

    }

    private void hideFragment(FragmentTransaction transaction) {
        if (passengerNoticeFragment != null) {
            transaction.hide(passengerNoticeFragment);
        }
        if (mapFragment != null) {
            transaction.hide(mapFragment);
        }
        if (tuntouFragment != null) {
            transaction.hide(tuntouFragment);
        }
        if (busMesageFragment != null) {
            transaction.hide(busMesageFragment);
        }
    }
    private void removeFragment(FragmentTransaction transaction) {
        if (passengerNoticeFragment != null) {
            transaction.remove(passengerNoticeFragment);
            passengerNoticeFragment = null;
        }
        if (mapFragment != null) {
            transaction.remove(mapFragment);
            mapFragment = null;
        }
        if (tuntouFragment != null) {
            transaction.remove(tuntouFragment);
            tuntouFragment = null;
        }
        if (busMesageFragment != null) {
            transaction.remove(busMesageFragment);
            busMesageFragment = null;
        }
    }

    //如果你需要考虑更好的体验，可以这么操作
    @Override
    public void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }


    class MyTask extends TimerTask {


        @Override
        public void run() {
            handler.sendEmptyMessage(0x123);

        }

    }

    /**
     * 重新开始计时
     */
    public void timingAgain() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        task = new MyTask();
        timer.schedule(task, delayTime);
        Log.i(TAG, "重新计时");

    }

    @Override
    public void onFragmentInteraction(Boolean bool) {
        if (bool == true) {
            timingAgain();
        }
    }
    @Override
    public void videoPosition(int videoPosition,int currentFile) {
        myVideoPosition = videoPosition;
        mCurrentFile = currentFile;

    }

}
