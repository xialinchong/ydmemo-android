package com.yidianhulian.ydmemo.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;

/**
 * 重复提醒任务
 * 
 * @author ydhl 2014-11-19
 */

public class RepeatRemindTask extends Activity {

    private Button mRepeatByDay;
    private Button mRepeatByWeek;
    private Button mRepeatByMonth;
    private Button mRepeatByYear;
    private Button mStartTimeBtn;
    private Button mEndTimeBtn;
    private ImageButton mNeverBtn;
    private EditText mRepeatTimes;// 重复周期，选择按天就代表多少天，其他同理
    private TextView mUnit;
    private TextView mRepeatTimesHint;

    // 只有选择按周的时候才有
    private LinearLayout mDayOfWeekLayout;
    // private EditText mDayOfWeek;//星期几
    private Button mMondayBtn;
    private Button mTuesdayBtn;
    private Button mWednesdayBtn;
    private Button mThursdayBtn;
    private Button mFridayBtn;
    private Button mSatusdayBtn;
    private Button mSundayBtn;
    private String mDayOfWeek;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATUSDAY = 6;
    public static final int SUNDAY = 7;

    private AlertDialog mStartPicker;
    private AlertDialog mEndPicker;
    private DatePicker mStartDatePicker;
    private DatePicker mEndDatePicker;

    public static final int GET_REPEAT_SET = 4;
    public static final int CANCEL_REPEAT_SET = 5;

