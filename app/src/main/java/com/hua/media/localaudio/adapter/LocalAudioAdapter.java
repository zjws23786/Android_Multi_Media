package com.hua.media.localaudio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hua.librarytools.utils.DensityUtil;
import com.hua.media.R;
import com.hua.media.bean.AudioBean;

import java.util.List;

/**
 * Created by hjz on 2017/11/20 0020.
 */

public class LocalAudioAdapter extends BaseAdapter {
    private Context mContext;
    private List<AudioBean> lists;

    public LocalAudioAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<AudioBean> list) {
        this.lists = list;
    }

    @Override
    public int getCount() {
        return lists == null ? 0 : lists.size();
    }

    @Override
    public Object getItem(int i) {
        return lists == null ? null : lists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return lists == null ? 0 : i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null){
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_local_auido,null);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(mContext,72));
            view.setLayoutParams(layoutParams);
            holder.artistPhotoIv = view.findViewById(R.id.artist_photo_iv);
            holder.displayNameTv = view.findViewById(R.id.display_name_tv);
            holder.artistNameTv = view.findViewById(R.id.artist_name_tv);
            holder.albumTv = view.findViewById(R.id.album_tv);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        AudioBean audioBean = lists.get(i);
//        holder.artistPhotoIv.setImageResource();
        holder.displayNameTv.setText(audioBean.getTitle());
        holder.artistNameTv.setText(audioBean.getArtist());
        holder.albumTv.setText(audioBean.getAlbum());
        return view;
    }

    class ViewHolder{
        ImageView artistPhotoIv;
        TextView displayNameTv;
        TextView artistNameTv;
        TextView albumTv;
    }


}
