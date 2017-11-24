package com.hua.media.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hua.librarytools.utils.PreferencesUtils;
import com.hua.librarytools.utils.UIToast;
import com.hua.media.IMusicPlayerService;
import com.hua.media.R;
import com.hua.media.bean.AudioBean;
import com.hua.media.common.Constant;
import com.hua.media.localaudio.AudioPlayerActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjz on 2017/11/21 0021.
 */

public class MusicPlayerService extends Service {
    private List<AudioBean> lists = new ArrayList<>();
    private int position;
    private NotificationManager notificationManager; //状态栏通知
    private static final Integer PENDING_REQUEST_CODE = 1;

    /**
     * 播放音乐
     */
    private MediaPlayer mediaPlayer;
    /**
     * 当前播放音频文件对象
     */
    private AudioBean audioBean;
    /**
     * 随机播放
     */
    public static final int REPEAT_RANDOM = 1;
    /**
     * 单曲循环
     */
    public static final int REPEAT_SINGLE = 2;
    /**
     * 全部循环
     */
    public static final int REPEAT_ALL = 3;
    /**
     * 播放模式
     */
    private int playmode = REPEAT_ALL;

    @Override
    public void onCreate() {
        super.onCreate();
        playmode = PreferencesUtils.getPreference(this, Constant.PLAY_MODE_PRE,Constant.PLAY_MODE_KEY,REPEAT_ALL);
        //得到音频列表
        getAudioData();
    }

