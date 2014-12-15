package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.testin.agent.TestinAgent;
import com.yidianhulian.ydmemo.model.User;

public class YDMemoApplication extends Application {
    private User loginUser;
    private Map<String, User> mLocalContacts;
    private JSONObject mMessageCustomContent = null; // 通知或消息的自定义参数
    private JSONObject mDraftComment = null;
    private JSONObject mPostFialComment = null;
    private static List<Activity> activities = new ArrayList<Activity>();
    public String deviceToken = null;
    private MyHandler mHandler;

    public User loginUser() {
        if (loginUser != null)
            return loginUser;

        loginUser = new CacheHelper(getApplicationContext()).loginUser();
        return loginUser;
    }

    public void saveUser(User user) {
        loginUser = user;
        new CacheHelper(getApplicationContext()).saveUser(user);
    }

    public String getOption(String name) {
        return new CacheHelper(getApplicationContext()).getOption(name);
    }

    public void setOption(String name, String value) {
        new CacheHelper(getApplicationContext()).setOption(name, value);
    }


    public boolean hasPostFailComment(long memoId) {
        return mPostFialComment.has(memoId+"");
    }
    
    public void addPostFialComment(long memoId, String postToken) {
        JSONObject tokens;
        try {
            tokens = mPostFialComment.getJSONObject(memoId+"");
        } catch (Exception e1) {
            tokens = new JSONObject();
        }
        try {
            tokens.put(postToken, "");
            mPostFialComment.put(memoId+"", tokens);
            new Thread("memo_post_comment_fail"){
                public void run() {
                    setOption("memo_post_comment_fail", mPostFialComment.toString());
                }
            }.start();
        } catch (JSONException e) {
        }
    }
    public void removePostFialComment(long memoId, String postToken) {
        JSONObject tokens;
        try {
            tokens = mPostFialComment.getJSONObject(memoId+"");
        } catch (Exception e1) {
            tokens = new JSONObject();
        }
        try {
            tokens.put(postToken, null);
            if(tokens.length()==0)tokens = null;
            mPostFialComment.put(memoId+"", tokens);
            new Thread("memo_post_comment_fail"){
                public void run() {
                    setOption("memo_post_comment_fail", mPostFialComment.toString());
                }
            }.start();
        } catch (JSONException e) {
        }
    }
    public void updateDraftComment(Long memoId, String comment){
        try {
            mDraftComment.put(memoId+"", comment);
            new Thread("memo_draft_comment"){
                public void run() {
                    setOption("memo_draft_comment", mDraftComment.toString());
                }
            }.start();
        } catch (JSONException e) {
        }
    }
    public boolean hasDraftComment(Long memoId){
        return mDraftComment.has(memoId+"");
    }
    public String getDraftComment(Long memoId){
        try {
            return hasDraftComment(memoId) ? mDraftComment.getString(memoId+"") : "";
        } catch (JSONException e) {
            return "";
        }
    }

    public Map<String, User> localContacts() {
        return mLocalContacts;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent itn = new Intent(getApplicationContext(),
                MapLoactionService.class);
        
        TestinAgent.init(this);

        getApplicationContext().startService(itn);
        
        mHandler = new MyHandler(this);
        
        try {
            mDraftComment = new JSONObject(getOption("memo_draft_comment"));
        } catch (Exception e) {
            mDraftComment = new JSONObject();
        }
        
        try {
            mPostFialComment = new JSONObject(getOption("memo_post_comment_fail"));
        } catch (Exception e) {
            mPostFialComment = new JSONObject();
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {
                TestinAgent.onStop(YDMemoApplication.this);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                TestinAgent.onStart(YDMemoApplication.this);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity,
                    Bundle outState) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if ((activity instanceof Refreshable)
                        && !activities.contains(activity)) {
                    activities.add(activity);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activities.contains(activity)) {
                    activities.remove(activity);
                }
            }

            @Override
            public void onActivityCreated(Activity activity,
                    Bundle savedInstanceState) {
            }
        });
        Util.registerPush(this);
        new Thread("getContacts") {
            @Override
            public void run() {
                super.run();                
                mLocalContacts = Util.localContacts(YDMemoApplication.this);
                //处理没有权限读取联系人时给出提示信息,@HuJinhao,@2014-12-12
                if (mLocalContacts == null) {
                	mLocalContacts = new HashMap<String, User>();   
                	mHandler.sendEmptyMessage(0);
                }
            }

        }.start();

    }

    /**
     * 设置通知或消息的自定义参数
     * 
     * @param customContent
     * @author HuJinhao
     * @since 2014-10-28
     */
    public void setMessageCustomContent(String customContent) {
        try {
            mMessageCustomContent = new JSONObject(customContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取通知或消息的自定义参数
     * 
     * @param customContent
     * @author HuJinhao
     * @since 2014-10-28
     */
    public JSONObject getMessageCustomContent() {
        return mMessageCustomContent;
    }

    /**
     * 清除通知或消息的自定义参数
     * 
     * @param customContent
     * @author HuJinhao
     * @since 2014-10-28
     */
    public void clearMessageCustomContent() {
        mMessageCustomContent = null;
    }

    /**
     * 获取之前保存的Activity
     * 
     * @author HuJinhao
     * @since 2014-10-30
     */
    public static List<Activity> getActivities() {
        return activities;
    }
    
    static class MyHandler extends Handler{
        YDMemoApplication mApp;
        public MyHandler( YDMemoApplication app) {          
            mApp = app;
        }
        
    	@Override
        public void handleMessage(Message msg) {
    		if (msg.what == 0) {
    			Util.showToast(mApp, mApp.getResources().getString(R.string.cannot_read_contact));
    		}
    	}
    }
}
