package com.hua.media.localvideo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hua.media.R;
import com.hua.media.base.BaseFragment;
import com.hua.media.bean.AudioBean;
import com.hua.media.bean.VideoBean;
import com.hua.media.common.Constant;
import com.hua.media.db.LocalAudioManage;
import com.hua.media.db.LocalVideoManage;
import com.hua.media.localvideo.adapter.LocalVideoAdapter;
import com.hua.media.utils.ListDataSavePreference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.Executors.*;

/**
 * @author hjz
 * @date 2017/11/20 0020
 */
public class LocalVideoFragment extends BaseFragment {
    private ListView mLocalVideoLv;
    private LocalVideoAdapter adapter;
    private List<VideoBean> videoList = new ArrayList<>();
    private ExecutorService executorService = null;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (videoList != null && videoList.size() > 0){
                adapter.setData(videoList);
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void setLayout(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.fragment_local_video,container,false);
    }

    @Override
    protected void findViewById() {
        mLocalVideoLv = (ListView) mView.findViewById(R.id.local_video_lv);
    }

    @Override
    protected void setListener() {
        mLocalVideoLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(),SystemVideoPlayerActivity.class);
                intent.putExtra("video",videoList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void init() {
        adapter = new LocalVideoAdapter(getContext());
        mLocalVideoLv.setAdapter(adapter);
        if (executorService == null){
            executorService = newSingleThreadExecutor();
        }
        executorService.execute(new LocalVideoThread());
    }

    public class LocalVideoThread implements Runnable{

        @Override
        public void run() {
            videoList.clear();
            Cursor cursor = LocalVideoManage.getLocalVideo(getContext());
            if (cursor != null){
                while (cursor.moveToNext()){
                    VideoBean videoBean = new VideoBean();
                    //获取视频标题
                    String title = cursor.getString(0);
                    videoBean.setTitle(title);
                    //视频的名称
                    String name = cursor.getString(1);
                    videoBean.setName(name);

                    //视频的时长
                    long duration = cursor.getLong(2);
                    videoBean.setDuration(duration);

                    //视频的文件大小
                    long size = cursor.getLong(3);
                    videoBean.setSize(size);

                    //视频的播放地址
                    String data = cursor.getString(4);
                    videoBean.setData(data);

                    //艺术家
                    String artist = cursor.getString(5);
                    videoBean.setArtist(artist);

                    //获取专辑名
                    String album = cursor.getString(6);
                    videoBean.setAlbum(album);

                    //获取视频在系统中的id
                    long idStr = cursor.getLong(7);
                    videoBean.setId(idStr);
                    videoList.add(videoBean);
                }
                cursor.close();
            }
            //Handler发消息
            handler.sendEmptyMessage(10);
        }
    }
}
