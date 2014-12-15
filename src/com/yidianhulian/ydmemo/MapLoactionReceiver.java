package com.yidianhulian.ydmemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @开机启动高德地图定位服务的广播
 * @author xialinchong
 * @2014-11-25
 */
public class MapLoactionReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.yidianhulian.ydmemo.MapLoactionReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Intent itn = new Intent(context, MapLoactionService.class);
            context.startService(itn);
        }
    }

}
