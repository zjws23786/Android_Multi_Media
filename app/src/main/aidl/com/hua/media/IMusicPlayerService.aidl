// IMusicPlayerService.aidl
package com.hua.media;

// Declare any non-default types here with import statements

interface IMusicPlayerService {

    /**
     * 根据位置打开对应的音频文件，并且播放
     * @param position
     */
    void openAudio(int position);

    /**
     * 根据位置打开对应的音频文件，并且播放
     * @param position
     */
    void fromListOpenAudio(int position);

    /**
     * 播放音乐
     */
    void start();

    /**
     * 播暂停音乐
     */
    void pause();

    /**
     * 停止
     */
    void stop();

    /**
     * 得到当前播放进度
     */
    int getCurrentPostion();

    /**
     * 得到当前音频的总时长
     */
    int getDuration();

    /**
     * 得到歌唱者名字
     */
    String getArtist();

    /**
     * 得到歌曲名称(不带有演唱者名字)
     */
    String getTitle();

    /**
     * 得到歌曲名称(带有演唱者名字)
     */
    String getName();

    /**
     * 得到歌曲播放的路径
     * @return
     */
    String getAudioPath();

    /**
     * 播放下一首
     */
    void next();

    /**
     * 播放上一首
     */
    void pre();

    /**
     * 设置播放模式
     * @param playMode
     */
    void setPlayMode(int playMode);

    /**
     * 得到播放模式
     * @return
     */
    int getPlayMode();

    /**
     * 是否在播放音频
     * @return
     */
    boolean isPlaying();

    //拖动进度条
    void seekTo(int position);
}
