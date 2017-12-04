package com.hua.media.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * @author hjz
 * @date 2017/12/1 0001
 * 本地视频管理类
 */

public class LocalVideoManage {

    public static Cursor getLocalVideo(Context context){
        //使用内容提供者形式获取数据
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        /**
         *歌曲信息
         //获取歌曲在系统中的id
         MediaStore.Video.Media._ID
         //获取歌曲的歌名
         MediaStore.Video.Media.TITLE;
         //获取歌曲所在专辑的id
         MediaStore.Video.Media.ALBUM_ID;
         //创建视频文件的艺术家，如果有的话
         MediaStore.Video.Media.ARTIST
         //获取歌曲的时长
         MediaStore.Video.Media.DURATION
         //获取歌曲的大小
         MediaStore.Video.Media.SIZE
         //视频文件来自的专辑（如果有的话）
         MediaStore.Video.Media.ALBUM
         //获取歌曲路径，如xx/xx/xx.mp3
         MediaStore.Video.Media.DATA
         //视频的书签
         MediaStore.Video.Media.BOOKMARK
         //视频的桶显示名称
         MediaStore.Video.Media.BUCKET_DISPLAY_NAME
         //视频的存储分区ID
         MediaStore.Video.Media.BUCKET_ID
         //视频的语言
         MediaStore.Video.Media.LANGUAGE
         //
         MediaStore.Video.Media.LANGUAGE


         专辑信息
         //获取专辑id
         MediaStore.Video.Albums._ID
         //视频文件来自的专辑（如果有的话）
         MediaStore.Video.Albums.ALBUM
         //创建视频文件的艺术家，如果有的话
         MediaStore.Video.Albums.ARTIST
         //获取专辑歌曲数
         MediaStore.Video.Albums.NUMBER_OF_SONGS
         */
        String[] objs = {
                //获取歌曲的歌名
                MediaStore.Video.Media.TITLE,
                //视频文件在sdcard上对应的名称
                MediaStore.Video.Media.DISPLAY_NAME,
                //视频总时长
                MediaStore.Video.Media.DURATION,
                //视频的文件大小
                MediaStore.Video.Media.SIZE,
                //视频的绝对地址
                MediaStore.Video.Media.DATA,
                //歌曲的演唱者
                MediaStore.Video.Media.ARTIST,
                //获取专辑名
                MediaStore.Video.Media.ALBUM,
                //获取歌曲在系统中的id
                MediaStore.Video.Media._ID
        };
        Cursor cursor = resolver.query(uri,objs,null,null,null);
        return cursor;
    }
}
