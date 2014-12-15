package com.yidianhulian.ydmemo.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.MemoCommentsAdapter;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.SelectContact;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.view.KeyboardLayout;
import com.yidianhulian.ydmemo.view.KeyboardLayout.onKybdsChangeListener;
import com.yidianhulian.ydmemo.widget.CommentView.OnCommentClick;

/**
 * 备忘留言
 * 
 * @author leeboo
 * 
 */
public class MemoComments extends Fragment implements Refreshable,
        CallApiListener {

    private YDMemoApplication mApp;
    private PullToRefreshListView mList;
    private ViewGroup mLayout;
    private Memo mMemo;
    private MemoCommentsAdapter mAdapter;
    private KeyboardLayout mPostCommentPanel;
    private ViewGroup mSendExtra;
    private ImageButton mPostCommentBtn;
    private EditText mCommentField;
    private List<Comment> mComments = new ArrayList<Comment>();
    private SharedPreferences mSp;
    private long mLastId = 0;
    /**
     * 是否处于文字编辑状态
     */
    private boolean mIsOnSendText;

    /**
     * 提交留言
     */
    private static final int API_POST_COMMENT = 1;
    public static final int API_LOAD_COMMENT = 2;
    private static final int API_POST_PIC = 3;
    private static final int SAVE_SELECT_IMG = 5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mApp = (YDMemoApplication) getActivity().getApplication();
        mLayout = (ViewGroup) inflater.inflate(R.layout.memo_comments,
                container, false);
        mList = (PullToRefreshListView) mLayout
                .findViewById(R.id.memo_comments);

        addEmptyView();

        mList.setMode(Mode.PULL_FROM_START);

        // post comment panel
        mPostCommentPanel = (KeyboardLayout) mLayout
                .findViewById(R.id.post_comment_panel);
        mPostCommentPanel.setOnkbdStateListener(new onKybdsChangeListener() {
            @Override
            public void onKeyBoardStateChange(int state) {
                mSendExtra.setVisibility(View.GONE);
            }
        });
        mSendExtra = (ViewGroup) mLayout.findViewById(R.id.send_extra);
        bindSendExtraEvent();
        mCommentField = (EditText) mLayout.findViewById(R.id.comment_field);
        mCommentField.setOnFocusChangeListener(new OnFocusChangeListener() {
            
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( ! hasFocus && mMemo!=null){
                    if (mCommentField.getText().toString().isEmpty()) {
                        mApp.updateDraftComment(mMemo.id(), null);
                    } else {
                        mApp.updateDraftComment(mMemo.id(), mCommentField.getText().toString());
                    }
                }else{
                    mSendExtra.setVisibility(View.GONE);
                }
            }
        });
        mCommentField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mCommentField.getText().toString().isEmpty()) {
                    mIsOnSendText = false;
                    mPostCommentBtn.setImageResource(R.drawable.add_msg_selector);
                } else {
                    mIsOnSendText = true;
                    mPostCommentBtn.setImageResource(R.drawable.send_selector);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0)
                    return;
                char ch = s.charAt(s.length() - 1);
                if (ch == '@') {
                    Intent intent = new Intent(getActivity(),
                            SelectContact.class);
                    intent.putExtra(SelectContact.ARG_TITLE,
                            getString(R.string.at));
                    ArrayList<User> users = (ArrayList<User>) mMemo.followers();
                    users.add(mMemo.assigner());
                    intent.putParcelableArrayListExtra(
                            SelectContact.ARG_JUST_USERS, users);
                    startActivityForResult(intent,
                            SelectContact.REQUEST_FOR_MULTI_SELECT);
                }
            }
        });

        mPostCommentBtn = (ImageButton) mLayout.findViewById(R.id.post_btn);
        mPostCommentBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsOnSendText) {
                    if(mSendExtra.getVisibility()==View.GONE){
                        mSendExtra.setVisibility(View.VISIBLE);
                        com.yidianhulian.framework.Util.hideKeyboard(getActivity());
                        mList.getRefreshableView().smoothScrollToPosition(mMemo.comments().size() - 1);
                    }else{
                        mSendExtra.setVisibility(View.GONE);
                        mCommentField.requestFocus();
                    }
                    
                    return;
                }

                mApp.updateDraftComment(mMemo.id(), null);
                sendText(mCommentField.getText().toString().trim(), String.valueOf((new Date()).getTime()));
            }
        });

        mAdapter = new MemoCommentsAdapter(getActivity(), mComments,
                new OnCommentClick() {
                    @Override
                    public boolean onClick(Comment comment) {

                        if (comment == null || !comment.needRepost() || comment.getPostToken()==null)
                            return false;

                        comment.setNeedReposted("0");
                        mAdapter.notifyDataSetChanged();

                        switch(comment.commentType()){
                            case COMMENT_PIC: sendPic(comment.localFilePath(), comment.getPostToken());break;
                            default :sendText(comment.comment(), comment.getPostToken());break;
                        }
                        return true;
                    }
                });

        mList.setAdapter(mAdapter);
        mList.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                CallApiTask.doCallApi(API_LOAD_COMMENT, MemoComments.this,
                        MemoComments.this.getActivity(), CacheType.CUSTOM,
                        FetchType.FETCH_API);
            }

        });

        return mLayout;
    }

    private void bindSendExtraEvent() {
        Button sendPic = (Button) mSendExtra.findViewById(R.id.send_pic);
        sendPic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.isSdcardExisting()) {
                    Intent intent;
                    if (Build.VERSION.SDK_INT < 19) {
                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");

                    } else {
                        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    }
                    startActivityForResult(intent, SAVE_SELECT_IMG);
                } else {
                    Toast.makeText(v.getContext(), "请插入sd卡", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SAVE_SELECT_IMG:
            if (data != null) {
                //原始图片路径 srcPath xialinchong @2014-12-04
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr
                        .query(data.getData(), null, null, null, null);
                cursor.moveToFirst();
                String srcPath = cursor.getString(cursor
                        .getColumnIndex("_data"));
                
                if (srcPath != null && !srcPath.equals("")) {
                    sendPic(srcPath, String.valueOf((new Date()).getTime()));
                }
                break;
            }
            break;
        case SelectContact.REQUEST_FOR_MULTI_SELECT:
            if (data == null){
                mCommentField.setText(mCommentField.getText().toString()+" ");
                break;
            }
            List<User> atUsers = data
                    .getParcelableArrayListExtra(SelectContact.RESULT_SELECTED_USERS);
            if (atUsers == null){
                mCommentField.setText(mCommentField.getText().toString()+" ");
                break;
            }

            mCommentField.setText(mCommentField.getText().subSequence(0, mCommentField.getText().length() - 1));
            
            for (User user : atUsers) {
                String cellphone = ":" + user.cellphone() + ":";
                mCommentField.append(cellphone);
                mCommentField.append(" ");
            }
            
            mCommentField.setText(Util.showAtUser(mApp, mCommentField.getText().toString(), formatFollowers()));
            mApp.updateDraftComment(mMemo.id(), mCommentField.getText().toString());
            mCommentField.requestFocus();
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    

    /**
     * 显示空行
     */
    private void addEmptyView() {
        if (mComments.size() == 0) {
            Comment empty = new Comment(null);
            empty.setAttr("id", "-1");
            mComments.add(empty);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSp = getActivity().getSharedPreferences("CommentPagination",
                Context.MODE_PRIVATE);
        Bundle args = getArguments();
        if (args != null) {
            refresh((Memo) args.getParcelable("memo"));
        }
    }

    @Override
    public boolean refresh(Model model) {

        if (getView() == null)
            return false;// not ready;
        
        if (model == null) {
            mMemo = null;
            mCommentField.setText("");
            getView().setVisibility(View.INVISIBLE);
            return false;
        }

        if (model instanceof Memo) {
            if (mMemo == null || mMemo.id() == model.id()) {
                mMemo = (Memo) model;
                shown();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> query = new HashMap<String, String>();
        if (what == API_LOAD_COMMENT) {
            query.put("uid", mApp.loginUser().getAttr("id"));
            query.put("memo_id", String.valueOf(mMemo.id()));
            if (mLastId > 0) {
                query.put("last_id", String.valueOf(mLastId));
            }
            return new Api("get", Util.URI_LOAD_COMMENT, query);
        } else if (what == API_POST_COMMENT) {
            return new Api("post", String.format("%s?uid=%s&memo_id=%s",
                    Util.URI_POST_COMMENT, mApp.loginUser().id(), mMemo.id()),
                    (Map<String, String>) params[0]);
        } else if (what == API_POST_PIC) {
            final Map<String, String> args = (Map<String, String>) params[0];
            return new Api("post", String.format("%s?uid=%s&memo_id=%s",
                    Util.URI_POST_COMMENT, mApp.loginUser().id(), mMemo.id()),
                    args, new ArrayList<String>() {
                        private static final long serialVersionUID = 886681204636539339L;
                        {
                            add("pic");
                        }
                    });
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
        if (what == API_LOAD_COMMENT || what == API_POST_COMMENT) {
            return String.format("%s?uid=%s&memo_id=%s", Util.URI_LOAD_MEMO,
                    mApp.loginUser().id(), mMemo.id());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        if (what == API_LOAD_COMMENT) {
            mList.onRefreshComplete();
        }
        if (isDone) {
            Util.hideLoading();
            if (!isCallApiSuccess(context, what, result)) {
                if (what == API_POST_COMMENT || what == API_POST_PIC) {
                    Map<String, String> args = (HashMap<String, String>) params[0];
                    mMemo.setCommentNeedRepost(args.get("post_token"));
                    mApp.addPostFialComment(mMemo.id(), args.get("post_token"));
                    Util.updateCacheAndUI(mApp, mMemo, mApp.loginUser().id());
                    mAdapter.notifyDataSetChanged();
                }
                return;
            }
        }
        if (what == API_POST_COMMENT || what == API_POST_PIC) {
            mPostCommentBtn.setEnabled(true);
            Comment comment = new Comment(Api.getJSONValue(result, "data",
                    JSONObject.class));
            mApp.removePostFialComment(mMemo.id(), comment.getPostToken());

            Comment postTokenComment = new Comment(null);
            postTokenComment.setPostToken(comment.getPostToken());
            postTokenComment.setAttr("memo_id", String.valueOf(comment.memo_id()));
            postTokenComment.setWillRemoveFromCache(true);
            new CacheHelper(mApp).update(postTokenComment, String.valueOf(mApp.loginUser().id()));
            
            mMemo.updateCommentByPostToken(comment);
            
            mComments.clear();
            mComments.addAll(mMemo.comments());
            mAdapter.notifyDataSetChanged();
            
            Util.updateCacheAndUI(mApp, mMemo, mApp.loginUser().id());
            return;
        }
        if (what == API_LOAD_COMMENT) {
            JSONObject comments = Api.getJSONValue(result, "data",
                    JSONObject.class);

            Iterator<?> keys = comments.keys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                try {
                    mMemo.addComment(new Comment(comments.getJSONObject(key)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mComments.clear();
            mComments.addAll(mMemo.comments());

            if (mComments.size() > 0) {
                mLastId = mComments.get(0).id();
                mSp.edit().putLong("mLastCommentId-" + mMemo.id(), mLastId)
                        .commit();
                mList.getRefreshableView().smoothScrollToPosition(
                        comments.length() + 1);// 加1是让之前都comment现实在最底部
            }
            addEmptyView();
            mAdapter.notifyDataSetChanged();
            return;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {
        Map<String, String> args = (Map<String, String>) params[0];
        String postToken = args.get("post_token");

        mMemo.uploadCommentProgress(postToken, percent);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        // leeboo @10/26 把备忘加载memo中
        // 由于comment是sorted map，所以append于prepend 效果一样
        if (what != API_LOAD_COMMENT)
            return from;

        JSONObject comments = Api.getJSONValue(from, "data", JSONObject.class);
        JSONObject memo = Api.getJSONValue(to, "data", JSONObject.class);
        JSONObject oldComments;
        try {
            oldComments = memo.getJSONObject("comments");
        } catch (JSONException e1) {
            oldComments = new JSONObject();
        }

        Iterator<?> keys = comments.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            try {
                oldComments.put(key, comments.getJSONObject(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            memo.put("comments", oldComments);
            to.put("data", memo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return to;

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
     * 显示界面数据, 留言列表总是显示在最底部
     */
    private void shown() {

        if (mLastId == 0)
            mLastId = mSp.getLong("mLastCommentId-" + mMemo.id(), 0);

        User loginUser = mApp.loginUser();
        long loginUserId = loginUser.id();
        getView().setVisibility(View.VISIBLE);
        mAdapter.setMemo(mMemo);

        mComments.clear();
        mComments.addAll(mMemo.comments());
        addEmptyView();

        if (mLastId == 0 && mComments.size() > 0)
            mLastId = mComments.get(0).id();
        mAdapter.notifyDataSetChanged();
        mList.getRefreshableView().setSelection(mComments.size() - 1);

        // 留言，同意、关注处理
        mPostCommentPanel.setVisibility(View.GONE);

        if(Util.isEmpty(mCommentField.getText().toString()) && mApp.hasDraftComment(mMemo.id())){
            mCommentField.setText(Util.showAtUser(mApp, mApp.getDraftComment(mMemo.id()), formatFollowers()));
        }
        if (mMemo.isClosed()) {
            return;
        }

        if (mMemo.isAssigner(loginUserId)) {// my is assigner
            String status = mMemo.assignerStatus();
            if ("accept".equalsIgnoreCase(status)) {
                mPostCommentPanel.setVisibility(View.VISIBLE);
            }
            return;
        }

        // my is follower
        String status = mMemo.followerStatus(loginUserId);
        if ("accept".equalsIgnoreCase(status)) {
            mPostCommentPanel.setVisibility(View.VISIBLE);
        }
    }

    private void sendPic(String pic, String postToken) {

        Comment newComment = new Comment(null);
        newComment.setCommenter(mApp.loginUser());
        newComment.setAttr("comment", "");
        
        //压缩后的图片路径 picPath xialinchong @2014-12-04
        String picPath = Util.compressImage(pic);
        newComment.filePath(picPath);
        newComment.localFilePath(pic);
        
        newComment.fileType("pic");
        newComment.setPostToken(postToken);
        mMemo.addPostComment(newComment);
        mComments.clear();
        mComments.addAll(mMemo.comments());

        mAdapter.notifyDataSetChanged();

        Map<String, String> args = new HashMap<String, String>();
        args.put("pic", pic);
        args.put("post_token", newComment.getPostToken());

        mSendExtra.setVisibility(View.GONE);
        mList.getRefreshableView().smoothScrollToPosition(mMemo.comments().size() - 1);
        CallApiTask.doCallApi(API_POST_PIC, MemoComments.this,
                MemoComments.this.getActivity(), args);
    }

    private void sendText(String comment, String postToken) {
        if (comment.isEmpty()) {
            Util.showToast(MemoComments.this.getActivity(), getString(R.string.please_say_something));
            return;
        }
        mSendExtra.setVisibility(View.GONE);
        com.yidianhulian.framework.Util.hideKeyboard(MemoComments.this
                .getActivity());
        Comment newComment = new Comment(null);
        newComment.setCommenter(mApp.loginUser());
        newComment.setAttr("comment", comment);
        newComment.setPostToken(postToken);
        mMemo.addPostComment(newComment);
        mComments.clear();
        mComments.addAll(mMemo.comments());

        mAdapter.notifyDataSetChanged();

        Map<String, String> args = new HashMap<String, String>();
        args.put("comment", comment);
        args.put("post_token", newComment.getPostToken());


        mCommentField.setText("");
        mList.getRefreshableView().smoothScrollToPosition(mMemo.comments().size() - 1);
        CallApiTask.doCallApi(API_POST_COMMENT, MemoComments.this,
                MemoComments.this.getActivity(), args);
    }

}
