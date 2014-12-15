package com.yidianhulian.ydmemo.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;

/**
 * 留言的标记用户列表
 * 
 * @author leeboo
 * 
 */
@SuppressLint("UseSparseArrays")
public class TraceUsers {
    private GridView mUserGrid;
    private ArrayList<User> mUsers = new ArrayList<User>();
    private Activity mContext;

    YDMemoApplication mApp;

    private ArrayAdapter<User> mAdapter;

    public TraceUsers(Activity context, GridView gridView) {
        this.mContext = context;
        mUserGrid = gridView;
        mApp = (YDMemoApplication) mContext.getApplication();
        mAdapter = new ArrayAdapter<User>(context, R.layout.trace_user, mUsers){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Holder holder;
                
                if(convertView==null){
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.trace_user, parent, false);
                    holder= new Holder();
                    holder.avatar   = (ImageView)convertView.findViewById(R.id.trace_user_avatar);
                    holder.markType = (ImageView)convertView.findViewById(R.id.trace_user_type);
                    holder.avatarPress     = (Button)convertView.findViewById(R.id.trace_user_btn);
                    
                    holder.avatarPress.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            User user = (User)v.getTag();
                            if(user.id() != mApp.loginUser().id()){
                                Util.showContact(user, mContext);
                            }
                        }

                    });
                    convertView.setTag(holder);
                }else{
                    holder = (Holder)convertView.getTag();
                }
                
                User user = getItem(position);
                user.initLocalUserInfo(mApp);
                holder.avatarPress.setTag(user);
                holder.markType.setImageResource(Util.getDrawableIdByName(user.traceType()));
                                
                Util.loadAvatar(mApp, user, holder.avatar);
                return convertView;
            }

            @Override
            public User getItem(int position) {
                return  mUsers.get(position) ;
            }

            @Override
            public int getCount() {
                return mUsers.size();
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
            
        };
        mUserGrid.setAdapter(mAdapter);
    }
    
    public View getView(){
        return mUserGrid;
    }

    public void shown(List<User> users) {
        mUsers.clear();
        if (users != null) mUsers.addAll(users);
        mAdapter.notifyDataSetChanged();
        
    }

    public ArrayList<User> users() {
        return mUsers;
    }


    
    class Holder{
        protected Button avatarPress;
        ImageView avatar;
        ImageView markType;
    }
}
