package com.yidianhulian.ydmemo.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import cn.jpush.android.api.JPushInterface;

import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;

public class Splash extends Activity {
    private YDMemoApplication mApp;
    private MyHandler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mApp = (YDMemoApplication)getApplication();
        mHandler = new MyHandler(this, mApp);

        new Thread("Splash Waiting"){

            @Override
            public void run() {
                while (true) {
                    if(mApp.localContacts() != null){
                        mHandler.sendEmptyMessage(0);
                        return;
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        }.start();
    }

    @Override
    protected void onStart() {
    	super.onStart();
    	Intent intent = getIntent();
    	if (intent.hasExtra(Util.PUSH_PARAMS_KEY)) { //推送的自定义参数
    		YDMemoApplication app = (YDMemoApplication)getApplication();
			app.setMessageCustomContent(intent.getStringExtra(Util.PUSH_PARAMS_KEY));
    	}
    }
    
    @Override
	public void onStop() {
		super.onStop();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	JPushInterface.onResume(getApplicationContext());
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	JPushInterface.onPause(getApplicationContext());
    }
    
    static class MyHandler extends Handler{
            WeakReference<Splash> mActivity;
            WeakReference<YDMemoApplication> mApp;
            public MyHandler(Splash activity, YDMemoApplication app) {
                mActivity = new WeakReference<Splash>(activity);
                mApp = new WeakReference<YDMemoApplication>(app);
            }
            @Override
            public void handleMessage(Message msg) {
                Splash my = mActivity.get();
                YDMemoApplication app = mApp.get();
                Intent intent = new Intent();
                //第一次打开app显示欢迎向导,
                //不是第一次打开，如果没有登录则登录，
                //登录了进入主界面
                
                if(Util.getAppVersionName(app).equals(app.getOption("hasLaunched"))){// has launched
                    if(app.loginUser() != null){// has login
                        intent.setClass(my, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        my.startActivity(intent);
                    }else{//not login
                        intent.setClass(my, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        my.startActivity(intent);
                    }
                }else{
                    intent.setClass(my, Welcome.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    my.startActivity(intent);
                }
            }
    }
}
