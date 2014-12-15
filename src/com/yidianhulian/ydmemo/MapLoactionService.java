package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.yidianhulian.ydmemo.activity.RemindAlert;
import com.yidianhulian.ydmemo.model.Reminder;

/**
 * @开机启动高德地图定位服务
 * @author xialinchong
 * @2014-11-25
 */

public class MapLoactionService extends Service implements AMapLocationListener {

    private LocationManagerProxy mLocationManagerProxy;
    private JSONObject reminds;
    private ArrayList<String> hasRemindId = new ArrayList<String>();

    /**
     * 初始化定位
     */
    private void init() {
        // 初始化定位，只采用网络定位
        mLocationManagerProxy = LocationManagerProxy
                .getInstance(getBaseContext());
        mLocationManagerProxy.setGpsEnable(false);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用removeUpdates()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用destroy()方法
        // 其中如果间隔时间为-1，则定位只定一次,
        // 在单次定位情况下，定位无论成功与否，都无需调用removeUpdates()方法移除请求，定位sdk内部会移除
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60 * 1000, 15, this);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        reminds = Util.getLocalReminds(getApplicationContext());
        if (reminds == null) {
            return;
        }
        if (amapLocation != null
                && amapLocation.getAMapException().getErrorCode() == 0) {
            // 获取位置信息
            Double geoLat = amapLocation.getLatitude();
            Double geoLng = amapLocation.getLongitude();
            Iterator<?> keys = reminds.keys();
            while (keys.hasNext()) {
                String k = keys.next().toString();
                if (hasRemindId.contains(k)) {
                    return;
                }
                try {
                    JSONObject v = reminds.getJSONObject(k);
                    JSONObject currReminder = v.getJSONObject("reminder");
                    String gps = currReminder.getString("gps");
                    double length = 0;
                    if (!gps.equals("")) {
                        String lnt[] = gps.split(",");
                        LatLng start = new LatLng(Double.valueOf(lnt[0]),
                                Double.valueOf(lnt[1]));
                        LatLng end = new LatLng(geoLat, geoLng);
                        length = AMapUtils.calculateLineDistance(start, end);
                    }
                    if (length <= 100) {
                        //打开闹钟提醒
                        hasRemindId.add(k);
                        Util.PlaySound(getBaseContext());
                        
                        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        getBaseContext().sendBroadcast(closeDialogs);
                                    
                        String subject      = v.getString("subject");
                        Reminder reminder   = new Reminder(currReminder);
                        
                        Intent i = new Intent(getBaseContext(), RemindAlert.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                        i.putExtra("subject", subject);
                        i.putExtra("reminder", reminder);
                        
                        getBaseContext().startActivity(i);
                        
                        //也发到通知栏
                        Util.sendAlarmNotification(getBaseContext(), subject, reminder);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deactivate();
    }

    public void deactivate() {
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }
}
