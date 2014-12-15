package com.yidianhulian.ydmemo.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.yidianhulian.framework.ImageLoader;
import com.yidianhulian.ydmemo.CommentContextHandler;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.ReplyComment;
import com.yidianhulian.ydmemo.activity.ShowBigImage;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.User;
/**
 * 留言包含了所有的留言内容及他所回复的留言
 * @author leeboo
 *
 */
public class CommentView implements CommentContextHandler.OnCommentChanged{
    private Activity mContext;
    private ViewGroup mView;
    private YDMemoApplication mApp;
    private PlaceHolder mCommentHolder = new PlaceHolder();
    private CommentContextHandler contextHandler;
    private Comment mComment;
    private Memo mMemo;
    private OnClickListener mPicClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Comment comment = (Comment)v.getTag();
            if(comment ==null || comment.needRepost() || comment.getPostToken()!=null)return;
            
            Intent intent = new Intent();
            intent.setClass(mApp, ShowBigImage.class);
            //leeboo 20141204 修改为传对象
            intent.putExtra(ShowBigImage.ARG_COMMENT, comment);
            intent.putExtra(ShowBigImage.ARG_MEMO, mMemo);
            mContext.startActivity(intent);
        }
    };
    private OnClickListener mDefaultClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Comment comment = (Comment)v.getTag();
            if(comment==null)return;
            
            if(mClickListener!=null){
                boolean rst = mClickListener.onClick(comment);
                if(rst)return;
            }            
            if(comment.needRepost() || comment.getPostToken()!=null)return;
            contextHandler.showCommentContextMenu(comment);
        }
    };
    private OnCommentClick mClickListener;

    /**
     * viewgroup 必须设置好tag，tag为placeholder；这通常用于在adapter中重用convertView
     * 
     * @param context
     * @param view
     * @param clickListener
     */
    public CommentView(Activity context, ViewGroup view, Memo memo, OnCommentClick clickListener) {
        this.mContext = context;
        this.mView = view;
        mApp = (YDMemoApplication)context.getApplication();
        mMemo = memo;
        mCommentHolder = (PlaceHolder)view.getTag();
        contextHandler = new CommentContextHandler(mContext, memo, this);
        
        mClickListener = clickListener;
    }
    public CommentView(Activity context, Memo memo, OnCommentClick clickListener) {
        super();
        this.mContext = context;
        mApp = (YDMemoApplication)context.getApplication();
        mMemo = memo;
        contextHandler = new CommentContextHandler(mContext, memo, this);
        
        
        mView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.cell_comment, null);
        mCommentHolder.commentContent    = (ViewGroup) mView.findViewById(R.id.comment_content);
        mCommentHolder.replyContent     = (ViewGroup)mView.findViewById(R.id.reply_comment_content);
        //文本内容
        initTextComment();
        //图片内容
        initPicComment();
        
        mCommentHolder.commentAction    = (Button) mView.findViewById(R.id.comment_action);
        mCommentHolder.commentAction.setOnClickListener(mDefaultClickListener);
        mCommentHolder.commentAvatar    = (ImageView) mView.findViewById(R.id.commenter_avatar);
        mCommentHolder.userName         = (TextView) mView.findViewById(R.id.user_name);
        mCommentHolder.userAvatar       = (Button) mView.findViewById(R.id.avatar_btn);
        mCommentHolder.needRepost       = (ImageView) mView.findViewById(R.id.need_repost);
        mCommentHolder.commentDate      = (TextView) mView.findViewById(R.id.date);
        mCommentHolder.traceUsers       = new TraceUsers(mContext, (GridView) mView.findViewById(R.id.trace_users));
        
        
        mClickListener = clickListener;
        mView.setTag(mCommentHolder);
    }
    
    private void initPicComment() {
        mCommentHolder.picContent       = (ImageView) mCommentHolder.commentContent.findViewById(R.id.pic);
        mCommentHolder.replyPicContent  = (ImageView) mCommentHolder.replyContent.findViewById(R.id.pic);
        //图片点击事件 xialinchong @2014-11-28
        mCommentHolder.picContent.setOnClickListener(mPicClickListener);
        mCommentHolder.replyPicContent.setOnClickListener(mPicClickListener);

        mCommentHolder.picContainer     = (ViewGroup) mCommentHolder.commentContent.findViewById(R.id.pic_comment);
        mCommentHolder.replyPicContainer     = (ViewGroup) mCommentHolder.replyContent.findViewById(R.id.pic_comment);
        
        mCommentHolder.picProgress      = (TextView) mCommentHolder.commentContent.findViewById(R.id.pic_progress);
    }
    private void initTextComment() {
        mCommentHolder.textContent    = (TextView) mCommentHolder.commentContent.findViewById(R.id.comment);
        mCommentHolder.textContainer  = (ViewGroup) mCommentHolder.commentContent.findViewById(R.id.text_comment);
        
        mCommentHolder.replyTextContent    = (TextView) mCommentHolder.replyContent.findViewById(R.id.comment);
        mCommentHolder.replyTextContainer  = (ViewGroup) mCommentHolder.replyContent.findViewById(R.id.text_comment);
    }

    /**
     * 更新界面数据
     * @param comment
     */
    public void shown(Comment comment) {
        mComment = comment;
        User commenter = comment.commenter();
        if(commenter==null)return;
        
        commenter.initLocalUserInfo(mApp);
        mCommentHolder.commentIsMine = mApp.loginUser().id()==commenter.id();
        
        
        //leeboo 是否需要重发处理
        if(comment.getPostToken() == null){//发送成功
            mCommentHolder.needRepost.setVisibility(View.GONE);
            mCommentHolder.commentDate.setText( Util.dateFormat(mContext, comment.date()));
        }else if(comment.needRepost()){//发送失败，需要重发
            mCommentHolder.needRepost.setImageResource(R.drawable.alert);
            mCommentHolder.needRepost.setVisibility(View.VISIBLE);
            mCommentHolder.commentDate.setText(R.string.post_comment_error);
        }else{//发送中
            mCommentHolder.needRepost.setVisibility(View.GONE);
            mCommentHolder.commentDate.setText(R.string.posting);
        }
        
        
        //leeboo 作者头像
        mCommentHolder.userAvatar.setTag(commenter);
        mCommentHolder.userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showContact((User)v.getTag(), mContext);
            }
            
        });
        User loginUser = mApp.loginUser();
        mCommentHolder.userAvatar.setEnabled(loginUser.id()!=commenter.id());
        mCommentHolder.userName.setText(commenter.displayName()+":");
        
        Comment reply = mComment.getReply();
        if( reply!=null ){
            mCommentHolder.userName.setText(commenter.displayName()+" 回复 "+reply.commenter().displayName()+": ");
        }else{
            mCommentHolder.userName.setText(commenter.displayName()+":");
        }
        
        Util.loadAvatar(mApp, commenter, mCommentHolder.commentAvatar);

        mCommentHolder.commentAction.setTag(mComment);
        
        
        if("pic".equals(mComment.fileType())){
            shownPic();
        }else{
            shownText();
        }

        mCommentHolder.showTraceUsers(mComment.fileType(), comment.getTraceUsers());
        
        mCommentHolder.replyContent.setVisibility(View.GONE);
        if(reply!=null){
            mCommentHolder.replyContent.setVisibility(View.VISIBLE);
            if("pic".equals(reply.fileType())){
                shownReplyPic();
            }else{
                shownReplyText();
            }
        }
    }
    
    
    /**
     * 显示文字
     * @param comment
     */
    private void shownText() {
    	//隐藏图片区域,显示文本,@HuJinhao,@2014-11-27
    	mCommentHolder.picContainer.setVisibility(View.GONE);
    	mCommentHolder.textContainer.setVisibility(View.VISIBLE);
    	mCommentHolder.textContent.setText(Util.showAtUser(mApp, 
    	        mComment.comment(), formatFollowers()));

    }
    /**
     * 显示文字
     * @param comment
     */
    private void shownReplyText() {
        mCommentHolder.replyPicContainer.setVisibility(View.GONE);
        mCommentHolder.replyTextContainer.setVisibility(View.VISIBLE);
        mCommentHolder.replyTextContent.setText(Util.showAtUser(mApp, mComment.getReply().comment(), formatFollowers()));

    }
    private Map<String, User> formatFollowers(){
        Map<String, User> users = new HashMap<String, User>();
        if(mMemo==null) return users;
        ArrayList<User> followers = (ArrayList<User>)mMemo.followers();
        followers.add(mMemo.assigner());
        for (User user : followers) {
            users.put(user.cellphone(), user);
        }
        return users;
    }
    
    /**
     * 显示图片
     * @param comment
     */
    private void shownPic() {
    	//需加上显示图片,否则上传的图片显示不出来,@HuJinhao,@2014-11-27
    	mCommentHolder.picContainer.setVisibility(View.VISIBLE);
    	mCommentHolder.textContainer.setVisibility(View.GONE);
    	
        /**
         * @存放图片路劲
         * @author xialinchong
         * @2014-11-28
         * @mComment.filePath()是小图路劲，需要增加大图路劲
         */
        ImageLoader loader = new ImageLoader(mApp);
        mCommentHolder.picContent.setTag(mComment);
        loader.loadImage(mCommentHolder.picContent, mComment.filePath());
        
        if(mComment.getPostToken() == null || mComment.needRepost()){//发送成功;发送失败，需要重发
            mCommentHolder.picProgress.setVisibility(View.GONE);
        }else{//发送中
            mCommentHolder.picProgress.setVisibility(View.VISIBLE);
            if(mComment.uploadProgress() >=1.0f){
                mCommentHolder.picProgress.setText(mContext.getString(R.string.please_waiting));
            }else{
                mCommentHolder.picProgress.setText(String.format("%d%%", (int)(mComment.uploadProgress()*100)));
            }
        }
        
    }
    
    private void shownReplyPic() {
        mCommentHolder.replyPicContainer.setVisibility(View.VISIBLE);
        mCommentHolder.replyTextContainer.setVisibility(View.GONE);
        ImageLoader loader = new ImageLoader(mApp);
        Comment reply = mComment.getReply();
        mCommentHolder.replyPicContent.setTag(reply);
        loader.loadImage(mCommentHolder.replyPicContent, reply.filePath());
        
    }


    public ViewGroup getView(){
        return mView;
    }

    public class PlaceHolder {
        
        public TextView picProgress;
        public ImageView picContent;
        ViewGroup picContainer;
        
        public ViewGroup textContainer;
        
        
        TraceUsers traceUsers;
        public Button commentAction;
        TextView commentDate;
        ImageView commentAvatar;
        TextView userName;
        Button userAvatar;
        ImageView needRepost;
        boolean commentIsMine;
        public TextView textContent;
        
        ViewGroup replyContent;
        ViewGroup commentContent;

        public ViewGroup replyTextContainer;
        public TextView replyTextContent;
        public ViewGroup replyPicContainer;
        public ImageView replyPicContent;

        public void showTraceUsers(String fileType, Map<Long, User> traceUsers2) {
            if(traceUsers2.size()==0){
                traceUsers.getView().setVisibility(View.GONE);return;
            }
            traceUsers.getView().setVisibility(View.VISIBLE);
            traceUsers.shown(new ArrayList<User>(traceUsers2.values()));
        }
    }

    @Override
    public void beforeChange(Comment comment) {
        mCommentHolder.needRepost.setImageResource(R.drawable.busy);
        mCommentHolder.needRepost.setVisibility(View.VISIBLE);
    }
    @Override
    public void changeFail(Comment comment) {
        mCommentHolder.needRepost.setVisibility(View.GONE);
        Util.showToast(mContext, mApp.getString(R.string.data_post_failed));
    }
    
    /**
     * 留言点击事件
     * @author leeboo
     *
     */
    public interface OnCommentClick{
        /**
         * 如果不期望事件传递，返回true
         * @return
         */
        public boolean onClick(Comment comment);
    }
}
