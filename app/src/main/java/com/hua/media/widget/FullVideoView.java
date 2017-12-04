package com.hua.media.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.hua.media.common.Env;

/**
 * @author hjz
 * @date 2017/12/1 0001
 */

public class FullVideoView extends VideoView {
    protected int defaultWidth = 1920;
    protected int defaultHeight = 1080;

    public FullVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        defaultWidth = Env.screenWidth;
        defaultHeight = Env.screenHeight;

        //如果MeasureSpec没有施加约束，则使用提供的尺寸
        int width = getDefaultSize(defaultWidth,widthMeasureSpec);
        int height = getDefaultSize(defaultHeight,heightMeasureSpec);
        setMeasuredDimension(width,height);
    }
}
