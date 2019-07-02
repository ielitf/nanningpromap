package com.ceiv.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ceiv.signpost.FragmentInteractionListener;
import com.ceiv.signpost.R;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import java.util.ArrayList;
import java.util.List;

import static com.ceiv.fragment.PassengerNoticeFragment.getStringFromDrawableRes;

/**
 * @auther wjt
 * @date 2019/4/24
 */
public class TuntouFragment extends Fragment implements View.OnClickListener {
    private Context context;
    private Button tt1, tt2, tt3, tt4, tt5, tt6, tt7, tt8, tt9;
    private LinearLayout tuntou_ll, tuntou_detail_ll;
    private ImageView tuntou_detail_img;
    private View view;
    private FragmentInteractionListener mListener;
    private Banner banner_tun_detail;
    private List imgs = new ArrayList<>();
    private int delayTime = 5000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_layout_tuntou, container, false);
        context = getActivity();
        inintView(view);
        return view;
    }

    private void inintView(View view) {
        banner_tun_detail = view.findViewById(R.id.banner_tun_detail);
        tuntou_ll = view.findViewById(R.id.tuntou_ll);
        tuntou_detail_ll = view.findViewById(R.id.tuntou_detail_ll);
        tuntou_detail_img = view.findViewById(R.id.tuntou_detail_img);
        tuntou_detail_img.setOnClickListener(this);
        tuntou_ll.setOnClickListener(this);
        tuntou_detail_ll.setOnClickListener(this);

        banner_tun_detail.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                mListener.onFragmentInteraction(true);
            }
        });

        tt1 = view.findViewById(R.id.tt1);
        tt2 = view.findViewById(R.id.tt2);
        tt3 = view.findViewById(R.id.tt3);
        tt4 = view.findViewById(R.id.tt4);
        tt5 = view.findViewById(R.id.tt5);
        tt6 = view.findViewById(R.id.tt6);
        tt7 = view.findViewById(R.id.tt7);
        tt8 = view.findViewById(R.id.tt8);
        tt9 = view.findViewById(R.id.tt9);
        tt1.setOnClickListener(this);
        tt2.setOnClickListener(this);
        tt3.setOnClickListener(this);
        tt4.setOnClickListener(this);
        tt5.setOnClickListener(this);
        tt6.setOnClickListener(this);
        tt7.setOnClickListener(this);
        tt8.setOnClickListener(this);
        tt9.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tt1://乘客换乘点
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                tuntou_detail_img.setVisibility(View.VISIBLE);
                tuntou_detail_img.setImageResource(R.drawable.tt1_detail);
                break;
            case R.id.tt2://金融储蓄
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                tuntou_detail_img.setVisibility(View.VISIBLE);
                tuntou_detail_img.setImageResource(R.drawable.tt2_detail);
                break;
            case R.id.tt3://智慧公交
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                banner_tun_detail.setVisibility(View.VISIBLE);
                banner_tun_detail.setImageLoader(new GlideImageLoader());

                imgs = new ArrayList<>();
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt3_detail_1));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt3_detail_2));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt3_detail_3));
                banner_tun_detail.setImageLoader(new GlideImageLoader());
                banner_tun_detail.setImages(imgs);
                banner_tun_detail.setDelayTime(delayTime);
                banner_tun_detail.start();
                break;
            case R.id.tt4://农村物流
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                tuntou_detail_img.setVisibility(View.VISIBLE);
                tuntou_detail_img.setImageResource(R.drawable.tt4_detail);
                break;
            case R.id.tt5://新能源充电站
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                banner_tun_detail.setVisibility(View.VISIBLE);
                banner_tun_detail.setImageLoader(new GlideImageLoader());

                imgs = new ArrayList<>();
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt5_detail_1));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt5_detail_2));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt5_detail_3));
                banner_tun_detail.setImageLoader(new GlideImageLoader());
                banner_tun_detail.setImages(imgs);
                banner_tun_detail.setDelayTime(delayTime);
                banner_tun_detail.start();
                break;
            case R.id.tt6://卫生间
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                tuntou_detail_img.setVisibility(View.VISIBLE);
                tuntou_detail_img.setImageResource(R.drawable.tt6_detail);
                break;
            case R.id.tt7://邮政快递
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                tuntou_detail_img.setVisibility(View.VISIBLE);
                tuntou_detail_img.setImageResource(R.drawable.tt7_detail);
                break;
            case R.id.tt8://公路养护
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                banner_tun_detail.setVisibility(View.VISIBLE);
                banner_tun_detail.setImageLoader(new GlideImageLoader());

                imgs = new ArrayList<>();
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt8_detail_1));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt8_detail_2));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt8_detail_3));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt8_detail_4));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt8_detail_5));
                banner_tun_detail.setImageLoader(new GlideImageLoader());
                banner_tun_detail.setImages(imgs);
                banner_tun_detail.setDelayTime(delayTime);
                banner_tun_detail.start();
                break;
            case R.id.tt9://光伏发电
                tuntou_ll.setVisibility(View.GONE);
                tuntou_detail_ll.setVisibility(View.VISIBLE);
                banner_tun_detail.setVisibility(View.VISIBLE);
                banner_tun_detail.setImageLoader(new GlideImageLoader());

                imgs = new ArrayList<>();
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt9_detail_1));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt9_detail_2));
                imgs.add(getStringFromDrawableRes(getActivity(), R.drawable.tt9_detail_3));
                banner_tun_detail.setImageLoader(new GlideImageLoader());
                banner_tun_detail.setImages(imgs);
                banner_tun_detail.setDelayTime(delayTime);
                banner_tun_detail.start();
                break;
//            case R.id.tuntou_detail_img:
//                tuntou_detail_ll.setVisibility(View.GONE);
//                tuntou_ll.setVisibility(View.VISIBLE);
//                break;
            default:
                break;
        }
        mListener.onFragmentInteraction(true);
    }
    @Override
    public void onStart() {
        super.onStart();
        //开始轮播
        banner_tun_detail.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        //结束轮播
        banner_tun_detail.stopAutoPlay();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
