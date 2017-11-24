package com.hua.media.localaudio;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hua.media.R;
import com.hua.media.bean.AudioBean;
import com.hua.media.base.BaseFragment;
import com.hua.media.localaudio.adapter.LocalAudioAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/20 0020.
 */

public class LocalAudioFragment extends BaseFragment{
    private ListView listView;
    private LocalAudioAdapter adapter;
    private List<AudioBean> audioList = new ArrayList<>();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (audioList != null && audioList.size() > 0){
                adapter.setData(audioList);
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void setLayout(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.fragment_local_audio, container, false);
    }

    @Override
    protected void findViewById() {
        listView = mView.findViewById(R.id.local_audio_lv);
    }

    @Override
    protected void setListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //3.传递列表数据-对象-序列化
                Intent intent = new Intent(getActivity(),AudioPlayerActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void init() {
        adapter = new LocalAudioAdapter(getActivity());
        listView.setAdapter(adapter);
        //加载本地视频数据
        getLocalAudio();
    }

    private void getLocalAudio() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                audioList.clear();
                //使用内容提供者形式获取数据
                ContentResolver resolver = getContext().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                /**
                 *歌曲信息
                 //获取歌曲在系统中的id
                 MediaStore.Audio.Media._ID
                 //获取歌曲的歌名
                 MediaStore.Audio.Media.TITLE;
                 //获取歌曲所在专辑的id
                 MediaStore.Audio.Media.ALBUM_ID;
                 //获取专辑的歌手名
                 MediaStore.Audio.Media.ARTIST
                 //获取歌曲的时长
                 MediaStore.Audio.Media.DURATION
                 //获取歌曲的大小
                 MediaStore.Audio.Media.SIZE
                 //获取专辑名
                 MediaStore.Audio.Media.ALBUM
                 //获取歌曲路径，如xx/xx/xx.mp3
                 ediaStore.Audio.Media.DATA

                 专辑信息
                 //获取专辑id
                 MediaStore.Audio.Albums._ID
                 //获取专辑名
                 MediaStore.Audio.Albums.ALBUM
                 //获取专辑歌手
                 MediaStore.Audio.Albums.ARTIST
                 //获取专辑歌曲数
                 MediaStore.Audio.Albums.NUMBER_OF_SONGS
                 */
                String[] objs = {
                        //获取歌曲的歌名
                        MediaStore.Audio.Media.TITLE,
                        //视频文件在sdcard上对应的名称
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        //视频总时长
                        MediaStore.Audio.Media.DURATION,
                        //视频的文件大小
                        MediaStore.Audio.Media.SIZE,
                        //视频的绝对地址
                        MediaStore.Audio.Media.DATA,
                        //歌曲的演唱者
                        MediaStore.Audio.Media.ARTIST,
                        //获取专辑名
                        MediaStore.Audio.Media.ALBUM
                };
                Cursor cursor = resolver.query(uri,objs,null,null,null);
                if (cursor != null){
                    while (cursor.moveToNext()){
                        AudioBean audioBean = new AudioBean();
                        String title = cursor.getString(0);//获取歌曲的歌名
                        audioBean.setTitle(title);

                        String name = cursor.getString(1);//视频的名称
                        audioBean.setName(name);

                        long duration = cursor.getLong(2);//视频的时长
                        audioBean.setDuration(duration);

                        long size = cursor.getLong(3);//视频的文件大小
                        audioBean.setSize(size);

                        String data = cursor.getString(4);//视频的播放地址
                        audioBean.setData(data);

                        String artist = cursor.getString(5);//艺术家
                        audioBean.setArtist(artist);

                        String album = cursor.getString(6);////获取专辑名
                        audioBean.setAlbum(album);
                        audioList.add(audioBean);
                    }
                    cursor.close();
                }
                //Handler发消息
                handler.sendEmptyMessage(10);
            }
        }.start();
    }
}
