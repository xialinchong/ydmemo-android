package com.yidianhulian.ydmemo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Notify;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.view.SlideDelete;
import com.yidianhulian.ydmemo.widget.SlideDeleteListView;
import com.yidianhulian.ydmemo.widget.SlideDeleteListView.RemoveDirection;
import com.yidianhulian.ydmemo.widget.SlideDeleteListView.RemoveListener;
/**
 * 动态中心, 点击具体对某条动态则进入具体对界面并删除该动态：
 * 该界面通过startActivityForResult打开；
 * 把选择的动态返回并通过接口删除这条动态
 * @author leeboo
 *
 */
public class Notification extends Activity implements CallApiListener, RemoveListener{
    public static final int REQUEST_FOR_VIEW = 1;
    public static final String RESULT_NOTIFICATION = "result_notification";
    private static final int API_REFRESH = 1;
    private static final int API_LOAD_MORE = 2;
    private static final int API_REMOVE_NOTIFICATION = 3;
    private static final int API_CLEAN_NOTIFICATION = 4;
    private SlideDelete mListView; 
    private YDMemoApplication mApp;
    private int mPage;
    private boolean isFirstLaunch = true;
    private SharedPreferences mSp;
    private SortedMap<Long, Notify> mNotifies = new TreeMap<Long, Notify>();
    private ArrayList<Notify> mDatas = new ArrayList<Notify>();
    
    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Notify notify = mDatas.get(position);
            if(notify.id()==-1){
                return noContentView(position, convertView, parent);
            }
            Holder holder;
            if(convertView==null || /*empty view*/convertView.getTag()==null){
                holder = new Holder();
                convertView = LayoutInflater.from(Notification.this).inflate(R.layout.cell_notification, null);
                holder.date = (TextView)convertView.findViewById(R.id.notification_date);
                holder.icon = (ImageView)convertView.findViewById(R.id.notification_icon);
                holder.msg  = (TextView)convertView.findViewById(R.id.notification_msg);
                convertView.setTag(holder);
                
            }else{
                holder = (Holder)convertView.getTag();
            }

            holder.date.setText(notify.date());
            holder.msg.setText(Util.showAtUser(mApp, notify.msg(), mApp.localContacts()));
            holder.notify = notify;
            
            return convertView;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }
        
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public void notifyDataSetChanged() {
            mDatas.clear();
            mDatas = new ArrayList<Notify>(mNotifies.values());
            super.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetInvalidated() {
            mDatas.clear();
            mDatas = new ArrayList<Notify>(mNotifies.values());
            super.notifyDataSetInvalidated();
        }
        
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list2);
        addEmptyView();
        isFirstLaunch = true;
        
        mListView = (SlideDelete)findViewById(R.id.refresh_list);
        mListView.setAdapter(mAdapter);
        
        //设置滑动删除监听,@HuJinhao,@2014-12-10
        SlideDeleteListView slideDeleteListView = (SlideDeleteListView)mListView.getRefreshableView();
        slideDeleteListView.setRemoveListener(this);
        
        mApp    = (YDMemoApplication)getApplication();
        mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                

                // 第一次打开界面时，先显示缓存数据; 恢复之前的数据历史 leeboo
                if(isFirstLaunch){
                    isFirstLaunch = false;
                    mPage = mSp.getInt("mPage", 1);
                    CallApiTask.doCallApi(API_REFRESH, Notification.this, Notification.this, CacheType.REPLACE, FetchType.FETCH_CACHE_THEN_API);
                    
                }else{
                    mPage = 1;
                    mSp.edit().putInt("mPage", 1).commit();
                    removeEmptyView();
                    CallApiTask.doCallApi(API_REFRESH, Notification.this, Notification.this, CacheType.REPLACE, FetchType.FETCH_API);
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                removeEmptyView();
                CallApiTask.doCallApi(API_LOAD_MORE, Notification.this,
                        Notification.this, CacheType.CUSTOM,
                        FetchType.FETCH_API);
            }
        });
        
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Notify notify = ((Holder)view.getTag()).notify;
                if(notify==null || ! notify.isClickable())return;
                
                CallApiTask.doCallApi(API_REMOVE_NOTIFICATION, Notification.this, Notification.this, notify);
                
