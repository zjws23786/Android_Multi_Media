package com.hua.media.localaudio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hua.librarytools.utils.DensityUtil;
import com.hua.media.IMusicPlayerService;
import com.hua.media.R;
import com.hua.media.bean.AudioBean;
import com.hua.media.base.BaseActivity;
import com.hua.media.bean.KuGouRawLyric;
import com.hua.media.bean.KuGouSearchLyricResult;
import com.hua.media.bean.MetaChangedEvent;
import com.hua.media.network.AudioApi;
import com.hua.media.network.RxBus;
import com.hua.media.service.MusicPlayerService;
import com.hua.media.utils.ColorUtil;
import com.hua.media.utils.LyricUtil;
import com.hua.media.widget.LyricView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.Formatter;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 音乐播放器
 */
public class AudioPlayerActivity extends BaseActivity implements View.OnClickListener {
    private TextView mDisplayNameTv;
    private TextView mArtistNameTv;
    private SeekBar mSeekBar;
    private ImageView mLastOneIv;
    private ImageView mPlayPauseIv;
    private ImageView mNextOneIv;
    private TextView mCurrentSeekTv;
    private TextView mTotalSeekTv;
    private LyricView mLyricView;

    private int position;
    private boolean isFirstIn = true;
    /**
     * true:从状态栏进入的，不需要重新播放
     * false:从播放列表进入的
     */
    private boolean notification;

    /**
     * 进度更新
     */
    private static final int PROGRESS = 1;
    /**
     * 显示歌词
     */
    private static final int SHOW_LYRIC = 2;

    /**
     * 服务的代理类，通过它可以调用服务的方法
     */
    private IMusicPlayerService service;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_LYRIC://显示歌词

