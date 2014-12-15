package com.yidianhulian.ydmemo.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;

/**
 * 注册
 * 
 * @author leeboo
 * 
 */
public class Signup extends Activity implements CallApiListener {

    private static final int API_GET_VERIFY_CODE = 1;
    private static final int API_SIGN_UP = 2;

    private String mCode;
    private EditText mPhone;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mVerifyCode;

    private LinearLayout mVerifyLayout;
    private Button mDoVerifyBtn;
    private Button mSignup;
    private Button mGetCodeBtn;
    private Button mSignin;
    private YDMemoApplication mApp;
    private int mSeconds = Util.VERIFY_CODE_TIME_LIMIT;
    private Timer mTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        mApp = (YDMemoApplication) getApplication();

        mPhone = (EditText) findViewById(R.id.phone);
        /**
         * author xialinchong 限制注册手机号输入只能为0-9数字，其他一概不能输入
         */
        mPhone.setKeyListener(new NumberKeyListener() {

            @Override
            public boolean onKeyUp(View view, Editable content, int keyCode,
                    KeyEvent event) {
                if (mPhone.getText().length() < 11
                        && mVerifyLayout.getVisibility() != View.GONE) {
                    mVerifyLayout.setVisibility(View.GONE);
                    mVerifyCode.setText("");

                    mUsername.setVisibility(View.GONE);
                    mUsername.setText("");

                    mPassword.setVisibility(View.GONE);
                    mPassword.setText("");

                    mSignup.setVisibility(View.GONE);
                    mGetCodeBtn.setText(R.string.next);
                }
                return super.onKeyUp(view, content, keyCode, event);
            }

            @Override
            public int getInputType() {
                return android.text.InputType.TYPE_CLASS_PHONE;
            }

            @Override
            protected char[] getAcceptedChars() {
                return new char[] { '1', '2', '3', '4', '5', '6', '7', '8',
                        '9', '0' };
            }
        });

        mVerifyCode = (EditText) findViewById(R.id.verify_code);
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        /**
         * autor xialinchong 软键盘完成事件
         */
        mPassword
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                            KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE
                                || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                            com.yidianhulian.framework.Util.hideKeyboard(Signup.this);
                            signup();
                            return true;
                        }
                        return false;
                    }

                });

        mDoVerifyBtn = (Button) findViewById(R.id.do_verify);
        mGetCodeBtn = (Button) findViewById(R.id.get_code);
        mSignup = (Button) findViewById(R.id.signup);
        mSignin = (Button) findViewById(R.id.go_login);

        mGetCodeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone_number = mPhone.getText().toString();
                if (phone_number.length() < 11) {
                    Util.showToast(Signup.this, "手机号码只能是11位！");
                    return;
                }
                if (!phone_number.isEmpty() || !"".equals(phone_number)) {
                    Util.showLoadingAtButton(mGetCodeBtn, Signup.this, "");                   
                    InvokeApi(API_GET_VERIFY_CODE);                   
                } else {
                    Util.showToast(Signup.this, "电话号码不能为空！");
                }
            }
        });

        mSignup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        mSignin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDoVerifyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVerifyCode.getText().toString().equalsIgnoreCase(mCode)) {
                    mUsername.setVisibility(View.VISIBLE);
                    mUsername.setFocusable(true);
                    mUsername.requestFocus();
                    mPassword.setVisibility(View.VISIBLE);
                    mSignup.setVisibility(View.VISIBLE);
                } else {
                    Util.showToast(Signup.this,
                            getString(R.string.verify_code_error));
                }
            }
        });

        mVerifyLayout = (LinearLayout) findViewById(R.id.verify_layout);
    }

    private void signup() {
        // TODO 验证数据
        String phone_number = mPhone.getText().toString();
        String userName = mUsername.getText().toString();
        String passWord = mPassword.getText().toString();
        if (phone_number.length() < 11) {
            Util.showToast(Signup.this, "手机号码只能是11位!");
            return;
        }
        if (phone_number.isEmpty() || "".equals(phone_number)) {
            Util.showToast(Signup.this, "手机号码不能为空！");
            return;
        }
        if (userName.isEmpty() || "".equals(userName)) {
            Util.showToast(Signup.this, "用户名不能为空！");
            return;
        }
        if (passWord.isEmpty() || "".equals(passWord)) {
            Util.showToast(Signup.this, getString(R.string.password_is_empty));
            return;
        }
        Util.showLoadingAtButton(mSignup, Signup.this, getString(R.string.please_waiting));
        InvokeApi(API_SIGN_UP);
    }

    private void InvokeApi(int what) {
        CallApiTask.doCallApi(what, this, this);
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> args = new HashMap<String, String>();
        switch (what) {
        case API_GET_VERIFY_CODE:
            args.put("cellphone", mPhone.getText().toString());
            return new Api("get", Util.URI_VERIFY_CELLPHONE, args);
        case API_SIGN_UP:
            // TODO
            args.put("cellphone", mPhone.getText().toString());
            args.put("name", mUsername.getText().toString());
            args.put("pwd", mPassword.getText().toString());
            return new Api("post", Util.URI_SIGN_UP, args);
        default:
            return null;
        }
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isCache,
            Object... params) {
        if (!this.isCallApiSuccess(context, what, result)) {
            if (result == null) {
                return;
            }
            Util.showToast(this, Api.getStringValue(result, "msg"));
            return;
        }

        if (what == API_GET_VERIFY_CODE) {
            Util.hideLoadingAtButton(mGetCodeBtn, Signup.this);
            //加上获取验证码限制,不能一直可点,@HuJinhao
            mGetCodeBtn.setEnabled(false);
            mSeconds = Util.VERIFY_CODE_TIME_LIMIT;
            handleTime();  
            
            mCode = Api.getStringValue(result, "data");
            mVerifyLayout.setVisibility(View.VISIBLE);
            mVerifyCode.setFocusable(true);
            mVerifyCode.requestFocus();
            // 显示后面的控件
            return;
        }

        Util.hideLoadingAtButton(mSignup, Signup.this);
        mApp.saveUser(new User(mApp, Api.getJSONValue(result, "data",
                JSONObject.class)));
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        Util.registerPush(mApp);
        startActivity(intent);
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
        return from;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
    private void handleTime() {
    	final Timer timer = new Timer();    	
    	final Handler handler = new Handler() {
    		public void handleMessage(Message message) {
    			if (mSeconds == 0) {    				
    				mGetCodeBtn.setEnabled(true);
    				mGetCodeBtn.setText(getResources().getString(R.string.get_verify_code)); 	    				
    			} else {
    				mGetCodeBtn.setText(String.valueOf(mSeconds) + "秒");
    			} 
    			mSeconds--;
    			super.handleMessage(message);
    		}
    	};
    	TimerTask task = new TimerTask(){
			@Override
			public void run() {		
				Message message = new Message();      
				message.what = 1;				
				handler.sendMessage(message);	
				if (mSeconds == 0) {
					timer.cancel();					
				}
			}                   	
        };
        timer.schedule(task, 0, 1000);
    }

}
