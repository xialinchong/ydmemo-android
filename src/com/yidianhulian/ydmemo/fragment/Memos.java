package com.yidianhulian.ydmemo.fragment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.FragmentStackManager;
import com.yidianhulian.ydmemo.MemoAdapter;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.MainActivity;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Reminder;

/**
 * args int memoType 表明查看的memo类型
 * 
 * @author leeboo
 * 
 */
@SuppressLint("UseSparseArrays")
public class Memos extends Fragment implements CallApiListener, Refreshable{
    public static final String ARG_MEMO_TYPE = "memoType";

    /**
     * 下拉刷新
     */
    private static final int API_REFRESH = 1;
    /**
     * 上拉刷新
     */
    private static final int API_LOAD_MORE = 2;
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
    public static final int API_NOT_FOLLOW = 6;



    private FragmentStackManager mFragmentStackManager;
    private SortedMap<Long, Memo> mMemos = new TreeMap<Long, Memo>();
    private BaseAdapter mAdapter;
    private PullToRefreshListView mListView;
    /**
     * Profile.**
     */
    private int memoType;
    private String mUrl;
    private YDMemoApplication mApp;
    private int mPageNo = 1;
    private SharedPreferences mSp;
    private boolean isFirstLaunch = true;
  
    private HashMap<Integer, String> mTitles;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mApp = (YDMemoApplication) getActivity().getApplication();
        try {
            mFragmentStackManager = (FragmentStackManager) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    "Activity must implement NavigationDrawerCallbacks.");
        }
        mSp = getActivity().getSharedPreferences("memoPagination",
                Context.MODE_PRIVATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentStackManager = null;
    }
    
    public PullToRefreshListView getListView(){
        return mListView;
    }

    private void setup() {
        mTitles = new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1L;

            {
                put(Profile.MEMO_TYPE_MY_FOLLOW, getString(R.string.my_follow));
                put(Profile.MEMO_TYPE_MY_MEMO, getString(R.string.my_memo));
                put(Profile.MEMO_TYPE_REFUSED_MEMO, getString(R.string.memo_refuse_to_me));
                put(Profile.MEMO_TYPE_RECEIVED_INVITE,
                        getString(R.string.memo_invite_to_me));
                put(Profile.MEMO_TYPE_RECEIVED_MEMO, getString(R.string.memo_receive));
                put(Profile.MEMO_TYPE_CLOSED_MEMO, getString(R.string.closed_memo));
            }
        };

        memoType = getArguments().getInt(ARG_MEMO_TYPE);
        restoreActionBar();
        //leeboo 初始化一条表示“空”的memo
        addEmptyView();
        
        mUrl = CacheHelper.MEMOS_URL.get(memoType);
        mAdapter = new MemoAdapter(getActivity(), mMemos, R.layout.cell_memo);
        if (memoType == Profile.MEMO_TYPE_RECEIVED_MEMO) {
            ((MemoAdapter) mAdapter).setupLeftButton(R.drawable.ok_selector,
                    new OnClickListener() {
                        @Override
                        public void onClick(View btn) {
                            Util.showLoading(Memos.this.getActivity(),
                                    getString(R.string.please_waiting));
                            CallApiTask.doCallApi(API_ACCEPT, Memos.this,
                                    Memos.this.getActivity(), btn.getTag());
                        }
                    });
            ((MemoAdapter) mAdapter).setupRightButton(R.drawable.cancel_selector,
                    new OnClickListener() {
                        @Override
                        public void onClick(final View btn) {
                            Util.openConfirmDialog(getActivity(), R.string.not_follow, 
                                    R.drawable.memo, R.string.are_you_sure, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                int which) {

                                            Util.showLoading(Memos.this.getActivity(),
                                                    getString(R.string.please_waiting));
                                            CallApiTask.doCallApi(API_NOT_ACCEPT,
                                                    Memos.this, Memos.this.getActivity(),
                                                    btn.getTag());
                                        }
                                    });
                            
                        }
                    });
        } else if (memoType == Profile.MEMO_TYPE_RECEIVED_INVITE) {
            ((MemoAdapter) mAdapter).setupLeftButton(R.drawable.ok_selector,
                    new OnClickListener() {
                        @Override
                        public void onClick(View btn) {
                            Util.showLoading(Memos.this.getActivity(),
                                    getString(R.string.please_waiting));
                            CallApiTask.doCallApi(API_FOLLOW, Memos.this,
                                    Memos.this.getActivity(), btn.getTag());
                        }
                    });
            ((MemoAdapter) mAdapter).setupRightButton(R.drawable.cancel_selector,
                    new OnClickListener() {
                        @Override
                        public void onClick(final View btn) {
                            Util.openConfirmDialog(getActivity(), R.string.cancel_follow, 
                                    R.drawable.memo, R.string.are_you_sure, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                int which) {
                                            Util.showLoading(Memos.this.getActivity(),
                                                    getString(R.string.please_waiting));
                                            CallApiTask.doCallApi(API_NOT_FOLLOW,
                                                    Memos.this, Memos.this.getActivity(),
                                                    btn.getTag());
                                        }
                                    });
                        }
                    });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        isFirstLaunch = true;
        setup();

        mListView = (PullToRefreshListView) inflater.inflate(R.layout.list,
                container, false);
        mListView.setAdapter(mAdapter);

        mListView.setOnPullEventListener(new OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase<ListView> refreshView,
                    State state, Mode direction) {
                if(state == State.RELEASE_TO_REFRESH || state==State.MANUAL_REFRESHING || state==State.REFRESHING){
                    removeEmptyView();
                }else if(state == State.RESET){
                    addEmptyView();//当刷新结束时，如何还没有数据，则显示空数据提示 leeboo
                    mAdapter.notifyDataSetChanged();
                }
            }
            
        });
        
        mListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity()
                        .getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                

                // 第一次打开界面时，先显示缓存数据; 恢复之前的数据历史 leeboo
                if(isFirstLaunch){
                    isFirstLaunch = false;
                    mPageNo = mSp.getInt("mPageNo", 1);
                    CallApiTask.doCallApi(API_REFRESH, Memos.this, Memos.this.getActivity(),
                            CacheType.REPLACE, FetchType.FETCH_CACHE_ELSE_API);
                    
                }else{
                    mPageNo = 1;
                    mSp.edit().putInt("mPageNo", 1).commit();
                    removeEmptyView();
                    CallApiTask.doCallApi(API_REFRESH, Memos.this,
                            Memos.this.getActivity(), CacheType.REPLACE,
                            FetchType.FETCH_API);
                }
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity()
                        .getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);

                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                removeEmptyView();
                CallApiTask.doCallApi(API_LOAD_MORE, Memos.this,
                        Memos.this.getActivity(), CacheType.CUSTOM,
                        FetchType.FETCH_API);
            }
        });

        return mListView;
    }

    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        
        mListView.setRefreshing(true);        
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mFragmentStackManager == null || !mFragmentStackManager.isOpen()) {
            if(mFragmentStackManager.isTop(this, String.valueOf(memoType))){
                inflater.inflate(R.menu.memo_list, menu);
            }
            restoreActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public Api getApi(Context context, int what, Object... params) {
        YDMemoApplication app = (YDMemoApplication) getActivity()
                .getApplication();
        Map<String, String> queryString = new HashMap<String, String>();

        if (what == API_LOAD_MORE) {
            queryString.put("uid", app.loginUser().getAttr("id"));
            queryString.put("page", String.valueOf(mPageNo + 1));
            return new Api("get", mUrl, queryString);

        } else if (what == API_REFRESH) {
            queryString.put("uid", app.loginUser().getAttr("id"));
            queryString.put("page", String.valueOf(mPageNo));
            return new Api("get", mUrl, queryString);

        } else if (what == API_ACCEPT || what == API_FOLLOW) {
            queryString.put("uid", app.loginUser().getAttr("id"));
            queryString.put("memo_id", String.valueOf(((Memo) params[0]).id()));
            queryString.put("action", "accept");
            return new Api("get", Util.URI_HANDLE_SHARE, queryString);

        } else if (what == API_NOT_ACCEPT || what == API_NOT_FOLLOW) {
            queryString.put("uid", app.loginUser().getAttr("id"));
            queryString.put("memo_id", String.valueOf(((Memo) params[0]).id()));
            queryString.put("action", "refuse");
            return new Api("get", Util.URI_HANDLE_SHARE, queryString);
        }

        return null;
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
        Util.hideLoading();
        if( ! isCallApiSuccess(context, what, result)){
            if(what != API_REFRESH && what != API_LOAD_MORE){
                Util.showToast(getActivity(), getString(R.string.has_error));
            }else{
                addEmptyView();
                mListView.onRefreshComplete();
                mAdapter.notifyDataSetChanged();
            }
            
            return;
        }
        if (what == API_REFRESH) {
            mListView.onRefreshComplete();
            mMemos.clear();

            JSONObject datas = Api.getJSONValue(result, "data", JSONObject.class);
            Iterator<?> ite = datas.keys();
            while (ite.hasNext()) {
                Object key = ite.next(); 
                try {
                    Memo memo = new Memo(datas.getJSONObject(key.toString()));
                    mMemos.put(memo.id(), memo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            addEmptyView();
        } else if (what == API_LOAD_MORE) {
            mListView.onRefreshComplete();

            JSONObject datas = Api.getJSONValue(result, "data", JSONObject.class);
            if(datas.length() > 0){
                Iterator<?> ite = datas.keys();
                while (ite.hasNext()) {
                    Object key = ite.next(); 
                    try {
                        Memo memo = new Memo(datas.getJSONObject(key.toString()));
                        mMemos.put(memo.id(), memo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mPageNo++;
                mSp.edit().putInt("mPageNo", mPageNo).commit();
            }
            addEmptyView();
            
        } else if (what == API_ACCEPT || what == API_FOLLOW
                || what == API_NOT_ACCEPT || what == API_NOT_FOLLOW) {
            
        	Memo memo = new Memo(Api.getJSONValue(result, "data", JSONObject.class));
        	MainActivity mainActivity = (MainActivity) getActivity();
            if(what == API_ACCEPT || what == API_FOLLOW){                
            	Util.createMemoAlarms(mainActivity, memo); //接受邀请的时候建立本地闹钟提醒,@HuJinhao,@2014-10-31
            }            
            mMemos.remove(memo.id());
            
            Util.updateCacheAndUI(context, memo, mApp.loginUser().id());
            addEmptyView();

        }
        mAdapter.notifyDataSetChanged();
    }

    private void addEmptyView() {
        if(mMemos.size()==0){
            Memo emptyMemo = new Memo(null);
            emptyMemo.setAttr("id", "-1");
            mMemos.put(-1l, emptyMemo);
        }
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        if (what == API_REFRESH || what == API_LOAD_MORE) {
            return String.format("%s?uid=%s", mUrl, mApp.loginUser().id());
        }
        return null;
    }
    
    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        if(what!=API_LOAD_MORE)return from;
        JSONObject toData = Api.getJSONValue(to, "data", JSONObject.class);
        JSONObject fromData = Api.getJSONValue(from, "data", JSONObject.class);
        if (toData == null) return from;
        if (fromData == null) return to;
        
        Iterator<?> ite = fromData.keys();
        while (ite.hasNext()) {
            String key = ite.next().toString();
            try {
                toData.put(key, fromData.getJSONObject(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        try {
            to.put("data", toData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return to;
    }

    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(this.getActivity(), getString(R.string.network_error));
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
    public void restoreActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitles.get(memoType));
    }

    private void removeEmptyView() {
        if(mMemos.size()==1 && mMemos.firstKey() == -1){//“空数据”刷新时移除“无数据”提示
            mMemos.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean refresh(Model model) {
        if(model instanceof Memo){
            removeEmptyView();
            updateMemo((Memo)model);
            addEmptyView();
            mAdapter.notifyDataSetChanged();
            return true;
        }

        if(model instanceof Comment){
            Comment newComment = (Comment)model;
            if(mMemos.containsKey(newComment.memo_id())){
                mMemos.get(newComment.memo_id()).addComment(newComment);
                mAdapter.notifyDataSetChanged();
                if(newComment.commenter().id()!=mApp.loginUser().id()) Util.PlayVibrate(getActivity());
                return true;
            }
        }
        
        if(model instanceof Reminder){
            Reminder newReminder = (Reminder)model;
            if(mMemos.containsKey(newReminder.memo_id())){
                mMemos.get(newReminder.memo_id()).addReminder(newReminder);
                mAdapter.notifyDataSetChanged();
                if(newReminder.isCreater(mApp.loginUser().id())) Util.PlayVibrate(getActivity());
                return true;
            }
        }
        return false;
    }

    private void updateMemo(Memo memo) {
        mMemos.remove(memo.id());
        if(memo.isWillRemoveFromCache())return;
        
        Long uid = mApp.loginUser().id();
        
        switch(memoType){
        case Profile.MEMO_TYPE_CLOSED_MEMO:
            if( ! memo.isClosed() || ( ! memo.isAssigner(uid) && !memo.isFollower(uid))) return;
            break;
        case Profile.MEMO_TYPE_MY_MEMO:
            if( ! memo.isAssigner(uid) || memo.isClosed()) return;
            break;
        case Profile.MEMO_TYPE_MY_FOLLOW:
            if( ! memo.isFollower(uid) || memo.isClosed() || !"accept".equals(memo.followerStatus(uid))) return;
            break;
        case Profile.MEMO_TYPE_RECEIVED_INVITE:
            if( ! memo.isFollower(uid) || !"pending".equals(memo.followerStatus(uid)) || memo.isClosed()) return;
            break;
        case Profile.MEMO_TYPE_REFUSED_MEMO:
            if( ! memo.isCreater(uid) || !"refused".equals(memo.assignerStatus()) || memo.isClosed()) return;
            break;
        case Profile.MEMO_TYPE_RECEIVED_MEMO:
            if( ! memo.isAssigner(uid) || !"pending".equals(memo.assignerStatus()) || memo.isClosed()) return;
            break;
        }
        mMemos.put(memo.id(), memo);
    }
}
