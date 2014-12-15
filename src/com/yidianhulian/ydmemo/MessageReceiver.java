package com.yidianhulian.ydmemo;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;


public class MessageReceiver extends BroadcastReceiver implements CallApiListener {
	private static final String TAG = "JPush";
	
	private static final int API_GET_MEMO = 0;
	private static final int API_GET_COMMENT = 1;
	private static final int API_GET_REMIND = 2;
	private static final int API_REFRESH_MISC = 3;
	
	private Context mContext;
	private int mType; //消息类型
	private String mTitle; //消息主题
	private String mContent; //消息内容
	private String mCustomParams; //接收传递的自定义参数

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		
		Bundle bundle = intent.getExtras();		
		if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MessageReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	
        	User loginUser = new CacheHelper(context).loginUser();
    		long login_id = 0;
            if (loginUser != null) {
            	login_id = loginUser.id();
            }
        	mTitle = bundle.getString(JPushInterface.EXTRA_TITLE);
        	mContent = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        	mCustomParams = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if ( ! Util.isEmpty(mCustomParams) ) {
				try {
					JSONObject obj = new JSONObject(mCustomParams);
					int mid 			= Api.getIntegerValue(obj, "mid");
					mType 				= Api.getIntegerValue(obj, "type");				

		            if (login_id != 0) {		            	
		            	Map<String, String> query = new HashMap<String, String>();
	            		query.put("uid", String.valueOf(login_id));
		            		
	            		switch (mType) {		            		
		            		case Util.M_NEW_MEMO :
		            		case Util.M_MEMO_INVITE :
		            			query.put("memo_id", String.valueOf(mid));					
								CallApiTask.doCallApi(API_GET_MEMO, MessageReceiver.this, context, query);
								break;
		            		case Util.M_NEW_COMMENT :
		            			query.put("id", Api.getStringValue(obj, "sid"));
		            			query.put("type", "comment");
		            			CallApiTask.doCallApi(API_GET_COMMENT, MessageReceiver.this, context, query);
		            			break;
		            		case Util.M_NEW_REMIND :
		            			JSONObject sids = new JSONObject(Api.getStringValue(obj, "sid"));
		            			if (sids.has(String.valueOf(login_id))) {			            			
			            			query.put("id", sids.getString(String.valueOf(login_id)));
			            			query.put("type", "remind");
			            			CallApiTask.doCallApi(API_GET_REMIND, MessageReceiver.this, context, query);
		            			}
		            			break;
		            		case Util.M_NEW_DYNAMIC : //刷新动态
		            			CallApiTask.doCallApi(API_REFRESH_MISC, MessageReceiver.this, context, query);
		            			break;
		            		default:
		            			break;
		            	}	            			            	
		            }	
				} catch (JSONException e) {

				}

			}
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.e(TAG, "[MessageReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[MessageReceiver] Unhandled intent - " + intent.getAction());
        }
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public Api getApi(Context context, int what, Object... params) {		
		HashMap<String, String> query = (HashMap<String, String>)params[0];
		if (what == API_GET_MEMO) {
			return new Api("get", Util.URI_LOAD_MEMO, query);
		} else if (what == API_REFRESH_MISC) {
			return new Api("get", Util.URI_MISC, query);
		} else {
			return new Api("get", Util.URI_GET_BY_ID, query);
		}	
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

	@SuppressWarnings("unchecked")
    @Override
	public void handleResult(Context context, int what, JSONObject result, boolean isDone,
			Object... params) {
		if ( ! Util.checkResult(result) ) return;
		
		Map<String, String> args = (Map<String, String>)params[0];
		
		JSONObject json     = Api.getJSONValue(result, "data", JSONObject.class);
		Model model = null;
		if (what == API_GET_MEMO) {
		    model = new Memo(json); 
        } else if (what == API_GET_COMMENT) {
            model = new Comment(json);
        } else if (what == API_GET_REMIND) {
            model = new Reminder(json);
        } else if (what == API_REFRESH_MISC) {
            model = new Option(json);
        }
		
		if(model == null)return;
		Long uid = Long.valueOf(args.get("uid").toString());
		boolean res = Util.updateCacheAndUI(context, model, uid);
		CacheHelper cacheHelper = new CacheHelper(mContext);
		Option option = cacheHelper.getSetting(uid);
		
		//处理来新留言时是否通知自己,@HuJinhao,@2014-12-12
		if(model instanceof Comment){
		    Memo memo = cacheHelper.getMemo(((Comment) model).memo_id(), uid);
		    if(memo!=null && option!=null && memo.isAssigner(uid) && ! option.noticeMyMemoHasComment()) return;
		    if(memo!=null && option!=null && memo.isFollower(uid) && ! option.noticeFollowHasComment()) return;
		}
		
		//非刷新动态才处理通知或播放声音
		if (what != API_REFRESH_MISC) {
			if ( ! res ) {
				Util.pushLocalNotification(mContext, mTitle, mContent, mCustomParams, mType);
			}else{
			    Util.PlaySound(mContext);//leeboo 新数据播放声音 TODO 不同数据不同声音
			}
		}
		//如果是提醒消息,需要建立定时提醒业务
		if (what == API_GET_REMIND) {
			Util.createAlarmReminder(mContext, mTitle, (Reminder)model);
		}
	}


    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }

    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }

}
