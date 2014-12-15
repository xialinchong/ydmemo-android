package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.activity.MainActivity;
import com.yidianhulian.ydmemo.fragment.MemoDetail;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.User;

@SuppressLint({ "UseSparseArrays", "InflateParams" })
public class MemoAdapter extends BaseAdapter implements CallApiListener {
    protected static final int API_CLOSE_MEMO = 1;
    protected static final int API_CANCEL_FOLLOW = 2;
    protected static final int API_RESUME_MEMO = 3;
    private Activity mContext;
    private int mResid;
    private List<Memo> mDatas;
    private SortedMap<Long, Memo> mMemos;
    private YDMemoApplication mApp;

    private OnClickListener mLeftBtnHandler;
    private OnClickListener mRightBtnHandler;
    private int mLeftBtnBackground;
    private int mRightBtnBackground;

    public MemoAdapter(Activity context, SortedMap<Long, Memo> datas, int resid) {
        this.mContext = context;
        this.mResid = resid;
        if (datas == null)
            datas = new TreeMap<Long, Memo>();
        mMemos = datas;
        this.mDatas = new ArrayList<Memo>(datas.values());
        Collections.reverse(mDatas);
        mApp = (YDMemoApplication) mContext.getApplication();

    }

    @Override
    public void notifyDataSetChanged() {
        this.mDatas.clear();
        this.mDatas.addAll(mMemos.values());
        Collections.reverse(mDatas);
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        this.mDatas.clear();
        this.mDatas.addAll(mMemos.values());
        Collections.reverse(mDatas);
        super.notifyDataSetInvalidated();
    }

    public MemoAdapter setupLeftButton(int res, OnClickListener onclick) {
        mLeftBtnHandler = onclick;
        mLeftBtnBackground = res;
        return this;
    }

    public MemoAdapter setupRightButton(int res, OnClickListener onclick) {
        mRightBtnHandler = onclick;
        mRightBtnBackground = res;
        return this;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDatas.get(position).id();
    }

