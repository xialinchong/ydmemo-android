package com.yidianhulian.ydmemo.activity;

/**
 * 修改密码
 * author xialinchong
 * 2014-11-18
 */

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
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;

public class EditPassWord extends Activity implements CallApiListener {

    // private ViewGroup mLayout;
    private EditText mOldPwd;
    private EditText mNewPwd;
    private String mOldTextValue;
    private String mNewTextValue;
    private YDMemoApplication mApp;

    // fragment换成activity xialinchong 2014-12-11
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_pwd);
        mApp = (YDMemoApplication) getApplication();

        mOldPwd = (EditText) findViewById(R.id.old_pwd);
        mNewPwd = (EditText) findViewById(R.id.new_pwd);

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
            // getFragmentManager().popBackStack();//退栈
            //
            // //退回个人设置页面时,恢复启用侧边栏图标以及关闭它,@HuJinhao,@2014-11-26
            // MainActivity mainActivity = (MainActivity) this;
            // mainActivity.enabledDrawerToggleIndicatorIcon(true);
            // mainActivity.closeDrawers();
            this.finish();
            return true;
        } else if (item.getItemId() == R.id.ok) {
            // 检查是否为空，保存数据
            mOldTextValue = mOldPwd.getText().toString();
            mNewTextValue = mNewPwd.getText().toString();
            if (mOldTextValue.equals("")) {
                Util.showToast(this, getString(R.string.password_is_empty));
                return true;
            }
            if (mNewTextValue.equals("")) {
                Util.showToast(this, getString(R.string.password_is_empty));
                return true;
            }
            Util.showLoading(this, getString(R.string.please_waiting));
            saveData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        CallApiTask.doCallApi(0, EditPassWord.this, this);
    }

    private void restoreActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.edit_pwd);
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("op", mOldTextValue);
        data.put("np", mNewTextValue);
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
        Util.updateCacheAndUI(this, user, user.id());
        Util.showToast(this, getString(R.string.change_psw_success));
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
