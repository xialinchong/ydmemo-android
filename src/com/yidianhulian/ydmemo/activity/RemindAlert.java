package com.yidianhulian.ydmemo.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.model.Reminder;

/**
 *  系统提醒闹钟界面
 *  @author HuJinhao
 *  @since 2014-10-29
 */
 
public class RemindAlert extends Activity {	
	private String memo_subject;
	private Reminder reminder;
	private YDMemoApplication mApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.remind_alert);
		mApp = (YDMemoApplication)getApplication();
		
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		 | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		 | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		win.setGravity(Gravity.BOTTOM);
				
		Intent intent = getIntent();
		
		TextView remind_subject = (TextView)findViewById(R.id.remind_subject);
		TextView remind_content = (TextView)findViewById(R.id.remind_content);
		Button remind_known_btn = (Button)findViewById(R.id.remind_known_btn);
		Button remind_after_btn = (Button)findViewById(R.id.remind_after_btn);
		
		
		memo_subject 	= intent.getStringExtra("subject");
		reminder		= (Reminder)intent.getParcelableExtra("reminder");
		
		remind_subject.setText(memo_subject);
		remind_content.setText(reminder.title());
		
		remind_known_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {		
				//stopService(new Intent(AlarmReceiver.ACTION));
				doAfterAlarm();
				clearNotification();
				RemindAlert.this.finish();
			}
		});
		
		remind_after_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {								
				//stopService(new Intent(AlarmReceiver.ACTION));
				clearNotification();
				
				CacheHelper helper = new CacheHelper(mApp);
				
				int delay_minute = 5;
				if(mApp.loginUser()!=null){
				    Option option = helper.getSetting(mApp.loginUser().id());
				    if(option!=null) delay_minute = option.alertInterval();
				}
				Util.createDelayAlarmReminder(RemindAlert.this, memo_subject, reminder, delay_minute);
				String notice_format = getResources().getString(R.string.delay_alert_notice);
				Toast.makeText(RemindAlert.this, String.format(notice_format, delay_minute), Toast.LENGTH_SHORT).show();

				RemindAlert.this.finish();
			}
		});
			
	}
	
	@Override
	protected void onStop() {		
		super.onStop();
//		AlarmAlertWakeLock.releaseCpuLock();
		doAfterAlarm();
	}
	
	private void doAfterAlarm() {
		if (reminder.repeat_type().isEmpty()) { //非重复闹钟,清除掉之前闹钟
			Util.removeLocalRemind(RemindAlert.this, String.valueOf(reminder.id()));	
		} else { //创建下一次重复闹钟
			Util.createAlarmReminder(this, memo_subject, reminder);
		}
	}
	
	/**
	 * 清除通知
	 */
	private void clearNotification() {
		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);    	
    	nm.cancel((int)reminder.id());
	}
}
