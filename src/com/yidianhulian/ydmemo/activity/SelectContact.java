package com.yidianhulian.ydmemo.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.ydmemo.PinYin;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.view.PinnedHeaderListAdapter;
import com.yidianhulian.ydmemo.view.PinnedHeaderListView;
import com.yidianhulian.ydmemo.view.SideBar;
import com.yidianhulian.ydmemo.view.YDGroupListView;
import com.yidianhulian.ydmemo.view.YDGroupListView.YDGroupListViewDelegate;
/**
 * 该activity会通过startActivityForResult打开，
 * requestCode为REQUEST_FOR_MULTI_SELECT 或 REQUEST_FOR_SINGLE_SELECT。分别表示单选及多选
 * <br/>
 * 该activity的标题通过ARG_TITLE传递过来,同时ARG_IGNORE_USERS可以指定忽略的用户，忽略的用户不显示出来,
 * ,ARG_JUST_USERS可以指定只显示的用户，其他的用户不显示出来
 * <br/>
 * 用户选择后可通过RESULT_SELECTED_USERS作为intent的键值通过setActivityResult返回数据
 * 传递及返回的数据是List&lt;User&gt;
 *
 * 
 * @author leeboo
 *
 */
@SuppressLint({ "InflateParams", "DefaultLocale" })
public class SelectContact extends Activity implements CallApiListener {
    /**
     * 单选
     */
    public static final int REQUEST_FOR_MULTI_SELECT = 1;
    /**
     * 多选
     */
    public static final int REQUEST_FOR_SINGLE_SELECT = 2;
    /**
     * 邀请朋友
     */
    public static final int REQUEST_FOR_INVITE = 3;
    /**
     * 传递页面标题
     */
    public static final String ARG_TITLE = "title";
    /**
     * 传递忽略的用户，不显示出来
     */
    public static final String ARG_IGNORE_USERS = "ARG_IGNORE_USERS";
    /**
     * 传递显示的用户，其他不显示出来
     */
    public static final String ARG_JUST_USERS = "ARG_JUST_USERS";
    /**
     * 传递默认选中的用户，
     */
    public static final String ARG_SELECTED_USERS = "ARG_SELECTED_USERS";
    /**
     * 返回选中的用户结果
     */
    public static final String RESULT_SELECTED_USERS = "RESULT_SELECTED_USERS";
    private YDGroupListView mContactList;
    private int mRequestCode;
    private List<Object> mRawContacts = new ArrayList<Object>();

