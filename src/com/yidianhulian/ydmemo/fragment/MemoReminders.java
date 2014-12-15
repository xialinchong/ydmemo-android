package com.yidianhulian.ydmemo.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.MemoRemindersAdapter;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.MainActivity;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Reminder;
/**
 * 备忘留言
 * @author leeboo
 *
 */
public class MemoReminders extends Fragment 
    implements Refreshable,CallApiListener{

    private YDMemoApplication mApp;
    private PullToRefreshListView mList;
    private ViewGroup mLayout;
    private Memo mMemo;
    private MemoRemindersAdapter mAdapter;
    private List<Reminder> mReminders = new ArrayList<Reminder>();
    private Handler myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ((RefreshMemoInterface)getActivity()).reloadMemo();
            mList.onRefreshComplete();
        }
        
    };
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mApp    = (YDMemoApplication)getActivity().getApplication();
        mLayout = (ViewGroup) inflater.inflate(R.layout.memo_reminders, container, false);
        mList   = (PullToRefreshListView)mLayout.findViewById(R.id.memo_reminders);
        mList.setMode(Mode.PULL_FROM_START);
        
        addEmptyView();
        mAdapter = new MemoRemindersAdapter(getActivity(), mReminders);
        mList.setAdapter(mAdapter);
        mList.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                myHandler.sendEmptyMessage(0);
            }
        });
        return mLayout;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args =  getArguments();
        if(args != null){
            refresh((Memo)args.getParcelable("memo"));
        }
    }
    
    private void addEmptyView() {
        if(mReminders.size()==0){
            Reminder empty = new Reminder(null);
            empty.setAttr("id", "-1");
            mReminders.add(empty);
        }
    }
    
    private void shown(){
        getView().setVisibility(View.VISIBLE);
        mReminders.clear();
        for (Reminder reminder : mMemo.reminders()) {
            mReminders.add(reminder);
        }
        addEmptyView();
        mAdapter.setMemo(mMemo);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean refresh(Model model) {
        if(getView()==null) return false;//not ready;
        
        if(model==null){
            mMemo = null;
            getView().setVisibility(View.INVISIBLE);
            return false;
        }
        
        mList.onRefreshComplete();
        if( model instanceof Memo){
            if(mMemo==null ||  mMemo.id() == model.id() ){
                mMemo = (Memo) model;
                shown();
                return true;
            }
        }
        return false;
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        
        return null;
    }


    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }


    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        
    }


    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        
        return null;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
       
    }

    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return from;
    }
    
    public interface RefreshMemoInterface{
        void reloadMemo();
    }

}
