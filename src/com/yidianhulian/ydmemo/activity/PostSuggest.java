package com.yidianhulian.ydmemo.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Model;

/**
 * 意见反馈
 * 
 * @author leeboo
 * 
 */
public class PostSuggest extends Activity implements CallApiListener,
        Refreshable {
    private YDMemoApplication mApp;

    private EditText mPost_msg;
    private static final int SAVE_DATA = 0;

    // fragment换成activity xialinchong 2014-12-11
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_suggest);
        restoreActionBar();

        mApp = (YDMemoApplication) getApplication();
        mPost_msg = (EditText) findViewById(R.id.post_msg);
    }

    private void loadData(int what) {
        CallApiTask.doCallApi(SAVE_DATA, PostSuggest.this, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * autor xialinchong 提交反馈意见 按钮放到optionmenu上
         */
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else if (item.getItemId() == R.id.ok) {
            if (mPost_msg.getText().toString().trim().equals("")) {
                Util.showToast(this, getString(R.string.please_say_something));
                return true;
            }
            Util.showLoading(this, getString(R.string.please_waiting));
            loadData(SAVE_DATA);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    private void restoreActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.post_suggest);

    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("suggest", mPost_msg.getText().toString());
        return new Api("post", Util.URI_POST_SUGGEST + "?uid="
                + mApp.loginUser().id(), data);
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,
            JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        Util.hideLoading();
        if (!this.isCallApiSuccess(context, what, result)) {
            if (result == null) {
                return;
            }
            Util.showToast(this, Api.getStringValue(result, "msg"));
            return;
        }
        mPost_msg.setText("");
        Util.showToast(context, context.getString(R.string.data_post_success));
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return null;
    }

    @Override
    public void apiNetworkException(Context context, int what, Exception e,
            Object... params) {
        Util.showToast(this, getString(R.string.network_error));
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {

    }

    @Override
    public boolean refresh(Model model) {
        return false;
    }
}
