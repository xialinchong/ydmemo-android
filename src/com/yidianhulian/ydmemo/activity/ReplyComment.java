package com.yidianhulian.ydmemo.activity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.widget.CommentView;
/**
 * 回复某条留言
 * 传入参数：comment：被回复留言对象; Memo
 * @author leeboo
 *
 */
public class ReplyComment extends Activity implements CallApiListener {
    public static final String COMMENT = "comment";
    public static final String MEMO = "memo";
    /**
     * 提交留言
     */
    private static final int API_POST_COMMENT = 1;
    
    private Comment mComment;
    private YDMemoApplication mApp;
    private ViewGroup commentPanel;
    private EditText mNewComment;
    private Memo mMemo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reply_comment);
        mApp = (YDMemoApplication)getApplication();
        
        commentPanel = (ViewGroup)findViewById(R.id.ori_comment);
        mNewComment = (EditText)findViewById(R.id.reply_comment);
        
        mComment = (Comment)getIntent().getParcelableExtra(COMMENT);
        mMemo = (Memo)getIntent().getParcelableExtra(MEMO);
        if(mComment==null){
            Util.showToast(this, getString(R.string.comment_not_found));
            finish();
            return;
        }
        
        shown();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.reply_comment);
        
    }
    
    private void shown(){
        CommentView commentView = new CommentView(this,mMemo, null);
        commentPanel.removeAllViews();
        commentPanel.addView(commentView.getView());
        commentView.shown(mComment);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.ok, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }else if(item.getItemId()==R.id.ok){
        	String comment = mNewComment.getText().toString().trim();
            if (comment.isEmpty()) {
                Util.showToast(ReplyComment.this, getResources().getString(R.string.please_input_something));
                return false;
            }
            
            Util.showLoading(ReplyComment.this, getResources().getString(R.string.data_in_processing));
            
            Map<String, String> args = new HashMap<String, String>();
            args.put("reply_to", String.valueOf(mComment.id()));
            args.put("comment", comment);
            args.put("post_token", String.valueOf((new Date()).getTime()));
            
            CallApiTask.doCallApi(API_POST_COMMENT, ReplyComment.this, ReplyComment.this, args);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
	@Override
	public Api getApi(Context context, int what, Object... params) {
		if (what == API_POST_COMMENT) {
            return new Api("post", String.format("%s?uid=%s&memo_id=%s",
                    Util.URI_POST_COMMENT, mApp.loginUser().id(), mComment.memo_id()),
                    (Map<String, String>) params[0]);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCacheKey(Context context, int what, Object... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleResult(Context context, int what, JSONObject result,
			boolean isDone, Object... params) {

        Util.hideLoading();
        if (!isCallApiSuccess(context, what, result)) { 
        	if (result == null) {
        		Util.showToast(context, getResources().getString(R.string.network_error));
        	} else {
        		Util.showToast(context, Api.getStringValue(result, "msg"));
        	}
            return;
        }
        
        if (what == API_POST_COMMENT ) {
        	Comment comment = new Comment(Api.getJSONValue(result, "data", JSONObject.class));
        	
        	Util.showToast(context, getResources().getString(R.string.data_post_success));
        	comment.setPostToken(null);
        	Util.updateCacheAndUI(context, comment, mApp.loginUser().id());
        	
        	ReplyComment.this.finish();
        	
        	return;
        }
        
	}

	@Override
	public JSONObject handleCache(Context context, int what, JSONObject from,
			JSONObject to, Object... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateProgress(Context context, int what, float percent,
			Object... params) {
		// TODO Auto-generated method stub
		
	}
    
    
}
