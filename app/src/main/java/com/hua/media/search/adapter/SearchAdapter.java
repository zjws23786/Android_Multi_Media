package com.hua.media.search.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hua.media.R;
import com.hua.media.bean.SearchModel;
import com.hua.media.utils.ImageUtils;

import java.util.List;

/**
 * @author hjz
 * @date 2017/12/8 0008
 */

public class SearchAdapter extends BaseAdapter {
    private Context mContext;
    private List<SearchModel.ItemsBean> lists;

    public SearchAdapter(Context context){
        this.mContext = context;
    }

    public void setData(List<SearchModel.ItemsBean> lists) {
        this.lists = lists;
    }

    @Override
    public int getCount() {
        return lists == null ? 0 : lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists == null ? null : lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return lists == null ? 0 : position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHoder viewHoder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_search,null);
            viewHoder = new ViewHoder();
            viewHoder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHoder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHoder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

            convertView.setTag(viewHoder);
        }else{
            viewHoder = (ViewHoder) convertView.getTag();
        }
        SearchModel.ItemsBean itemsBean = lists.get(position);
        ImageUtils.displayHeader(itemsBean.getItemImage().getImgUrl1(), viewHoder.iv_icon);
        viewHoder.tv_name.setText(itemsBean.getItemTitle());
        viewHoder.tv_desc.setText(itemsBean.getKeywords());
        return convertView;
    }

    class ViewHoder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }
}
