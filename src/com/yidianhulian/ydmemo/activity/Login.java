package com.yidianhulian.ydmemo.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;

/**
 * 登录
 * 
 * @author leeboo
 * 
 */
public class Login extends Activity implements CallApiListener {

    protected static final int API_LOGIN = 0;
    private Button mLoginBtn;
    private Button mSignupBtn;
    private EditText mCellphone;
    private EditText mPsw;
    private YDMemoApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mCellphone = (EditText) findViewById(R.id.cellphone);
        mPsw = (EditText) findViewById(R.id.psw);
        mApp = (YDMemoApplication) getApplication();

        /**
         * autor xialinchong 软键盘完成事件
         */
        mPsw.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    com.yidianhulian.framework.Util.hideKeyboard(Login.this);
                    login();
                    return true;
                }
                return false;
            }

        });

        mLoginBtn = (Button) findViewById(R.id.loginbtn);
        mLoginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        mSignupBtn = (Button) findViewById(R.id.signupbtn);
        mSignupBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Signup.class);
                Login.this.startActivity(intent);
            }
        });
    }

    private void login() {
        if (mCellphone.getText().toString().isEmpty()) {
            Util.showToast(Login.this, getString(R.string.enter_username));
            return;
        }
        if (mPsw.getText().toString().isEmpty()) {
            Util.showToast(Login.this, getString(R.string.enter_password));
            return;
        }
        Util.showLoadingAtButton(mLoginBtn, Login.this, "登录中");
        CallApiTask.doCallApi(API_LOGIN, Login.this, Login.this);
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("cellphone", mCellphone.getText().toString());
        data.put("pwd", mPsw.getText().toString());
        return new Api("post", Util.URI_LOGIN, data);
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
        Util.hideLoadingAtButton(mLoginBtn, this);
        if (!Util.checkResult(this, result, getString(R.string.login_error)))
            return;
        mApp.saveUser(new User(mApp, Api.getJSONValue(result, "data",
                JSONObject.class)));
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Util.registerPush(mApp);
    }

    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(this, getString(R.string.network_error));
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }

    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }

}