    private void getAudioData() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                lists.clear();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.TITLE, //获取歌曲的歌名
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//视频总时长
                        MediaStore.Audio.Media.SIZE,//视频的文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者
                        MediaStore.Audio.Media.ALBUM  //获取专辑名
                };
                Cursor cursor = resolver.query(uri,objs,null,null,null);
                if (cursor != null){
                    while (cursor.moveToNext()){
                        AudioBean bean = new AudioBean();
                        String title = cursor.getString(0);//获取歌曲的歌名
                        bean.setTitle(title);

                        String name = cursor.getString(1);//视频的名称
                        bean.setName(name);

                        long duration = cursor.getLong(2);//视频的时长
                        bean.setDuration(duration);

                        long size = cursor.getLong(3);//视频的文件大小
                        bean.setSize(size);

                        String data = cursor.getString(4);//视频的播放地址
                        bean.setData(data);

                        String artist = cursor.getString(5);//艺术家
                        bean.setArtist(artist);

                        String album = cursor.getString(6);////获取专辑名
                        bean.setAlbum(album);
                        lists.add(bean);
                    }
                    cursor.close();
                }
            }
        }.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    /**
     * 代理对象
     */
    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.pause();
        }

        @Override
        public int getCurrentPostion() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getTitle() throws RemoteException {
            return service.getTitle();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }
    };

    /**
     * 根据位置打开对应的音频文件，并且播放
     * @param position
     */
    private void openAudio(int position){
        this.position = position;
        if (lists != null && lists.size() > 0){
            audioBean = lists.get(position);
            if (mediaPlayer != null){
                mediaPlayer.reset();  //重置（恢复默认值）
            }

            try {
                mediaPlayer = new MediaPlayer();
                //设置监听相关监听
                //准备好
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                // 播放出错
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                // 播放完成
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                //设置数据源
                mediaPlayer.setDataSource(audioBean.getData());
                //异步执行
                mediaPlayer.prepareAsync();

                //单曲循环
                if (playmode == MusicPlayerService.REPEAT_SINGLE){
                    //不会触发播放完成的回调
                    mediaPlayer.setLooping(true);
                }
                //随机播放
                else if (playmode == MusicPlayerService.REPEAT_RANDOM){
//                    mediaPlayer.setLooping(false);
                }
                //全部循环播放
                else if (playmode == MusicPlayerService.REPEAT_ALL){

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            UIToast.showBaseToast(this,"还没有数据", R.style.AnimationToast);
        }
    }

    /**
     * 播放音乐
     */
    private void start(){
        if (mediaPlayer != null){
            mediaPlayer.start();
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //点中状态栏通知跳转到对应的页面
            Intent intent = new Intent(this, AudioPlayerActivity.class);
            //标识是从状态拦点击进入播放页面
            intent.putExtra("notification",true);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,PENDING_REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.notification_music_playing)
                    .setContentTitle(getArtist())
                    .setContentText("正在播放:"+getName())
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(PENDING_REQUEST_CODE,notification);
        }
    }

    /**
     * 播暂停音乐
     */
    private void pause(){
        mediaPlayer.pause();
        notificationManager.cancel(PENDING_REQUEST_CODE);
    }

    /**
     * 停止
     */
    private void stop(){

    }

    /**
     * 得到当前播放进度
     */
    private int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 得到当前音频的总时长
     */
    private int getDuration(){
//        Log.v("hjz","getDuration="+mediaPlayer.getDuration());
        return mediaPlayer.getDuration();
    }

    /**
     * 得到歌唱者名字
     */
    private String getArtist(){
        return audioBean.getArtist();
    }

    /**
     * 获取歌曲名称（不带演唱者名字）
     * @return
     */
    private String getTitle(){
        return audioBean.getTitle();
    }

    /**
     * 得到歌曲名称（带演唱者名字）
     */
    private String getName(){
        return audioBean.getName();
    }

    /**
     * 得到歌曲播放的路径
     * @return
     */
    private String getAudioPath(){
        return audioBean.getData();
    }

    /**
     * 播放下一首
     */
    private void next(){
        //1、根据当前播放模式，设置下一首的位置
        setNextPosition();
        //2、根据当前的播放模式和下标位置去播放对应内容
        openNextAudio();
    }

    /**
     * 播放上一首
     */
    private void pre(){
        //1、根据当前播放模式，设置上一首的位置
        setPrePosition();
        //2、根据当前的播放模式和下标位置去播放对应内容
        openPreAudio();
    }

    /**
     * 设置播放模式
     * @param playMode
     */
    private void setPlayMode(int playMode){
        playMode = playMode;
        PreferencesUtils.setPreferences(this,Constant.PLAY_MODE_PRE,Constant.PLAY_MODE_KEY,playMode);
        if(playmode==MusicPlayerService.REPEAT_SINGLE){
            //单曲循环播放-不会触发播放完成的回调
            mediaPlayer.setLooping(true);
        }else{
            //不循环播放
            mediaPlayer.setLooping(false);
        }
    }

    /**
     * 得到播放模式
     * @return
     */
    private int getPlayMode(){
        return playmode;
    }

    /**
     * 是否在播放音频
     * @return
     */
    private boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    /**
     * 拖动进度条
     * @param position
     * @throws RemoteException
     */
    private void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    //准备好
    private class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //通知相关页面
            EventBus.getDefault().post(audioBean);
            //准备好了开始播放
            start();
        }
    }

    //播放出错
    private class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            next();
            return true;  //自个处理
        }
    }

    //播放完成
    private class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            next();
        }
    }

    //根据当前播放模式，设置下一首的位置
    private void setNextPosition() {
        playmode = getPlayMode();
        //单曲
        if (playmode == MusicPlayerService.REPEAT_SINGLE){
            //单曲播放 position还是当前position值
        }
        //随机播放
        else if (playmode == MusicPlayerService.REPEAT_RANDOM){

        }
        //全部循环播放
        else if (playmode == MusicPlayerService.REPEAT_ALL){
            position++;
            if(position >=lists.size()){
                position = 0;
            }
        }
    }

    //根据当前的播放模式和下标位置去播放对应内容
    private void openNextAudio() {
        playmode = getPlayMode();
        //单曲循环
        if (playmode == MusicPlayerService.REPEAT_SINGLE){
            if (position < lists.size()){ //合法资源
                openAudio(position);
            }else{  //非合法资源
                position = lists.size() -1;
            }
        }
        //顺随机播放
        else if (playmode == MusicPlayerService.REPEAT_RANDOM){

        }
        //全部循环播放
        else if (playmode == MusicPlayerService.REPEAT_ALL){
            if (position < lists.size()){ //合法资源
                openAudio(position);
            }else{  //非合法资源
                position = 0;
                openAudio(position);
            }
        }
    }

    //根据当前播放模式，设置上一首的位置
    private void setPrePosition() {
        playmode = getPlayMode();
        //单曲
        if (playmode == MusicPlayerService.REPEAT_SINGLE){

        }
        //随机
        else if (playmode == MusicPlayerService.REPEAT_RANDOM){
           position --;
        }
        //全部循环播放
        else if (playmode == MusicPlayerService.REPEAT_ALL){
            position--;
            if(position < 0){
                position = lists.size()-1;
            }
        }
    }

    //根据当前的播放模式和下标位置去播放对应内容
    private void openPreAudio() {
        playmode = getPlayMode();
        //单曲
        if (playmode == MusicPlayerService.REPEAT_SINGLE){
            if(position >= 0){
                //正常范围
                openAudio(position);
            }else{
                position = 0;
                openAudio(position);
            }
        }
        //随机
        else if (playmode == MusicPlayerService.REPEAT_RANDOM){

        }
        //全部顺序播放
        else if (playmode == MusicPlayerService.REPEAT_ALL){
            if(position >= 0){
                //正常范围
                openAudio(position);
            }else{
                position = lists.size()-1;
                openAudio(position);
            }
        }
    }
}
