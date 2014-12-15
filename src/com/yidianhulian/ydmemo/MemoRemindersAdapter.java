package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yidianhulian.ydmemo.activity.AddRemind;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.widget.ReminderView;
import com.yidianhulian.ydmemo.widget.TraceView;

@SuppressLint({ "InflateParams", "UseSparseArrays" })
public class MemoRemindersAdapter extends BaseAdapter {
    private Activity mContext;
    private List<Reminder> mReminders = new ArrayList<Reminder>();
    private YDMemoApplication mApp;
    private User mCurrUser;
    private Memo mMemo;
    
    public MemoRemindersAdapter(Activity context,List<Reminder> reminders) {
        super();
        this.mReminders      = reminders;
        mApp = (YDMemoApplication)context.getApplication();
        mCurrUser = mApp.loginUser();
        this.mContext   = context;
    }
    
    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setMemo(Memo memo){
        mMemo = memo;
    }

    @Override
    public int getCount() {
        return mReminders.size();
    }

    @Override
    public Object getItem(int position) {
        return mReminders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private View noContentView(int position, View convertView, ViewGroup root) {
        return LayoutInflater.from(mContext).inflate(R.layout.empty_trace, null);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Reminder reminder    = (Reminder)getItem(position);
        if(reminder.id()==-1l){
            return noContentView(position, convertView, parent);
        }
        
        ReminderView viewGroup;
        if(convertView==null || /*empty view*/convertView.getTag()==null){
            viewGroup   = new ReminderView(mContext);
            convertView = viewGroup.getView();
        }else{
            viewGroup   = new ReminderView(mContext, (ViewGroup)convertView);
        }
        viewGroup.setMemo(mMemo);
        viewGroup.shown(reminder);
        
        return convertView;
    }
}
