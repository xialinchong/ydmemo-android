package com.yidianhulian.ydmemo.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;

/**
 * memo的用户显示，处理控件,
 * 
 * @author leeboo
 * 
 */
@SuppressLint("UseSparseArrays")
public class MemoUsers {
    protected static final int TYPE_ADD = 0;
    protected static final int TYPE_USER = 1;
    private ViewGroup mRoot;
    private GridView mUserGrid;
    private ArrayList<User> mUsers = new ArrayList<User>();
    private ArrayList<User> mCheckedUsers= new ArrayList<User>();
    private AddUserListener mListener;
    private Activity mContext;

    YDMemoApplication mApp;

    private Boolean mCanCheck;
    private ArrayAdapter<User> mAdapter;
    private boolean mCanAdd;
    /**
     * 
     * @param context
     * @param root include memo_users.xml的id
     */
    public MemoUsers(Activity context, ViewGroup root) {
        this.mContext = context;
        mApp = (YDMemoApplication) mContext.getApplication();
        mRoot = root;
        mAdapter = new ArrayAdapter<User>(context, R.layout.memo_user, mUsers){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Holder holder;
                
                if(convertView==null || 
                        (convertView.getTag()==null && getItemViewType(position)==TYPE_USER) ||
                        (getItemViewType(position)==TYPE_ADD) && convertView.getTag()!=null){
                    if(getItemViewType(position)==TYPE_ADD){
                        ImageButton btn = new ImageButton(mContext);
                        btn.setBackgroundResource(R.drawable.inverse_add_bg_selector);
                        btn.setImageResource(R.drawable.add_selector);
                        btn.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(mListener !=null) mListener.onClick(MemoUsers.this);
                            }
                        });
                        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                                AbsListView.LayoutParams.WRAP_CONTENT, com.yidianhulian.framework.Util.dip2px(mContext, 64));
                        btn.setLayoutParams(lp);
                        return btn;
                    }
                    
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.memo_user, parent, false);
                    holder= new Holder();
                    holder.avatar   = (ImageView)convertView.findViewById(R.id.memo_user_avatar);
                    holder.pending  = (ImageView)convertView.findViewById(R.id.memo_user_pending);
                    holder.checkBox = (CheckBox)convertView.findViewById(R.id.memo_user_checked);
                    holder.name     = (TextView)convertView.findViewById(R.id.memo_user_name);
                    holder.avatarPress     = (Button)convertView.findViewById(R.id.avatar_btn_press);
                    
                    holder.avatarPress.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mCanCheck) {
                                CheckBox cb = (CheckBox) ((ViewGroup) v.getParent())
                                        .findViewById(R.id.memo_user_checked);
                                cb.setChecked( ! cb.isChecked());
                                User user = (User)v.getTag();
                                if(cb.isChecked() && mCheckedUsers.indexOf(user) == -1){
                                    mCheckedUsers.add(user);
                                }else if( ! cb.isChecked()){
                                    mCheckedUsers.remove(user);
                                }
                                return;
                            }

                            Util.showContact((User)v.getTag(), mContext);
                        }

                    });
                    convertView.setTag(holder);
                }else{
                    
                    if(getItemViewType(position)==TYPE_ADD){
                        return convertView;
                    }
                    
                    holder = (Holder)convertView.getTag();
                }
                
                User user = getItem(position);
                user.initLocalUserInfo(mApp);
                holder.avatarPress.setTag(user);
                holder.avatarPress.setEnabled(mCanCheck || mApp.loginUser().id()!=user.id());
                holder.checkBox.setVisibility(mCanCheck ? View.VISIBLE : View.INVISIBLE);
                holder.name.setText(user.displayName());
 
                holder.checkBox.setChecked(mCheckedUsers.indexOf(user)!=-1);
                
                Util.loadAvatar(mApp, user, holder.avatar);
                if( !Util.isEmpty( user.status() ) && ! "accept".equals(user.status())){
                    holder.pending.setVisibility(View.VISIBLE);
                }else{
                    holder.pending.setVisibility(View.GONE);
                }
                return convertView;
            }

            @Override
            public User getItem(int position) {
                if(mCanAdd && position==0)return null;
                try{
                    if(mCanAdd) return mUsers.get(position -1 ) ;
                }catch(Exception e){
                    e.printStackTrace();
                    return null;
                }
                return  mUsers.get(position) ;
            }

            @Override
            public int getCount() {
                return mUsers.size() + (mCanAdd ? 1 : 0);
            }

            @Override
            public int getItemViewType(int position) {
                if(mCanAdd && position==0)return TYPE_ADD;
                return TYPE_USER;
            }

            @Override
            public int getViewTypeCount() {
                if(mCanAdd)return 2;
                return 1;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
            
        };
        mUserGrid = (GridView) mRoot.findViewById(R.id.memo_users);
        mUserGrid.setAdapter(mAdapter);
    }
    
    public void shown(boolean canAdd, boolean canCheck, List<User> users, List<User> checkedUsers) {
        mCheckedUsers.addAll(checkedUsers);
        shown(canAdd, canCheck, users);
    }
    
    public void shown(boolean canAdd, boolean canCheck, List<User> users) {
        mUsers.clear();
        mCanAdd = canAdd;
        if (users != null) mUsers.addAll(users);
        mAdapter.notifyDataSetChanged();
        
        mCanCheck = canCheck;
    }

    public ArrayList<User> users() {
        return mUsers;
    }

    public List<User> checkedUser() {
        return mCheckedUsers;
    }

    public void setAddUserListener(AddUserListener listener) {
        mListener = listener;
    }

    public interface AddUserListener {
        public void onClick(MemoUsers memoUser);

        public void onSelectedUser(MemoUsers view, List<User> users);
    }
    

    
    class Holder{
        protected Button avatarPress;
        ImageView avatar;
        ImageView pending;
        TextView name;
        CheckBox checkBox;
    }
}
