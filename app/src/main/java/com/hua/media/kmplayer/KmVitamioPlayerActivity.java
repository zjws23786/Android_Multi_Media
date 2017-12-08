package com.hua.media.kmplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hua.librarytools.utils.DensityUtil;
import com.hua.librarytools.utils.UIToast;
import com.hua.media.R;
import com.hua.media.base.BaseActivity;
import com.hua.media.common.Env;
import com.hua.media.utils.DateTools;
import com.hua.media.utils.HardwareInfoUtils;
import com.hua.media.utils.NetSpeedUtils;
import com.hua.media.widget.VitamioVideoView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;

public class KmVitamioPlayerActivity extends BaseActivity implements View.OnClickListener {
    private TextView mVideoPathTv; //视频路径
    private ImageView mBatteryIv; //电量显示
    private TextView mSystemTimeTv; //系统时间
    private VitamioVideoView mVitamioVideoView;
    private LinearLayout mVitamioTopLayout;
    private RelativeLayout mVolumeLayout;
    private ImageView mPlayerVolumeIv;
    private TextView mVolumePercentageTv;
    private RelativeLayout mBrightLayout;
    private TextView mBrightPercentageTv;
    private RelativeLayout mProgressLayout;
    private ImageView mProgressTimeIv;
    private TextView mProgressTimeTv;
    private LinearLayout mVitamioBottomLayout;
    private SeekBar mVitamioPosSeekBar;
    private ImageView mVitamioPauseImg;
    private TextView mVitamioCurrentTimeTv;
    private TextView mVitamioTotalTimeTv;
    private LinearLayout mVitamioProgressbarLayout;
    private TextView mVitamioBufferNetSpeedTv;
    private ImageView swichPlayerIv;  //切换播放模式
    private TextView mLoadRateTv;  //视频缓冲的进度

    private Uri videoUri;
    private NetSpeedUtils netSpeedUtils;
    private int playerWidth; //视频窗口宽
    private int playerHeight; //视频窗口高
    private AudioManager mAudioManager; //音频管理器
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
    private int currentVolume; //当前音量值
    private int maxVolume;  //最大音量值
    private boolean isFullScreen = false; //是否全屏【true表示全屏】
    private boolean isAdjust = false; //触摸屏幕后是否到达调整手机音量或亮度
    private int threshold = 54; //触摸屏幕到达调整的最小临界值
    private int startX;  //手指视频View的X轴位置
    private int startY;  //手指视频View的Y轴位置
    private float lastX = 0; //手指视频View的X轴位置
    private float lastY = 0; //手指视频View的Y轴位置
    private int playingTime; //视频播放时间
    private int videoTotalTime; //视频播放的总时长
    private float mBrightness; //当前亮度值
    private boolean isClick = true;

    /**
     * Handler更新UI 的播放进度条和播放相关数据
     */
    private static final int UPDATE_UI = 1;
    /**
     * 自动隐藏顶部和底部View的时间
     */
    private static final int HIDE_TIME = 5000;
    /**
     * 显示网络速度
     */
    private static final int SHOW_SPEED = 2;
    /**
     * 监听电量变化的广播
     */
    private MyBatteryReceiver batteryReceiver;


    private final MyHandler mHandler = new MyHandler(this);

    public class MyHandler extends Handler {
        WeakReference<Activity > mActivityReference;

