package com.ceiv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.ceiv.entity.CommonBean;
import com.ceiv.signpost.R;

import java.util.ArrayList;

public class TuntouAdapter extends MyBaseAdapter<CommonBean> {
    private LayoutInflater inflater;
    public TuntouAdapter(Context context, ArrayList<CommonBean> mData) {
        super(context, mData);
        inflater = LayoutInflater.from(context);
    }

    @Override
    protected View newView(Context context, int position, ViewGroup parentView) {
        ViewHolder holderView = new ViewHolder();
        View convertView = inflater.inflate(R.layout.item_tuntou, null, false);
        holderView.tuntouIcon = (ImageView) convertView.findViewById(R.id.item_tuntou_img);
        convertView.setTag(holderView);
        return convertView;
    }

    @Override
    protected void bindView(Context context, View view, int position, CommonBean model) {
        ViewHolder holderView = (ViewHolder) view.getTag();
        holderView.tuntouIcon.setImageResource(model.getIcon());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        private ImageView tuntouIcon;
    }
}
