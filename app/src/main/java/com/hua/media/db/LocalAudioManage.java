package com.hua.media.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by hjz on 2017/11/28 0028.
 */

public class LocalAudioManage {

    public static Cursor getLocalAudio(Context context){
        //使用内容提供者形式获取数据
        ContentResolver resolver = context.getContentResolver();
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
                MediaStore.Audio.Media.ALBUM,
                //获取歌曲在系统中的id
                MediaStore.Audio.Media._ID
        };
//        Cursor cursor = resolver.query(uri,objs,null,null,MediaStore.Audio.Media.DATA);
        Cursor cursor = resolver.query(uri,objs,null,null,null);
        return cursor;
    }
}
