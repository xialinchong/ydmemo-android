package com.yidianhulian.ydmemo.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Model;

public abstract class TraceView {
    protected Activity mContext;
    protected ViewGroup mView;
    protected YDMemoApplication mApp;

    /**
     * 通过重用的view构造，view中必须设置好tag为PlaceHolder
     * 
     * @param context
     * @param view
     */
    public TraceView(Activity context, ViewGroup view) {
        super();
        this.mContext = context;
        mApp = (YDMemoApplication)context.getApplication();
        mView  = view;
    }
    
    public TraceView(Activity context) {
        super();
        this.mContext = context;
        mApp = (YDMemoApplication)context.getApplication();
        TraceViewPlaceHolder holder = getPlaceHolder();
        mView       = (ViewGroup)LayoutInflater.from(mContext).inflate(R.layout.cell_trace, null);
        holder.traceIcon  = (ImageView)mView.findViewById(R.id.trace_type);
        holder.traceDate  = (TextView)mView.findViewById(R.id.trace_date);
        
        mView.setTag(holder);
        
        initUI();
    }

    /**
     * 显示数据
     * 
     * @param reminder
     */
    public abstract void shown(Model reminder);
    public abstract void initUI();
    public abstract TraceViewPlaceHolder getPlaceHolder();

    public ViewGroup getView(){
        return mView;
    }

    public class TraceViewPlaceHolder {
        protected ImageView traceIcon;
        protected TextView traceDate;
    }

}
