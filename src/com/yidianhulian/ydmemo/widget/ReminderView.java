package com.yidianhulian.ydmemo.widget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.ReminderContextHandler;
import com.yidianhulian.ydmemo.ReminderContextHandler.OnReminderChanged;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.activity.AddRemind;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Reminder;

public class ReminderView extends TraceView implements OnReminderChanged {

    private ReminderContextHandler contextHandler;
    private Memo mMemo; 
    
    public ReminderView(Activity context, ViewGroup view) {
        super(context, view);
    }
    
    public ReminderView(Activity context) {
        super(context);
    }
    
    public void initUI() {
        ReminderPlaceHolder holder = (ReminderPlaceHolder)mView.getTag();

        ViewStub stub = (ViewStub)mView.findViewById(R.id.trace_content);
        stub.setLayoutResource(R.layout.remind_view);
        ViewGroup remindView = (ViewGroup)stub.inflate();
        
        contextHandler = new ReminderContextHandler(mContext, this);

        holder.remindDate  = (TextView)remindView.findViewById(R.id.reminder_date);
        holder.remindTitle = (TextView)remindView.findViewById(R.id.reminder_title);
        holder.remindButton= (Button)remindView.findViewById(R.id.remind_action);
        holder.remindBG    = (ViewGroup)remindView.findViewById(R.id.remind_bg);
        
        holder.remindButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Reminder reminder = (Reminder) v.getTag();
                Intent intent = new Intent();
                intent.setClass(mContext, AddRemind.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("reminder", reminder);
                bundle.putParcelable("ARG_MEMO", mMemo);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
    }
    
    @Override
    public TraceViewPlaceHolder getPlaceHolder() {
        return new ReminderPlaceHolder();
    }

    public void setMemo(Memo memo){
        mMemo = memo;
    }
    
    /**
     * 显示reminder数据
     * 
     * @param reminder
     */
    public void shown(Model model) {
        Reminder reminder = (Reminder)model;
        if(reminder==null)return;
        
        ReminderPlaceHolder holder = (ReminderPlaceHolder)mView.getTag();
        
        holder.traceDate.setText((reminder.isCreater(mApp.loginUser().id()) ? "" : reminder.creater().displayName()+"\r\n") 
                + Util.dateFormat(mContext, reminder.createdOn()));
        
        holder.remindTitle.setText(reminder.title());
        
        String nextRemindTime = reminder.calculate_alarm_time(System.currentTimeMillis());       
        if(reminder.isRepeat()){
            holder.traceIcon.setImageResource(R.drawable.repeat_bell);          
            String nextDesc = "";
            if ( ! Util.isEmpty(nextRemindTime) ) {
            	nextDesc = "，下次提醒:" + nextRemindTime;
            }
            holder.remindDate.setText(reminder.repeatDesc(mContext.getResources()) + nextDesc);
        }else{
            holder.traceIcon.setImageResource(R.drawable.bell);
            holder.remindDate.setText(reminder.date());
        }
        
        holder.remindButton.setTag(reminder);
        
        if(Util.isEmpty(nextRemindTime)){ //已结束
            holder.remindDate.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG );
            holder.remindDate.getPaint().setAntiAlias(true);
            holder.remindBG.setBackgroundResource(R.drawable.closed_reminder_bg);
        }else{
            holder.remindDate.getPaint().setFlags( holder.remindTitle.getPaint().getFlags());
            holder.remindBG.setBackgroundResource(R.drawable.reminder_bg);
        }
    }
    @Override
    public void reminderRemoved() {
        
    }

    public class ReminderPlaceHolder extends TraceViewPlaceHolder{
        Button  remindButton;
        TextView remindTitle;
        TextView remindDate;
        ViewGroup remindBG;
    }

}
