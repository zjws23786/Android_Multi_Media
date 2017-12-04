package com.hua.media.localvideo.adapter;

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
import com.hua.media.bean.VideoBean;

import java.util.List;

/**
 * @author hjz
 * @date 2017/12/1 0001
 */

public class LocalVideoAdapter extends BaseAdapter {
    private Context mContext;
    private List<VideoBean> videoList;

    public LocalVideoAdapter(Context context){
        this.mContext = context;
    }

    public void setData(List<VideoBean> videoList) {
        this.videoList = videoList;
    }

    @Override
    public int getCount() {
        return videoList == null ? 0:videoList.size();
    }

    @Override
    public Object getItem(int position) {
        return videoList == null ? null:videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return videoList == null ? 0:position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_local_video,null);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(mContext,72));
            convertView.setLayoutParams(layoutParams);
            holder.artistPhotoIv = (ImageView) convertView.findViewById(R.id.artist_photo_iv);
            holder.displayNameTv = (TextView) convertView.findViewById(R.id.display_name_tv);
            holder.artistNameTv = (TextView) convertView.findViewById(R.id.artist_name_tv);
            holder.popupMenu = (ImageView) convertView.findViewById(R.id.popup_menu);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        VideoBean bean = videoList.get(position);
        holder.displayNameTv.setText(bean.getTitle());
        holder.artistNameTv.setText(bean.getArtist());
        return convertView;
    }

    class ViewHolder{
        ImageView artistPhotoIv;
        TextView displayNameTv;
        TextView artistNameTv;
        ImageView popupMenu;
    }
}