    public static final String BTN_BY_DAY = "day";
    public static final String BTN_BY_WEEK = "week";
    public static final String BTN_BY_MONTH = "month";
    public static final String BTN_BY_YEAR = "year";
    private String mBtnType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeat_remind_task);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.set_repeat);

        Intent itn = getIntent();
        mBtnType = itn.getStringExtra("btnType");
        
        if (itn.getStringExtra("theDayOfWeek").equals("")) {
            mDayOfWeek = "1";
        } else {
            mDayOfWeek = itn.getStringExtra("theDayOfWeek");
        }

        mRepeatByDay = (Button) findViewById(R.id.repeat_by_day);
        mRepeatByWeek = (Button) findViewById(R.id.repeat_by_week);
        mRepeatByMonth = (Button) findViewById(R.id.repeat_by_month);
        mRepeatByYear = (Button) findViewById(R.id.repeat_by_year);
        mRepeatByDay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRepeatByDay.setSelected(true);
                mRepeatByWeek.setSelected(false);
                mRepeatByMonth.setSelected(false);
                mRepeatByYear.setSelected(false);
                mDayOfWeekLayout.setVisibility(View.GONE);
                mBtnType = BTN_BY_DAY;
                mRepeatTimesHint.setText(R.string.day_times);
                mUnit.setText("天");
            }
        });
        mRepeatByWeek.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRepeatByDay.setSelected(false);
                mRepeatByWeek.setSelected(true);
                mRepeatByMonth.setSelected(false);
                mRepeatByYear.setSelected(false);
                mBtnType = BTN_BY_WEEK;
                mDayOfWeekLayout.setVisibility(View.VISIBLE);
                mRepeatTimesHint.setText(R.string.week_times);
                mUnit.setText("周");
            }
        });
        mRepeatByMonth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRepeatByDay.setSelected(false);
                mRepeatByWeek.setSelected(false);
                mRepeatByMonth.setSelected(true);
                mRepeatByYear.setSelected(false);
                mBtnType = BTN_BY_MONTH;
                mDayOfWeekLayout.setVisibility(View.GONE);
                mRepeatTimesHint.setText(R.string.month_times);
                mUnit.setText("月");
            }
        });
        mRepeatByYear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRepeatByDay.setSelected(false);
                mRepeatByWeek.setSelected(false);
                mRepeatByMonth.setSelected(false);
                mRepeatByYear.setSelected(true);
                mBtnType = BTN_BY_YEAR;
                mDayOfWeekLayout.setVisibility(View.GONE);
                mRepeatTimesHint.setText(R.string.year_times);
                mUnit.setText("年");
            }
        });

        mStartTimeBtn = (Button) findViewById(R.id.start_time_btn);
        mStartTimeBtn.setText(itn.getStringExtra("startDate"));
        mStartTimeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mStartPicker.show();
            }
        });
        mEndTimeBtn = (Button) findViewById(R.id.end_time_btn);
        mEndTimeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEndPicker.show();
            }
        });
        mNeverBtn = (ImageButton) findViewById(R.id.btn_never);
        mNeverBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEndTimeBtn.setText(R.string.never);
                mEndTimeBtn.setPadding(com.yidianhulian.framework.
                        Util.dip2px(RepeatRemindTask.this, 80), 
                        0, com.yidianhulian.framework.
                        Util.dip2px(RepeatRemindTask.this, 5), 0);
                mNeverBtn.setVisibility(View.GONE);
            }
        });
        if (itn.getStringExtra("endDate").equals("")) {
            mEndTimeBtn.setText(R.string.never);
        } else {
            mEndTimeBtn.setText(itn.getStringExtra("endDate"));
            mEndTimeBtn.setPadding(com.yidianhulian.framework.
                    Util.dip2px(RepeatRemindTask.this, 80), 
                    0, com.yidianhulian.framework.
                    Util.dip2px(RepeatRemindTask.this, 42), 0);
            mNeverBtn.setVisibility(View.VISIBLE);
        }
        
        mRepeatTimes = (EditText) findViewById(R.id.repeat_times);
        mRepeatTimes.setText(itn.getStringExtra("cycle"));
        mRepeatTimes.setSelection(itn.getStringExtra("cycle").length());
        mUnit = (TextView) findViewById(R.id.unit);
        
        mRepeatTimesHint = (TextView) findViewById(R.id.repeat_times_hint);

        mDayOfWeekLayout = (LinearLayout) findViewById(R.id.day_of_week_layout);
        mMondayBtn = (Button) findViewById(R.id.monday);
        mMondayBtn.setOnClickListener(new BtnOnClickLinstener(MONDAY));
        mTuesdayBtn = (Button) findViewById(R.id.tuesday);
        mTuesdayBtn.setOnClickListener(new BtnOnClickLinstener(TUESDAY));
        mWednesdayBtn = (Button) findViewById(R.id.wednesday);
        mWednesdayBtn.setOnClickListener(new BtnOnClickLinstener(WEDNESDAY));
        mThursdayBtn = (Button) findViewById(R.id.thursday);
        mThursdayBtn.setOnClickListener(new BtnOnClickLinstener(THURSDAY));
        mFridayBtn = (Button) findViewById(R.id.firday);
        mFridayBtn.setOnClickListener(new BtnOnClickLinstener(FRIDAY));
        mSatusdayBtn = (Button) findViewById(R.id.saturday);
        mSatusdayBtn.setOnClickListener(new BtnOnClickLinstener(SATUSDAY));
        mSundayBtn = (Button) findViewById(R.id.sunday);
        mSundayBtn.setOnClickListener(new BtnOnClickLinstener(SUNDAY));

        getStartDialog();
        getEndDialog();
        if (mBtnType.equals(BTN_BY_DAY) || mBtnType.equals("no")) {
            mRepeatByDay.setSelected(true);
            mRepeatByWeek.setSelected(false);
            mRepeatByMonth.setSelected(false);
            mRepeatByYear.setSelected(false);
            mDayOfWeekLayout.setVisibility(View.GONE);
            mRepeatTimesHint.setText(R.string.day_times);
            mUnit.setText("天");
        } else if (mBtnType.equals(BTN_BY_WEEK)) {
            mRepeatByDay.setSelected(false);
            mRepeatByWeek.setSelected(true);
            mRepeatByMonth.setSelected(false);
            mRepeatByYear.setSelected(false);
            mDayOfWeekLayout.setVisibility(View.VISIBLE);
            mRepeatTimesHint.setText(R.string.week_times);
            mUnit.setText("周");
        } else if (mBtnType.equals(BTN_BY_MONTH)) {
            mRepeatByDay.setSelected(false);
            mRepeatByWeek.setSelected(false);
            mRepeatByMonth.setSelected(true);
            mRepeatByYear.setSelected(false);
            mDayOfWeekLayout.setVisibility(View.GONE);
            mRepeatTimesHint.setText(R.string.month_times);
            mUnit.setText("月");
        } else if (mBtnType.equals(BTN_BY_YEAR)) {
            mRepeatByDay.setSelected(false);
            mRepeatByWeek.setSelected(false);
            mRepeatByMonth.setSelected(false);
            mRepeatByYear.setSelected(true);
            mDayOfWeekLayout.setVisibility(View.GONE);
            mRepeatTimesHint.setText(R.string.year_times);
            mUnit.setText("年");
        }
        setBtnSelectStatus(Integer.valueOf(mDayOfWeek));
    }

    class BtnOnClickLinstener implements OnClickListener {
        private int mDay;

        public BtnOnClickLinstener(int day) {
            this.mDay = day;
        }

        @SuppressLint("SimpleDateFormat")
		@Override
        public void onClick(View v) {
            mDayOfWeek = mDay + "";
            setBtnSelectStatus(mDay);
            //选周时,开始日期需跟着对应,@HuJinhao,@2014-12-11
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            mStartTimeBtn.setText(format.format(Util.calculateDateByWeekday(mDay)));
        }

    }

    private void setBtnSelectStatus (int what) {
        switch (what) {
        case MONDAY:
            mMondayBtn.setSelected(true);
            mTuesdayBtn.setSelected(false);
            mWednesdayBtn.setSelected(false);
            mThursdayBtn.setSelected(false);
            mFridayBtn.setSelected(false);
            mSatusdayBtn.setSelected(false);
            mSundayBtn.setSelected(false);
            break;
        case TUESDAY:
            mMondayBtn.setSelected(false);
            mTuesdayBtn.setSelected(true);
            mWednesdayBtn.setSelected(false);
            mThursdayBtn.setSelected(false);
            mFridayBtn.setSelected(false);
            mSatusdayBtn.setSelected(false);
            mSundayBtn.setSelected(false);
            break;
        case WEDNESDAY:
            mMondayBtn.setSelected(false);
            mTuesdayBtn.setSelected(false);
            mWednesdayBtn.setSelected(true);
            mThursdayBtn.setSelected(false);
            mFridayBtn.setSelected(false);
            mSatusdayBtn.setSelected(false);
            mSundayBtn.setSelected(false);
            break;
        case THURSDAY:
            mMondayBtn.setSelected(false);
            mTuesdayBtn.setSelected(false);
            mWednesdayBtn.setSelected(false);
            mThursdayBtn.setSelected(true);
            mFridayBtn.setSelected(false);
            mSatusdayBtn.setSelected(false);
            mSundayBtn.setSelected(false);
            break;
        case FRIDAY:
            mMondayBtn.setSelected(false);
            mTuesdayBtn.setSelected(false);
            mWednesdayBtn.setSelected(false);
            mThursdayBtn.setSelected(false);
            mFridayBtn.setSelected(true);
            mSatusdayBtn.setSelected(false);
            mSundayBtn.setSelected(false);
            break;
        case SATUSDAY:
            mMondayBtn.setSelected(false);
            mTuesdayBtn.setSelected(false);
            mWednesdayBtn.setSelected(false);
            mThursdayBtn.setSelected(false);
            mFridayBtn.setSelected(false);
            mSatusdayBtn.setSelected(true);
            mSundayBtn.setSelected(false);
            break;
        case SUNDAY:
            mMondayBtn.setSelected(false);
            mTuesdayBtn.setSelected(false);
            mWednesdayBtn.setSelected(false);
            mThursdayBtn.setSelected(false);
            mFridayBtn.setSelected(false);
            mSatusdayBtn.setSelected(false);
            mSundayBtn.setSelected(true);
            break;
        }
    }

    // 开始时间对话框
    private void getStartDialog() {
        View dialogView = getLayoutInflater().inflate(
                R.layout.remind_date_picker, null);
        mStartDatePicker = (DatePicker) dialogView
                .findViewById(R.id.remind_date_datePicker);
        TimePicker timePicker = (TimePicker) dialogView
                .findViewById(R.id.remind_date_timePicker);
        timePicker.setVisibility(View.GONE);
        AlertDialog.Builder start = new AlertDialog.Builder(this);
        start.setTitle(R.string.start_time);
        start.setView(dialogView);
        start.setIcon(R.drawable.date_and_time);
        start.setNegativeButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @SuppressLint("SimpleDateFormat")
					@Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mStartDate = String.format("%s-%s-%s",
                                mStartDatePicker.getYear(),
                                mStartDatePicker.getMonth() + 1,
                                mStartDatePicker.getDayOfMonth());
                        mStartTimeBtn.setText(mStartDate);
                        //如果是按周提醒,选择日期时,对应下面的星期几需跟着变,@HuJinhao,@2014-12-11
                        if (mBtnType.equals("week")) {
	                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	                        try {
								int weekday = Util.calculateWeekdayByDate(format.parse(mStartDate));
								setBtnSelectStatus(weekday);
								mDayOfWeek = String.valueOf(weekday);
							} catch (ParseException e) {
								e.printStackTrace();
							}
                        }
                        dialog.dismiss();
                    }
                });
        mStartPicker = start.create();
    }

    // 结束时间对话框
    private void getEndDialog() {
        View dialogView = getLayoutInflater().inflate(
                R.layout.remind_date_picker, null);
        mEndDatePicker = (DatePicker) dialogView
                .findViewById(R.id.remind_date_datePicker);
        TimePicker timePicker = (TimePicker) dialogView
                .findViewById(R.id.remind_date_timePicker);
        timePicker.setVisibility(View.GONE);
        AlertDialog.Builder end = new AlertDialog.Builder(this);
        end.setTitle(R.string.start_time);
        end.setView(dialogView);
        end.setIcon(R.drawable.date_and_time);
        end.setNegativeButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mEndtDate = String.format("%s-%s-%s",
                                mEndDatePicker.getYear(),
                                mEndDatePicker.getMonth() + 1,
                                mEndDatePicker.getDayOfMonth());
                        mEndTimeBtn.setText(mEndtDate);
                        mEndTimeBtn.setPadding(com.yidianhulian.framework.
                                Util.dip2px(RepeatRemindTask.this, 80), 
                                0, com.yidianhulian.framework.
                                Util.dip2px(RepeatRemindTask.this, 42), 0);
                        mNeverBtn.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
        mEndPicker = end.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(CANCEL_REPEAT_SET, intent);
            finish();
            return true;
        }
        if (item.getItemId() == R.id.ok) {
            if (check_value())
                return true;
            Intent intent = new Intent();
            intent.putExtra("startDate", mStartTimeBtn.getText().toString());
            intent.putExtra("endDate", mEndTimeBtn.getText().toString());
            intent.putExtra("cycle", mRepeatTimes.getText().toString());
            intent.putExtra("theDayOfWeek", mDayOfWeek);
            if (mBtnType.equals("no")) {
                intent.putExtra("btnType", BTN_BY_DAY);
            } else {
                intent.putExtra("btnType", mBtnType);
            }

            setResult(GET_REPEAT_SET, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean check_value() {
        if (mStartTimeBtn.getText().toString().equals("")) {
            Util.showToast(this, "请设置重复提醒开始时间！");
            return true;
        }
        if (mRepeatTimes.getText().toString().equals("")) {
            Util.showToast(this, "重复周期不能为空！");
            return true;
        }
        if (Integer.valueOf(mRepeatTimes.getText().toString().substring(0, 1))
                < 1) {
            Util.showToast(this, "重复周期不能小于1！");
            return true;
        }
        if (mBtnType.equals(BTN_BY_WEEK) && mDayOfWeek.equals("")) {
            Util.showToast(this, "提醒时间不能为空！");
            return true;
        }
        return false;
    }
}