//                Util.removeCacheAndUI(Notification.this, notify, mApp.loginUser().id());
                isFirstLaunch = true;
                Intent data = new Intent();
                data.setClass(Notification.this, MemoDetailActivity.class);
                data.putExtra(RESULT_NOTIFICATION, notify);
                startActivity(data);
            }
        });
        
        restoreActionBar();
        
        mSp = getSharedPreferences("notificationPagination", Context.MODE_PRIVATE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if(isFirstLaunch){
            mAdapter.notifyDataSetChanged();
            mListView.setRefreshing();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater flater = new MenuInflater(this);
        flater.inflate(R.menu.clean, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            setResult(RESULT_CANCELED, null);
            finish();
            return true;
        }else if(item.getItemId()==R.id.remove){
            Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.remove).setCancelable(true).setTitle(R.string.clean_all_notifies)
            .setNegativeButton(R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CallApiTask.doCallApi(API_CLEAN_NOTIFICATION, Notification.this, Notification.this);
                    mNotifies.clear();
                    addEmptyView();
                    mAdapter.notifyDataSetChanged();
                }
            }).setPositiveButton(R.string.cancel, new OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
            return true;
        } 
        return super.onOptionsItemSelected(item);
    }
    

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.notification_center);
    }
    
    class Holder{
        ImageView icon;
        TextView  msg;
        TextView  date;
        Notify notify;
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> queryStr = new HashMap<String, String>();
        queryStr.put("uid", mApp.loginUser().id()+"");
        if(what==API_REFRESH){
            return new Api("get", Util.URI_NOTIFICATION, queryStr);
            
        }else if(what==API_LOAD_MORE){
            queryStr.put("page", mPage+"");
            return new Api("get", Util.URI_NOTIFICATION, queryStr);
            
        }else if(what==API_REMOVE_NOTIFICATION){
            Notify notify  = (Notify)params[0];
            queryStr.put("nid", notify.id()+"");
            return new Api("post", Util.URI_REMOVE_NOTIFICATION, queryStr);
        }else if(what==API_CLEAN_NOTIFICATION){
//            Notify notify  = (Notify)params[0];
            queryStr.put("nid", "-1");
            return new Api("post", Util.URI_REMOVE_NOTIFICATION, queryStr);
        }
        return null;
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,
            JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public void apiNetworkException(Context context, int what, Exception e,
            Object... params) {
        
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return String.format("%s?uid=%s", Util.URI_NOTIFICATION, mApp.loginUser().id());
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        if(isDone) mListView.onRefreshComplete();
        if( ! isCallApiSuccess(context, what, result)){
            if(what != API_REFRESH && what != API_LOAD_MORE){
                Util.showToast(this, getString(R.string.has_error));
            }else{
                addEmptyView();
                
                mAdapter.notifyDataSetChanged();
            }
            return;
        }
        
        if (what == API_REFRESH) {
            mNotifies.clear();

            updateNotify(result);
            addEmptyView();
        } else if (what == API_LOAD_MORE) {

                updateNotify(result);
                mPage++;
                mSp.edit().putInt("mPage", mPage).commit();
            addEmptyView();
        }else if(what==API_REMOVE_NOTIFICATION || what==API_CLEAN_NOTIFICATION){
            Option misc = new Option(Api.getJSONValue(result, "data", JSONObject.class));
            Util.updateCacheAndUI(this, misc, mApp.loginUser().id());
        }
        
        mAdapter.notifyDataSetChanged();
    }

    private void updateNotify(JSONObject result) {
        JSONArray datas = Api.getJSONValue(result, "data", JSONArray.class);
        for(int i=0; i<datas.length(); i++){
            try {
                Notify notify = new Notify(datas.getJSONObject(i));
                mNotifies.put(notify.id(), notify);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        if(what!=API_LOAD_MORE)return from;
        JSONArray toData = Api.getJSONValue(to, "data", JSONArray.class);
        JSONArray fromData = Api.getJSONValue(from, "data", JSONArray.class);
        if (toData == null) return from;
        if (fromData == null) return to;
        
        for (int i = 0; i < fromData.length(); i++) {
            try {
                toData.put(fromData.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        try {
            to.put("data", toData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return to;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {
        
    }
    
    
    private void removeEmptyView() {
        if(mNotifies.size()==1 && mNotifies.firstKey() == -1){//“空数据”刷新时移除“无数据”提示
            mNotifies.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void addEmptyView() {
        if(mNotifies.size()==0){
            Notify empty = new Notify(null);
            empty.setAttr("id", "-1");
            mNotifies.put(-1l, empty);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private View noContentView(int position, View convertView, ViewGroup root) {
        return LayoutInflater.from(this).inflate(R.layout.empty_notification, null);
    }

	@Override
	public void removeItem(RemoveDirection direction, int position) {		
		Notify notify = mDatas.get(position - 1);
    	mNotifies.remove(notify.id());
    	mAdapter.notifyDataSetChanged();
    	
		CallApiTask.doCallApi(API_REMOVE_NOTIFICATION, Notification.this, Notification.this, notify);		
	}
    
}
