package com.ceiv.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ceiv.signpost.FragmentInteractionListener;
import com.ceiv.signpost.R;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther wjt
 * @date 2019/4/24
 */
public class BusMesageFragment extends Fragment {
    private  Banner banner;
    private FragmentInteractionListener mListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bus_message,container,false);
        inintView(view);
        return view;

    }

    private void inintView(View view) {
        List imgs=new ArrayList<>();
        imgs.add(getStringFromDrawableRes(getActivity(),R.drawable.bus_chengxiang1));
        imgs.add(getStringFromDrawableRes(getActivity(),R.drawable.bus_chengxiang2));
        imgs.add(getStringFromDrawableRes(getActivity(),R.drawable.bus_line_1));
        imgs.add(getStringFromDrawableRes(getActivity(),R.drawable.bus_line_2 ));
        imgs.add(getStringFromDrawableRes(getActivity(),R.drawable.bus_line_3));
        banner= (Banner) view.findViewById(R.id.banner);
        //设置banner样式
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(imgs);
        //banner设置方法全部调用完毕时最后调用
        //设置自动轮播，默认为true
        //设置轮播时间
        banner.setDelayTime(10000);
        banner.start();


        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                mListener.onFragmentInteraction(true);
            }
        });
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

    public static String getStringFromDrawableRes(Context context, int id) {
        Resources resources = context.getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE +"://"+ resources.getResourcePackageName(id) +"/" + resources.getResourceTypeName(id) +"/"
                + resources.getResourceEntryName(id);
        return path;}

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

//    public interface FragmentInteractionListener {
//        void onFragmentInteraction(Boolean bool);
//    }

}

