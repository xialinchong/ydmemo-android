package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.model.Reminder;

/**
 * 用于Reminder提醒的上下文菜单管理
 * 
 * @author leeboo
 *
 */
@SuppressWarnings("unchecked")
public class ReminderContextHandler implements CallApiListener{
    private Activity mActivity;
    Dialog mDlg;
    List<Map<String, ?>> mMenus = new ArrayList<Map<String,?>>();
    SimpleAdapter mAdapter;
    ListView mListView;
    private Reminder mReminder;
    private OnReminderChanged mChangeListener;
    private static final int SECTION = 0;
    private static final int MENU = 1;
    private static final int REMOVE_REMIND = 2;
    
    private YDMemoApplication mApp;
    
    public ReminderContextHandler(Activity activity, OnReminderChanged changeListener){
        mApp = (YDMemoApplication) activity.getApplication();
        mActivity = activity;
        mDlg = new Dialog(activity);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mChangeListener = changeListener;
        
        mDlg.setContentView(R.layout.comment_context_menu);
        mDlg.setCancelable(true);
        mDlg.setCanceledOnTouchOutside(true);

        mAdapter = new SimpleAdapter(mActivity, mMenus, 
                R.layout.cell_comment_context_menu, new String[]{"icon","label"}, 
                new int[]{R.id.context_menu_icon, R.id.context_menu_label}){
                    @Override
                    public View getView(int position, View convertView,
                            ViewGroup parent) {
                        Map<String, Integer> data = (Map<String, Integer>)mMenus.get(position);
                        if(getItemViewType(position)==MENU){
                            convertView     = LayoutInflater.from(mActivity).inflate(R.layout.cell_comment_context_menu, parent, false);
                            ImageView icon  = (ImageView)convertView.findViewById(R.id.context_menu_icon);
                            TextView label  = (TextView)convertView.findViewById(R.id.context_menu_label);
                            
                            icon.setImageResource(data.get("icon"));
                            label.setText(data.get("label"));
                        }else{
                            convertView     = LayoutInflater.from(mActivity).inflate(R.layout.cell_comment_context_section, parent, false);
                            TextView label  = (TextView)convertView.findViewById(R.id.context_menu_section);
                            label.setText(data.get("label"));
                        }
                        convertView.setTag(data.get("label"));//leeboo 菜单文本
                        return convertView;
                    }

                    @Override
                    public int getItemViewType(int position) {
                        Map<String, Integer> data = (Map<String, Integer>)mMenus.get(position);
                        if(data.size()==1) return SECTION;
                        return MENU;
                    }

                    @Override
                    public int getViewTypeCount() {
                        return 2;
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        return getItemViewType(position)!=SECTION;
                    }
        };
        
        mListView = (ListView)mDlg.findViewById(R.id.context_menu_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mDlg.dismiss();
                if(view.getTag() == null)return;
                if((Integer)view.getTag() == R.string.remove){
                    //删除Reminder
                    handData(REMOVE_REMIND);
                    return;
                }
            }
        });
    }
    public void showReminderContextMenu(Reminder reminder) {
        mReminder = reminder;
        mMenus.clear();
        int[][] menusItems = new int[][]{
                {R.drawable.remove,         R.string.remove}
        };
        
        for (int[] is : menusItems) {
            Map<String, Integer> item = new HashMap<String, Integer>();
            if(is.length == 2){
                item.put("icon", is[0]);
                item.put("label", is[1]);
            }
            if(is.length == 1){
                item.put("label", is[0]);
            }
            mMenus.add(item);
        }
        mDlg.show();
        mAdapter.notifyDataSetChanged();
    }
    
    private void handData (int what) {
        Util.showLoading(mActivity, "删除中...");
        CallApiTask.doCallApi(what, this, mActivity);
    }
    
    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("uid", mApp.loginUser().id() + "");
        args.put("remind_id", mReminder.id() + "");
        return new Api("get",Util.URI_REMOVE_REMIND, args);
    }
    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }
    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(mActivity, mActivity.getString(R.string.network_error));
    }
    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }
    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
        Util.hideLoading();
        if( ! Util.checkResult(mActivity, result, "删除提醒失败！")){
            return;
        }
        Util.removeCacheAndUI(mApp, mReminder, mApp.loginUser().id());
        Util.removeLocalRemind(mActivity, mReminder.id() + "");
        Util.removeAlarm(mActivity, mReminder.id() + "");
        mChangeListener.reminderRemoved();
    }
    
    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
    public interface OnReminderChanged{
        void reminderRemoved();
    }
}
