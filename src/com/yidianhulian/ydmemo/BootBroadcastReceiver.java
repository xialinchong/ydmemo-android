package com.yidianhulian.ydmemo;


import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yidianhulian.framework.db.KVHandler;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;

/**
 * 开机监听类,主要负责启动之前的报警提醒业务
 * @author HuJinhao
 * @since 2014-11-02
 */


public class BootBroadcastReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            User user = new CacheHelper(context).loginUser();
            if (user == null)
                return ;
            
            JSONObject reminds = Util.getLocalReminds(context);
            if (reminds != null) {
            	Iterator<?> keys = reminds.keys();
                while (keys.hasNext()) {  
                	String k = keys.next().toString();  
                    try {
                    	JSONObject v = reminds.getJSONObject(k);
                    	Util.createAlarmReminder(context, v.get("subject").toString(), new Reminder(v.getJSONObject("reminder")));                    	
					} catch (JSONException e) {
						e.printStackTrace();
					}  
                }
            }
            
        }
    }



}
