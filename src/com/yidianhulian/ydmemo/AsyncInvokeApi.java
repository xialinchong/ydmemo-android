package com.yidianhulian.ydmemo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import cn.jpush.android.api.JPushInterface;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.model.User;

/**
 * 负责异步调用API
 * @author HuJinhao
 *
 */
public class AsyncInvokeApi implements CallApiListener{
	private static final int UPDATE_TOKEN = 0; //更新消息推送注册ID
	
	/**
	 * 注册极光推送
	 * @param app
	 */
	public static void registerPush(final YDMemoApplication app) {
//    	JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
		Context context = app.getApplicationContext();
        JPushInterface.init(context);  // 初始化 JPush
        String regId = JPushInterface.getRegistrationID(context);
        User loginUser = app.loginUser();
        if ( ! Util.isEmpty(regId) && loginUser != null) {
        	Map<String, String> data = new HashMap<String, String>();
        	data.put("uid", "" + app.loginUser().id());
    		data.put("token", regId);
    		data.put("type", "android");
    		data.put("action", "add");
    		
    		CallApiTask.doCallApi(UPDATE_TOKEN, new AsyncInvokeApi(), context, data);
        }
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Api getApi(Context context, int what, Object... params) {
		HashMap<String, String> query = (HashMap<String, String>)params[0];
		if (what == UPDATE_TOKEN) {
			return new Api("post", Util.URI_UPDATE_TOKEN, query);
		}
		return null;
	}

	@Override
	public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
		return false;
	}

	@Override
	public void apiNetworkException(Context context, int what,Exception e, Object... params) {
		
	}

	@Override
	public String getCacheKey(Context context, int what, Object... params) {
		return null;
	}

	@Override
	public void handleResult(Context context, int what, JSONObject result, boolean isDone,
			Object... params) {
		
	}

    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }

    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
	
}
