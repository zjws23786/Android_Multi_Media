package com.hua.media.search;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hua.librarytools.utils.PreferencesUtils;
import com.hua.librarytools.utils.UIToast;
import com.hua.librarytools.widget.refreshlist.PullToRefreshListView;
import com.hua.media.R;
import com.hua.media.base.BaseActivity;
import com.hua.media.bean.SearchModel;
import com.hua.media.common.Constant;
import com.hua.media.network.InformationApi;
import com.hua.media.search.adapter.SearchAdapter;
import com.hua.media.utils.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import rx.Subscriber;

/**
 * @author hjz
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {
    private EditText mInputSearchEt;
    private ImageView mVoiceIv;
    private TextView mSearchTv;
    private PullToRefreshListView mSearchLv;
    private ProgressBar mSearchPb;
    private TextView mNoDataTv;
    private String searchStr = "";
    private int p = 1;
    private int n = 20;
    private int allTotal = 0;
    private SearchAdapter adapter;

    private List<SearchModel.ItemsBean> lists = new ArrayList<>();

    // 语音听写UI
    private RecognizerDialog recognizerDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();


    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_search);
    }

    @Override
    protected void findViewById() {
        mInputSearchEt = (EditText) findViewById(R.id.input_search_et);
        mVoiceIv = (ImageView) findViewById(R.id.voice_iv);
        mSearchTv = (TextView) findViewById(R.id.search_tv);
        mSearchLv = (PullToRefreshListView) findViewById(R.id.search_lv);
        mSearchPb = (ProgressBar) findViewById(R.id.search_pb);
        mNoDataTv = (TextView) findViewById(R.id.no_data_tv);
    }

    @Override
    protected void setListener() {
        mVoiceIv.setOnClickListener(this);
        mSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p  = 1;
                searchStr = mInputSearchEt.getText().toString().trim();
                loadData();
            }
        });

        mSearchLv.setPullAndRefreshListViewListener(new PullToRefreshListView.PullAndRefreshListViewListener() {
            @Override
            public void onRefresh() {  //下拉刷新
                p = 1;
                loadData();
            }

            @Override
            public void onLoadMore() {  //加载更多
                loadData();
            }
        });
    }

    @Override
    protected void init() {
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        recognizerDialog = new RecognizerDialog(this, mInitListener);
        //2.设置accent、 language等参数
        recognizerDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");//中文
        recognizerDialog.setParameter(SpeechConstant.ACCENT, "mandarin");//普通话

        adapter = new SearchAdapter(this);
        mSearchLv.setAdapter(adapter);
        //加载更多
        mSearchLv.setPullLoadEnable(true);
        //保存当前刷新的时间在Preferences内存中
        mSearchLv.setTimeTag("PullToRefreshSearch");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.voice_iv:
//                // 清空显示内容
                mInputSearchEt.setText(null);
                mIatResults.clear();
                recognizerDialog.setListener(mRecognizerDialogListener);
                recognizerDialog.show();
                break;
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);

        }

        /**
         * 识别回调错误.
         */
        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mInputSearchEt.setText(resultBuffer.toString());
        mInputSearchEt.setSelection(mInputSearchEt.length());

        p  = 1;
        loadData();
    }

    private void loadData() {
        Subscriber<SearchModel> subscriber = new Subscriber<SearchModel>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(SearchModel searchModel) {
                if (p == 1){
                    lists.clear();
                }

                allTotal = searchModel.getTotal();
                lists.addAll(searchModel.getItems());
                mSearchLv.setVisibility(View.VISIBLE);
                adapter.setData(lists);
                adapter.notifyDataSetChanged();
                mSearchLv.stopRefresh(true);
                mSearchLv.stopLoadMore();
                if (lists.size() >= allTotal){
                    mSearchLv.noMoreData("疼，到底了");
                }else{
                    p++;
                }
            }
        };
        String url = "http://hot.news.cntv.cn/";
        InformationApi.getSearchList(subscriber,url,p,n,searchStr);
    }


    private void showTip(final String str) {
        UIToast.showBaseToast(this,str,R.style.AnimationToast);
    }

}