        public MyHandler(Activity activity) {
            mActivityReference= new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final Activity activity = mActivityReference.get();
            if (activity != null){
                switch (msg.what){
                    case UPDATE_UI:
                        int currentPosition = (int) mVitamioVideoView.getCurrentPosition();
                        int totalPosition = (int) mVitamioVideoView.getDuration();
                        updateTextViewWithTimeFormat(mVitamioCurrentTimeTv , currentPosition);
                        mVitamioPosSeekBar.setMax(totalPosition);
                        mVitamioPosSeekBar.setProgress(currentPosition);
                        //设置系统时间
                        mSystemTimeTv.setText(getSysteTime());
                        mHandler.removeMessages(UPDATE_UI);
                        mHandler.sendEmptyMessageDelayed(UPDATE_UI , 500);
                        break;
                    case SHOW_SPEED:
                        //1.得到网络速度
                        String netSpeed = netSpeedUtils.getNetSpeed(activity);
//                        Log.v("hjz","netSpeed="+netSpeed);
                        //显示网络速
                        mVitamioBufferNetSpeedTv.setText("缓存中..."+netSpeed);

                        //2.每两秒更新一次
                        mHandler.removeMessages(SHOW_SPEED);
                        mHandler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);
                        break;
                    default:
                        showOrHide();
                        break;
                }
            }
        }
    }

    @Override
    protected void setLayout() {
        if (!LibsChecker.checkVitamioLibs(this)){
            return;
        }
        setContentView(R.layout.activity_km_vitamio_player);
    }

    @Override
    protected void findViewById() {
        mVitamioVideoView = (VitamioVideoView) findViewById(R.id.vitamio_video_view);
        mVitamioTopLayout = (LinearLayout) findViewById(R.id.vitamio_top_layout);
        mVolumeLayout = (RelativeLayout) findViewById(R.id.volume_layout);
        mPlayerVolumeIv = (ImageView) findViewById(R.id.player_volume_iv);
        mVolumePercentageTv = (TextView) findViewById(R.id.volume_percentage_tv);
        mBrightLayout = (RelativeLayout) findViewById(R.id.bright_layout);
        mBrightPercentageTv = (TextView) findViewById(R.id.bright_percentage_tv);
        mProgressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        mProgressTimeIv = (ImageView) findViewById(R.id.progress_time_iv);
        mProgressTimeTv = (TextView) findViewById(R.id.progress_time_tv);
        mVitamioBottomLayout = (LinearLayout) findViewById(R.id.vitamio_bottom_layout);
        mVitamioPosSeekBar = (SeekBar) findViewById(R.id.vitamio_pos_seekBar);
        mVitamioPauseImg = (ImageView) findViewById(R.id.vitamio_pause_img);
        mVitamioCurrentTimeTv = (TextView) findViewById(R.id.vitamio_current_time_tv);
        mVitamioTotalTimeTv = (TextView) findViewById(R.id.vitamio_total_time_tv);
        mVitamioProgressbarLayout = (LinearLayout) findViewById(R.id.vitamio_progressbar_layout);
        mVitamioBufferNetSpeedTv = (TextView) findViewById(R.id.vitamio_buffer_net_speed_tv);
        swichPlayerIv = (ImageView) findViewById(R.id.swich_player_iv);
        mLoadRateTv = (TextView) findViewById(R.id.load_rate_tv);
        mVideoPathTv = (TextView) findViewById(R.id.video_path_tv);
        mBatteryIv = (ImageView) findViewById(R.id.battery_iv);
        mSystemTimeTv = (TextView) findViewById(R.id.system_time_tv);
    }

    @Override
    protected void setListener() {
        //播放或暂停
        mVitamioPauseImg.setOnClickListener(this);
        //切换了视频播放模式
        swichPlayerIv.setOnClickListener(this);

        //接收对SeekBar进度级别的更改的通知。 还提供了用户什么时候在SeekBar内启动和停止触摸手势的通知。
        mVitamioPosSeekBar.setOnSeekBarChangeListener(new MyPosOnSeekBarChangeListener());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.vitamio_pause_img:
                if (mVitamioVideoView.isPlaying()){
                    mVitamioPauseImg.setImageResource(R.drawable.video_start_style);
                    mVitamioVideoView.pause();
                    mHandler.removeMessages(UPDATE_UI);
                }else{
                    mVitamioProgressbarLayout.setVisibility(View.GONE);
                    mVitamioPauseImg.setImageResource(R.drawable.video_stop_style);
                    mVitamioVideoView.start();
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }
                break;
            case R.id.swich_player_iv:
                showSwichPlayerDialog();
                break;
        }
    }

    private void showSwichPlayerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("万能播放器提醒您");
        builder.setMessage("当您播放视频，有声音没有画面的时候，请切换万能播放器播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startVitamioPlayer();
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    /**
     * a,把数据按照原样传入VtaimoVideoPlayer播放器
     b,关闭系统播放器
     */
    private void startVitamioPlayer() {
        if(mVitamioVideoView != null){
            mVitamioVideoView.stopPlayback();
        }

        Intent intent = new Intent(this,KmPlayerActivity.class);
        if (videoUri != null){
            intent.setData(videoUri);
        }
        startActivity(intent);
        finish();//关闭页面
    }

    @Override
    protected void init() {
        //初始化获取当前网速工具类
        netSpeedUtils = new NetSpeedUtils();
        //得到播放地址
        videoUri = getIntent().getData();
        /** 获取视频播放窗口的尺寸 */
        ViewTreeObserver viewObserver = mVitamioVideoView.getViewTreeObserver();
        //当指定视图状态发生改变时触发
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVitamioVideoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerWidth = mVitamioVideoView.getWidth();
                playerHeight = mVitamioVideoView.getHeight();
            }
        });

        //音频管理器
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取系统最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取当前值
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        playVideo();

        //注册电量广播
        batteryReceiver = new MyBatteryReceiver();
        IntentFilter intentFiler = new IntentFilter();
        //当电量变化的时候发这个广播
        intentFiler.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, intentFiler);

        //设置系统时间
        mSystemTimeTv.setText(getSysteTime());

        //开始更新网络速度
        mHandler.sendEmptyMessage(SHOW_SPEED);
    }

    class MyBatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //0~100;
            int level = intent.getIntExtra("level", 0);
            //主线程
            setBattery(level);
        }
    }

    /**
     * 设置手机电量
     * @param level
     */
    private void setBattery(int level) {
        if (level <= 0) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            mBatteryIv.setImageResource(R.drawable.ic_battery_100);
        } else {
            mBatteryIv.setImageResource(R.drawable.ic_battery_100);
        }
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    private void playVideo() {
        //网络视频播放
        mVitamioVideoView.setVideoURI(videoUri);
        mVideoPathTv.setText(videoUri.toString());
        mVitamioVideoView.requestFocus();

        //准备好的监听
        mVitamioVideoView.setOnPreparedListener(new MyOnPreparedListener());

        //播放完成了的监听
        mVitamioVideoView.setOnCompletionListener(new MyOnCompletionListener());

        //播放出错了的监听
        mVitamioVideoView.setOnErrorListener(new MyOnErrorListener());

        //监听视频播放卡-系统的api
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mVitamioVideoView.setOnInfoListener(new MyOnInfoListener());
        }

        //视频缓冲的进度监听事件
        mVitamioVideoView.setOnBufferingUpdateListener(new MyOnBufferingUpdateListener());

        //视频区域相关事件
        mVitamioVideoView.setOnTouchListener(mTouchListener);
    }

    /**
     * 当媒体文件被加载，并准备好了去调用
     */
    private class MyOnPreparedListener implements io.vov.vitamio.MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(io.vov.vitamio.MediaPlayer mp) {
            mVitamioProgressbarLayout.setVisibility(View.GONE);
            mVitamioVideoView.start();
            if (playingTime != 0){
                mVitamioVideoView.seekTo(playingTime);
            }

            updateTextViewWithTimeFormat(mVitamioTotalTimeTv , (int) mVitamioVideoView.getDuration());
            mHandler.sendEmptyMessage(UPDATE_UI);
            mHandler.postDelayed(hideRunnable, HIDE_TIME);
        }
    }

    /**
     * 在播放期间到达媒体文件结束时回调
     */
    private class MyOnCompletionListener implements io.vov.vitamio.MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(io.vov.vitamio.MediaPlayer mp) {
            mHandler.removeMessages(UPDATE_UI);
            mVitamioVideoView.pause();
            mVitamioPauseImg.setImageResource(R.drawable.video_start_style);
            mVitamioPosSeekBar.setProgress(0);
            mVitamioCurrentTimeTv.setText("00:00");
        }
    }


    private class MyOnErrorListener implements io.vov.vitamio.MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
            //1.播放的视频格式不支持--跳转到万能播放器继续播放
