package com.yidianhulian.ydmemo.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.widget.MemoUsers;
import com.yidianhulian.ydmemo.widget.MemoUsers.AddUserListener;

/**
 * startActivityForResult打开，添加成功后通过setresult返回新memo
 * 
 * @author leeboo
 *
 */
public class AddMemo extends Activity implements CallApiListener{
    public static final int API_ADD_MEMO = 1;
    public static final String RESULT_NEW_MEMO = "RESULT_NEW_MEMO";
    public static final String RESULT_NEW_USER = "RESULT_NEW_USER";
    public static final int REQUEST_FOR_NEW_MEMO = 2;
    
    private EditText mSubject;
    private Button mRemindDateBtn;
    private EditText mDesc;
    private Button mRemindAllBtn;
    private CheckBox mRemindChk;
    private MemoUsers mFollowers;
    private YDMemoApplication mApp;
    private AddUserListener mAddFollowerListener;
    private AlertDialog mDatePicker;
    private String mRemindDate;
    private DatePicker mRemindDatePicker;
    private TimePicker mRemindTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_memo);
        mApp = (YDMemoApplication)getApplication();
        
        mSubject      = (EditText)findViewById(R.id.add_memo_subject);
        mSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSubject.getText().length() > 100) {
                    Util.showToast(AddMemo.this, "备忘主题最多只能输入100个字符!");
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mDesc         = (EditText)findViewById(R.id.add_memo_desc);
        
        mRemindDateBtn= (Button)findViewById(R.id.add_memo_remind_date_btn);
        mRemindAllBtn = (Button)findViewById(R.id.add_memo_remind_all_btn);
        mRemindChk    = (CheckBox)findViewById(R.id.add_memo_remind_chk);
        View dialogView   = getLayoutInflater().inflate(R.layout.remind_date_picker, null);
        mRemindDatePicker   = (DatePicker)dialogView.findViewById(R.id.remind_date_datePicker);
        mRemindTimePicker   = (TimePicker)dialogView.findViewById(R.id.remind_date_timePicker);
        
        mRemindDatePicker.setMinDate((new Date()).getTime()-1000);
        AlertDialog.Builder builder = new AlertDialog.Builder(AddMemo.this);
        builder.setTitle(R.string.remind_date);
        builder.setView(dialogView);
        builder.setIcon(R.drawable.date_and_time);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRemindDate = String.format("%s-%s-%s %s:%s:00", mRemindDatePicker.getYear(),
                        mRemindDatePicker.getMonth()+1, mRemindDatePicker.getDayOfMonth(),
                        mRemindTimePicker.getCurrentHour(),mRemindTimePicker.getCurrentMinute());
                mRemindDateBtn.setText(mRemindDate);
                dialog.dismiss();   
            }
        });
        builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   
            }
        });
        builder.setPositiveButton(R.string.cancel_remind, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRemindDate = null;
                mRemindDateBtn.setText(R.string.touch_select_date);
                dialog.dismiss();   
            }
        });

        mDatePicker = builder.create();
        
        mRemindDateBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mDatePicker.show();
            }
        });
        
        mFollowers    = new MemoUsers(this, (ViewGroup)findViewById(R.id.add_memo_follower));
        
        mAddFollowerListener = new AddUserListener() {
            @Override
            public void onSelectedUser(MemoUsers view, List<User> users) {
                view.shown(true, false, users);
            }
            
            @Override
            public void onClick(MemoUsers memoUser) {
                ArrayList<User> users = new ArrayList<User>(memoUser.users());
                
                Intent intent = new Intent();
                intent.setClass(AddMemo.this, SelectContact.class);
                intent.putExtra(SelectContact.ARG_TITLE, getString(R.string.share_to_users));
                intent.putExtra("requestCode", SelectContact.REQUEST_FOR_MULTI_SELECT);
                intent.putParcelableArrayListExtra(
                        SelectContact.ARG_IGNORE_USERS, new ArrayList<User>(){{
                            add(mApp.loginUser());
                        }});
                intent.putParcelableArrayListExtra(SelectContact.ARG_SELECTED_USERS, users);
                startActivityForResult(intent, SelectContact.REQUEST_FOR_MULTI_SELECT);
            }
        };
        mFollowers.setAddUserListener(mAddFollowerListener);
        mFollowers.shown(true, false, null);
        
        mRemindAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRemindChk.setChecked( ! mRemindChk.isChecked());
            }
        });
        
        restoreActionBar();
    }
    
    

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null)return;
        if(requestCode == SelectContact.REQUEST_FOR_MULTI_SELECT){
            mAddFollowerListener.onSelectedUser(mFollowers, (List<User>)data.getSerializableExtra(SelectContact.RESULT_SELECTED_USERS));
        }
    }



    private void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.add_memo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.ok, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            Intent data = new Intent();
            setResult(getIntent().getIntExtra("requestCode", 0), data);
            finish();
            return true;
        }
        
        if(item.getItemId()==R.id.ok){
            saveMemo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void saveMemo() {
        String subject = mSubject.getText().toString();
        if(subject.isEmpty() || "".equals( subject ) ){
            Util.showToast(this, getString(R.string.memo_subject_is_empty));
            return;
        }
        if (subject.length() > 100) {
            Util.showToast(this, getString(R.string.memo_subject_is_too_long));return;
        }
        Util.showLoading(this, getString(R.string.please_waiting));
        CallApiTask.doCallApi(API_ADD_MEMO, this, getApplicationContext());
    }



    @Override
    public Api getApi(Context context, int what, Object... params) {
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("subject",     mSubject.getText().toString().trim());
        args.put("desc",        mDesc.getText().toString().trim());
        if(mRemindDate != null){
            args.put("remind_date", mRemindDate);
            args.put("remind_all",  mRemindChk.isChecked() ? "1" : "0");
        }
        
        User assigner = mApp.loginUser();
        args.put("assigner_name",       assigner.displayName());
        args.put("assigner_cellphone",  assigner.cellphone());
        
        List<User> followers = mFollowers.users();
        StringBuffer phone = new StringBuffer();
        StringBuffer name = new StringBuffer();
        for (User user : followers) {
            phone.append(user.cellphone());
            phone.append(",");
            
            name.append(user.displayName());
            name.append(",");
        }
        
        args.put("follower_cellphones", phone.toString());
        args.put("follower_names",      name.toString());
        
        return new Api("post",String.format("%s?uid=%s", Util.URI_POST_MEMO, mApp.loginUser().id()), args);
    }



    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone, Object... params) {
        Util.hideLoading();
        if( ! Util.checkResult(this, result, "增加备忘失败")){
            return;
        }
        JSONObject json     = Api.getJSONValue(result, "data", JSONObject.class);
        JSONArray  users    = Api.getJSONValue(result, "new_users", JSONArray.class);
        ArrayList<User> newUsers = new ArrayList<User>();
        if(users !=null && users.length()>0){
            for(int i=0; i<users.length(); i++){
                try {
                    newUsers.add(new User(mApp, users.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //建立本地提醒,@HuJinhao,@2014-10-30
        Memo memo = new Memo(Api.getJSONValue(json, "memo", JSONObject.class));
        if (mRemindDate != null) {         	        
        	Util.createAlarmReminder(this, memo.subject(), memo.reminders().get(0));        	
        }//end
        
        Intent data = new Intent();
        data.putExtra(RESULT_NEW_USER, newUsers);
        setResult(getIntent().getIntExtra("requestCode", 0), data);
        Util.updateCacheAndUI(context, memo, mApp.loginUser().id());
        finish();
    }

    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(this, getString(R.string.network_error));
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }

    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }

    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }
    
}