    private View noContentView(int position, View convertView, ViewGroup root) {
        return LayoutInflater.from(mContext).inflate(R.layout.no_memos, null);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup root) {
        Memo memo = mDatas.get(position);
        if (memo.id() == -1) {
            return noContentView(position, convertView, root);
        }
        User user = memo.assigner();
        user.initLocalUserInfo(mApp);
        MemoPlaceHolder placeholder;

        if (convertView == null || /* empty view */convertView.getTag() == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResid, null);
            placeholder = new MemoPlaceHolder();
            placeholder.button1 = (ImageButton) convertView
                    .findViewById(R.id.button1);
            placeholder.button2 = (ImageButton) convertView
                    .findViewById(R.id.button2);
            placeholder.button3 = (ImageButton) convertView
                    .findViewById(R.id.button3);
            placeholder.button2Budge = (TextView) convertView
                    .findViewById(R.id.button2_budge);
            placeholder.button2BottomBudge = (TextView) convertView
                    .findViewById(R.id.button2_bottom_budge);
            placeholder.button2LeftBudge = (ImageView) convertView
                    .findViewById(R.id.button2_left_budge);
            placeholder.button3Budge = (TextView) convertView
                    .findViewById(R.id.button3_budge);
            placeholder.action = (Button) convertView.findViewById(R.id.action);
            placeholder.action.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Memo memo = (Memo) v.getTag();
                    if (memo == null)
                        return;
                    MainActivity activity = (MainActivity) mContext;
                    activity.getMemoDetail().openMemo(memo.id(),
                            MemoDetail.MEMO_DETAIL_TYPE_CONTENT);

                }
            });
            placeholder.title = (TextView) convertView
                    .findViewById(R.id.memoTitle);

            convertView.setTag(placeholder);

        } else {
            placeholder = (MemoPlaceHolder) convertView.getTag();
        }

        placeholder.button2Budge.setVisibility(View.GONE);
        placeholder.button2BottomBudge.setVisibility(View.GONE);
        placeholder.button2LeftBudge.setVisibility(View.GONE);
        placeholder.button3Budge.setVisibility(View.GONE);

        // 自定义按钮
        if (mLeftBtnHandler != null) {
            placeholder.button1.setOnClickListener(mLeftBtnHandler);
            placeholder.button1.setBackgroundResource(mLeftBtnBackground);

            placeholder.button3.setOnClickListener(mRightBtnHandler);
            placeholder.button3.setBackgroundResource(mRightBtnBackground);

            placeholder.button2.setVisibility(View.GONE);
        } else {
            showButton1(memo, placeholder.button1);

            if (memo.unread_comment() > 0) {
                placeholder.button2
                        .setBackgroundResource(R.drawable.new_comment_selector);
                placeholder.button2Budge.setText(String.valueOf(memo
                        .unread_comment()));
                placeholder.button2Budge.setVisibility(View.VISIBLE);
            } else {
                placeholder.button2
                        .setBackgroundResource(R.drawable.no_comment_selector);
            }
            if (mApp.hasDraftComment(memo.id())) {
                placeholder.button2BottomBudge.setVisibility(View.VISIBLE);
            }
            if (mApp.hasPostFailComment(memo.id())) {
                placeholder.button2LeftBudge.setVisibility(View.VISIBLE);
            }
            if (memo.unread_reminder() > 0) {
                placeholder.button3
                        .setBackgroundResource(R.drawable.new_reminder_selector);
                placeholder.button3Budge.setText(String.valueOf(memo
                        .unread_reminder()));
                placeholder.button3Budge.setVisibility(View.VISIBLE);
            } else {
                placeholder.button3
                        .setBackgroundResource(R.drawable.no_reminder_selector);
            }
            placeholder.button2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Memo memo = (Memo) v.getTag();
                    if (memo == null)
                        return;
                    MainActivity activity = (MainActivity) mContext;
                    activity.getMemoDetail().openMemo(memo.id(),
                            MemoDetail.MEMO_DETAIL_TYPE_COMMENTS);
                }
            });
            placeholder.button3.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Memo memo = (Memo) v.getTag();
                    if (memo == null)
                        return;
                    MainActivity activity = (MainActivity) mContext;
                    activity.getMemoDetail().openMemo(memo.id(),
                            MemoDetail.MEMO_DETAIL_TYPE_TRACE);
                }
            });
        }

        placeholder.button1.setTag(memo);
        placeholder.button2.setTag(memo);
        placeholder.button3.setTag(memo);
        placeholder.action.setTag(memo);
        placeholder.memo = memo;

        placeholder.title.setText(memo.getAttr("subject"));

        return convertView;
    }

    private void showButton1(Memo memo, ImageButton action) {
        action.setVisibility(View.INVISIBLE);

        if (memo.isAssigner(mApp.loginUser().id())) {// is mine
            if (!memo.isClosed()) {
                action.setVisibility(View.VISIBLE);
                action.setBackgroundResource(R.drawable.remove_selector);
                action.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.showLoading(mContext, "关闭中");
                        Map<String, String> query = new HashMap<String, String>();
                        query.put("is_closed", "1");
                        CallApiTask.doCallApi(API_CLOSE_MEMO, MemoAdapter.this,
                                mContext, v.getTag(), query);
                    }
                });
            } else {
                action.setVisibility(View.VISIBLE);
                action.setBackgroundResource(R.drawable.resume_memo);
                action.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.showLoading(mContext, "恢复中");
                        Map<String, String> query = new HashMap<String, String>();
                        query.put("is_closed", "0");
                        CallApiTask.doCallApi(API_RESUME_MEMO,
                                MemoAdapter.this, mContext, v.getTag(), query);
                    }
                });
            }
        } else if (memo.isFollower(mApp.loginUser().id())
                && "accept".equals(memo.followerStatus(mApp.loginUser().id()))) {// is
                                                                                 // follower
            if (!memo.isClosed()) {
                action.setVisibility(View.VISIBLE);
                action.setBackgroundResource(R.drawable.cancel_selector);
                action.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Util.openConfirmDialog(mContext, R.string.cancel_follow, 
                                R.drawable.memo, R.string.are_you_sure, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        Util.showLoading(mContext,
                                                "正在取消关注");
                                        CallApiTask.doCallApi(
                                                API_CANCEL_FOLLOW,
                                                MemoAdapter.this,
                                                mContext,
                                                (Memo) v.getTag());
                                    }
                                });
                        
                    }
                });
            }
        }
    }

    public class MemoPlaceHolder {
        public ImageButton button1;
        public ImageButton button2;
        public ImageButton button3;
        ImageView button2LeftBudge;
        public TextView button2Budge;
        public TextView button2BottomBudge;
        public TextView button3Budge;
        public Button action;
        public TextView title;
        public Memo memo;
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> query = new HashMap<String, String>();
        Memo mMemo = (Memo) params[0];
        if (what == API_CLOSE_MEMO || what == API_RESUME_MEMO) {
            return new Api("post", String.format("%s?uid=%s&memo_id=%s",
                    Util.URI_MEMO_EDIT, mApp.loginUser().id(), mMemo.id()),
                    (Map<String, String>) params[1]);
        } else if (what == API_CANCEL_FOLLOW) {
            query.put("uid", mApp.loginUser().getAttr("id"));
            query.put("memo_id", String.valueOf(mMemo.id()));
            query.put("action", "refuse");
            return new Api("get", Util.URI_HANDLE_SHARE, query);
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
        return null;
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        if (isDone) {
            Util.hideLoading();
        }
        if (isCallApiSuccess(context, what, result, params)) {
            switch (what) {
            case API_CANCEL_FOLLOW:
                Util.showToast(mContext,
                        mContext.getString(R.string.follow_has_been_cancelled));
                break;
            case API_CLOSE_MEMO:
                Util.showToast(mContext,
                        mContext.getString(R.string.memo_has_been_closed));
                break;
            case API_RESUME_MEMO:
                Util.showToast(mContext,
                        mContext.getString(R.string.memo_has_been_resumed));
                break;
            }
        }

        Memo memo;
        if (what == API_CANCEL_FOLLOW) {
            memo = (Memo) params[0];
            Util.removeCacheAndUI(context, memo, mApp.loginUser().id());
            MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.closeDrawers();
            return;
        }

        memo = new Memo(Api.getJSONValue(result, "data", JSONObject.class));
        Util.updateCacheAndUI(context, memo, mApp.loginUser().id());
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return null;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {

    }
}
