package com.yidianhulian.ydmemo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.ydmemo.FragmentStackManager;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.activity.About;
import com.yidianhulian.ydmemo.activity.AddMemo;
import com.yidianhulian.ydmemo.activity.Login;
import com.yidianhulian.ydmemo.activity.MainActivity;
import com.yidianhulian.ydmemo.activity.MySetting;
import com.yidianhulian.ydmemo.activity.Notification;
import com.yidianhulian.ydmemo.activity.PostSuggest;
import com.yidianhulian.ydmemo.activity.SelectContact;
import com.yidianhulian.ydmemo.activity.SendSms;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Notify;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.model.User;

/**
 * 左边抽屉菜单
 */
public class Profile extends Fragment implements CallApiListener,Refreshable {
    /**
     * 收到的备忘
     */
	public static final String MISC_TOTAL_ASSIGN = "total_assign";
	
    /**
     * 我的备忘
     */
	public static final int MEMO_TYPE_MY_MEMO = 1;
	/**
     * 我的关注备忘
     */
	public static final int MEMO_TYPE_MY_FOLLOW = 2;
	/**
     * 被拒绝备忘
     */
	public static final int MEMO_TYPE_REFUSED_MEMO = 3;
	/**
     * 被指派备忘
     */
	public static final int MEMO_TYPE_RECEIVED_MEMO = 4;
	/**
     * 受邀请备忘
     */
	public static final int MEMO_TYPE_RECEIVED_INVITE = 5;
	public static final int ALLOW_ASSIGN_TO_ME = 6;
	public static final int NOTIFY_MY_HAS_COMMENT = 7;
	public static final int NOTIFY_FOLLOW_HAS_COMMENT = 8;
	public static final int ABOUT_YDMEMO = 9;
	public static final int POST_SUGGEST = 10;
	public static final int INVITE = 11;
	public static final int LOGOUT = 12;
	/**
	 * 关闭的备忘
	 */
	public static final int MEMO_TYPE_CLOSED_MEMO = 13;
	public static final int MY_SETTING = 14;
	
	private static final int API_REFRESH_PROFILE = 1;
    protected static final int API_UPDATE_SETTING = 2;
    protected static final int API_LOGOUT = 3;
    public static final String REFRESH_FLAG = "Profile";

	private FragmentStackManager mFragmentStackManager;
	private DrawerLayout mDrawerLayout;
	private ScrollView mProfileLayout;
	private View mFragmentView;

	private YDMemoApplication mApp;
	private Option mMisc;

	@SuppressLint("UseSparseArrays")
	private Map<Integer, Button> buttons = new HashMap<Integer, Button>();
    private TextView mInviteBudge;
    private TextView mClosedMemoBudge;
    private TextView mMineBudge;
    private TextView mFollowBudge;
    private ImageView mMyAvatar;
    private TextView mMyName;
    private Button mMyBtn;

    private TextView mNotificationBudge;
    private ImageButton mNotificationBtn;

    

	public Profile() {
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mFragmentStackManager = (FragmentStackManager) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement NavigationDrawerCallbacks.");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mApp = (YDMemoApplication)getActivity().getApplication();
	    ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.profile, container, false);
	    mProfileLayout      = (ScrollView)viewGroup.findViewById(R.id.pull_refresh_scrollview);
		Button myMemo = (Button)mProfileLayout.findViewById(R.id.my_memo);

		
		mInviteBudge      = (TextView)mProfileLayout.findViewById(R.id.invite_budge);
		mClosedMemoBudge  = (TextView)mProfileLayout.findViewById(R.id.closed_memo_budge);
		mMineBudge        = (TextView)mProfileLayout.findViewById(R.id.mine_memo_budge);
		mFollowBudge      = (TextView)mProfileLayout.findViewById(R.id.follow_memo_budge);
		mMyAvatar = (ImageView)mProfileLayout.findViewById(R.id.my_avatar);
		mMyName   = (TextView)mProfileLayout.findViewById(R.id.my_name);
		mMyBtn    = (Button)mProfileLayout.findViewById(R.id.my_btn);
		
		mNotificationBudge = (TextView)mProfileLayout.findViewById(R.id.notification_budge);
		mNotificationBtn = (ImageButton)mProfileLayout.findViewById(R.id.notification);
		mNotificationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Notification.class);
