package com.yidianhulian.ydmemo.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.ReminderContextHandler;
import com.yidianhulian.ydmemo.ReminderContextHandler.OnReminderChanged;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.widget.MemoUsers;

/**
 * 通过startActivityForResult打开， 增加、修改提醒，增加成功后返回Memo对象<br/>
 * 1. 修改提醒传入ARG_REMINDER；<br/>
 * 2. 根据comment建立提醒传入ARG_COMMENT<br/>
 * 3. 总是要传入ARG_MEMO
 * 
 * @author leeboo
 * 
 */
public class AddRemind extends Activity implements CallApiListener,
        OnGeocodeSearchListener {

    public static final int REQUEST_DATE = 10;
    public static final String RESULE_DATE = "RESULE_DATE";
    public static final String ARG_MEMO = "ARG_MEMO";
    public static final String ARG_REMINDER = "reminder";
    public static final String ARG_COMMENT = "ARG_COMMENT";
    public static final int API_ADD_REMIND = 02;

    private YDMemoApplication mApp;
    private EditText mAddRemindText;
    private MemoUsers mRemindUsers;
    private Memo mMemo;
    private Button mRemindDateBtn;
    private AlertDialog mDatePicker;
    private DatePicker mRemindDatePicker;
    private TimePicker mRemindTimePicker;
    private String mRemindDate;
    // 增加重复任务、提醒地点 xialinchong 2014-11-19
    private Button mRemindRepeatBtn;
    private String mBtnType = "no";
    private String mStartDate = "";
    private String mStartTime = "";
    private String mEndDate = "";
    private String mCycle = "";
    private String mTheDayofWeek = "";
    private Button mRemindAddrBtn;

    // 增加提醒地点 xialinchong @2014-11-24
    private String mPoint = "";
    private String mAddressName = "";

    // 判断intent是否有值，有就显示 xialinchong @2014-12-04
    private GeocodeSearch mGeocoderSearch;
    private Reminder mModifyReminder;
    private Comment mComment;
    private ViewGroup mReminderUserPanel;

    private ImageButton mClearRepeatTaskBtn;
    private ImageButton mClearAddrBtn;
    
    private void setBtnPadding (boolean isVisible) {
        if ( ! isVisible) {
            mRemindRepeatBtn.setPadding(
                    com.yidianhulian.framework.Util.dip2px(this, 110), 0, 
                    com.yidianhulian.framework.Util.dip2px(this, 10), 0);
        }
    }
    
    private void setupViewByIntent() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        
        mGeocoderSearch = new GeocodeSearch(this);
        mGeocoderSearch.setOnGeocodeSearchListener(this);
        
        mModifyReminder = getIntent().getParcelableExtra(ARG_REMINDER);
        if (mModifyReminder == null) {
            actionBar.setTitle(R.string.add_remind);
            SimpleDateFormat sDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:00");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());
            calendar.add(calendar.DATE, 1);
            String date[] = sDateFormat.format(calendar.getTime()).split(" ");
            mStartDate = date[0];
            mStartTime = date[1];
            mRemindDate = mStartDate + " " + mStartTime;
            if(mComment!=null){
                mAddRemindText.setText(mComment.comment());
            }
            mReminderUserPanel.setVisibility(View.VISIBLE);
        } else {
            actionBar.setTitle(R.string.modify_remind);
            mReminderUserPanel.setVisibility(View.GONE);
            mAddRemindText.setText(mModifyReminder.title());
            mAddRemindText.setSelection(mModifyReminder.title().length());
            mRemindDate = mModifyReminder.date();
            String modifydate[] = mRemindDate.split(" ");
            mStartDate = modifydate[0];
            mStartTime = modifydate[1];
            if (mModifyReminder.isRepeat()) {
                mRemindRepeatBtn.setText(mModifyReminder
                        .repeatDesc(getResources()));
                mEndDate = mModifyReminder.repeat_end_on();
                mCycle = mModifyReminder.repeat_every() + "";
                mTheDayofWeek = mModifyReminder.repeat_on() + "";
                mBtnType = mModifyReminder.repeat_type();
                mClearRepeatTaskBtn.setVisibility(View.VISIBLE);
            }

            if (mModifyReminder.gps() != null
                    && !mModifyReminder.gps().equals("null")
                    && !mModifyReminder.gps().equals("")) {
                String[] gps = mModifyReminder.gps().split(",");
                LatLonPoint latLonPoint = new LatLonPoint(
                        Double.parseDouble(gps[0]), Double.parseDouble(gps[1]));
                getAddress(latLonPoint);
                mPoint = gps[0] + "," + gps[1];
            }
        }

        mRemindDateBtn.setText(mRemindDate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_remind);
        mApp = (YDMemoApplication) getApplication();

        mRemindDateBtn = (Button) findViewById(R.id.add_remind_date_btn);
        mAddRemindText = (EditText) findViewById(R.id.add_remind_title);
        mReminderUserPanel = (ViewGroup) findViewById(R.id.reminder_users);

        mRemindDateBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mDatePicker.show();
            }
        });

        mMemo = (Memo) getIntent().getParcelableExtra(ARG_MEMO);
        mComment = (Comment) getIntent().getParcelableExtra(ARG_COMMENT);

        mRemindUsers = new MemoUsers(this,
                (ViewGroup) findViewById(R.id.remind_memo_users));
        List<User> users = mMemo.followers();
        users.add(mMemo.assigner());
        mRemindUsers.shown(false, true, users, new ArrayList<User>() {
            private static final long serialVersionUID = 6257158976745938502L;

            {
                add(mApp.loginUser());
            }
        });

        View dialogView = getLayoutInflater().inflate(
                R.layout.remind_date_picker, null);
        mRemindDatePicker = (DatePicker) dialogView
                .findViewById(R.id.remind_date_datePicker);
        mRemindTimePicker = (TimePicker) dialogView
                .findViewById(R.id.remind_date_timePicker);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remind_date);
        builder.setView(dialogView);
        builder.setIcon(R.drawable.date_and_time);
        builder.setNegativeButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRemindDate = String.format("%s-%s-%s %s:%s:00",
                                mRemindDatePicker.getYear(),
                                mRemindDatePicker.getMonth() + 1,
                                mRemindDatePicker.getDayOfMonth(),
                                mRemindTimePicker.getCurrentHour(),
                                mRemindTimePicker.getCurrentMinute());
                        mRemindDateBtn.setText(mRemindDate);
                        String time[] = mRemindDate.split(" ");
                        mStartDate = time[0];
                        mStartTime = time[1];
                        dialog.dismiss();
                    }
                });

        mDatePicker = builder.create();

        mRemindRepeatBtn = (Button) findViewById(R.id.remind_repeat_btn);
        mRemindRepeatBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String cycle = mCycle;
                if (mCycle.equals("")) {
                    cycle = "1";
                }
                intent.setClass(AddRemind.this, RepeatRemindTask.class);
                intent.putExtra("startDate", mStartDate);
                intent.putExtra("endDate", mEndDate);
                intent.putExtra("cycle", cycle);
                intent.putExtra("theDayOfWeek", mTheDayofWeek);
                intent.putExtra("btnType", mBtnType);
                startActivityForResult(intent, RepeatRemindTask.GET_REPEAT_SET);
            }
        });

        mRemindAddrBtn = (Button) findViewById(R.id.remind_addr_btn);
        mRemindAddrBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AddRemind.this, MapForRemind.class);
                intent.putExtra("point", mPoint);
                intent.putExtra("addressName", mAddressName);
                startActivityForResult(intent, MapForRemind.GET_ADDRESS);
            }
        });
        
        mClearRepeatTaskBtn = (ImageButton)findViewById(R.id.btn_clear_repeat);
        mClearRepeatTaskBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	mBtnType = "no";
                mStartDate = "";
                mStartTime = "";
                mEndDate = "";
                mCycle = "";
                mTheDayofWeek = "";
                mRemindRepeatBtn.setText("");
                v.setVisibility(View.GONE);
            }
        });
        
        mClearAddrBtn = (ImageButton)findViewById(R.id.btn_clear_addr);
        mClearAddrBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	mPoint = "";
            	mRemindAddrBtn.setText("");
            	v.setVisibility(View.GONE);
            }
        });
        
        setupViewByIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
        case RepeatRemindTask.GET_REPEAT_SET:
            // 重复提醒结果返回处理 xialinchong 2014-11-20
            if (data == null)
                return;
            mBtnType = data.getStringExtra("btnType");
            mStartDate = data.getStringExtra("startDate");
            mEndDate = data.getStringExtra("endDate");
            if (mEndDate.equals("从不")) {
                mEndDate = "";
            }
            mCycle = data.getStringExtra("cycle");
            if (mBtnType.equals("week")) {
                mTheDayofWeek = data.getStringExtra("theDayOfWeek");
            }
            mRemindDate = mStartDate + " " + mStartTime;
            mRemindDateBtn.setText(mRemindDate);
            if (!mCycle.equals("")) {
                if (mBtnType.equals(RepeatRemindTask.BTN_BY_DAY)) {
                    mRemindRepeatBtn.setText("每" + mCycle + "天重复一次");
                } else if (mBtnType.equals(RepeatRemindTask.BTN_BY_WEEK)) {
                    mRemindRepeatBtn.setText("每" + mCycle + "周重复一次");
                } else if (mBtnType.equals(RepeatRemindTask.BTN_BY_MONTH)) {
                    mRemindRepeatBtn.setText("每" + mCycle + "月重复一次");
                } else if (mBtnType.equals(RepeatRemindTask.BTN_BY_YEAR)) {
                    mRemindRepeatBtn.setText("每" + mCycle + "年重复一次");
                }
                mClearRepeatTaskBtn.setVisibility(View.VISIBLE);
            } else if (!mBtnType.equals(RepeatRemindTask.BTN_BY_WEEK)) {
                mTheDayofWeek = "";
            } else {
                mBtnType = "no";
                mRemindRepeatBtn.setText(mCycle);
                mStartDate = "";
                mEndDate = "";
                mTheDayofWeek = "";
            }
            break;
        case MapForRemind.GET_ADDRESS:
            mPoint = data.getStringExtra("point");
            mAddressName = data.getStringExtra("addressName");
            mRemindAddrBtn.setText(mAddressName);
            mClearAddrBtn.setVisibility(View.VISIBLE);
            break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);

        inflater.inflate(R.menu.ok, menu);
        if (mModifyReminder != null) {
            inflater.inflate(R.menu.more, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(REQUEST_DATE, intent);
            finish();
            return true;
        }

        if (item.getItemId() == R.id.ok) {
            saveRemind();
            return true;
        } else if (item.getItemId() == R.id.more) {
            ReminderContextHandler handler = new ReminderContextHandler(this,
                    new OnReminderChanged() {

                        @Override
                        public void reminderRemoved() {
                            Intent intent = new Intent();
                            setResult(REQUEST_DATE, intent);
                            finish();
                        }

                    });
            handler.showReminderContextMenu(mModifyReminder);
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveRemind() {
        if (mModifyReminder == null) {
            List<User> remindUser = mRemindUsers.checkedUser();
            if (remindUser == null || remindUser.size() == 0) {
                Util.showToast(this, getString(R.string.reminder_user_is_empty));
                return;
            }
        }
        String remindText = mAddRemindText.getText().toString().trim();
        if (remindText.isEmpty()) {
            Util.showToast(this, getString(R.string.reminder_content_is_empty));
            return;
        }

        Util.showLoading(this, getString(R.string.posting));
        CallApiTask.doCallApi(API_ADD_REMIND, this, this);
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {

        Map<String, String> args = new HashMap<String, String>();
        args.put("remind_title", mAddRemindText.getText().toString().trim());
        args.put("remind_date", mRemindDate);

        String url;
        if (mModifyReminder == null) {
            List<User> remindUser = mRemindUsers.checkedUser();
            String reminders = "";
            for (User user : remindUser) {
                reminders = reminders + "," + user.id();
            }
            args.put("reminders", reminders);
            url = Util.URI_ADD_REMINDER;
        } else {
            args.put("rid", mModifyReminder.id() + "");
            url = Util.URI_MODIFY_REMIND;
        }

        args.put("repeat_start_on", mStartDate);
        args.put("repeat_end_on", mEndDate);
        args.put("repeat_every", mCycle);
        args.put("repeat_type", mBtnType);
        args.put("repeat_on", mTheDayofWeek);
        args.put("gps", mPoint);

        return new Api("post", String.format("%s?uid=%s&memo_id=%s", url, mApp
                .loginUser().id(), mMemo.id()), args);
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        Util.hideLoading();
        if (!Util.checkResult(this, result,
                getString(R.string.data_post_failed))) {
            return;
        }

        try {
            Reminder r = new Reminder(result.getJSONObject("data"));
            Util.updateCacheAndUI(context, r, mApp.loginUser().id());
            // 建立提醒闹钟,@HuJinhao,@2014-10-30
            List<User> remindUsers = mRemindUsers.checkedUser();
            if (remindUsers.contains(((YDMemoApplication) getApplication())
                    .loginUser())) {
                Util.createAlarmReminder(this, mMemo.subject(), r);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setResult(RESULT_OK);

        finish();
    }

    @Override
    public void apiNetworkException(Context context, int what, Exception e,
            Object... params) {
        Util.showToast(this, getString(R.string.network_error));
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,
            JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return from;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {

    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
//        Util.showLoadingAtButton(mRemindAddrBtn, this, "正在获取地址...");
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        mGeocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
//        Util.hideLoading();
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                mAddressName = result.getRegeocodeAddress().getFormatAddress();
                mRemindAddrBtn.setText(mAddressName);
                mClearAddrBtn.setVisibility(View.VISIBLE);
            } else {
                Util.showToast(this, "没有找到匹配地址");
            }
        } else {
            Util.showToast(this, "没有找到匹配地址");
        }
    }

}