                    //1.得到当前的进度
                    try {
                        int currentPosition = service.getCurrentPostion();

                        //2.把进度传入ShowLyricView控件，并且计算该高亮哪一句
                        mLyricView.setCurrentTimeMillis(currentPosition);
                        //3.实时的发消息
                        handler.removeMessages(SHOW_LYRIC);
                        handler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case PROGRESS:
                    try {
                        //1.得到当前进度
                        int currentPosition = service.getCurrentPostion();

                        //2.设置SeekBar.setProgress(进度)
                        mSeekBar.setProgress(currentPosition);

                        //3.时间进度跟新
                        mCurrentSeekTv.setText(stringForTime(currentPosition));
                        mTotalSeekTv.setText(stringForTime(service.getDuration()));

                        //4.每秒更新一次
                        handler.removeMessages(PROGRESS);
                        handler.sendEmptyMessageDelayed(PROGRESS,1000);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {

        /**
         * 连接成功回调该方法
         * @param componentName
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            try {
                if (service != null){
                    //从列表页面进来
                    if (!notification){
                        service.openAudio(position);
                    }
                    //从状态栏点击进来
                    else{
                        fillData();
                    }

                    //服务绑定成功了  就需加载歌词
//                    onPreparedLyric();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        /**
         * 当断开连接时回调该方法
         * @param componentName
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                if (service != null){
                    service.stop();
                    service = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * 准备工作
     */
    private void onPreparedLyric() {
        loadLyric();
//        Subscription subscription = RxBus.getInstance()
//                .toObservable(MetaChangedEvent.class)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<MetaChangedEvent>() {
//                    @Override
//                    public void call(MetaChangedEvent event) {
////                        mPresenter.updateNowPlayingCard();
//                        loadLyric();
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//
//                    }
//                });
//        RxBus.getInstance().addSubscription(this, subscription);
    }

    /**
     * 加载歌词
     */
    private void loadLyric() {
        try {
            String title = service.getTitle();
            String artist = service.getArtist();
            long duration = service.getDuration();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(artist)) {
                return;
            }
            Subscriber<KuGouSearchLyricResult> subscriber = new Subscriber<KuGouSearchLyricResult>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(KuGouSearchLyricResult kuGouSearchLyricResult) {
                    if (kuGouSearchLyricResult.status == 200
                            && kuGouSearchLyricResult.candidates != null
                            && kuGouSearchLyricResult.candidates.size() != 0) {
                        KuGouSearchLyricResult.Candidates candidates = kuGouSearchLyricResult.candidates.get(0);
//                        return mKuGouApiService.getRawLyric(candidates.id, candidates.accesskey);
                        getRawLyric(candidates.id, candidates.accesskey);
                    }
                }
            };
            AudioApi.searchLyric(subscriber,title, artist, duration);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void getRawLyric(String id, String accesskey) {
        Subscriber<KuGouRawLyric> subscriber = new Subscriber<KuGouRawLyric>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(KuGouRawLyric kuGouRawLyric) {
                try{
                    if (kuGouRawLyric != null) {
                        String rawLyric = LyricUtil.decryptBASE64(kuGouRawLyric.content);
                        File file = LyricUtil.writeLrcToLoc(service.getTitle(), service.getArtist(), rawLyric);
                        mLyricView.setHintColor(Color.BLACK);
                        if (file == null) {
                            mLyricView.reset("暂无歌词");
                        } else {
                            mLyricView.setLyricFile(file, "UTF-8");
                            handler.sendEmptyMessage(SHOW_LYRIC);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


            }
        };
        AudioApi.getRawLyric(subscriber,id,accesskey);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_audio_player);
    }

    @Subscribe
    public void onEventMainThread(AudioBean audioBean){
        fillData();
    }

    @Override
    protected void findViewById() {
        mDisplayNameTv = (TextView) findViewById(R.id.display_name_tv);
        mArtistNameTv = (TextView) findViewById(R.id.artist_name_tv);
        mSeekBar = (SeekBar) findViewById(R.id.audio_seek_bar);
        mLastOneIv = (ImageView) findViewById(R.id.last_one_iv);
        mPlayPauseIv = (ImageView) findViewById(R.id.play_pause_iv);
        mNextOneIv = (ImageView) findViewById(R.id.next_one_iv);
        mCurrentSeekTv = (TextView) findViewById(R.id.current_seek_tv);
        mTotalSeekTv = (TextView) findViewById(R.id.total_seek_tv);
        mLyricView = (LyricView) findViewById(R.id.lyric_view);
        setLyricParam();
    }

    private void setLyricParam() {
        mLyricView.setLineSpace(15.0f);
        mLyricView.setTextSize(17.0f);
        mLyricView.setPlayable(true);
        //高亮字体颜色
        mLyricView.setHighLightTextColor(Color.parseColor("#DD5044"));
        //滑动到相关行字体颜色
        mLyricView.setCurrentShowColor(Color.parseColor("#80B547"));
        //默认字体颜色
        mLyricView.setDefaultColor(Color.parseColor("#4B4D48"));
        mLyricView.setTouchable(true);
        mLyricView.setOnPlayerClickListener(new LyricView.OnPlayerClickListener() {
            @Override
            public void onPlayerClicked(long progress, String content) {
                try {
                    service.seekTo((int) progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void setListener() {
        mLastOneIv.setOnClickListener(this);
        mPlayPauseIv.setOnClickListener(this);
        mNextOneIv.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    //拖动进度
                    try {
                        service.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void init() {
        notification = getIntent().getBooleanExtra("notification", false);
        if(!notification){
            position = getIntent().getIntExtra("position",0);
        }
        bindAndStartService();
    }

    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);//不至于实例化多个服务
    }

    //填充
    private void fillData() {
        try {
            if (isFirstIn){
                if (service.getDuration() > 1){
                    isFirstIn = false;
                    //服务绑定成功了  就需加载歌词
                    onPreparedLyric();
                }
            }
            mDisplayNameTv.setText(service.getName());
            mArtistNameTv.setText(service.getArtist());
            mSeekBar.setMax(service.getDuration());
            handler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (service != null){
            try{
                switch (view.getId()){
                    case R.id.last_one_iv:
                        service.pre();
                        break;
                    case R.id.play_pause_iv:
                        if(service.isPlaying()){
                            //暂停
                            service.pause();
                            //播放
                            mPlayPauseIv.setBackgroundResource(R.drawable.audio_start_selector_btn);
                        }else{
                            //播放
                            service.start();
                            //暂停
                            mPlayPauseIv.setBackgroundResource(R.drawable.audio_pause_selector_btn);
                        }
                        break;
                    case R.id.next_one_iv:
                        service.next();
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;

        int minutes = (totalSeconds / 60) % 60;

        int hours = totalSeconds / 3600;

        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        //2.EventBus取消注册
        EventBus.getDefault().unregister(this);
        //解绑服务
        if(serviceConnection != null){
            unbindService(serviceConnection);
            serviceConnection = null;
        }
        super.onDestroy();
    }
}
