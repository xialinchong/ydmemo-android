package com.yidianhulian.ydmemo;

import android.content.Context;
import android.os.PowerManager;

public class AlarmAlertWakeLock {

    private static PowerManager.WakeLock sCpuWakeLock;

    public static void acquireCpuWakeLock(Context context) {      
        if (sCpuWakeLock != null) {
            return;
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sCpuWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "AlarmRemind");
        sCpuWakeLock.acquire();
    }

    public static void releaseCpuLock() {       
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
