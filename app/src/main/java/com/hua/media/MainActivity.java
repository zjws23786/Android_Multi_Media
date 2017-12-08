package com.hua.media;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.hua.media.base.BaseActivity;
import com.hua.media.localaudio.LocalAudioFragment;
import com.hua.media.localvideo.LocalVideoFragment;
import com.hua.media.networkaudio.NetWorkAudioFragment;
import com.hua.media.networkvideo.NetWorkVideoFragment;
import com.hua.media.search.SearchActivity;
import com.hua.media.widget.ShadeView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private List<Fragment> baseFragments = new ArrayList<>();
    private List<ShadeView> tabIndicators;
    private ViewPager viewPager;
    private Button mSearchBtn;

    private FragmentPagerAdapter adapter;
    private ShadeView localVideoSv;
    private ShadeView localAudioSv;
    private ShadeView networkVideoSv;
    private ShadeView networkMusicSv;

    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void findViewById() {
        viewPager = (ViewPager) findViewById(R.id.id_viewpager);
        localVideoSv = (ShadeView) findViewById(R.id.local_video_sv);
        localAudioSv = (ShadeView) findViewById(R.id.local_audio_sv);
        networkVideoSv = (ShadeView) findViewById(R.id.network_video_sv);
        networkMusicSv = (ShadeView) findViewById(R.id.network_music_sv);
        mSearchBtn = (Button) findViewById(R.id.search_btn);
    }

    @Override
    protected void setListener() {
        viewPager.addOnPageChangeListener(this);
        mSearchBtn.setOnClickListener(this);
    }

    @Override
    protected void init() {
        baseFragments.clear();
        tabIndicators = new ArrayList<>();
        String[] titles = new String[]{"本地视频", "本地音频", "网络视频", "网络音频"};
        for (String title : titles) {
//            TabFragment tabFragment = new TabFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString("Title", title);
//            tabFragment.setArguments(bundle);
//            tabFragments.add(tabFragment);
            switch (title){
                case "本地视频":
                    baseFragments.add(new LocalVideoFragment());
                    break;
                case "本地音频":
                    baseFragments.add(new LocalAudioFragment());
                    break;
                case "网络视频":
                    baseFragments.add(new NetWorkVideoFragment());
                    break;
                case "网络音频":
                    baseFragments.add(new NetWorkAudioFragment());
                    break;
            }
        }
        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return baseFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return baseFragments.get(arg0);
            }
        };
        viewPager.setAdapter(adapter);
        initTabIndicator();
    }

    private void initTabIndicator() {

        tabIndicators.add(localVideoSv);
        tabIndicators.add(localAudioSv);
        tabIndicators.add(networkVideoSv);
        tabIndicators.add(networkMusicSv);
        localVideoSv.setOnClickListener(this);
        localAudioSv.setOnClickListener(this);
        networkVideoSv.setOnClickListener(this);
        networkMusicSv.setOnClickListener(this);
        localVideoSv.setIconAlpha(1.0f);
    }

    @Override
    public void onClick(View v) {
        resetTabsStatus();
        switch (v.getId()) {
            case R.id.local_video_sv:
                tabIndicators.get(0).setIconAlpha(1.0f);
                viewPager.setCurrentItem(0, false);
                break;
            case R.id.local_audio_sv:
                tabIndicators.get(1).setIconAlpha(1.0f);
                viewPager.setCurrentItem(1, false);
                break;
            case R.id.network_video_sv:
                tabIndicators.get(2).setIconAlpha(1.0f);
                viewPager.setCurrentItem(2, false);
                break;
            case R.id.network_music_sv:
                tabIndicators.get(3).setIconAlpha(1.0f);
                viewPager.setCurrentItem(3, false);
                break;
            case R.id.search_btn:  //搜索功能
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 重置Tab状态
     */
    private void resetTabsStatus() {
        for (int i = 0; i < tabIndicators.size(); i++) {
            tabIndicators.get(i).setIconAlpha(0);
        }
    }

    /**
     * 如果是直接点击图标来跳转页面的话，position值为0到3，positionOffset一直为0.0
     * 如果是通过滑动来跳转页面的话
     * 假如是从第一页滑动到第二页
     * 在这个过程中，positionOffset从接近0逐渐增大到接近1.0，滑动完成后又恢复到0.0，而position只有在滑动完成后才从0变为1
     * 假如是从第二页滑动到第一页
     * 在这个过程中，positionOffset从接近1.0逐渐减小到0.0，而position一直是0
     *
     * @param position             当前页面索引
     * @param positionOffset       偏移量
     * @param positionOffsetPixels 偏移量
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset > 0) {
            ShadeView leftTab = tabIndicators.get(position);
            ShadeView rightTab = tabIndicators.get(position + 1);
            leftTab.setIconAlpha(1 - positionOffset);
            rightTab.setIconAlpha(positionOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
//        if (position == 2) {
//            tabIndicators.get(position).setIconBitmap(this, R.drawable.discover_green);
//        } else {
//            tabIndicators.get(2).setIconBitmap(this, R.drawable.discover);
//        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
