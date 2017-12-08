package com.hua.media.common;

/**
 * Created by hjz on 2017/11/21 0021.
 */

public class Constant {

    /**
     * 播放模式
     */
    public static final String PLAY_MODE_PRE = "play_mode_pre";
    public static final String PLAY_MODE_KEY = "play_mode_key";

    /**
     * 本地音频数据集
     */
    public static final String LOCAL_AUDIO_PRE = "local_audio_pre";
    public static final String LOCAL_AUDIO_KEY = "local_audio_key";

    /**
     * 科大讯飞
     */
    public static final String VOICE_IFLYTEK_PRE = "voice_iflytek_pre";
    public static final String VOICE_IFLYTEK_TRANSLATE_KEY = "voice_iflytek_translate_key";
    public static final String VOICE_IFLYTEK_LANGUAGE_KEY = "voice_iflytek_language_key";
    // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
    public static final String VOICE_IFLYTEK_VADEOS_KEY = "voice_iflytek_vadeos_key";
    // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
    public static final String VOICE_IFLYTEK_PUNC_KEY = "voice_iflytek_punc_key";
}
