package com.yidianhulian.ydmemo.activity;

/**
 * 编辑昵称
 * author xialinchong
 * 2014-11-18
 */

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;

public class EditNickName extends Activity implements CallApiListener {

    // private ViewGroup mLayout;
    private EditText mEditName;
    private String mTextValue;
    private YDMemoApplication mApp;

    // fragment换成activity xialinchong 2014-12-11
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (YDMemoApplication) getApplication();
        setContentView(R.layout.edit_nickname);
        mEditName = (EditText) findViewById(R.id.edit_name);
        mEditName.setText(mApp.loginUser().name());
        // 光标位置放在文本最后
        mEditName.setSelection(mApp.loginUser().name().length());
        restoreActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else if (item.getItemId() == R.id.ok) {
            // 检查是否为空，保存数据
            mTextValue = mEditName.getText().toString();
            if (mTextValue.equals("")) {
                Util.showToast(this, getString(R.string.nickname_is_empty));
                return true;
            }
            Util.showLoading(this, getString(R.string.please_waiting));
            saveData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        CallApiTask.doCallApi(0, EditNickName.this, this);
    }

    private void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.edit_nickname);
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", mTextValue);
        return new Api("post", Util.URI_SAVE_PROFILE + "?uid="
                + mApp.loginUser().id(), data);
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,
            JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public void apiNetworkException(Context context, int what, Exception e,
            Object... params) {
        Util.showToast(this, getString(R.string.network_error));
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
                Util.showToast(this, "网络错误！");
                return;
            }
            Util.showToast(this, Api.getStringValue(result, "msg"));
            return;
        }
        JSONObject data = Api.getJSONValue(result, "data", JSONObject.class);
        User user = new User(mApp, data);
        Util.updateCacheAndUI(context, user, user.id());

        Util.showToast(this, "昵称修改成功！");
        getFragmentManager().popBackStack();
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return from;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {

    }
}
