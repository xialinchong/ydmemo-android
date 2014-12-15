package com.yidianhulian.ydmemo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.MainActivity;
import com.yidianhulian.ydmemo.activity.SelectContact;
import com.yidianhulian.ydmemo.activity.SendSms;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.widget.MemoUsers;
import com.yidianhulian.ydmemo.widget.MemoUsers.AddUserListener;

/**
 * 备忘内容
 * 
 * @author leeboo
 * 
 */
public class MemoContent extends Fragment implements Refreshable,
        CallApiListener {

    private YDMemoApplication mApp;
    private ViewGroup mDetailView;

    private ImageView mAssignerAvatar;
    private EditText mMemoDescEditor;
    private TextView mMemoDesc;
    private EditText mMemoSubject;
    private TextView mMemoDate;
    private Activity mContext;
    private Memo mMemo;
    private ImageButton mMemoAction;
    private ImageButton mMemoEdit;

    private MemoUsers mMemoUsers;
    protected boolean inEdit;
    private ViewGroup mMemoFollowers;
    private OnClickListener editListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!inEdit) {
                // 增加textview，编辑状态显示editview，否则显示textview author xialinchong
                // 2014-11-19
                mMemoDesc.setVisibility(View.GONE);
                mMemoSubject.setEnabled(true);
                mMemoEdit.setImageResource(R.drawable.done);
                mMemoFollowers.setVisibility(View.GONE);
                inEdit = true;
                mDescLayout.setVisibility(View.VISIBLE);
            } else {
                if (mMemo == null)
                    return;
                if (mMemoSubject.getText().length() > 100) {
                    Util.showToast(getActivity(), getString(R.string.memo_subject_is_too_long));
                    return;
                }
                saveMemo();
                mMemoFollowers.setVisibility(View.VISIBLE);
                mMemoSubject.setEnabled(false);
                mMemoDesc.setVisibility(View.VISIBLE);
                mMemoDesc.requestFocus();
                mMemoEdit.setImageResource(R.drawable.edit);
                inEdit = false;
                mDescLayout.setVisibility(View.GONE);
            }
        }
    };
    private OnClickListener removeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Util.openConfirmDialog(getActivity(), R.string.confirm_remove_memo,
                    R.drawable.memo, R.string.remove_memo_desc,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Util.showLoading(getActivity(), getString(R.string.removing));
                            CallApiTask.doCallApi(API_REMOVE_MEMO,
                                    MemoContent.this, getActivity());
                        }
                    });
        }
    };

    /**
     * 取消关注
     */
    private static final int API_CANCEL_FOLLOW = 1;
    private static final int API_CLOSE_MEMO = 2;
    public static final int API_ADD_FOLLOWER = 3;
    private static final int API_SAVE = 4;
    private static final int API_REMOVE_MEMO = 5;

    // xialinchong 编辑备忘说明-触摸范围
    private FrameLayout mDescLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContext = getActivity();
        mApp = (YDMemoApplication) getActivity().getApplication();

        mDetailView = (ViewGroup) inflater.inflate(R.layout.memo_content, null);

        mAssignerAvatar = (ImageView) mDetailView
                .findViewById(R.id.memo_assigner_avatar);
        mMemoDescEditor = (EditText) mDetailView.findViewById(R.id.memo_desc);
        mMemoDesc = (TextView) mDetailView.findViewById(R.id.desc_text);
        mMemoDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
        mMemoSubject = (EditText) mDetailView.findViewById(R.id.memo_subject);

        mMemoFollowers = (ViewGroup) mDetailView
                .findViewById(R.id.memo_followers);

        mMemoAction = (ImageButton) mDetailView
                .findViewById(R.id.memo_detail_action);
        mMemoEdit = (ImageButton) mDetailView.findViewById(R.id.memo_edit);

        mMemoEdit.setOnClickListener(editListener);

        mMemoDescEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mMemo == null)
                    return;
                if (!hasFocus
                        && !mMemoDescEditor.getText().toString()
                                .equals(mMemo.desc())) {
                    saveMemo();
                }
            }
        });

        mMemoDate = (TextView) mDetailView.findViewById(R.id.memo_date);

        mMemoUsers = new MemoUsers(getActivity(), mMemoFollowers);
        mMemoUsers.setAddUserListener(new AddUserListener() {
            @Override
            public void onSelectedUser(MemoUsers view, List<User> users) {

            }

            @Override
            public void onClick(MemoUsers memoUser) {
                ArrayList<User> users = new ArrayList<User>(memoUser.users());
                users.add(mApp.loginUser());

                Intent intent = new Intent();
                intent.setClass(getActivity(), SelectContact.class);
                intent.putExtra(SelectContact.ARG_TITLE, getString(R.string.share_to_users));
                intent.putExtra("requestCode",
                        SelectContact.REQUEST_FOR_MULTI_SELECT);
                intent.putParcelableArrayListExtra(
                        SelectContact.ARG_IGNORE_USERS, users);
                startActivityForResult(intent,
                        SelectContact.REQUEST_FOR_MULTI_SELECT);
            }
        });

        // 编辑说明触摸范围 xialinchong
        mDescLayout = (FrameLayout) mDetailView.findViewById(R.id.desc_layout);
        mDescLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMemoDescEditor.setFocusable(true);
                mMemoDescEditor.setFocusableInTouchMode(true);
                mMemoDescEditor.requestFocus();
                mMemoDescEditor.setSelection(mMemoDescEditor.getText()
                        .toString().length());

                com.yidianhulian.framework.Util.showKeyboard(getActivity(),
                        mMemoDescEditor);
            }
        });

        return mDetailView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            refresh((Memo) args.getParcelable("memo"));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if (requestCode == SelectContact.REQUEST_FOR_MULTI_SELECT) {
            List<User> users = data
                    .getParcelableArrayListExtra(SelectContact.RESULT_SELECTED_USERS);
            if (users == null || users.size() == 0)
                return;

            Map<String, String> query = new HashMap<String, String>();
            query.put("uid", mApp.loginUser().getAttr("id"));
            query.put("memo_id", String.valueOf(mMemo.id()));

            StringBuffer phones = new StringBuffer();
            StringBuffer names = new StringBuffer();
            for (User user : users) {
                user.initLocalUserInfo(mApp);
                phones.append(user.cellphone());
                phones.append(",");
                names.append(user.displayName());
                names.append(",");
            }

            query.put("follower_cellphones", phones.toString());
            query.put("follower_names", names.toString());
            Util.showLoading(this.getActivity(), getString(R.string.inviting));
            CallApiTask.doCallApi(API_ADD_FOLLOWER, this, this.getActivity(),
                    query);
        }

    }

    private void shown(Memo memo) {
        getView().setVisibility(View.VISIBLE);
        mMemo = memo;
        Util.loadAvatar((YDMemoApplication) mContext.getApplication(),
                memo.assigner(), mAssignerAvatar);

        mMemoDescEditor.setText(memo.desc());
        mMemoDesc.setText(memo.desc());
        mMemoDate.setText(memo.date() + " " + memo.assigner().displayName());
        mMemoSubject.setText(memo.subject());
        if (memo.desc().trim().isEmpty()) {
            mMemoDesc.setHint(R.string.memo_desc);
        }
        mMemoEdit.setImageResource(R.drawable.edit);
        mMemoEdit.setVisibility(View.GONE);
        mMemoAction.setVisibility(View.GONE);

        List<User> followers = mMemo.followers();// copyed leeboo

        if (!followers.contains(mMemo.assigner())) {
            followers.add(mMemo.assigner());
        }
        long loginUid = mApp.loginUser().id();
        mMemoUsers.shown(
                mMemo.isAssigner(loginUid)
                        && "accept".equalsIgnoreCase(mMemo.assignerStatus())
                        && !mMemo.isClosed(), false, followers);

        inEdit = false;
        mMemoDesc.setVisibility(View.VISIBLE);
        mDescLayout.setVisibility(View.GONE);
        mMemoSubject.setEnabled(false);
        mMemoFollowers.setVisibility(View.VISIBLE);

        if (mMemo.isAssigner(loginUid)) {// is mine
            if (!mMemo.isClosed()) {
                mMemoAction.setVisibility(View.VISIBLE);
                mMemoAction.setImageResource(R.drawable.remove_selector);
                mMemoAction.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.showLoading(getActivity(), getString(R.string.closing));
                        Map<String, String> query = new HashMap<String, String>();
                        query.put("is_closed", "1");
                        CallApiTask.doCallApi(API_CLOSE_MEMO, MemoContent.this,
                                getActivity(), query);
                    }
                });
                mMemoEdit.setVisibility(View.VISIBLE);
                mMemoEdit.setOnClickListener(editListener);
            } else {
                mMemoEdit.setVisibility(View.VISIBLE);
                mMemoEdit.setImageResource(R.drawable.remove_selector);
                mMemoEdit.setOnClickListener(removeListener);

                mMemoAction.setVisibility(View.VISIBLE);
                mMemoAction.setImageResource(R.drawable.resume_memo);
                mMemoAction.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.showLoading(getActivity(), getString(R.string.resuming));
                        Map<String, String> query = new HashMap<String, String>();
                        query.put("is_closed", "0");
                        CallApiTask.doCallApi(API_CLOSE_MEMO, MemoContent.this,
                                getActivity(), query);
                    }
                });
            }
        } else if (mMemo.isFollower(loginUid)
                && "accept".equals(mMemo.followerStatus(loginUid))) {// is
                                                                     // follower
            if (!mMemo.isClosed()) {
                mMemoAction.setVisibility(View.VISIBLE);
                mMemoAction.setImageResource(R.drawable.cancel_selector);
                mMemoAction.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.showLoading(getActivity(), getString(R.string.cancelling));
                        CallApiTask.doCallApi(API_CANCEL_FOLLOW,
                                MemoContent.this, getActivity());
                    }
                });
            }
        }

    }

    @Override
    public boolean refresh(Model model) {

        if (getView() == null)
            return false;// not ready;

        if (model == null) {
            mMemo = null;
            getView().setVisibility(View.INVISIBLE);
            return false;
        }
        if (model instanceof Memo) {
            if (mMemo == null || mMemo.id() == model.id()) {
                shown((Memo) model);
                return true;
            }
        }
        return false;
    }

    private void saveMemo() {
        com.yidianhulian.framework.Util.hideKeyboard(getActivity());
        Util.showLoading(getActivity(), getString(R.string.posting));
        CallApiTask.doCallApi(API_SAVE, this, this.getActivity());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> query = new HashMap<String, String>();

        if (what == API_CLOSE_MEMO) {
            return new Api("post", String.format("%s?uid=%s&memo_id=%s",
                    Util.URI_MEMO_EDIT, mApp.loginUser().id(), mMemo.id()),
                    (Map<String, String>) params[0]);
        } else if (what == API_SAVE) {

            query.put("subject", mMemoSubject.getText().toString().trim());
            query.put("desc", mMemoDescEditor.getText().toString().trim());
            return new Api("post", String.format("%s?uid=%s&memo_id=%s",
                    Util.URI_MEMO_EDIT, mApp.loginUser().id(), mMemo.id()),
                    query);
        } else if (what == API_ADD_FOLLOWER) {
            return new Api("post", Util.URI_SHARE_MEMO,
                    (Map<String, String>) params[0]);
        } else if (what == API_CANCEL_FOLLOW) {
            query.put("uid", mApp.loginUser().getAttr("id"));
            query.put("memo_id", String.valueOf(mMemo.id()));
            query.put("action", "refuse");
            return new Api("get", Util.URI_HANDLE_SHARE, query);
        } else if (what == API_REMOVE_MEMO) {
            query.put("uid", mApp.loginUser().getAttr("id"));
            query.put("memo_id", String.valueOf(mMemo.id()));
            return new Api("get", Util.URI_REMOVE_MEMO, query);
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
        if (getActivity() == null)
            return;// leeboo 在处理回调时activity已经销毁了，比如退出了详细界面

        if (what == API_ADD_FOLLOWER) {
            Util.showToast(this.getActivity(), getString(R.string.invite_has_been_send));
            JSONObject data = Api
                    .getJSONValue(result, "data", JSONObject.class);
            JSONArray newUsers = Api.getJSONValue(data, "new_users",
                    JSONArray.class);
            if (newUsers != null && newUsers.length() > 0) {
                ArrayList<User> users = new ArrayList<User>();
                for (int i = 0; i < newUsers.length(); i++) {
                    try {
                        users.add(new User(mApp, newUsers.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(getActivity(), SendSms.class);
                intent.putParcelableArrayListExtra(SendSms.ARG_USERS, users);
                intent.putExtra(SendSms.ARG_SHARE_DEFAULT, String.format(getString(R.string.focus_this_thing_in_memo), mMemo.subject()));
                intent.putExtra(
                        SendSms.ARG_SHARE_TIP,
                        getString(R.string.below_user_not_used));
                startActivity(intent);
            }

            mMemo = new Memo(Api.getJSONValue(data, "memo", JSONObject.class));
        } else if (what == API_REMOVE_MEMO || what == API_CANCEL_FOLLOW) {
            Util.removeCacheAndUI(context, mMemo, mApp.loginUser().id());
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.closeDrawers();
            return;
        } else {
            mMemo = new Memo(Api.getJSONValue(result, "data", JSONObject.class));
        }
        Util.updateCacheAndUI(context, mMemo, mApp.loginUser().id());

        if (what == API_CLOSE_MEMO) {
            Activity activity = getActivity();
            if( ! (activity instanceof MainActivity))return;
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.closeDrawers();
        }
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return from;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {

    }
}