//            startVitamioPlayer();
            //2.播放网络视频的时候，网络中断---1.如果网络确实断了，可以提示用于网络断了；2.网络断断续续的，重新播放
            //3.播放的时候本地文件中间有空白---下载做完成
            switch (extra){
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    UIToast.showBaseToast(KmVitamioPlayerActivity.this,"超时",R.style.AnimationToast);
                    mVitamioProgressbarLayout.setVisibility(View.VISIBLE);
                    break;
                case MediaPlayer.MEDIA_ERROR_IO:
                    UIToast.showBaseToast(KmVitamioPlayerActivity.this,"IO",R.style.AnimationToast);
                    mVitamioProgressbarLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    /**
     * 听视频播放卡-系统的api
     */
    private class MyOnInfoListener implements io.vov.vitamio.MediaPlayer.OnInfoListener {

        @Override
        public boolean onInfo(io.vov.vitamio.MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    mVitamioProgressbarLayout.setVisibility(View.VISIBLE);
                    break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了，拖动卡结束了
                    mVitamioProgressbarLayout.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    }


    private class MyOnBufferingUpdateListener implements io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener{

        @Override
        public void onBufferingUpdate(io.vov.vitamio.MediaPlayer mp, int percent) {
            mLoadRateTv.setText(percent + "%");
        }
    }

    /**
     * 播放进度条
     */
    private class MyPosOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        //通知进度水平已经改变。
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            updateTextViewWithTimeFormat(mVitamioCurrentTimeTv , progress);
        }

        //通知用户已经开始触摸手势。
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeMessages(UPDATE_UI);
        }

        //通知用户已完成触摸手势。
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            mVitamioVideoView.seekTo(progress);
            mHandler.sendEmptyMessage(UPDATE_UI);
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    lastX = x;
                    lastY = y;
                    startX = (int) x;
                    startY = (int) y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - lastX;
                    float deltaY = y - lastY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    //XY轴的间距都大于临界值
                    if (absDeltaX > threshold && absDeltaY > threshold){
                        //竖直方向的距离大于水平方向的
                        if (absDeltaX < absDeltaY){
                            isAdjust = true;
                        }else{
                            isAdjust = false;
                        }
                    }else if (absDeltaX < threshold && absDeltaY > threshold){
                        isAdjust = true;
                    }else if (absDeltaX > threshold && absDeltaY < threshold){
                        isAdjust = false;
                    }else{
                        return true;
                    }

                    //进行屏幕亮度或音量的调节
                    if (isAdjust){
                        //亮度【视屏的亮度范围是0~255】
                        if (x < playerWidth/2){
                            mBrightLayout.setVisibility(View.VISIBLE);
                            if (deltaY > 0){ //屏幕亮度变亮
                                changeBrightness(-absDeltaY);
                            }else{ //屏幕亮度变暗
                                changeBrightness(absDeltaY);
                            }
                        }else{ //音量
                            mVolumeLayout.setVisibility(View.VISIBLE);
                            if (deltaY > 0){
                                changeVolume(-absDeltaY);
                            }else{
                                changeVolume(absDeltaY);
                            }
                        }
                    }else{
                        mProgressLayout.setVisibility(View.VISIBLE);
                        if (deltaX > 0) { //快进
                            forward(absDeltaX);
                        } else if (deltaX < 0) { //快退
                            backward(absDeltaX);
                        }
                    }
                    lastX = x;
                    lastY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    mBrightLayout.setVisibility(View.GONE);
                    mVolumeLayout.setVisibility(View.GONE);
                    mProgressLayout.setVisibility(View.GONE);
                    if (Math.abs(x - startX) > threshold
                            || Math.abs(y - startY) > threshold){
                        isClick = false;
                    }
                    lastX = 0;
                    lastY = 0;
                    startX = 0;
                    if (isClick) {
                        showOrHide();
                    }
                    isClick = true;
                    break;
            }
            return true;
        }
    };

    /**
     * 亮度的变化
     * @param changY
     */
    public void changeBrightness(float changY){
        WindowManager.LayoutParams attribute = getWindow().getAttributes();
        mBrightness = attribute.screenBrightness;
        float index = changY / playerHeight;
        mBrightness += index;

        if (mBrightness > 1.0f){
            mBrightness = 1.0f;
        }

        if (mBrightness < 0.01f){
            mBrightness = 0.01f;
        }

        attribute.screenBrightness = mBrightness;
        getWindow().setAttributes(attribute);
        mBrightPercentageTv.setText((int) (attribute.screenBrightness * 100) + "%");

    }

    /**
     * 音量的变化
     * @param changY
     */
    public void changeVolume(float changY){
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
        if (changY >= DensityUtil.dip2px(this, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
            if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                currentVolume++;
            }
            mPlayerVolumeIv.setImageResource(R.drawable.center_player_volume);
        } else if (changY <= -DensityUtil.dip2px(this, STEP_VOLUME)) {// 音量调小
            if (currentVolume > 0) {
                currentVolume--;
                if (currentVolume == 0) {// 静音，设定静音独有的图片
                    mPlayerVolumeIv.setImageResource(R.drawable.center_player_silence);
                }
            }
        }
        int percentage = (currentVolume * 100) / maxVolume;
        mVolumePercentageTv.setText(percentage + "%");
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume, 0);

    }

    /**
     * 快进
     * @param deltaX
     */
    private void forward(float deltaX) {
        playingTime = (int) mVitamioVideoView.getCurrentPosition();
        videoTotalTime = (int) mVitamioVideoView.getDuration();
        //快进了多少
        int forwardTime = (int) (deltaX / playerWidth * videoTotalTime);
        int currentTime = playingTime + forwardTime;
        if (currentTime > videoTotalTime){
            currentTime = videoTotalTime - 10;
        }
        mVitamioVideoView.seekTo(currentTime);
        mVitamioPosSeekBar.setProgress(currentTime);
        updateTextViewWithTimeFormat(mVitamioCurrentTimeTv,currentTime);
        mProgressTimeIv.setImageResource(R.drawable.center_player_forward);
        mProgressTimeTv.setText(DateTools.getTimeStr(currentTime) + "/" + DateTools.getTimeStr(videoTotalTime));
    }

    /**
     * 快退
     * @param deltaX
     */
    private void backward(float deltaX) {
        playingTime = (int) mVitamioVideoView.getCurrentPosition();
        videoTotalTime = (int) mVitamioVideoView.getDuration();
        int backwardTime = (int) (deltaX / playerWidth * videoTotalTime);
        int currentTime = playingTime - backwardTime;
        if (currentTime < 0){
            currentTime = 0;
        }
        mVitamioVideoView.seekTo(currentTime);
        mVitamioPosSeekBar.setProgress(currentTime);
        updateTextViewWithTimeFormat(mVitamioCurrentTimeTv,currentTime);
        mProgressTimeIv.setImageResource(R.drawable.center_player_backward);
        mProgressTimeTv.setText(DateTools.getTimeStr(currentTime) + "/" + DateTools.getTimeStr(videoTotalTime));
    }

    private void updateTextViewWithTimeFormat(TextView tv, int milliSecond) {
        int second = milliSecond/1000;
        int hh = second/3600;
        int mm = second%3600/60;
        int ss = second%60;

        String timeStr = null;
        if (hh != 0){
            timeStr = String.format("%02d:%02d:%02d" , hh , mm , ss);
        }else{
            timeStr = String.format("%02d:%02d" , mm , ss);
        }
        tv.setText(timeStr);
    }

    private Runnable hideRunnable = new Runnable() {

        @Override
        public void run() {
            showOrHide();
        }
    };

    private void showOrHide() {
        if (mVitamioTopLayout.getVisibility() == View.VISIBLE) {
            mVitamioTopLayout.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(this,
                    R.anim.option_leave_from_top);
            animation.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mVitamioTopLayout.setVisibility(View.GONE);
                }
            });
            mVitamioTopLayout.startAnimation(animation);

            mVitamioBottomLayout.clearAnimation();
            Animation animation1 = AnimationUtils.loadAnimation(this,
                    R.anim.option_leave_from_bottom);
            animation1.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mVitamioBottomLayout.setVisibility(View.GONE);
                }
            });
            mVitamioBottomLayout.startAnimation(animation1);
        } else {
            mVitamioTopLayout.setVisibility(View.VISIBLE);
            mVitamioTopLayout.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(this,
                    R.anim.option_entry_from_top);
            mVitamioTopLayout.startAnimation(animation);

            mVitamioBottomLayout.setVisibility(View.VISIBLE);
            mVitamioBottomLayout.clearAnimation();
            Animation animation1 = AnimationUtils.loadAnimation(this,
                    R.anim.option_entry_from_bottom);
            mVitamioBottomLayout.startAnimation(animation1);
            mHandler.removeCallbacks(hideRunnable);
            mHandler.postDelayed(hideRunnable, HIDE_TIME);
        }
    }

    private class AnimationImp implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    @Override
    protected void onDestroy() {
        if (mVitamioVideoView != null){
            mVitamioVideoView.stopPlayback();
            mVitamioVideoView = null;
        }
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if (batteryReceiver != null){
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        super.onDestroy();
    }
}
