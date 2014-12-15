package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.widget.CommentView;
import com.yidianhulian.ydmemo.widget.CommentView.OnCommentClick;

@SuppressLint({ "InflateParams", "UseSparseArrays" })
public class MemoCommentsAdapter extends BaseAdapter {
    private Activity mContext;
    private List<Comment> mComments = new ArrayList<Comment>();
    private User mCurrUser;
    public static final int TYPE_MINE = 0;
    public static final int TYPE_OTHER = 1;
    private YDMemoApplication mApp;
    private OnCommentClick mCommentClickListener;
    private Memo mMemo;
    
    public MemoCommentsAdapter(Activity context, List<Comment> comments, 
            OnCommentClick commentClickListener) {
        super();
        if(comments==null){
            this.mComments  = new ArrayList<Comment>();
        }else{
            this.mComments  = comments;
        }
        mApp            = (YDMemoApplication)context.getApplication();
        this.mCurrUser  = mApp.loginUser();
        this.mContext   = context;
        mCommentClickListener = commentClickListener;
    }
    
    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setMemo(Memo memo){
        mMemo = memo;
    }
    
    public int getViewTypeCount() {
        return 2;
    };

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public int getItemViewType(int position) {
        Comment comment = (Comment)getItem(position);
        if(comment.commenter().id() == mCurrUser.id()) return  TYPE_MINE;
        return  TYPE_OTHER;
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private View noContentView(int position, View convertView, ViewGroup root) {
        return LayoutInflater.from(mContext).inflate(R.layout.empty_comment, null);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment comment = (Comment)getItem(position);
        
        if(comment.id()==-1l){
            return noContentView(position, convertView, parent);
        }
        
        CommentView commentView;
        
        if(convertView==null || /*empty view*/convertView.getTag()==null){//第一次创建 leeboo
            commentView = new CommentView(mContext, mMemo, mCommentClickListener);
            commentView.shown(comment);
            return commentView.getView();
        }
        
        //相同类型的view重用 leeboo
        commentView = new CommentView(mContext, (ViewGroup)convertView, mMemo, mCommentClickListener);
        commentView.shown(comment);
        
        return convertView;
    }
}