//                startActivityForResult(intent, Notification.REQUEST_FOR_VIEW);
                startActivity(intent);
            }
        });
		
		mMyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMenu(MY_SETTING);
            }
        });

		myMemo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				selectMenu(MEMO_TYPE_MY_MEMO);
			}
		});
		buttons.put(MEMO_TYPE_MY_MEMO, myMemo);
		
		Button followMemo = (Button)mProfileLayout.findViewById(R.id.my_follow);
		followMemo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				selectMenu(MEMO_TYPE_MY_FOLLOW);
			}
		});
		buttons.put(MEMO_TYPE_MY_FOLLOW, followMemo);
		
		Button closedMemo = (Button)mProfileLayout.findViewById(R.id.my_closed_memo);
		closedMemo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                selectMenu(MEMO_TYPE_CLOSED_MEMO);
            }
        });
        buttons.put(MEMO_TYPE_CLOSED_MEMO, closedMemo);
		
		
		
		Button inviteMemo = (Button)mProfileLayout.findViewById(R.id.memo_invite_to_me);
		inviteMemo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    selectMenu(MEMO_TYPE_RECEIVED_INVITE);
			}
		});
		buttons.put(MEMO_TYPE_RECEIVED_INVITE, inviteMemo);
		
		
		Button about = (Button)mProfileLayout.findViewById(R.id.about_ydmemo);
		about.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				selectMenu(ABOUT_YDMEMO);
			}
		});
		buttons.put(ABOUT_YDMEMO, about);
		
		Button post = (Button)mProfileLayout.findViewById(R.id.post_suggest);
		post.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				selectMenu(POST_SUGGEST);
			}
		});
		buttons.put(POST_SUGGEST, post);
		
		Button inviteFriend = (Button)mProfileLayout.findViewById(R.id.invite_friend);
		inviteFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    Intent intent = new Intent();
                intent.setClass(getActivity(), SelectContact.class);
                intent.putExtra("request", SelectContact.REQUEST_FOR_INVITE);
                intent.putExtra(SelectContact.ARG_TITLE, "邀请好友");
                startActivityForResult(intent, SelectContact.REQUEST_FOR_INVITE);
			}
		});
		buttons.put(INVITE, inviteFriend);
		
		Button logout = (Button)mProfileLayout.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    Intent intent = new Intent();
			    intent.setClass(getActivity(), Login.class);
			    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			    startActivity(intent);
			    CallApiTask.doCallApi(API_LOGOUT, Profile.this, getActivity());
			}
		});
		buttons.put(LOGOUT, logout);
		
		
		return viewGroup;
	}

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		selectMenu(MEMO_TYPE_MY_MEMO);
		CallApiTask.doCallApi(API_REFRESH_PROFILE, this, this.getActivity(), CacheType.REPLACE, FetchType.FETCH_CACHE_THEN_API);
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentView);
	}

	public void setUp(DrawerLayout drawerLayout) {
		mFragmentView = getActivity().findViewById(R.id.profile);
		mDrawerLayout = drawerLayout;
	}

	private void clearSelected(){
		for (Iterator<Button> iterator = buttons.values().iterator(); iterator.hasNext();) {
			iterator.next().setSelected(false);
		}
	}
	
	private void selectMenu(int type) {
//		clearSelected();
//		Button button = buttons.get(type);
//		if(button !=null) button.setSelected(true);
		
		Fragment fragment = null;
        switch(type){
        case Profile.MEMO_TYPE_MY_MEMO:
        case Profile.MEMO_TYPE_MY_FOLLOW:
        case Profile.MEMO_TYPE_REFUSED_MEMO:
        case Profile.MEMO_TYPE_RECEIVED_MEMO:
        case Profile.MEMO_TYPE_RECEIVED_INVITE:
        case Profile.MEMO_TYPE_CLOSED_MEMO:
            fragment = new Memos();
            Bundle args = new Bundle();
            args.putInt(Memos.ARG_MEMO_TYPE, type);
            fragment.setArguments(args);
            break;
        case Profile.ABOUT_YDMEMO:
            Intent about = new Intent();
            about.setClass(getActivity(), About.class);
            startActivity(about);
            return;
        case Profile.POST_SUGGEST:
//            fragment = new PostSuggest();
            //optionmenu冲突，把fragment换成activity
            Intent intent = new Intent();
            intent.setClass(getActivity(), PostSuggest.class);
            startActivity(intent);
            return;
        case Profile.MY_SETTING:
            Intent mySetting = new Intent();
            mySetting.setClass(getActivity(), MySetting.class);
            startActivity(mySetting);
            return;
        }
		
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentView);
		}
		if (mFragmentStackManager != null && fragment!=null) {
			mFragmentStackManager.pushFragment(fragment, String.valueOf(type));
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mFragmentStackManager = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (mDrawerLayout != null && isDrawerOpen()) {
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId()==android.R.id.home){
	        MainActivity mainActivity = (MainActivity)mFragmentStackManager;
	        if(mainActivity.getMemoDetail().isOpen()) return false;
	        
	        if(isDrawerOpen()){
	            mDrawerLayout.closeDrawer(mFragmentView);
	        }else{
	            mDrawerLayout.openDrawer(mFragmentView);
	        }
        }else if(item.getItemId()==R.id.add_memo){
            Intent intent = new Intent();
            intent.setClass(getActivity(), AddMemo.class);
            intent.putExtra("requestCode", AddMemo.REQUEST_FOR_NEW_MEMO);
            startActivityForResult(intent, AddMemo.REQUEST_FOR_NEW_MEMO);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (data == null) {
            return;
        }
        
        ArrayList<User> users;
        
        if(requestCode==AddMemo.REQUEST_FOR_NEW_MEMO){//增加备忘
            users     = data.getParcelableExtra(AddMemo.RESULT_NEW_USER);
        }else{//选择联系人
            users = data.getParcelableArrayListExtra(SelectContact.RESULT_SELECTED_USERS);
        }
        
        if(users==null)return;
        
        if(users.size() <=0 )return;
        Intent intent = new Intent(getActivity(), SendSms.class);
        intent.putParcelableArrayListExtra(SendSms.ARG_USERS, users);
        intent.putExtra(SendSms.ARG_SHARE_DEFAULT, getString(R.string.invite_friend_message));
        intent.putExtra(SendSms.ARG_SHARE_TIP, getString(R.string.sms_invite_friend_message));
        startActivity(intent);
        
    }



    private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setIcon(R.drawable.logo);
		actionBar.setTitle(R.string.app_name);
		shown();
	}

	private ActionBar getActionBar() {
		return getActivity().getActionBar();
	}


    @SuppressWarnings("unchecked")
    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> queryStr = new HashMap<String, String>();
        String uid = String.valueOf(mApp.loginUser().id());
        
        if(what==API_REFRESH_PROFILE){
            queryStr.put("uid", uid);
            return new Api("get", Util.URI_MISC, queryStr);
        }else if(what==API_UPDATE_SETTING){
            return new Api("post", String.format("%s?uid=%s", Util.URI_SAVE_SETTING, uid), (Map<String, String>)params[0]);
        } else if (what == API_LOGOUT) {
            mApp.saveUser(null);
            if (mApp.deviceToken == null) {
                return null;
            }
            queryStr.put( "token" , mApp.deviceToken );
            queryStr.put( "type"  , "android" );
            queryStr.put( "action", "remove" );
            queryStr.put( "uid"   , uid );
            return new Api("post", Util.URI_UPDATE_TOKEN, queryStr);
        }
        return null;
    }


    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone, Object... params) {
        if(isDone){
            if( ! Util.checkResult(result)){
                return ;
            }
        }
        if(what==API_REFRESH_PROFILE){
            mMisc = new Option(Api.getJSONValue(result, "data", JSONObject.class));
            shown();
        }
    }
    
    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what,  Object... params) {
        return String.format("%s?uid=%s", Util.URI_MISC, mApp.loginUser().id());
    }

    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        Util.showToast(context, context.getString(R.string.network_error));
    }


    private void shown(){
        mMyName.setText(mApp.loginUser().name());
        Util.loadAvatar(mApp, mApp.loginUser(), mMyAvatar);        
        
        if(mMisc==null)return;
        int totalInvite = mMisc.totalInvited();
        int closedMemo  = mMisc.totalClosed();
        int mineMemo    = mMisc.totalMine();
        int followMemo  = mMisc.totalFollow();
        
        
        mNotificationBudge.setVisibility(mMisc.hasNotification() ? View.VISIBLE : View.GONE);

        mInviteBudge.setVisibility(View.INVISIBLE);
        mMineBudge.setVisibility(View.INVISIBLE);
        mFollowBudge.setVisibility(View.INVISIBLE);
        mClosedMemoBudge.setVisibility(View.INVISIBLE);
        
        if(totalInvite>0){
            mInviteBudge.setText(String.valueOf(totalInvite));
            mInviteBudge.setVisibility(View.VISIBLE);
        }
        if(closedMemo>0){
            mClosedMemoBudge.setText(String.valueOf(closedMemo));
            mClosedMemoBudge.setVisibility(View.VISIBLE);
        }
        if(mineMemo>0){
            mMineBudge.setText(String.valueOf(mineMemo));
            mMineBudge.setVisibility(View.VISIBLE);
        }
        if(followMemo>0){
            mFollowBudge.setText(String.valueOf(followMemo));
            mFollowBudge.setVisibility(View.VISIBLE);
        }
        
        if( ! ((MainActivity)getActivity()).getMemoDetail().isOpen()){//备忘详情没有打开的情况下
            if(mMisc.hasNotification() || totalInvite>0){
                ((MainActivity)getActivity()).updateMessageDrawerToggle();
            }else{
                ((MainActivity)getActivity()).updateNormalDrawerToggle();
            }
        }
    }


    @Override
    public boolean refresh(Model model) {
        if(model instanceof User){
            mApp.saveUser((User)model);
            shown();
            return true;
        }
        if(model instanceof Option){
            mMisc = (Option)model;
            shown();
            return true;
        }
        
        CallApiTask.doCallApi(API_REFRESH_PROFILE, this, getActivity(), CacheType.REPLACE, FetchType.FETCH_API);
        
        return false;
    }
}