    private Map<String, User> mUsedUsers = new HashMap<String, User>();
    private ArrayList<User> mSelectedUsers = new ArrayList<User>();
    private YDMemoApplication mApp;
    private ViewGroup mEmptyContact;
    private EditText mSearchField;
    private MyHandler mHandler;
    protected static final int NO_RESULT = 1;
    protected static final int HAS_RESULT = 2;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.select_contact);
       mApp = (YDMemoApplication)getApplication();
       mRequestCode = getIntent().getIntExtra("request", REQUEST_FOR_MULTI_SELECT);
       mHandler = new MyHandler(this, mApp);
       mSearchField = (EditText)findViewById(R.id.search_field);
       mSearchField.addTextChangedListener(new TextWatcher() {
           @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
                searchContact(mSearchField.getText().toString());
            }
        });
       
       PinnedHeaderListView listview = (PinnedHeaderListView)findViewById(R.id.contact_list);
       mEmptyContact = (ViewGroup)findViewById(R.id.empty_contact);
       mContactList = new YDGroupListView(this, mRawContacts,
               listview, 
               (SideBar) findViewById(R.id.contact_sidebar),
               (ViewGroup)LayoutInflater.from(this).inflate(R.layout.section, listview, false)
               );
       mContactList.setDelegate(new YDGroupListViewDelegate() {
            
           
            @Override
            public String[] sideBarSections() {
                return new String[]{ "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                    "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                    "Y", "Z" };
            }
            
            public void formatData(YDGroupListView listview, List<Object> rawDatas){
                List<String> sections           = new ArrayList<String>();
                List<Integer> sectionIndexs     = new ArrayList<Integer>();
                Map<String, Integer> keyIndexs  = new HashMap<String, Integer>();
                List<Object> contacts           = new ArrayList<Object>();
                
                if(rawDatas!=null){
                    Map<String, List<User>> keyAndContacts = new HashMap<String, List<User>>();
                    for (Object obj : rawDatas) {
                        User user = (User) obj;
                        String displayName = user.displayName();
                        String key;
                        if(displayName==null || displayName.isEmpty()){
                            key = "#";
                        }else{
                            char firstChar = displayName.trim().charAt(0);
                            if(firstChar >=97 && firstChar <=122 || firstChar >= 65 && firstChar <= 90){
                                key = String.valueOf(firstChar).toUpperCase();
                            }else{
                                key = String.valueOf(PinYin.pinyinFirstLetter(firstChar)).toUpperCase();
                            }
                        }
                        
                        
                        List<User> users = keyAndContacts.get(key);
                        if(users == null)users = new ArrayList<User>();
                        users.add(user);
                        
                        keyAndContacts.put(key, users);
                        if(sections.indexOf(key) == -1)sections.add(key);
                    }
                    
                    Collections.sort(sections);
                    for (String key : sections) {
                        key = key.toUpperCase();
                        contacts.add(key);
                        sectionIndexs.add(contacts.size() - 1);
                        keyIndexs.put(key, contacts.size() - 1);
                        for(User user: keyAndContacts.get(key)){
                            contacts.add(user);
                        }
                    }
                }
                
                listview.setDatas(contacts);
                listview.setKeyIndexs(keyIndexs);
                listview.setSectionIndexs(sectionIndexs);
                listview.setSections(sections);
            }

            @Override
            public PinnedHeaderListAdapter getAdapter() {
                return new PinnedHeaderListAdapter(SelectContact.this, mContactList, mContactList.getDatas()){

                    @Override
                    protected View getSectionView(int position,
                            View convertView, ViewGroup parent) {
                        SectionHolder holder;        
                        if(convertView==null || !(convertView.getTag() instanceof SectionHolder)){
                            convertView = LayoutInflater.from(mContext).inflate(R.layout.section, null);
                            holder = new SectionHolder();
                            holder.title = (TextView)convertView.findViewById(R.id.section_title);
                            
                            convertView.setTag(holder);
                        }else{
                            holder = (SectionHolder)convertView.getTag();
                        }
                        
                        holder.title.setText(getItem(position).toString().toUpperCase());
                        return convertView;
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        if(getItemViewType(position) == TYPE_SECTION)return false;
                        return super.isEnabled(position);
                    }

                    @Override
                    protected View getNormalView(int position,
                            View convertView, ViewGroup parent) {
                        ViewHolder holder;        
                        if(convertView==null || !(convertView.getTag() instanceof ViewHolder)){
                            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_contact, null);
                            holder = new ViewHolder();
                            holder.cellphone = (TextView)convertView.findViewById(R.id.contact_cellphone);
                            holder.name      = (TextView)convertView.findViewById(R.id.contact_name);
                            holder.avatar    = (ImageView)convertView.findViewById(R.id.contact_avatar);
                            holder.checkbox  = (CheckBox)convertView.findViewById(R.id.contact_chk);
                            holder.contactTip= (TextView)convertView.findViewById(R.id.contact_tip);
                            
                            convertView.setTag(holder);
                        }else{
                            holder = (ViewHolder)convertView.getTag();
                        }
                        
                        User user = (User)mListView.getDatas().get(position);//本地通讯录数据
                        User usedUser = mUsedUsers.get(user.cellphone());//注册的用户数据
                        if(usedUser !=null){
                            usedUser.initLocalUserInfo(user);
                            user = usedUser;
                        }
                        
                        holder.cellphone.setText(user.cellphone());
                        holder.name.setText(user.displayName());
                        
                        Util.loadAvatar(mApp, user, holder.avatar);
                        
                        holder.checkbox.setChecked(mSelectedUsers.indexOf(user)!=-1);
                        holder.checkbox.setVisibility(View.VISIBLE);
                        holder.contactTip.setVisibility(View.INVISIBLE);
                        
                        if(mRequestCode == REQUEST_FOR_INVITE){
                            if(mUsedUsers.get(user.cellphone()) != null){
                                holder.checkbox.setVisibility(View.INVISIBLE);
                                holder.contactTip.setVisibility(View.VISIBLE);
                                holder.contactTip.setText("已注册");
                            }
                        }
                        return convertView;
                    }

                    @Override
                    public void configurePinnedHeader(View header,
                            int position, int alpha) {
                        int realPosition = position;
                        int section = getSectionForPosition(realPosition);
                        if(section == -1)return;
                        String title = (String) getSections()[section];
                        ((TextView) header.findViewById(R.id.section_title))
                                .setText(title.toUpperCase());
                    }
                };
            }
            
            
            @Override
            public com.yidianhulian.ydmemo.view.SideBar.OnItemClickListener sideBarClickListener() {
                return new SideBar.OnItemClickListener() {
                    @Override
                    public void onItemClick(String s) {
                        if (mContactList.getKeyIndexs().get(s) != null) {
                            mContactList.getListView().setSelection(mContactList.getKeyIndexs().get(s));
                        } 
                    }
                };
            }
            @Override
            public OnScrollListener listScrollListener() {
                return new OnScrollListener() {
    
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        // ignore
                    }
    
    
                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem,
                            int visibleItemCount, int totalItemCount) {
                        if (view instanceof PinnedHeaderListView) {
                            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
                        }
                    }
                };
            }
            
            @Override
            public OnItemClickListener listItemClickListener() {
                return new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        ViewHolder holder = (ViewHolder)view.getTag();
                        if(holder.checkbox.getVisibility()==View.INVISIBLE)return;
                        
                        holder.checkbox.setChecked( ! holder.checkbox.isChecked());
                        User user = (User)mContactList.getDatas().get(position);//本地通讯录数据
                        User usedUser = mUsedUsers.get(user.cellphone());//注册的用户数据
                        
                        if(usedUser !=null )user = usedUser;
                        
                        if(holder.checkbox.isChecked()){
                            mSelectedUsers.add(user);
                        }else if(mSelectedUsers.indexOf(user) != -1){
                            mSelectedUsers.remove(user);
                        }
                        updateTitle();
                    }
                };
            }
            
        });

       updateTitle();
       
       searchContact(null);
    }



    private void updateTitle() {
        ActionBar actionbar = getActionBar();
        if(mSelectedUsers.size()==0){
            actionbar.setTitle(getIntent().getStringExtra(ARG_TITLE));
        }else{
            actionbar.setTitle(String.format("%s (%s 人)", getIntent().getStringExtra(ARG_TITLE), mSelectedUsers.size()));
        }
        actionbar.setIcon(R.drawable.logo);
        actionbar.setDisplayHomeAsUpEnabled(true);
      
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.ok, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home ){
            finish();
        }
        if(item.getItemId()==R.id.ok){
            selectDone();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchContact(final String name){
        new Thread("Filter Contact"){
            public void run() {
                mRawContacts.clear();
                
                List<User> selectedUsers = getIntent().getParcelableArrayListExtra(ARG_SELECTED_USERS);
                List<User> ignores       = getIntent().getParcelableArrayListExtra(ARG_IGNORE_USERS);
                List<User> shownUsers    = getIntent().getParcelableArrayListExtra(ARG_JUST_USERS);
                
                Map<String, User> users;
                if(shownUsers!=null && shownUsers.size()>0){
                    users = new HashMap<String, User>();
                    for (User user : shownUsers) {
                        users.put(user.cellphone(), user);
                    }
                }else{
                    users  = mApp.localContacts();
                }
                

                Map<String, User> filtered = new HashMap<String, User>();
                if( ! Util.isEmpty(name) && users!=null){
                    for (Iterator<User> iterator = users.values().iterator(); iterator.hasNext();) {
                        User user = iterator.next();
                        if(user.displayName().contains(name) || user.cellphone().contains(name)){
                            filtered.put(user.cellphone(), user);
                        }
                    }
                }else{
                    filtered = users;
                }
                
                
                if(filtered !=null && filtered.size()>0){
                    
                    if(ignores != null){
                        for (User user : filtered.values()) {
                            if( ignores.indexOf(user) ==-1 ) mRawContacts.add(user);
                        }
                    }else{
                        mRawContacts.addAll(filtered.values());
                    }
                    if(selectedUsers !=null)mSelectedUsers.addAll(selectedUsers);
                    
                    mHandler.sendEmptyMessage(HAS_RESULT);
                 }else{
                    mHandler.sendEmptyMessage(NO_RESULT);
                 }
            };
        }.start();
        
    }



    private void selectDone() {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(RESULT_SELECTED_USERS, mSelectedUsers);
        setResult(mRequestCode, data);
        finish();
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> args = new HashMap<String, String>();
        StringBuffer phones = new StringBuffer();
        for (Object user : mRawContacts) {
            phones.append(((User)user).cellphone());
            phones.append(",");
        }
        args.put("u", phones.toString());
        return new Api("post", Util.URI_CHECK_SIGNUP_USER, args);
    }


    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }


    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return Util.URI_CHECK_SIGNUP_USER;
    }


    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
        mUsedUsers.clear();
        if( ! Util.checkResult(result)){
            return;
        }
        JSONArray datas = Api.getJSONValue(result, "data", JSONArray.class);
        for (int i = 0; i < datas.length(); i++) {
            try {
                User user = new User(datas.getJSONObject(i));
                mUsedUsers.put(user.cellphone(), user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mContactList.refresh();
    }

    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return from;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(this, getString(R.string.network_error));
    }

    static class MyHandler extends Handler{
            WeakReference<SelectContact> mActivity;
            WeakReference<YDMemoApplication> mApp;
            public MyHandler(SelectContact activity, YDMemoApplication app) {
                mActivity = new WeakReference<SelectContact>(activity);
                mApp = new WeakReference<YDMemoApplication>(app);
            }
            @Override
            public void handleMessage(Message msg) {
                SelectContact my = mActivity.get();
                if(msg.what==HAS_RESULT){
                    my.mContactList.refresh();
                    my.mEmptyContact.setVisibility(View.GONE);

                    if(my.mRawContacts.size() > 0){
                        CallApiTask.doCallApi(0, my,  my, CacheType.REPLACE, FetchType.FETCH_CACHE_AWAYS_API);
                    }
                }else if(msg.what == NO_RESULT){
                    my.mContactList.refresh();
                    my.mEmptyContact.setVisibility(View.VISIBLE);
                }
            }
    }
    
    class ViewHolder {
        public TextView cellphone;
        public TextView name;
        public ImageView avatar;
        public CheckBox checkbox;
        public TextView contactTip;
    }
    
    class SectionHolder {
        public TextView title;
    }
}
