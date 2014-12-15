package com.yidianhulian.ydmemo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class AlarmService extends Service {
	
	@Override
	public void onCreate() {
		AlarmAlertWakeLock.acquireCpuWakeLock(this);
	}
	
	public class LocalBinder extends Binder {
		public AlarmService getService() {
			return AlarmService.this;
		}
	}
	
	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
		Util.PlaySound(AlarmService.this);
		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		AlarmAlertWakeLock.releaseCpuLock();
	}
}
