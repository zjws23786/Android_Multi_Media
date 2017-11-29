package com.hua.media.localaudio;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hua.librarytools.utils.CustomLayoutDialog;
import com.hua.librarytools.utils.PreferencesUtils;
import com.hua.librarytools.utils.UIToast;
import com.hua.media.R;
import com.hua.media.bean.AudioBean;
import com.hua.media.base.BaseFragment;
import com.hua.media.common.Constant;
import com.hua.media.db.LocalAudioManage;
import com.hua.media.localaudio.adapter.LocalAudioAdapter;
import com.hua.media.utils.ListDataSavePreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/20 0020.
 */

public class LocalAudioFragment extends BaseFragment implements LocalAudioAdapter.PopupMenuOnClick {
    private ListView listView;
    private LocalAudioAdapter adapter;
    private List<AudioBean> audioList = new ArrayList<>();

    private CustomLayoutDialog layoutDialog;
    private TextView deleteSongNameTv; //要删除歌名
    private TextView cancelTv; //dialog取消
    private TextView determineTv;//dialog确定

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

        CustomLayoutDialog.Builder dialogBuild = new CustomLayoutDialog.Builder(getContext());
        layoutDialog = dialogBuild.create(R.layout.dialog_delete_song, 0.85f,0);
        deleteSongNameTv = layoutDialog.findViewById(R.id.delete_song_name_tv);
        cancelTv = layoutDialog.findViewById(R.id.cancel_tv);
        determineTv = layoutDialog.findViewById(R.id.determine_tv);
        layoutDialog.setCanceledOnTouchOutside(false);// 点击外部区域关闭
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
        adapter.setMenuOnClick(this);
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
                Cursor cursor = LocalAudioManage.getLocalAudio(getContext());
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

                        long idStr = cursor.getLong(7);//获取歌曲在系统中的id
                        audioBean.setId(idStr);
                        audioList.add(audioBean);
                    }
                    cursor.close();
                    ListDataSavePreference.setDataList(getContext(),Constant.LOCAL_AUDIO_PRE,Constant.LOCAL_AUDIO_KEY,audioList);
                }
                //Handler发消息
                handler.sendEmptyMessage(10);
            }
        }.start();
    }

    //删除歌曲
    @Override
    public void deleteSongClick(View view, final int position) {
        PopupMenu menu = new PopupMenu(getContext(), view);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.popup_song_delete: //删除
                        AudioBean audioBean = audioList.get(position);
                        showDialog(audioBean);
                        break;
                }
                return false;
            }
        });
        menu.inflate(R.menu.popup_song);
        menu.show();
    }

    private void showDialog(final AudioBean audioBean) {
        if (!layoutDialog.isShowing()){
            deleteSongNameTv.setText("删除 "+ audioBean.getTitle());
            layoutDialog.show();
        }
        //取消
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutDialog.isShowing()){
                    layoutDialog.dismiss();
                }
            }
        });

        //确定
        determineTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //使用内容提供者形式获取数据
                ContentResolver resolver = getContext().getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String where = MediaStore.Audio.Media._ID + "=?";
                String[] selectionArgs={audioBean.getId()+""};
                int deleteCount = resolver.delete(uri, where, selectionArgs);
                if (deleteCount > 0){
                    layoutDialog.dismiss();
                    getLocalAudio();
                    UIToast.showBaseToast(getContext(),"删除成功",R.style.AnimationToast);
                }
            }
        });
    }
}
