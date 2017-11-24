package com.hua.media.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by hjz on 2017/11/20 0020.
 */

public abstract class BaseFragment extends Fragment {
    protected View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(mView == null){
            initView(inflater,container);
        }else{
            ViewGroup p = (ViewGroup) mView.getParent();
            if (p != null){
                p.removeAllViewsInLayout();
            }
        }
        return mView;
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        setLayout(inflater, container);
        findViewById();
        setListener();
        init();
    }

    protected abstract void setLayout(LayoutInflater inflater, ViewGroup container);

    protected abstract void findViewById();

    protected abstract void setListener();

    protected abstract void init();
}
