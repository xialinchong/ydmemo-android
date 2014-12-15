package com.yidianhulian.ydmemo;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.yidianhulian.ydmemo.activity.RemindAlert;
import com.yidianhulian.ydmemo.model.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

/**
 *  系统提醒闹钟接收类
 *  @author HuJinhao
 *  @since 2014-10-29
 */

public class AlarmReceiver extends BroadcastReceiver implements AMapLocationListener {

	public static final String ACTION = "com.yidianhulian.ydmemo.Alarm_Alert";
	
	private Context mContext;
	private LocationManagerProxy mLocationManagerProxy;
	private String mSubject;
	private Reminder mReminder;
	private int mLocationTimes = 0; //当前已尝试定位次数
	private final int mMaxLocationTimes = 3; //最大尝试定位次数
	
	@Override
	public void onReceive(Context context, Intent intent) {		
		mContext = context;
		if (intent.getAction().equals(ACTION)){		
			mSubject 	= intent.getStringExtra("subject");
	        mReminder 	= (Reminder)intent.getParcelableExtra("reminder");
	        if ( ! Util.isEmpty(mReminder.gps()) ) { //有地点提醒的,需要判断满足才提醒
	        	init();
	        } else {
	        	showAlert();
	        }
		}
	}
	
	private void showAlert() {
//		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		Context context = mContext;
		Util.PlaySound(context);
		
		Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

		Intent i = new Intent(context, RemindAlert.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		i.putExtra("subject", mSubject);
		i.putExtra("reminder", mReminder);
		
		context.startActivity(i);
		
		//也发到通知栏
		Util.sendAlarmNotification(context, mSubject, mReminder);
		
//		Intent intentAlarmService = new Intent(context, AlarmService.class);
//		context.startService(intentAlarmService);
	}
	
	/**
     * 初始化定位
     */
    private void init() {
        // 初始化定位，只采用网络定位
        mLocationManagerProxy = LocationManagerProxy
                .getInstance(mContext);
        mLocationManagerProxy.setGpsEnable(false);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, -1, 5, this);
        
        mLocationTimes++;
    }

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		boolean isSuccess = false;
		if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
			isSuccess = true;
			mLocationTimes = 0;
			
			// 获取位置信息
            Double geoLat = amapLocation.getLatitude();
            Double geoLng = amapLocation.getLongitude();
            String lnt[] = mReminder.gps().split(",");
            LatLng start = new LatLng(Double.valueOf(lnt[0]),
                    Double.valueOf(lnt[1]));
            LatLng end = new LatLng(geoLat, geoLng);
            float length = AMapUtils.calculateLineDistance(start, end);
            if (length <= 100) {
            	showAlert();
            }
		}
		
		// 销毁定位
		mLocationManagerProxy.destroy();
		
		if ( ! isSuccess && mLocationTimes < mMaxLocationTimes) {
			init();
		}
	}
		    
	
}
