package com.yidianhulian.ydmemo.fragment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.AddRemind;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;

/**
 * 备忘详情，传入参数ARG_MEMO_ID
 * 
 * @author leeboo
 *
 */
public class MemoDetail extends Fragment implements CallApiListener,
        Refreshable {
    public static final String ARG_MEMO_ID = "memo_id";
    public static final String ARG_MEMO = "memo";
    public static final int API_LOAD_MEMO = 1;

    /**
     * 接受指派
     */
    private static final int API_ACCEPT = 3;
    /**
     * 不接受指派
     */
    private static final int API_NOT_ACCEPT = 4;
    /**
     * 关注
     */
    private static final int API_FOLLOW = 5;
    /**
     * 不关注
     */
    private static final int API_NOT_FOLLOW = 6;
    private static final int API_UPDATE_LAST_COMMENT = 7;
    
    public static final String MEMO_DETAIL_TYPE_CONTENT = "memo-detail-follows";
    public static final String MEMO_DETAIL_TYPE_COMMENTS = "memo-detail-comments";
    public static final String MEMO_DETAIL_TYPE_TRACE = "memo-detail-trace";
    public static final int MEMO_DETAIL_TYPE_CONTENT_INDEX = 0;
    public static final int MEMO_DETAIL_TYPE_COMMENTS_INDEX = 1;
    public static final int MEMO_DETAIL_TYPE_TRACE_INDEX = 2;
    
    private YDMemoApplication mApp;
    private long mMemoId;
    private Memo mMemo;
    private ViewGroup mContainer;
    private DrawerLayout mDrawerLayout;
    private Map<String, Fragment> mListFragment = new HashMap<String, Fragment>();
    private Button mContentTabBtn, mCommentTabBtn, mTraceTabBtn;
    private float mOffset = 0;
    // 页卡1-》页卡2的偏移量
    float moveOne;
    // 页卡1-》页卡3的偏移量
    float moveTwo;

    private ViewGroup mError;
    private Button mReloadBtn;
    private String mDefaultShow;
    private android.widget.ImageView mCursor;
    private int mCurrIndex = 0;
    private ViewGroup mLoading;

    public void reloadData() {
        setLoading(true);
        CallApiTask.doCallApi(API_LOAD_MEMO, this, getActivity(),
                CacheType.CUSTOM, FetchType.FETCH_API_ELSE_CACHE);
    }

    public void setUp(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
        restoreActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (YDMemoApplication) getActivity().getApplication();
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(
                R.layout.memo_detail, container, false);

        mContainer = (ViewGroup) viewGroup.findViewById(R.id.memo_container);
        mError = (ViewGroup) viewGroup.findViewById(R.id.error);
        mLoading = (ViewGroup) viewGroup.findViewById(R.id.loading);
        mReloadBtn = (Button) viewGroup.findViewById(R.id.error_button);
        mReloadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                reloadData();
            }
        });

        mContentTabBtn = (Button) viewGroup.findViewById(R.id.memo_followers);
        mCommentTabBtn = (Button) viewGroup.findViewById(R.id.memo_comments);
        mTraceTabBtn = (Button) viewGroup.findViewById(R.id.memo_reminders);

        setHasOptionsMenu(true);

        mCursor = (ImageView) viewGroup.findViewById(R.id.cursor);

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mOffset = (dm.widthPixels - 50) / 3;

        moveOne = mOffset;
        moveTwo = mOffset * 2f;

        LayoutParams lParams = mCursor.getLayoutParams();
        lParams.width = (int) mOffset;
        mCursor.setLayoutParams(lParams);

        Matrix matrix = new Matrix();
        matrix.postTranslate(mOffset, 0);
        mCursor.setImageMatrix(matrix);


        mListFragment.put(MEMO_DETAIL_TYPE_CONTENT, new MemoContent());
        mListFragment.put(MEMO_DETAIL_TYPE_COMMENTS, new MemoComments());
        mListFragment.put(MEMO_DETAIL_TYPE_TRACE, new MemoReminders());
        

        mContentTabBtn.setOnClickListener(new MyButtonOnClickListener(0));
        mCommentTabBtn.setOnClickListener(new MyButtonOnClickListener(1));
        mTraceTabBtn.setOnClickListener(new MyButtonOnClickListener(2));
        
        return viewGroup;
    }

    private void restoreActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.logo);
        if(mMemo != null){
            actionBar.setTitle(mMemo.subject());
        }else{
            actionBar.setTitle(R.string.memo_detail);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isOpen()) {
            restoreActionBar();

            if (mMemo == null
                    || (mMemo.isClosed() || !"accept".equalsIgnoreCase(mMemo
                            .assignerStatus())))
                return;

            long currUserId = mApp.loginUser().id();
            if ((mMemo.isAssigner(currUserId) && "accept".equalsIgnoreCase(mMemo.assignerStatus()))
             || (mMemo.isFollower(currUserId) && "accept".equalsIgnoreCase(mMemo.followerStatus(currUserId)))) {
                inflater.inflate(R.menu.add_reminder, menu);
                
            }else if(((mMemo.isFollower(currUserId) && "pending".equalsIgnoreCase(mMemo.followerStatus(currUserId)))) 
                   || (mMemo.isAssigner(currUserId) && "pending".equalsIgnoreCase(mMemo.assignerStatus()))){
                inflater.inflate(R.menu.ok, menu);
                inflater.inflate(R.menu.cancel, menu);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    public boolean isOpen() {
        return mDrawerLayout != null
                && mDrawerLayout.isDrawerOpen(this.getView());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (isOpen()) {
                mDrawerLayout.closeDrawer(this.getView());
            }
        } else if (item.getItemId() == R.id.add_reminder_menu) {
            Intent intent = new Intent(getActivity(), AddRemind.class);
            intent.putExtra(AddRemind.ARG_MEMO, mMemo);
            startActivityForResult(intent, AddRemind.REQUEST_DATE);
            return true;
        } else if (item.getItemId() == R.id.ok) {
            handleOk();
            return true;
        } else if (item.getItemId() == R.id.cancel) {
            Util.openConfirmDialog(getActivity(), R.string.are_you_sure, R.drawable.memo, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handleCancel();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleOk() {
        User loginUser = mApp.loginUser();
        long loginUserId = loginUser.id();
        
        if (mMemo.isAssigner(loginUserId)) {// my is assigner
            String status = mMemo.assignerStatus();
            if ("pending".equalsIgnoreCase(status)) {
                Util.showLoading(getActivity(), getString(R.string.please_waiting));
                CallApiTask.doCallApi(API_ACCEPT, MemoDetail.this, getActivity());
            }
            return;
        }

        // my is follower
        String status = mMemo.followerStatus(loginUserId);
        if ("pending".equalsIgnoreCase(status)) {
            Util.showLoading(getActivity(),  getString(R.string.please_waiting));
            CallApiTask.doCallApi(API_FOLLOW, MemoDetail.this, getActivity());
        }
    }

    private void handleCancel() {
        User loginUser = mApp.loginUser();
        long loginUserId = loginUser.id();
        
        if (mMemo.isAssigner(loginUserId)) {// my is assigner
            String status = mMemo.assignerStatus();
            if ("pending".equalsIgnoreCase(status)) {
                Util.showLoading(getActivity(),  getString(R.string.please_waiting));
                CallApiTask.doCallApi(API_NOT_ACCEPT, MemoDetail.this,
                        getActivity());
            }
            return;
        }

        // my is follower
        String status = mMemo.followerStatus(loginUserId);
        if ("pending".equalsIgnoreCase(status)) {
            Util.showLoading(getActivity(),  getString(R.string.please_waiting));
            CallApiTask.doCallApi(API_NOT_FOLLOW, MemoDetail.this,
                    getActivity());
        }
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> query = new HashMap<String, String>();
        query.put("uid", String.valueOf(mApp.loginUser().id()));
        query.put("memo_id", String.valueOf(mMemoId));
        
        if (what == API_LOAD_MEMO) {
            return new Api("get", Util.URI_LOAD_MEMO, query);
        } else if (what == API_ACCEPT || what == API_FOLLOW) {
            query.put("action", "accept");
            return new Api("get", Util.URI_HANDLE_SHARE, query);
        } else if (what == API_NOT_ACCEPT || what == API_NOT_FOLLOW) {
            query.put("action", "refuse");
            return new Api("get", Util.URI_HANDLE_SHARE, query);
        }else if(what==API_UPDATE_LAST_COMMENT){
            return new Api("get", Util.URI_UPDATE_READ_COMMENT, query);
        }
        return null;
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
        Util.hideLoading();
        if (isDone) {
            setLoading(false);

            if (!isCallApiSuccess(context, what, result)) {
                if(Util.getErrorCode(result)==Util.ERROR_CODE_DATA_REMOVED){//备忘被删除
                    Util.showToast(getActivity(), getString(R.string.memo_removed));
                    Memo memo = new Memo(null);
                    memo.setAttr("id", mMemoId+"");
                    Util.removeCacheAndUI(getActivity(), memo, mApp.loginUser().id());
                    closeMemo();
                    if(mDrawerLayout!=null) {
                        mDrawerLayout.closeDrawer(this.getView());
                    }else{
                        getActivity().finish();
                    }
                    return;
                }
                hasError();
                return;
            }
        }

        mMemo = new Memo(Api.getJSONValue(result, "data", JSONObject.class));

        // 接受邀请的时候建立本地闹钟提醒,@HuJinhao,@2014-10-30
        if (what == API_ACCEPT || what == API_FOLLOW || what == API_LOAD_MEMO) {
            Util.createMemoAlarms(context, mMemo);
        }// end
        setLoading(false);

        if (what != API_LOAD_MEMO) {
            Util.updateCacheAndUI(context, mMemo, mApp.loginUser().id());
        }else{
            shown();
        }
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return String.format("%s?uid=%s&memo_id=%s", CacheHelper.MEMO_URI,
                    mApp.loginUser().id(), mMemoId);
    }
    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        if(to==null) {
            return from;
        }
        
        JSONObject oldComments;
        try {
            oldComments = to.getJSONObject("data").getJSONObject("comments");
        } catch (Exception e1) {
            oldComments = new JSONObject();
        }
        
        
        JSONObject newComments;
        try {
            newComments = from.getJSONObject("data").getJSONObject("comments");
        } catch (Exception e1) {
            newComments = new JSONObject();
        }

        Iterator<?> keys = newComments.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            try {
                oldComments.put(key, newComments.getJSONObject(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        try {
            from.getJSONObject("data").put("comments", oldComments);
        } catch (Exception e) {
        }
        return from;
    }

    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(getActivity(), getString(R.string.network_error));
    }
    
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }

    /**
     * 加载memo
     * 
     * @param memoId
     * @param defaultShow
     */
    public void openMemo(Long memoId, String defaultShow) {
        mMemoId = memoId;
        if (mMemoId == 0)
            return;
        
        setLoading(true);
        mDefaultShow = defaultShow;
        if(mDrawerLayout!=null) mDrawerLayout.openDrawer(getView());
        
        
        refreshSubPages(null);//清空子界面数据
        
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                CallApiTask.doCallApi(API_LOAD_MEMO, MemoDetail.this,
                        getActivity(), CacheType.REPLACE,
                        FetchType.FETCH_CACHE_ELSE_API);
            }
        }, 500);

    }

    private void setLoading(boolean refreshing) {
        mError.setVisibility(View.GONE);
        mLoading.setVisibility(refreshing ? View.VISIBLE : View.GONE);
        mContainer.setVisibility(refreshing ? View.GONE : View.VISIBLE);
    }

    private void hasError() {
        mError.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
        mContainer.setVisibility(View.GONE);
    }

    /**
     * 得到memo后显示界面数据
     */
    private void shown() {
        getActivity().invalidateOptionsMenu();
        if (MEMO_DETAIL_TYPE_COMMENTS.equals(mDefaultShow)) {
            mCommentTabBtn.callOnClick();
        } else if (MEMO_DETAIL_TYPE_TRACE.equals(mDefaultShow)) {
            mTraceTabBtn.callOnClick();
        } else {
            mContentTabBtn.callOnClick();
        }
        setLoading(false);
    }

    @Override
    public boolean refresh(Model model) {
        if (mMemo == null)
            return false;

        if (model instanceof Memo) {
            if (mMemo.id() == model.id()) {
                mMemo = (Memo) model;
                shown();
                return true;
            }
            return false;
        }

        if (model instanceof Comment) {
            Comment newComment = (Comment) model;
            if (mMemo.id() == newComment.memo_id()) {
                mMemo.addComment(newComment);
                shown();
                return true;
            }
        }

        if (model instanceof Reminder) {
            Reminder newReminder = (Reminder) model;
            if (mMemo.id() == newReminder.memo_id()) {
                mMemo.addReminder(newReminder);
                shown();
                return true;
            }
        }
        return false;
    }
// case memocontent show be called and then clear mCommentField leeboo 1213
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (mMemo != null)
//            shown();
//    }


    //xialinchong 选项卡按钮监听
    class MyButtonOnClickListener implements View.OnClickListener {

        private int index = 0;

        public MyButtonOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_MEMO, mMemo);
            Fragment fragment = null;
            String tag = "";
            
            switch(index){
            case MEMO_DETAIL_TYPE_COMMENTS_INDEX:
                tag = MEMO_DETAIL_TYPE_COMMENTS;
                break;
            case MEMO_DETAIL_TYPE_CONTENT_INDEX:
                tag = MEMO_DETAIL_TYPE_CONTENT;
                break;
            case MEMO_DETAIL_TYPE_TRACE_INDEX:
                tag = MEMO_DETAIL_TYPE_TRACE;
                break;
            }
            
            subViewSelected(index);
            refreshSubPages(null);//清空所有打开的界面数据，重新显示
            
            fragment = mListFragment.get(tag);
            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            Fragment topFragment = manager.findFragmentById(R.id.subview);
            if(topFragment !=null ){
                ft.hide((Fragment)topFragment);
            }
            
            
            Fragment existFragment = manager.findFragmentByTag(tag);
            if(existFragment !=null){ //is added
                ((Refreshable)existFragment).refresh(mMemo);
                ft.show(existFragment).commitAllowingStateLoss();
                return;
            }
            
            if(fragment != null){
                fragment.setArguments(args);
                if(index == MEMO_DETAIL_TYPE_COMMENTS_INDEX){//第一次进时更新
                    updateLastReadComment();
                }
                
                ft.add(R.id.subview, fragment, tag).commitAllowingStateLoss();
            }
        }

    }

    private void subViewSelected(int arg0) {
        Animation animation = null;
        switch (arg0) {
        case MEMO_DETAIL_TYPE_CONTENT_INDEX:
            if (mCurrIndex == MEMO_DETAIL_TYPE_COMMENTS_INDEX) {
                animation = new TranslateAnimation(moveOne, 0, 0, 0);
            } else if (mCurrIndex == MEMO_DETAIL_TYPE_TRACE_INDEX) {
                animation = new TranslateAnimation(moveTwo, 0, 0, 0);
            }
            mDefaultShow = MEMO_DETAIL_TYPE_CONTENT;
            break;
        case MEMO_DETAIL_TYPE_COMMENTS_INDEX:
            if (mCurrIndex == MEMO_DETAIL_TYPE_CONTENT_INDEX) {
                animation = new TranslateAnimation(0, moveOne, 0, 0);
            } else if (mCurrIndex == MEMO_DETAIL_TYPE_TRACE_INDEX) {
                animation = new TranslateAnimation(moveTwo, moveOne, 0, 0);
            }
            mDefaultShow = MEMO_DETAIL_TYPE_COMMENTS;
            break;
        case MEMO_DETAIL_TYPE_TRACE_INDEX:
            if (mCurrIndex == MEMO_DETAIL_TYPE_CONTENT_INDEX) {
                animation = new TranslateAnimation(mOffset, moveTwo, 0, 0);
            } else if (mCurrIndex == MEMO_DETAIL_TYPE_COMMENTS_INDEX) {
                animation = new TranslateAnimation(moveOne, moveTwo, 0, 0);
            }
            mDefaultShow = MEMO_DETAIL_TYPE_TRACE;
            break;
        }
        mCurrIndex = arg0;
        if(animation != null){
            animation.setFillAfter(true);
            animation.setDuration(300);
            mCursor.startAnimation(animation);
        }
        
    }

    private void updateLastReadComment() {
        CallApiTask.doCallApi(API_UPDATE_LAST_COMMENT, this, getActivity());
    }
    public void closeMemo(){
        mMemo = null;
        refreshSubPages(null);
    }
    private void refreshSubPages(Memo memo){
        for (Fragment fragment : mListFragment.values()) {
            ((Refreshable)fragment).refresh(memo);
        }
    }
}
